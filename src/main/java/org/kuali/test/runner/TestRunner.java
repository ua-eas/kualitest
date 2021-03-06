/*
 * Copyright 2014 The Kuali Foundation
 * 
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl2.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kuali.test.runner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.kuali.test.KualiTestConfigurationDocument;
import org.kuali.test.KualiTestDocument.KualiTest;
import org.kuali.test.KualiTestRunnerDocument;
import org.kuali.test.ScheduledTest;
import org.kuali.test.ScheduledTestType;
import org.kuali.test.TestSuite;
import org.kuali.test.runner.execution.TestExecutionContext;
import org.kuali.test.runner.execution.TestExecutionMonitor;
import org.kuali.test.utils.ApplicationInstanceListener;
import org.kuali.test.utils.ApplicationInstanceManager;
import org.kuali.test.utils.Constants;
import org.kuali.test.utils.Utils;

/**
 * 
 * @author rbtucker
 */
public class TestRunner {
    private static final Logger LOG = Logger.getLogger(TestRunner.class);
    private KualiTestConfigurationDocument.KualiTestConfiguration configuration;
    private KualiTestRunnerDocument.KualiTestRunner testRunnerConfiguration;
    private final List<TestExecutionContext> scheduledTests = Collections.synchronizedList(new ArrayList<TestExecutionContext>());
    private final List<TestExecutionContext> executingTests = Collections.synchronizedList(new ArrayList<TestExecutionContext>());
    private boolean stopRunner = false;
    private Timer testInquiryTimer;
    private Timer configurationUpdateTimer;
    
    /**
     *
     * @param args
     */
    public static void main(final String args[]) {
        if (args.length != 1) {
            System.out.println("usage: TestRunner <config-file-path>");
        } else {
            if (!ApplicationInstanceManager.registerInstance(ApplicationInstanceManager.SINGLE_INSTANCE_NETWORK_SOCKET2)) {
                // instance already running.
                System.out.println("Another instance of this application is already running.  Exiting.");
                System.exit(0);
            } else {
                ApplicationInstanceManager.setApplicationInstanceListener(new ApplicationInstanceListener() {
                    @Override
                    public void newInstanceCreated() {
                    }
                });

                new TestRunner(args[0]);
            }
        }
    }
    
    public TestRunner() {
    }

    /**
     *
     * @param configuration
     */
    public TestRunner(KualiTestConfigurationDocument.KualiTestConfiguration configuration) {
        this.configuration = configuration;
    }
    
    /**
     *
     * @param configFileName
     */
    public TestRunner(String configFileName) {
        System.out.println("starting kuali test runner with file " + configFileName);
        if (loadConfiguration(configFileName)) {
            TimerTask testInquireyTimerTask = new TimerTask() {
                @Override
                public void run() {
                    if (!stopRunner) {
                        checkForRunnableTests();
                    }
                }
            };

	        testInquiryTimer = new Timer();
            long interval = 100*60*testRunnerConfiguration.getScheduledTestInquiryInterval();
            testInquiryTimer.scheduleAtFixedRate(testInquireyTimerTask, interval, interval);

            TimerTask configurationUpdateTimerTask = new TimerTask() {
                @Override
                public void run() {
                    if (!stopRunner) {
                        try {
                            updateTestRunnerConfiguration();
                        }
                        
                        catch (Exception ex) {
                            LOG.error(ex.toString(), ex);
                        }
                    }
                }
            };

	        configurationUpdateTimer = new Timer();
            interval = 100*60*testRunnerConfiguration.getConfigurationUpdateInterval();
            configurationUpdateTimer.scheduleAtFixedRate(configurationUpdateTimerTask, interval, interval);
            
        } else {
            System.exit(-1);
        }
    }

    private KualiTestRunnerDocument.KualiTestRunner getTestRunnerConfiguration() throws XmlException, IOException {
        KualiTestRunnerDocument.KualiTestRunner retval = null;       
        File trConfigFile =  Utils.getTestRunnerConfigurationFile(configuration);
                    
        if (trConfigFile.exists() && trConfigFile.isFile()) {
            retval = KualiTestRunnerDocument.Factory.parse(trConfigFile).getKualiTestRunner();
        }

        return retval;
    }
    
