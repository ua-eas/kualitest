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
import org.kuali.test.TestSuite;
import org.kuali.test.utils.ApplicationInstanceListener;
import org.kuali.test.utils.ApplicationInstanceManager;
import org.kuali.test.utils.Constants;
import org.kuali.test.utils.Utils;


public class TestRunner {
    private static final Logger LOG = Logger.getLogger(TestRunner.class);
    private KualiTestConfigurationDocument.KualiTestConfiguration configuration;
    private final List<TestExecutionContext> scheduledTests = Collections.synchronizedList(new ArrayList<TestExecutionContext>());
    private final List<TestExecutionContext> executingTests = Collections.synchronizedList(new ArrayList<TestExecutionContext>());
    private boolean stopRunner = false;
    private Timer timer;
    
    
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
    
    public TestRunner(String configFileName) {
        if (loadConfiguration(configFileName)) {
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    if (!stopRunner) {
                        checkForRunnableTests();
                    }
                }
            };

	        timer = new Timer();
            timer.scheduleAtFixedRate(timerTask, Constants.TEST_RUNNER_CHECK_INTERVAL, Constants.TEST_RUNNER_CHECK_INTERVAL);
        } else {
            System.exit(-1);
        }
    }

    public void stopRunner() {
        stopRunner = true;
        timer.cancel();
    }
    
    private void checkForRunnableTests() {
        Iterator <TestExecutionContext> it = scheduledTests.iterator();
        
        while (it.hasNext()) {
            TestExecutionContext ec = it.next();
            
            if ((ec.getScheduledTime() != null) && (ec.getScheduledTime().getTime() < System.currentTimeMillis())) {
                KualiTest kualiTest = ec.getKualiTest();
                TestSuite testSuite = ec.getTestSuite();
                String nm = "unknown";
                String platformName = "unknown";
                
                if (kualiTest != null) {
                    nm = ("'" + kualiTest.getTestHeader().getTestName());
                    platformName= kualiTest.getTestHeader().getPlatformName();
                } else if (testSuite != null) {
                    nm = (" suite '" + testSuite.getName());
                    platformName = testSuite.getPlatformName();
                }
                
                System.out.println("starting test " + nm + "' for platform " + platformName);
                
                it.remove();
                ec.startTest();
                executingTests.add(ec);
            }
        }

        // removed completed tests
        it = executingTests.iterator();
        
        while (it.hasNext()) {
            TestExecutionContext ec = it.next();
            if (ec.isCompleted()) {
                it.remove();
            }
        }
    }
    
    public void scheduleTest(String platformName, String testName, Date scheduledTime) {
        if (scheduledTime == null) {
            System.out.println("scheduled time is null - abort scheduling");
        } else if (scheduledTime.getTime() <= System.currentTimeMillis()) {
            System.out.println("scheduled time is in past - abort scheduling");
        } else {
            KualiTest test = Utils.findKualiTest(configuration, platformName, testName);
            
            if (test != null) {
                scheduledTests.add(new TestExecutionContext(configuration, test, scheduledTime));
            } else {
                System.out.println("failed to find kuali test '" + testName + "' for plaform " + platformName);
            }
        }
    }

    public void scheduleTestSuite(String platformName, String testSuiteName, Date scheduledTime) {
        if (scheduledTime == null) {
            System.out.println("scheduled time is null - abort scheduling");
        } else if (scheduledTime.getTime() <= System.currentTimeMillis()) {
            System.out.println("scheduled time is in past - abort scheduling");
        } else {
            TestSuite testSuite = Utils.findTestSuite(configuration, platformName, testSuiteName);
            
            if (testSuite != null) {
                scheduledTests.add(new TestExecutionContext(configuration, testSuite, scheduledTime));
            } else {
                System.out.println("failed to find test suite '" + testSuiteName + "' for plaform " + platformName);
            }
        }
    }

    public void runTest(String platformName, String testName) {
        KualiTest test = Utils.findKualiTest(configuration, platformName, testName);

        if (test != null) {
            executingTests.add(new TestExecutionContext(configuration, test));
        } else {
            System.out.println("failed to find kuali test '" + testName + "' for plaform " + platformName);
        }
    }

    public void runTestSuite(String platformName, String testSuiteName) {
        TestSuite testSuite = Utils.findTestSuite(configuration, platformName, testSuiteName);

        if (testSuite != null) {
            scheduledTests.add(new TestExecutionContext(configuration, testSuite));
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
