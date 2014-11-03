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
import java.util.List;
import org.kuali.test.TestHeader;
import org.kuali.test.TestOperation;
import org.kuali.test.TestOperationType;
import org.kuali.test.runner.output.PoiHelper;
import org.kuali.test.utils.Utils;

/**
 *
 * @author rbtucker
 */
public class TestExecutionMonitor extends Thread {
    private static final int DEFAULT_TEST_MONITORING_SLEEP_TIME = 2000;
    private List <TestExecutionContext> testExecutionList;
    private TestOperation currentTestOperation;
    private int testOperationCount = 0;
    
    /**
     *
     * @param testExecutionList
     */
    public TestExecutionMonitor(List <TestExecutionContext> testExecutionList) {
        this.testExecutionList = testExecutionList;
        for (TestExecutionContext tec : testExecutionList) {
            tec.startTest();
        }
        
        start();
    }

    @Override
    public void run() {
        while (!testsCompleted()) {
            try {
                Thread.sleep(DEFAULT_TEST_MONITORING_SLEEP_TIME);
                
                // if we only have 1 test then we can monitor the test operations
                if (testExecutionList.size() == 1) {
                    TestExecutionContext tec = testExecutionList.get(0);
                    currentTestOperation = tec.getCurrentTestOperation();
                    testOperationCount = tec.getTestOperationCount();
                }
            } 
            
            catch (InterruptedException ex) {};
        }
        
        outputTestResults();
    }
    
    public boolean testsCompleted() {
        boolean retval = true;
        for (TestExecutionContext tec : testExecutionList) {
            if (!tec.isCompleted()) {
                retval = false;
                break;
            }
        }
        
        return retval;
    }
    
    private void outputTestResults() {
        TestExecutionContext tec = testExecutionList.get(0);
        TestHeader testHeader = null;
        
        if (tec.getKualiTest() != null) {
            testHeader = tec.getKualiTest().getTestHeader();
        }
        
        int[] count = getReportCounts();
        Utils.sendMail(tec.getConfiguration(), tec.getTestSuite(), testHeader, getTestResultsFileList(), count[0], count[1], count[2]);
    }
    
    private int[] getReportCounts() {
        int[] retval = {0, 0, 0};
        for (TestExecutionContext tec : testExecutionList) {
            for (KualiTestWrapper test : tec.getCompletedTests()) {
                retval[0] += test.getErrorCount();
                retval[1] += test.getWarningCount();
                retval[2] += test.getSuccessCount();
            }
        }
        
        return retval;
    }
    
    private List <File> getTestResultsFileList() {
        List <File> retval = new ArrayList<File>();
        
        if (testExecutionList.size() == 1) {
            TestExecutionContext tec = testExecutionList.get(0);
            
            if (tec.getTestResultsFile() != null) {
                retval.add(tec.getTestResultsFile());
            }
            
            if (tec.getGeneratedCheckpointFiles() != null) {
                retval.addAll(tec.getGeneratedCheckpointFiles());
            }
        } else {
            retval.add(getMergedResultFiles());
            
            for (TestExecutionContext tec : testExecutionList) {
                if (tec.getGeneratedCheckpointFiles() != null) {
                    retval.addAll(tec.getGeneratedCheckpointFiles());
                }
            }
        }
        
        return retval;
    }
    
    private File getMergedResultFiles() {
        PoiHelper poiHelper = new PoiHelper(false);
        List <File> files = new ArrayList<File>();
        
        for (TestExecutionContext tec : testExecutionList) {
            files.add(tec.getTestResultsFile());
        }
        
        String fname = files.get(0).getPath();
        
        int pos = fname.lastIndexOf(".");
        pos = fname.lastIndexOf("_", pos);
        
        fname = fname.substring(0, pos) + ".xlsx";
        return poiHelper.mergeWorkbookFiles(fname, files, true);
    }

    public TestOperation getCurrentTestOperation() {
        return currentTestOperation;
    }

    public int getTestOperationCount() {
        return testOperationCount;
    }

    public String buildDisplayMessage(long startTime) {
        StringBuilder retval = new StringBuilder(256);
        
        retval.append("<html>&nbsp;&nbsp;");

        TestExecutionContext tec = testExecutionList.get(0);

        retval.append("<span>Test ");
        
        if ((tec.getTestSuite() != null) && (tec.getCurrentTest() != null)) {
            retval.append(tec.getCurrentTest().getTestHeader().getTestName());
            retval.append("<br />&nbsp;&nbsp;");
        }
        retval.append("operation ");
        if ((getCurrentTestOperation() != null)
            && (getCurrentTestOperation().getOperation() != null)) {
            retval.append(getCurrentTestOperation().getOperation().getIndex());
            retval.append(" of ");
            if (getTestOperationCount() > 0) {
                retval.append(getTestOperationCount());
            }

            retval.append("</span><table style='border-collapse: collapse; border-spacing: 0;'><tr>");
            retval.append("<th style='text-align: right; padding: 0px 0px 0px 5px;'>Operation Type:</th><td style='padding: 0px 0px 0px 5px;'>");
            retval.append(getCurrentTestOperation().getOperationType().toString());
            retval.append("</td></tr>");
            if (getCurrentTestOperation().getOperationType().equals(TestOperationType.CHECKPOINT)) {
                retval.append("<tr><th style='text-align: right; padding: 0px 0px 0px 5px;'>Checkpoint Type:</th><td style='padding: 0px 0px 0px 5px;'>");
                retval.append(getCurrentTestOperation().getOperation().getCheckpointOperation().getType().toString());
                retval.append("</td></tr>");
                retval.append("<tr><th style='text-align: right; padding: 0px 0px 0px 5px;'>Checkpoint Name:</th><td style='padding: 0px 0px 0px 5px;'>");
                retval.append(getCurrentTestOperation().getOperation().getCheckpointOperation().getName());
                retval.append("</td></tr>");
            }
        } else {
            retval.append("- of -</span><table>");
        }

        retval.append("<tr><th style='text-align: right; padding: 0px 0px 0px 5px;'>Elapsed Time:</th><td style='padding: 0px 0px 0px 5px;'>");

        long seconds = ((System.currentTimeMillis() - startTime) / 1000);
        if (seconds >= 60) {
            retval.append((seconds / 60));
            retval.append("min. ");
        }

        retval.append(seconds % 60);
        retval.append("sec.</td></tr></table>");
        retval.append("</html>");

        return retval.toString();
    }
}