    /**
     *
     * @throws XmlException
     * @throws IOException
     */
    protected synchronized void updateTestRunnerConfiguration() throws XmlException, IOException {
        testRunnerConfiguration = getTestRunnerConfiguration() ;
        
        List <ScheduledTest> activeTests = new ArrayList<ScheduledTest>();
        scheduledTests.clear();
        if (testRunnerConfiguration.getScheduledTests() != null) {
            for (ScheduledTest test : testRunnerConfiguration.getScheduledTests().getScheduledTestArray()) {
                if (test.getStartTime().getTimeInMillis() >= System.currentTimeMillis()) {
                    if (test.getType().equals(ScheduledTestType.TEST_SUITE)) {
                        scheduleTestSuite(test.getPlaformName(), test.getName(), test.getStartTime().getTime(), test.getTestRuns(),  test.getRampUpTime(), test.getRepeatInterval());
                    } else if (test.getType().equals(ScheduledTestType.PLATFORM_TEST)) {
                        scheduleTest(test.getPlaformName(), test.getName(), test.getStartTime().getTime(), test.getTestRuns(), test.getRampUpTime(), test.getRepeatInterval());
                    }
                    
                    activeTests.add(test);
                }
            }
        }
        
        if (!activeTests.isEmpty()) {
            testRunnerConfiguration.getScheduledTests().setScheduledTestArray(activeTests.toArray(new ScheduledTest[activeTests.size()]));
        } else {
            testRunnerConfiguration.getScheduledTests().setScheduledTestArray(null);
        }
        
        saveTestRunnerConfiguration();
    }
    
    private synchronized void saveTestRunnerConfiguration() throws IOException {
        File f =  Utils.getTestRunnerConfigurationFile(configuration);
        KualiTestRunnerDocument doc = KualiTestRunnerDocument.Factory.newInstance();
        doc.setKualiTestRunner(testRunnerConfiguration);
        doc.save(f);
    }
    
    public void stopRunner() {
        stopRunner = true;
        testInquiryTimer.cancel();
        configurationUpdateTimer.cancel();
    }
    
    private Calendar getNextScheduledTime(Date lastRunTime, String repeatInterval) {
        Calendar retval = Calendar.getInstance();
        retval.setTime(lastRunTime);
        
        if (Constants.HOURLY.equals(repeatInterval)) {
            retval.add(Calendar.HOUR_OF_DAY, 1);
        } else if (Constants.DAILY.equals(repeatInterval)) {
            retval.add(Calendar.DATE, 1);
        } else if (Constants.WEEKLY.equals(repeatInterval)) {
            retval.add(Calendar.DATE, 7);
        } else if (Constants.MONTHLY.equals(repeatInterval)) {
            retval.add(Calendar.MONTH, 1);
        } else {
            retval = null;
        }
        
        return retval;
    }
    
    private void checkForRunnableTests() {
        synchronized(scheduledTests) {
            Iterator <TestExecutionContext> it = scheduledTests.iterator();
            while (it.hasNext()) {
                TestExecutionContext ec = it.next();

                if ((ec.getScheduledTime() != null) && (ec.getScheduledTime().getTime() < System.currentTimeMillis())) {
                    List <TestExecutionContext> testExecutions = ec.getTestInstances();
                    new TestExecutionMonitor(testExecutions, ec.getRampUpTime());
                    executingTests.addAll(testExecutions);
                    
                    String repeatInterval = ec.getRepeatInterval();
                    if (StringUtils.isNotBlank(repeatInterval)) {
                        Calendar cal = getNextScheduledTime(ec.getScheduledTime(), repeatInterval);
                        if (cal != null) {
                            rescheduleTest(ec, cal);
                        } else {
                            it.remove();
                        }
                    } else {
                        it.remove();
                    }
                }
            }
        }
        
        Iterator <TestExecutionContext> it  = executingTests.iterator();

        while (it.hasNext()) {
            TestExecutionContext ec = it.next();
            if (ec.isCompleted()) {
                it.remove();
            }
        }
    }

    private void rescheduleTest(TestExecutionContext tec, Calendar newDate) {
        try {
            testRunnerConfiguration = getTestRunnerConfiguration();
            ScheduledTest scheduledTest = testRunnerConfiguration.getScheduledTests().addNewScheduledTest();
            if (tec.getKualiTest() != null) {
                scheduledTest.setName(tec.getKualiTest().getTestHeader().getTestName());
                scheduledTest.setType(ScheduledTestType.PLATFORM_TEST);
            } else {
                scheduledTest.setName(tec.getTestSuite().getName());
                scheduledTest.setType(ScheduledTestType.TEST_SUITE);
            }

            scheduledTest.setTestRuns(tec.getTestRun());
            scheduledTest.setRepeatInterval(tec.getRepeatInterval());
            scheduledTest.setStartTime(newDate);
            scheduledTest.setPlaformName(tec.getPlatform().getName());
            saveTestRunnerConfiguration();
        } 
        
        catch (XmlException ex) {
            LOG.error(ex.toString(), ex);
        } 
        
        catch (IOException ex) {
            LOG.error(ex.toString(), ex);
        }
    }

