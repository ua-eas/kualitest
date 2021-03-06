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
import java.io.FileReader;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.kuali.test.TestHeader;
import org.kuali.test.TestOperation;
import org.kuali.test.TestOperationType;
import org.kuali.test.runner.output.PoiHelper;
import org.kuali.test.utils.Constants;
import org.kuali.test.utils.Utils;
import org.kuali.test.utils.ZipDirectory;

/**
 *
 * @author rbtucker
 */
public class TestExecutionMonitor extends Thread {
    private static final Logger LOG = Logger.getLogger(TestExecutionMonitor.class);
    private static final int DEFAULT_TEST_MONITORING_SLEEP_TIME = 2000;
    private List <TestExecutionContext> testExecutionList;
    private TestOperation currentTestOperation;
    private int testOperationCount = 0;
    private String overrideEmail;
    private Map<Integer, Boolean> runErrorFlags = new HashMap<Integer, Boolean>();
    
    /**
     *
     * @param testExecutionList
     */
    public TestExecutionMonitor(List <TestExecutionContext> testExecutionList, int rampUpTime) {
        this.testExecutionList = testExecutionList;
        int i = 1;
        
        int waitInterval = 0;
        
        if ((testExecutionList != null) && !testExecutionList.isEmpty()) {
            waitInterval = ((rampUpTime * 1000) / testExecutionList.size());
        }
        
        for (TestExecutionContext tec : testExecutionList) {
            tec.startTest();
            tec.setTestRun(i++);
            
            if (waitInterval > 0) {
                try {
                    Thread.sleep(waitInterval);
                } 
                
                catch (InterruptedException ex) {}
            }
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

        Utils.sendMail(tec.getConfiguration(), overrideEmail, tec.getTestSuite(), testHeader, getTestResultsFileList(), count[0], count[1], count[2]);
    }
    
    private int[] getReportCounts() {
        int[] retval = {0, 0, 0};
        for (TestExecutionContext tec : testExecutionList) {
            for (KualiTestWrapper test : tec.getCompletedTests()) {
                if ((test.getErrorCount() > 0) || (test.getWarningCount() > 0)) {
                    runErrorFlags.put(tec.getTestRun(), Boolean.TRUE);
                }
                
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
            
            if (tec.getPerformanceDataFile() != null) {
                retval.add(tec.getPerformanceDataFile());
            }

            if ((tec.getGeneratedCheckpointFiles() != null) && !tec.getGeneratedCheckpointFiles().isEmpty()) {
                if (tec.getGeneratedCheckpointFiles().size() > 1) {
                    File parentDir = tec.getGeneratedCheckpointFiles().get(0).getParentFile();
                    File f = new File(parentDir.getPath() + File.separator + "checkpoint-files-" + Constants.FILENAME_TIMESTAMP_FORMAT.format(new Date()) + ".zip");
                    try {
                        new ZipDirectory(parentDir, f, tec.getGeneratedCheckpointFiles());
                    } catch (Exception ex) {
                        LOG.error(ex.toString(), ex);
                    }
                    
                    retval.add(f);
                } else {
                    retval.addAll(tec.getGeneratedCheckpointFiles());
                }
            }
        } else {
            File merged = getMergedResultFile();
            
            if (merged != null) {
                retval.add(merged);
            }
            
            merged = getMergedPerformanceDataFile();
            
            if (merged != null) {
                retval.add(merged);
            }
            
            List <File> checkpointFiles = new ArrayList<File>();
            for (TestExecutionContext tec : testExecutionList) {
                if (tec.getGeneratedCheckpointFiles() != null) {
                    checkpointFiles.addAll(tec.getGeneratedCheckpointFiles());
                }
            }
            
            if (!checkpointFiles.isEmpty()) {
                if (checkpointFiles.size() > 1) {
                    File parentDir = checkpointFiles.get(0).getParentFile();
                    File f = new File(parentDir.getPath() + File.separator + "checkpoint-files-" + Constants.FILENAME_TIMESTAMP_FORMAT.format(new Date()) + ".zip");
                    try {
                        new ZipDirectory(parentDir, f, checkpointFiles);
                    } catch (Exception ex) {
                        LOG.error(ex.toString(), ex);
                    }
                    
                    retval.add(f);
                } else {
                    retval.addAll(checkpointFiles);
                }
            }
            
        }
        
        return retval;
    }
    
    private File getMergedPerformanceDataFile() {
        File retval = null;
        PrintWriter pw = null;
        try {
            boolean headerWritten = false;
            for (TestExecutionContext tec : testExecutionList) {
                File f = tec.getPerformanceDataFile();
                if (f != null) {
                    if (pw == null) {
                        int pos = f.getPath().lastIndexOf("_");
                        pw = new PrintWriter(retval = new File(f.getPath().substring(0, pos) + ".csv"));
                    }

                    LineNumberReader lnr = null;
                    try {
                        lnr = new LineNumberReader(new FileReader(f));

                        if (headerWritten) {
                            lnr.readLine();
                        }
                        
                        String line = null;
                        
                        while ((line = lnr.readLine()) != null) {
                            pw.println(line);
                        }
                        
                        headerWritten = true;
                    }
                    
                    catch (Exception ex) {
                        LOG.error(ex.toString(), ex);
                    }
                    
                    finally {
                        if (lnr != null) {
                            try {
                                lnr.close();
                                FileUtils.deleteQuietly(f);
                            }
                            
                            catch (Exception ex) {};
                        }
                    }
                }
            }
        }

        catch (Exception ex) {
            LOG.error(ex.toString(), ex);
            retval = null;
        }
        
        finally {
            if (pw != null) {
                pw.close();
            }
        }
        
        return retval;
    }
    
    private File getMergedResultFile() {
        PoiHelper poiHelper = new PoiHelper(false);
        List <File> files = new ArrayList<File>();

        boolean[] errorRuns = new boolean[testExecutionList.size()];

        for (TestExecutionContext tec : testExecutionList) {
            if (runErrorFlags.containsKey(tec.getTestRun())) {
                errorRuns[tec.getTestRun()-1] = runErrorFlags.get(tec.getTestRun());
            }
            files.add(tec.getTestResultsFile());
        }
        
        String fname = files.get(0).getPath();
        
        int pos = fname.lastIndexOf(".");
        pos = fname.lastIndexOf("_", pos);
        
        fname = fname.substring(0, pos) + ".xlsx";
        return poiHelper.mergeWorkbookFiles(fname, files, errorRuns, true);
    }

    public TestOperation getCurrentTestOperation() {
        return currentTestOperation;
    }

    public int getTestOperationCount() {
        return testOperationCount;
    }

    public String buildDisplayMessage(String header, long startTime) {
        StringBuilder retval = new StringBuilder(256);
        
        retval.append("<html><div style='padding: 5px 0px 10px 5px;'>");
        retval.append(header);
        retval.append("</div>");

        TestExecutionContext tec = testExecutionList.get(0);

        retval.append("<div style='padding: 5px 0px 0px 5px;'>Test ");
        
        if ((tec.getTestSuite() != null) && (tec.getCurrentTest() != null)) {
            retval.append("'");
            retval.append(tec.getCurrentTest().getTestHeader().getTestName());
            retval.append("'<br />&nbsp;&nbsp;");
        }
        retval.append("operation ");
        if ((getCurrentTestOperation() != null)
            && (getCurrentTestOperation().getOperation() != null)) {
            retval.append(getCurrentTestOperation().getOperation().getIndex());
            retval.append(" of ");
            if (getTestOperationCount() > 0) {
                retval.append(getTestOperationCount());
            }

            retval.append("</div><table style='border-collapse: collapse; border-spacing: 0;'><tr>");
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
            retval.append("- of -</div><table>");
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

    public String getOverrideEmail() {
        return overrideEmail;
    }

    public void setOverrideEmail(String overrideEmail) {
        this.overrideEmail = overrideEmail;
    }
    
    public void haltTests() {
        for (TestExecutionContext tec : testExecutionList) {
            tec.haltTest();
        }
    }
}
