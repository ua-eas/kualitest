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

package org.kuali.test.runner.execution;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.test.KualiTestConfigurationDocument;
import org.kuali.test.KualiTestDocument.KualiTest;
import org.kuali.test.Platform;
import org.kuali.test.SuiteTest;
import org.kuali.test.TestHeader;
import org.kuali.test.TestOperation;
import org.kuali.test.TestSuite;
import org.kuali.test.runner.exceptions.TestException;
import org.kuali.test.runner.output.PoiHelper;
import org.kuali.test.utils.Constants;
import org.kuali.test.utils.Utils;


public class TestExecutionContext extends Thread {
    private static final Logger LOG = Logger.getLogger(TestExecutionContext.class);
    private static final int DEFAULT_HTTP_RESPONSE_BUFFER_SIZE = 1024;
    private List <File> testResultFiles = new ArrayList<File>();
    
    private StringBuilder lastHttpResponseData = new StringBuilder(DEFAULT_HTTP_RESPONSE_BUFFER_SIZE);
    
    private Platform platform;
    private TestSuite testSuite;
    private KualiTest kualiTest;
    private Date scheduledTime;
    private Date startTime;
    private Date endTime;
    private int testRun = 1;
    private int testRuns = 1;
    private boolean completed = false;
    
    private KualiTestConfigurationDocument.KualiTestConfiguration configuration;
    public TestExecutionContext() {
    }
    
    public TestExecutionContext(KualiTestConfigurationDocument.KualiTestConfiguration configuration, 
        TestSuite testSuite, Date scheduledTime, int testRuns) {
        this.testSuite = testSuite;
        this.scheduledTime = scheduledTime;
        this.configuration = configuration;
        this.testRuns = testRuns;
        platform = Utils.findPlatform(configuration, testSuite.getPlatformName());
    }

    public TestExecutionContext(KualiTestConfigurationDocument.KualiTestConfiguration configuration, 
        TestSuite testSuite) {
        this(configuration, testSuite, null, 1);
    }

    public TestExecutionContext(KualiTestConfigurationDocument.KualiTestConfiguration configuration, 
        KualiTest kualiTest, Date scheduledTime, int testRuns) {
        this.kualiTest = kualiTest;
        this.scheduledTime = scheduledTime;
        this.configuration = configuration;
        this.testRuns = testRuns;
        platform = Utils.findPlatform(configuration, kualiTest.getTestHeader().getPlatformName());
    }

    public TestExecutionContext(KualiTestConfigurationDocument.KualiTestConfiguration configuration, KualiTest kualiTest) {
        this(configuration, kualiTest, null, 1);
    }
    
    @Override
    public void run() {
        runTest();
    }
    
    public void runTest() {
        startTime= new Date();
        
        PoiHelper poiHelper = new PoiHelper();
        poiHelper.writeReportHeader(testSuite, kualiTest);
        poiHelper.writeColumnHeaders();

        if (testSuite != null) {
            for (SuiteTest suiteTest : testSuite.getSuiteTests().getSuiteTestArray()) {
                KualiTest test = Utils.findKualiTest(configuration, suiteTest.getTestHeader().getPlatformName(), suiteTest.getTestHeader().getTestName());

                if (test != null) {
                    poiHelper.writeTestHeader(test);
                    runTest(test, poiHelper);
                }
            }
        } else if (kualiTest != null) {
            runTest(kualiTest, poiHelper);
        }

        endTime= new Date();

        TestHeader testHeader = null;

        if (kualiTest != null) {
            testHeader = kualiTest.getTestHeader();
        }

        File f = new File(buildTestReportFileName());
        testResultFiles.add(f);
        poiHelper.writeFile(f);

        Utils.sendMail(configuration, testSuite, testHeader, testResultFiles);

        completed = true;
    }
    