    /**
     * 
     * @param platformName
     * @param testName
     * @param scheduledTime
     * @param testRuns
     * @param rampUpTime
     * @param repeatInterval 
     */
    public void scheduleTest(String platformName, String testName, Date scheduledTime, int testRuns, int rampUpTime, String repeatInterval) {
        if (scheduledTime == null) {
            System.out.println("scheduled time is null - abort scheduling");
        } else if (scheduledTime.getTime() <= System.currentTimeMillis()) {
            System.out.println("scheduled time is in past - abort scheduling");
        } else {
            KualiTest test = Utils.findKualiTest(configuration, platformName, testName);
            
            if (test != null) {
                scheduledTests.add(new TestExecutionContext(configuration, test, scheduledTime, testRuns, rampUpTime, repeatInterval));
            } else {
                System.out.println("failed to find kuali test '" + testName + "' for plaform " + platformName);
            }
        }
    }

    /**
     * 
     * @param platformName
     * @param testSuiteName
     * @param scheduledTime
     * @param testRuns
     * @param rampUpTime
     * @param repeatInterval 
     */
    public void scheduleTestSuite(String platformName, String testSuiteName, Date scheduledTime, int testRuns, int rampUpTime, String repeatInterval) {
        if (scheduledTime == null) {
            System.out.println("scheduled time is null - abort scheduling");
        } else if (scheduledTime.getTime() <= System.currentTimeMillis()) {
            System.out.println("scheduled time is in past - abort scheduling");
        } else {
            TestSuite testSuite = Utils.findTestSuite(configuration, platformName, testSuiteName);
            
            if (testSuite != null) {
                scheduledTests.add(new TestExecutionContext(configuration, testSuite, scheduledTime, testRuns, rampUpTime, repeatInterval));
            } else {
                System.out.println("failed to find test suite '" + testSuiteName + "' for plaform " + platformName);
            }
        }
    }

    /**
     * 
     * @param platformName
     * @param testName
     * @param testRuns
     * @param rampUpTime
     * @return 
     */
    public TestExecutionMonitor runTest(String platformName, String testName, int testRuns, int rampUpTime) {
        TestExecutionMonitor retval = null;
        KualiTest test = Utils.findKualiTest(configuration, platformName, testName);

        if (test != null) {
            List <TestExecutionContext> testExecutions = new ArrayList<TestExecutionContext>();
            
            for (int i = 0; i < testRuns; ++i) {
                TestExecutionContext tec = new TestExecutionContext(configuration, (KualiTest)test.copy());
                testExecutions.add(tec);
            }
            
            retval = new TestExecutionMonitor(testExecutions, rampUpTime);
        } else {
            System.out.println("failed to find kuali test '" + testName + "' for plaform " + platformName);
        }
        
        return retval;
    }

    /**
     * 
     * @param configFileName
     * @param platformName
     * @param testSuiteName
     * @param testRuns
     * @param rampUpTime
     * @return 
     */
    public TestExecutionMonitor runTestSuite(String configFileName, String platformName, String testSuiteName, int testRuns, int rampUpTime) {
        TestExecutionMonitor retval = null;
        if (loadConfiguration(configFileName)) {
            retval = runTestSuite(platformName, testSuiteName, testRuns, rampUpTime);
        }
        return retval;
    }

    /**
     * 
     * @param configFileName
     * @param platformName
     * @param testName
     * @param testRuns
     * @param rampUpTime
     * @return 
     */
    public TestExecutionMonitor runTest(String configFileName, String platformName, String testName, int testRuns, int rampUpTime) {
        TestExecutionMonitor retval = null;
        if (loadConfiguration(configFileName)) {
            retval = runTest(platformName, testName, testRuns, rampUpTime);
        }
        return retval;
    }

