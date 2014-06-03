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
import org.apache.poi.hssf.usermodel.HSSFHeader;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.util.CellRangeAddress;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Header;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.kuali.test.CheckpointProperty;
import org.kuali.test.FailureAction;
import org.kuali.test.KualiTestConfigurationDocument;
import org.kuali.test.KualiTestDocument.KualiTest;
import org.kuali.test.Platform;
import org.kuali.test.SuiteTest;
import org.kuali.test.TestOperation;
import org.kuali.test.TestSuite;
import org.kuali.test.runner.exceptions.TestException;
import org.kuali.test.utils.Constants;
import org.kuali.test.utils.Utils;


public class TestExecutionContext extends Thread {
    private static final Logger LOG = Logger.getLogger(TestExecutionContext.class);
    
    private static final String[] HEADER_NAMES = {
        "Checkpoint Name",
        "Checkpoint Type",
        "Start Time",
        "End Time",
        "Run Time (sec)",
        "Expected Values",
        "Actual Values",
        "Status",
        "Errors"
    };
    
    private Platform platform;
    private TestSuite testSuite;
    private KualiTest kualiTest;
    private Date scheduledTime;
    private Date startTime;
    private Date endTime;
    private int currentReportRow = 0;
    private boolean completed = false;
    private CellStyle cellStyleNormal = null;
    private CellStyle cellStyleBold = null;
    private CellStyle cellStyleTestHeader = null;
    private CellStyle cellStyleIgnore = null;
    private CellStyle cellStyleSuccess = null;
    private CellStyle cellStyleWarning = null;
    private CellStyle cellStyleError = null;
    
    private KualiTestConfigurationDocument.KualiTestConfiguration configuration;

