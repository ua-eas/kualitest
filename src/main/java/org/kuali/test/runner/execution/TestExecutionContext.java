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

import java.io.FileOutputStream;
import java.util.Date;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.kuali.test.Checkpoint;
import org.kuali.test.CheckpointType;
import org.kuali.test.KualiTestConfigurationDocument;
import org.kuali.test.KualiTestDocument.KualiTest;
import org.kuali.test.SuiteTest;
import org.kuali.test.TestOperation;
import org.kuali.test.TestSuite;
import org.kuali.test.runner.exceptions.TestException;
import org.kuali.test.utils.Constants;
import org.kuali.test.utils.Utils;


public class TestExecutionContext extends Thread {
    private static final Logger LOG = Logger.getLogger(TestExecutionContext.class);
    
    private TestSuite testSuite;
    private KualiTest kualiTest;
    private Date scheduledTime;
    private Date startTime;
    private Date endTime;
    private boolean completed = false;
    private KualiTestConfigurationDocument.KualiTestConfiguration configuration;

    public TestExecutionContext(KualiTestConfigurationDocument.KualiTestConfiguration configuration, 
        TestSuite testSuite, Date scheduledTime) {
        this.testSuite = testSuite;
        this.scheduledTime = scheduledTime;
        this.configuration = configuration;
        
        // if no scheduled time then run immediately
        if (scheduledTime == null) {
            startTest();
        }
    }

    public TestExecutionContext(KualiTestConfigurationDocument.KualiTestConfiguration configuration, 
        TestSuite testSuite) {
        this(configuration, testSuite, null);
    }

    public TestExecutionContext(KualiTestConfigurationDocument.KualiTestConfiguration configuration, 
        KualiTest kualiTest, Date scheduledTime) {
        this.kualiTest = kualiTest;
        this.scheduledTime = scheduledTime;
        this.configuration = configuration;
        
        // if no scheduled time then run immediately
        if (scheduledTime == null) {
            startTest();
        }
    }

    public TestExecutionContext(KualiTestConfigurationDocument.KualiTestConfiguration configuration, KualiTest kualiTest) {
        this(configuration, kualiTest, null);
    }
    
    @Override
    public void run() {
        startTime= new Date();
        FileOutputStream fos = null;
        
        try {
            Workbook testReport = new XSSFWorkbook();

            if (testSuite != null) {
                for (SuiteTest suiteTest : testSuite.getSuiteTests().getSuiteTestArray()) {
                    KualiTest test = Utils.findKualiTest(configuration, suiteTest.getTestHeader().getPlatformName(), suiteTest.getTestHeader().getTestName());

                    if (test != null) {
                        runTest(test, testReport);
                    }
                }
            } else if (kualiTest != null) {
                runTest(kualiTest, testReport);
            }

            endTime= new Date();

            fos = new FileOutputStream(buildTestReportFileName());
            testReport.write(fos);
        }
        
        catch (Exception ex) {
            LOG.error(ex.toString(), ex);
        }
        
        finally {
            if (fos != null) {
                try {
                    fos.close();
                }
                
                catch (Exception ex) {};
            }
        }
        
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
        retval.append(Constants.FILENAME_TIMESTAMP_FORMAT.format(new Date()));
        retval.append(".xlsx");
        
        return null;
    }
    
    private void runTest(KualiTest test, Workbook testReport) {
        for (TestOperation op : test.getOperations().getOperationArray()) {
            executeTestOperation(op, testReport);
        }
    }

    private void executeTestOperation(TestOperation op, Workbook testReport) {
        try {
            OperationExecution opExec = OperationExecutionFactory.getInstance().getOperationExecution(op);
            
            if (opExec != null) {
                opExec.execute();
            }
        } 
        
        catch (TestException ex) {
            
        }
    }
    
    private void executeCheckpoint(Checkpoint checkpoint) throws TestException {
        switch (checkpoint.getType().intValue()) {
            case CheckpointType.INT_FILE:
                break;
            case CheckpointType.INT_HTTP:
                break;
            case CheckpointType.INT_MEMORY:
                break;
            case CheckpointType.INT_SQL:
                break;
            case CheckpointType.INT_WEB_SERVICE:
                break;
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
}