    /**
     * 
     * @param platformName
     * @param testSuiteName
     * @param testRuns
     * @param rampUpTime
     * @return 
     */
    public TestExecutionMonitor runTestSuite(String platformName, String testSuiteName, int testRuns, int rampUpTime) {
        TestExecutionMonitor retval = null;
        TestSuite testSuite = Utils.findTestSuite(configuration, platformName, testSuiteName);

        if (testSuite != null) {
            List <TestExecutionContext> testExecutions = new ArrayList<TestExecutionContext>();
            
            for (int i = 0; i < testRuns; ++i) {
                TestExecutionContext tec = new TestExecutionContext(configuration, testSuite);
                testExecutions.add(tec);
            }
            
            retval = new TestExecutionMonitor(testExecutions, rampUpTime);
        } else {
            System.out.println("failed to find test suite '" + testSuiteName + "' for plaform " + platformName);
        }
        
        return retval;
    }

    /**
     *
     * @param platformName
     * @param testName
     */
    public void unScheduleTest(String platformName, String testName) {
        Iterator <TestExecutionContext> it = scheduledTests.iterator();
        
        while (it.hasNext()) {
            TestExecutionContext ec = it.next();
            KualiTest kualiTest = ec.getKualiTest();
            
            if (kualiTest != null) {
                if (StringUtils.equalsIgnoreCase(testName, kualiTest.getTestHeader().getTestName())
                    && StringUtils.equalsIgnoreCase(platformName, kualiTest.getTestHeader().getPlatformName())) {
                    it.remove();
                }
            }
        }
    }

    /**
     *
     * @param platformName
     * @param testSuiteName
     */
    public void unScheduleTestSuite(String platformName, String testSuiteName) {
        Iterator <TestExecutionContext> it = scheduledTests.iterator();
        
        while (it.hasNext()) {
            TestExecutionContext ec = it.next();
            TestSuite testSuite = ec.getTestSuite();
            
            if (testSuite != null) {
                if (StringUtils.equalsIgnoreCase(testSuiteName, testSuite.getName())
                    && StringUtils.equalsIgnoreCase(platformName, testSuite.getPlatformName())) {
                    it.remove();
                }
            }
        }
    }
    
    private boolean loadConfiguration(String configFilePath) {
        boolean retval = false;
        if (StringUtils.isNotBlank(configFilePath)) {
            File configFile = new File(configFilePath);

            if (configFile.exists() && configFile.isFile()) {
                List<XmlError> xmlValidationErrorList = new ArrayList<XmlError>();

                try {
                    configuration = KualiTestConfigurationDocument.Factory.parse(configFile).getKualiTestConfiguration();

                    String s = configuration.getRepositoryLocation().replace(Constants.REPOSITORY_ROOT_REPLACE, configFile.getParent());
                    configuration.setRepositoryLocation(s);

                    s = configuration.getAdditionalDbInfoLocation().replace(Constants.REPOSITORY_ROOT_REPLACE, configFile.getParent());
                    configuration.setAdditionalDbInfoLocation(s);

                    s = configuration.getEncryptionPasswordFile().replace(Constants.REPOSITORY_ROOT_REPLACE, configFile.getParent());
                    configuration.setEncryptionPasswordFile(s);

                    s = configuration.getTagHandlersLocation().replace(Constants.REPOSITORY_ROOT_REPLACE, configFile.getParent());
                    configuration.setTagHandlersLocation(s);

                    s = configuration.getTestResultLocation().replace(Constants.REPOSITORY_ROOT_REPLACE, configFile.getParent());
                    configuration.setTestResultLocation(s);

                    // Create an XmlOptions instance and set the error listener.
                    XmlOptions validateOptions = new XmlOptions();
                    validateOptions.setErrorListener(xmlValidationErrorList);
                    configuration.validate(validateOptions);

                    if (!xmlValidationErrorList.isEmpty()) {
                        throw new XmlException("invalid xml file: " + configFile.getPath());
                    }
                    
                    updateTestRunnerConfiguration();
                    Utils.initializeHandlers(configuration);
                    retval = true;
                } 
                
                catch (XmlException ex) {
                    System.out.println(ex.toString());

                    for (XmlError error : xmlValidationErrorList) {
                        System.out.println(error.toString());
                    }
                } 
                
                catch (IOException ex) {
                    System.out.println(ex.toString());
                }
            }
        }
        
        return retval;
    }
    
    /**
     *
     * @return
     */
    public List <TestExecutionContext> getExecutingTests() {
        List <TestExecutionContext> retval = new ArrayList<TestExecutionContext>();
        
        for (TestExecutionContext ec : executingTests) {
            if (!ec.isCompleted()) {
                retval.add(ec);
            }
        }
        
        return retval;
    }
    
    /**
     *
     * @return
     */
    public String getEncryptionPassword() {
        return Utils.getEncryptionPassword(configuration);
    }

}