    public TestExecutionContext(KualiTestConfigurationDocument.KualiTestConfiguration configuration, 
        TestSuite testSuite, Date scheduledTime) {
        this.testSuite = testSuite;
        this.scheduledTime = scheduledTime;
        this.configuration = configuration;
        platform = Utils.findPlatform(configuration, testSuite.getName());
        
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
        platform = Utils.findPlatform(configuration, kualiTest.getTestHeader().getPlatformName());

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
            Sheet sheet = testReport.createSheet();
            createPoiCellStyles(testReport);
            writeReportHeader(sheet);
            writeColumnHeaders(sheet);
            
            if (testSuite != null) {
                for (SuiteTest suiteTest : testSuite.getSuiteTests().getSuiteTestArray()) {
                    KualiTest test = Utils.findKualiTest(configuration, suiteTest.getTestHeader().getPlatformName(), suiteTest.getTestHeader().getTestName());

                    if (test != null) {
                        writeTestHeader(sheet, test);
                        runTest(test, sheet);
                    }
                }
            } else if (kualiTest != null) {
                runTest(kualiTest, sheet);
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
    
    private void createPoiCellStyles(Workbook wb) {
        // create bold cell style
        Font font = wb.createFont();
        font.setBoldweight(Font.BOLDWEIGHT_BOLD);
        font.setFontHeightInPoints((short) 10);
        cellStyleBold = wb.createCellStyle();
        cellStyleBold.setFont(font);

        // create standard cell style
        font = wb.createFont();
        font.setBoldweight(Font.BOLDWEIGHT_NORMAL);
        font.setFontHeightInPoints((short) 10);
        cellStyleNormal = wb.createCellStyle();
        cellStyleNormal.setFont(font);

        // create standard cell style
        font = wb.createFont();
        font.setBoldweight(Font.BOLDWEIGHT_NORMAL);
        font.setFontHeightInPoints((short) 12);
        cellStyleTestHeader = wb.createCellStyle();
        cellStyleTestHeader.setFillBackgroundColor(HSSFColor.GREY_25_PERCENT.index);
        cellStyleTestHeader.setFont(font);

    
        // create success cell style
        font = wb.createFont();
        font.setBoldweight(Font.BOLDWEIGHT_NORMAL);
        font.setFontHeightInPoints((short) 10);
        cellStyleSuccess = wb.createCellStyle();
        cellStyleSuccess.setFillBackgroundColor(HSSFColor.DARK_GREEN.index);
        cellStyleSuccess.setFont(font);

        // create success cell style
        font = wb.createFont();
        font.setBoldweight(Font.BOLDWEIGHT_NORMAL);
        font.setFontHeightInPoints((short) 10);
        cellStyleIgnore = wb.createCellStyle();
        cellStyleIgnore.setFillBackgroundColor(HSSFColor.GREY_80_PERCENT.index);
        cellStyleIgnore.setFont(font);

        // create warning cell style
        font = wb.createFont();
        font.setBoldweight(Font.BOLDWEIGHT_NORMAL);
        font.setFontHeightInPoints((short) 10);
        cellStyleWarning = wb.createCellStyle();
        cellStyleWarning.setFillBackgroundColor(HSSFColor.DARK_YELLOW.index);
        cellStyleWarning.setFont(font);

        // create error cell style
        font = wb.createFont();
        font.setBoldweight(Font.BOLDWEIGHT_NORMAL);
        font.setFontHeightInPoints((short) 10);
        cellStyleError = wb.createCellStyle();
        cellStyleError.setFillBackgroundColor(HSSFColor.DARK_RED.index);
        cellStyleError.setFont(font);
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
        retval.append(".xlsx");
        
        return null;
    }
    
    private void runTest(KualiTest test, Sheet sheet) {
        for (TestOperation op : test.getOperations().getOperationArray()) {
            executeTestOperation(op, sheet);
        }
    }

    private void executeTestOperation(TestOperation op, Sheet sheet) {
        OperationExecution opExec = null;
        
        Date opStartTime = new Date();
        try {
            opExec = OperationExecutionFactory.getInstance().getOperationExecution(op);
            if (opExec != null) {
                opExec.execute(configuration, platform);
                writeSuccessEntry(sheet, op, opStartTime);
            }
        } 
        
        catch (TestException ex) {
            writeFailureEntry(sheet, op, opStartTime, ex);
        }
    }
    
    protected void writeTestHeader(Sheet sheet, KualiTest test) {
        Row row = sheet.createRow(currentReportRow);
        sheet.addMergedRegion(new CellRangeAddress(currentReportRow, currentReportRow, 0, HEADER_NAMES.length-1));

        currentReportRow++;
        
        Cell cell = row.createCell(0);
        cell.setCellValue("Test: " + test.getTestHeader().getTestName());
        cell.setCellStyle(cellStyleTestHeader);
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
    
    protected void writeReportHeader(Sheet sheet) {
        Header header = sheet.getHeader();
 
        StringBuilder headerString = new StringBuilder(128);
        headerString.append(HSSFHeader.font("Arial", "Bold"));
        headerString.append(HSSFHeader.fontSize((short)14));

        headerString.append("Platform: ");
        
        if (testSuite != null) {
            headerString.append(testSuite.getPlatformName());
            headerString.append(", Test Suite: ");
            headerString.append(testSuite.getName());
        } else {
            headerString.append(kualiTest.getTestHeader().getPlatformName());
            headerString.append(", Test: ");
            headerString.append(kualiTest.getTestHeader().getTestName());
        }
        
        header.setLeft(headerString.toString());
        
        headerString.setLength(0);
        headerString.append(HSSFHeader.font("Arial", "normal"));
        headerString.append(HSSFHeader.fontSize((short)10));
        headerString.append("Run Date: ");
        headerString.append(Constants.DEFAULT_TIMESTAMP_FORMAT.format(new Date()));

        header.setRight(headerString.toString());
    }

    protected void writeColumnHeaders(Sheet sheet) {
        Row row = sheet.createRow(currentReportRow++);
        
        for (int i = 0; i < HEADER_NAMES.length; ++i) {
            Cell cell = row.createCell(i);
            cell.setCellValue(HEADER_NAMES[i]);
            cell.setCellStyle(cellStyleBold);
        }
    }
    
    protected Row writeBaseEntryInformation(Sheet sheet, TestOperation op, Date startTime) {
        Row retval = sheet.createRow(currentReportRow++);
                // checkpoint name
        Cell cell = retval.createCell(0);
        cell.setCellValue(op.getOperation().getCheckpointOperation().getName());
        cell.setCellStyle(cellStyleNormal);
        
        // cehckpoint type
        cell = retval.createCell(1);
        cell.setCellValue(op.getOperation().getCheckpointOperation().getType().toString());
        cell.setCellStyle(cellStyleNormal);
        
        // start time
        cell = retval.createCell(2);
        cell.setCellValue(startTime);
        cell.setCellStyle(cellStyleNormal);
        
        // endTime time
        long endts= System.currentTimeMillis();
        
        cell = retval.createCell(3);
        cell.setCellValue(new Date(endts));
        cell.setCellStyle(cellStyleNormal);

        // run time
        cell = retval.createCell(4);
        cell.setCellValue((endts - startTime.getTime()) * 1000);
        cell.setCellStyle(cellStyleNormal);
        
        // expected values
        cell = retval.createCell(5);
        StringBuilder s = new StringBuilder(128);
        for (CheckpointProperty cp : op.getOperation().getCheckpointOperation().getCheckpointProperties().getCheckpointPropertyArray()) {
            s.append(cp.getPropertyName());
            s.append("=");
            s.append(cp.getPropertyValue());
            s.append("\n");
        }

        cell.setCellValue(new HSSFRichTextString(s.toString()));
        cell.setCellStyle(cellStyleNormal);

        // actual values
        cell = retval.createCell(6);
        s.setLength(0);
        for (CheckpointProperty cp : op.getOperation().getCheckpointOperation().getCheckpointProperties().getCheckpointPropertyArray()) {
            s.append(cp.getPropertyName());
            s.append("=");
            s.append(cp.getActualValue());
            s.append("\n");
        }
        cell.setCellValue(new HSSFRichTextString(s.toString()));
        cell.setCellStyle(cellStyleNormal);

        return retval;
    }
    
    protected void writeSuccessEntry(Sheet sheet, TestOperation op, Date startTime) {
        Row row = writeBaseEntryInformation(sheet, op, startTime);
        
        // status
        Cell cell = row.createCell(7);
        cell.setCellValue("success");
        cell.setCellStyle(cellStyleSuccess);
        
    }

    protected void writeFailureEntry(Sheet sheet, TestOperation op, Date startTime, TestException ex) {
        Row row = writeBaseEntryInformation(sheet, op, startTime);
        
        // status
        Cell cell = row.createCell(7);
        
        FailureAction.Enum failureAction = findMaxFailureAction(op);
        
        switch (failureAction.intValue()) {
            case FailureAction.INT_IGNORE:
                cell.setCellStyle(cellStyleIgnore);
                break;
            case FailureAction.INT_WARNING:
                cell.setCellStyle(cellStyleWarning);
                break;
            case FailureAction.INT_ERROR_CONTINUE:
            case FailureAction.INT_ERROR_HALT_TEST:
                cell.setCellStyle(cellStyleWarning);
                break;
        }
        
        cell.setCellValue(failureAction.toString());

        cell = row.createCell(8);
        cell.setCellStyle(cellStyleNormal);
        cell.setCellValue(ex.getMessage());
    }
        
    private FailureAction.Enum findMaxFailureAction(TestOperation op) {
        FailureAction.Enum retval = null;
        
        if (op.getOperation().getCheckpointOperation().getCheckpointProperties() != null) {
            for (CheckpointProperty prop : op.getOperation().getCheckpointOperation().getCheckpointProperties().getCheckpointPropertyArray()) {
                FailureAction.Enum fa = prop.getOnFailure();
                
                if (retval == null) {
                    retval = fa;
                } else {
                    if (fa.intValue() > retval.intValue()) {
                        retval = fa;
                    }
                }
            }
        }
        
        return retval;
    }
}
