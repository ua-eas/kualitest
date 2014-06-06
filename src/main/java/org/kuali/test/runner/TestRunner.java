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
import org.kuali.test.utils.Utils;


public class TestRunner {
    private static final Logger LOG = Logger.getLogger(TestRunner.class);
    private KualiTestConfigurationDocument.KualiTestConfiguration configuration;
    private KualiTestRunnerDocument.KualiTestRunner testRunnerConfiguration;
    private final List<TestExecutionContext> scheduledTests = Collections.synchronizedList(new ArrayList<TestExecutionContext>());
    private final List<TestExecutionContext> executingTests = Collections.synchronizedList(new ArrayList<TestExecutionContext>());
    private boolean stopRunner = false;
    private Timer testInquiryTimer;
    private Timer configurationUpdateTimer;
    
    
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
    
    public TestRunner(KualiTestConfigurationDocument.KualiTestConfiguration configuration) {
        this.configuration = configuration;
    }
    
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

    protected void updateTestRunnerConfiguration() throws XmlException, IOException {
        File trConfigFile =  Utils.getTestRunnerConfigurationFile(configuration);
                    
        if (trConfigFile.exists() && trConfigFile.isFile()) {
            testRunnerConfiguration = KualiTestRunnerDocument.Factory.parse(trConfigFile).getKualiTestRunner();
        }
        
        List <ScheduledTest> activeTests = new ArrayList<ScheduledTest>();
        scheduledTests.clear();
        if (testRunnerConfiguration.getScheduledTests() != null) {
            for (ScheduledTest test : testRunnerConfiguration.getScheduledTests().getScheduledTestArray()) {
                if (test.getStartTime().getTimeInMillis() >= System.currentTimeMillis()) {
                    if (test.getType().equals(ScheduledTestType.TEST_SUITE)) {
                        scheduleTestSuite(test.getPlaformName(), test.getName(), test.getStartTime().getTime(), test.getTestRuns());
                    } else if (test.getType().equals(ScheduledTestType.PLATFORM_TEST)) {
                        scheduleTest(test.getPlaformName(), test.getName(), test.getStartTime().getTime(), test.getTestRuns());
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
    
    private void saveTestRunnerConfiguration() throws IOException {
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
    
    private void checkForRunnableTests() {
        System.out.println("checking for runnable test");
        
        synchronized(scheduledTests) {
            Iterator <TestExecutionContext> it = scheduledTests.iterator();

            while (it.hasNext()) {
                TestExecutionContext ec = it.next();

                if ((ec.getScheduledTime() != null) && (ec.getScheduledTime().getTime() < System.currentTimeMillis())) {
                    List <TestExecutionContext> testExecutions = ec.getTestInstances();

                    for (TestExecutionContext testExecution : testExecutions) {
                        KualiTest kualiTest = testExecution.getKualiTest();
                        TestSuite testSuite = testExecution.getTestSuite();
                        String nm = "unknown";
                        String platformName = "unknown";

                        if (kualiTest != null) {
                            nm = ("'" + kualiTest.getTestHeader().getTestName());
                            platformName= kualiTest.getTestHeader().getPlatformName();
                        } else if (testSuite != null) {
                            nm = (" suite '" + testSuite.getName());
                            platformName = testSuite.getPlatformName();
                        }

                        System.out.println("starting test '" + nm + "[" + testExecution.getTestRun() + "]' for platform " + platformName);
                    }

                    new TestExecutionMonitor(testExecutions);

                    executingTests.addAll(testExecutions);

                    it.remove();
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
    
    public void scheduleTest(String platformName, String testName, Date scheduledTime, int testRuns) {
        if (scheduledTime == null) {
            System.out.println("scheduled time is null - abort scheduling");
        } else if (scheduledTime.getTime() <= System.currentTimeMillis()) {
            System.out.println("scheduled time is in past - abort scheduling");
        } else {
            KualiTest test = Utils.findKualiTest(configuration, platformName, testName);
            
            if (test != null) {
                scheduledTests.add(new TestExecutionContext(configuration, test, scheduledTime, testRuns));
            } else {
                System.out.println("failed to find kuali test '" + testName + "' for plaform " + platformName);
            }
        }
    }

    public void scheduleTestSuite(String platformName, String testSuiteName, Date scheduledTime, int testRuns) {
        if (scheduledTime == null) {
            System.out.println("scheduled time is null - abort scheduling");
        } else if (scheduledTime.getTime() <= System.currentTimeMillis()) {
            System.out.println("scheduled time is in past - abort scheduling");
        } else {
            TestSuite testSuite = Utils.findTestSuite(configuration, platformName, testSuiteName);
            
            if (testSuite != null) {
                scheduledTests.add(new TestExecutionContext(configuration, testSuite, scheduledTime, testRuns));
            } else {
                System.out.println("failed to find test suite '" + testSuiteName + "' for plaform " + platformName);
            }
        }
    }

    public void runTest(String platformName, String testName) {
        KualiTest test = Utils.findKualiTest(configuration, platformName, testName);

        if (test != null) {
            List <TestExecutionContext> testExecutions = new ArrayList<TestExecutionContext>();
            testExecutions.add(new TestExecutionContext(configuration, test));
            new TestExecutionMonitor(testExecutions);
        } else {
            System.out.println("failed to find kuali test '" + testName + "' for plaform " + platformName);
        }
    }

    public void runTestSuite(String platformName, String testSuiteName) {
        TestSuite testSuite = Utils.findTestSuite(configuration, platformName, testSuiteName);

        if (testSuite != null) {
            List <TestExecutionContext> testExecutions = new ArrayList<TestExecutionContext>();
            testExecutions.add(new TestExecutionContext(configuration, testSuite));
            new TestExecutionMonitor(testExecutions);
        } else {
            System.out.println("failed to find test suite '" + testSuiteName + "' for plaform " + platformName);
        }
    }

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

                    // Create an XmlOptions instance and set the error listener.
                    XmlOptions validateOptions = new XmlOptions();
                    validateOptions.setErrorListener(xmlValidationErrorList);
                    configuration.validate(validateOptions);

                    if (!xmlValidationErrorList.isEmpty()) {
                        throw new XmlException("invalid xml file: " + configFile.getPath());
                    }
                    
                    updateTestRunnerConfiguration();
                    
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
    
    public List <TestExecutionContext> getExecutingTests() {
        List <TestExecutionContext> retval = new ArrayList<TestExecutionContext>();
        
        for (TestExecutionContext ec : executingTests) {
            if (!ec.isCompleted()) {
                retval.add(ec);
            }
        }
        
        return retval;
    }
}