    private String buildTestReportFileName() {
        StringBuilder retval = new StringBuilder(128);
        
        retval.append(configuration.getTestResultLocation());
        retval.append("/");
        if (testSuite != null) {
            retval.append(testSuite.getPlatformName());
            retval.append("/");
            retval.append(Utils.formatForFileName(testSuite.getName()));
        } else {
            retval.append(kualiTest.getTestHeader().getPlatformName());
            retval.append("/");
            retval.append(Utils.getTestFileName(kualiTest.getTestHeader()));
        }

        retval.append("-");
        retval.append(Constants.FILENAME_TIMESTAMP_FORMAT.format(startTime));
        retval.append("_");
        retval.append(testRun);
        retval.append(".xlsx");
        
        return retval.toString();
    }

    
    private void runTest(KualiTest test, PoiHelper poiHelper) {
        if (LOG.isInfoEnabled()) {
            LOG.info("--------------------------- starting test ---------------------------");
            LOG.info("platform: " + test.getTestHeader().getPlatformName());
            
            if (StringUtils.isNotBlank(test.getTestHeader().getTestSuiteName())) {
                LOG.info("test suite: " + test.getTestHeader().getTestSuiteName());
            }

            LOG.info("test: " + test.getTestHeader().getTestName());
            LOG.info("---------------------------------------------------------------------");
        }
        
        for (TestOperation op : test.getOperations().getOperationArray()) {
            executeTestOperation(op, poiHelper);
            
        }
    }

    private void executeTestOperation(TestOperation op, PoiHelper poiHelper) {
        OperationExecution opExec = null;
        
        Date opStartTime = new Date();
        try {
            opExec = OperationExecutionFactory.getInstance().getOperationExecution(this, op);
            if (opExec != null) {
                opExec.execute(configuration, platform);
                if (op.getOperation().getCheckpointOperation() != null) {
                    poiHelper.writeSuccessEntry(op, opStartTime);
                }
            }
        } 
        
        catch (TestException ex) {
            poiHelper.writeFailureEntry(op, opStartTime, ex);
        }
    }
    
    
    public TestSuite getTestSuite() {
        return testSuite;
    }

    public void setTestSuite(TestSuite testSuite) {
        this.testSuite = testSuite;
    }

    public KualiTest getKualiTest() {
        return kualiTest;
    }

    public void setKualiTest(KualiTest kualiTest) {
        this.kualiTest = kualiTest;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }
    
    public final void startTest() {
        start();
    }

    public Date getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(Date scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public boolean isCompleted() {
        return completed;
    }
    
    public StringBuilder getLastHttpResponseData() {
        return getLastHttpResponseData(false);
    }
    
    public StringBuilder getLastHttpResponseData(boolean clear) {
        if (lastHttpResponseData == null) {
            lastHttpResponseData= new StringBuilder(DEFAULT_HTTP_RESPONSE_BUFFER_SIZE);
        }
        
        if (clear) {
            lastHttpResponseData.setLength(0);
        }
        
        return lastHttpResponseData;
    }

    public List<File> getTestResultFiles() {
        return testResultFiles;
    }

    public int getTestRun() {
        return testRun;
    }

    public List<TestExecutionContext> getTestInstances() {
        List <TestExecutionContext> retval = new ArrayList<TestExecutionContext>();;
        retval.add(this);
        
        for (int i = 1; i < testRuns; ++i) {
            TestExecutionContext tec = new TestExecutionContext();
            tec.setStartTime(startTime);
            tec.setPlatform(platform);
            tec.setKualiTest(kualiTest);
            tec.setTestRun(i+1);
            tec.setConfiguration(configuration);
            retval.add(tec);
        }
        
        return retval;
    }

    public void setPlatform(Platform platform) {
        this.platform = platform;
    }

    public void setTestRun(int testRun) {
        this.testRun = testRun;
    }

    public void setConfiguration(KualiTestConfigurationDocument.KualiTestConfiguration configuration) {
        this.configuration = configuration;
    }
}
