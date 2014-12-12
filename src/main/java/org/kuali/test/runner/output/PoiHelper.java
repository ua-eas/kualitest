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
package org.kuali.test.runner.output;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.kuali.test.CheckpointProperty;
import org.kuali.test.FailureAction;
import org.kuali.test.KualiTestDocument.KualiTest;
import org.kuali.test.Operation;
import org.kuali.test.TestOperation;
import org.kuali.test.TestOperationType;
import org.kuali.test.TestSuite;
import org.kuali.test.runner.exceptions.TestException;
import org.kuali.test.utils.Constants;
import org.kuali.test.utils.Utils;

/**
 *
 * @author rbtucker
 */
public class PoiHelper {
    private static final Logger LOG = Logger.getLogger(PoiHelper.class);
    int currentReportRow = 0;

    private static final String[] HEADER_NAMES = {
        "Operation Number",
        "Operation Name",
        "Operation Type",
        "Group",
        "Section",
        "Start Time",
        "End Time",
        "Run Time (sec)",
        "Expected Values",
        "Actual Values",
        "Status",
        "Errors"
    };

    private CellStyle cellStyleNormal = null;
    private CellStyle cellStyleBold = null;
    private CellStyle cellStyleTestHeader = null;
    private CellStyle cellStyleIgnore = null;
    private CellStyle cellStyleSuccess = null;
    private CellStyle cellStyleWarning = null;
    private CellStyle cellStyleError = null;
    private CellStyle cellStyleTimestamp = null;
    private CellStyle cellStyleTime = null;
    private CellStyle cellStyleHeader = null;
    private Workbook wb;

    /**
     *
     */
    public PoiHelper() {
        this(true);
    }

    /**
     *
     * @param initilizeNewWorkbook
     */
    public PoiHelper(boolean initilizeNewWorkbook) {
        if (initilizeNewWorkbook) {
            wb = new XSSFWorkbook();
            wb.createSheet("kualitest");
            createPoiCellStyles();
        }
    }

    private void createPoiCellStyles() {
        createPoiCellStyles(wb);
    }
    
    private void createPoiCellStyles(Workbook workbook) {

        // create bold cell style
        Font font = workbook.createFont();
        font.setBoldweight(Font.BOLDWEIGHT_BOLD);
        font.setFontHeightInPoints((short)10);
        cellStyleBold = workbook.createCellStyle();
        cellStyleBold.setFont(font);

        // create standard cell style
        font = workbook.createFont();
        font.setBoldweight(Font.BOLDWEIGHT_NORMAL);
        font.setFontHeightInPoints((short)10);
        cellStyleNormal = workbook.createCellStyle();
        cellStyleNormal.setFont(font);
        cellStyleNormal.setVerticalAlignment(CellStyle.VERTICAL_TOP);

        // create test header cell style
        font = workbook.createFont();
        font.setBoldweight(Font.BOLDWEIGHT_BOLD);
        font.setFontHeightInPoints((short)10);
        cellStyleTestHeader = workbook.createCellStyle();
        cellStyleTestHeader.setFont(font);
        cellStyleTestHeader.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.index);
        cellStyleTestHeader.setFillPattern(CellStyle.SOLID_FOREGROUND);

        // create timestamp cell style
        font = workbook.createFont();
        font.setBoldweight(Font.BOLDWEIGHT_NORMAL);
        font.setFontHeightInPoints((short)10);
        cellStyleTimestamp = workbook.createCellStyle();
        cellStyleTimestamp.setFont(font);
        cellStyleTimestamp.setDataFormat(workbook.createDataFormat().getFormat("yyyy-mm-dd hh:mm:ss"));
        cellStyleTimestamp.setVerticalAlignment(CellStyle.VERTICAL_TOP);

        // create timestamp cell style
        font = workbook.createFont();
        font.setBoldweight(Font.BOLDWEIGHT_NORMAL);
        font.setFontHeightInPoints((short)10);
        cellStyleTime = workbook.createCellStyle();
        cellStyleTime.setFont(font);
        cellStyleTime.setDataFormat(workbook.createDataFormat().getFormat("hh:mm:ss"));
        cellStyleTime.setVerticalAlignment(CellStyle.VERTICAL_TOP);

        // create success cell style
        font = workbook.createFont();
        font.setColor(IndexedColors.DARK_GREEN.index);
        font.setBoldweight(Font.BOLDWEIGHT_NORMAL);
        font.setFontHeightInPoints((short)10);
        cellStyleSuccess = workbook.createCellStyle();
        cellStyleSuccess.setFont(font);
        cellStyleSuccess.setVerticalAlignment(CellStyle.VERTICAL_TOP);

        // create ignore cell style
        font = workbook.createFont();
        font.setColor(IndexedColors.BROWN.index);
        font.setBoldweight(Font.BOLDWEIGHT_BOLD);
        font.setFontHeightInPoints((short)10);
        cellStyleIgnore = workbook.createCellStyle();
        cellStyleIgnore.setFont(font);
        cellStyleIgnore.setVerticalAlignment(CellStyle.VERTICAL_TOP);

        // create warning cell style
        font = workbook.createFont();
        font.setColor(IndexedColors.DARK_YELLOW.index);
        font.setBoldweight(Font.BOLDWEIGHT_BOLD);
        font.setFontHeightInPoints((short)10);
        cellStyleWarning = workbook.createCellStyle();
        cellStyleWarning.setFont(font);
        cellStyleWarning.setVerticalAlignment(CellStyle.VERTICAL_TOP);

        // create error cell style
        font = workbook.createFont();
        font.setColor(IndexedColors.DARK_RED.index);
        font.setBoldweight(Font.BOLDWEIGHT_BOLD);
        font.setFontHeightInPoints((short)10);
        cellStyleError = workbook.createCellStyle();
        cellStyleError.setFont(font);
        cellStyleError.setVerticalAlignment(CellStyle.VERTICAL_TOP);

        // create header cell style
        font = workbook.createFont();
        font.setBoldweight(Font.BOLDWEIGHT_BOLD);
        font.setFontHeightInPoints((short)10);
        cellStyleHeader = (XSSFCellStyle) workbook.createCellStyle();
        cellStyleHeader.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.index);
        cellStyleHeader.setFillPattern(CellStyle.SOLID_FOREGROUND);
        cellStyleHeader.setFont(font);
    }

    /**
     *
     * @param testSuite
     * @param kualiTest
     */
    public void writeReportHeader(TestSuite testSuite, KualiTest kualiTest) {
        Sheet sheet = wb.getSheetAt(0);
        Row row = sheet.createRow(currentReportRow);
        sheet.addMergedRegion(new CellRangeAddress(currentReportRow, currentReportRow, 0, HEADER_NAMES.length - 1));

        Cell cell = row.createCell(0);

        StringBuilder headerString = new StringBuilder(128);

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

        headerString.append(", Run Date: ");
        headerString.append(Constants.DEFAULT_TIMESTAMP_FORMAT.format(new Date()));
        cell.setCellValue(headerString.toString());
        cell.setCellStyle(cellStyleHeader);

    }

    /**
     *
     */
    public void writeColumnHeaders() {
        Row row = wb.getSheetAt(0).createRow(++currentReportRow);

        for (int i = 0; i < HEADER_NAMES.length; ++i) {
            Cell cell = row.createCell(i);
            cell.setCellValue(HEADER_NAMES[i]);
            cell.setCellStyle(cellStyleBold);
        }
    }

    private String getOperationNameForOutput(TestOperation op) {
        String retval = "";
        switch (op.getOperationType().intValue()) {
            case TestOperationType.INT_CHECKPOINT:
                retval = op.getOperation().getCheckpointOperation().getName();
                break;
            case TestOperationType.INT_HTTP_REQUEST:
                retval = op.getOperation().getHtmlRequestOperation().getUrl();
                break;
            case TestOperationType.INT_TEST_EXECUTION_PARAMETER:
                retval = op.getOperation().getTestExecutionParameter().getName();
                break;
        }
        
        return retval;
    }
    
    private String getOperationTypeForOutput(TestOperation op) {
        String retval = "";
        switch (op.getOperationType().intValue()) {
            case TestOperationType.INT_CHECKPOINT:
                retval = "checkpoint[" + op.getOperation().getCheckpointOperation().getType().toString() + "]";
                break;
            case TestOperationType.INT_HTTP_REQUEST:
                retval = TestOperationType.HTTP_REQUEST.toString();
                break;
            case TestOperationType.INT_TEST_EXECUTION_PARAMETER:
                retval = TestOperationType.TEST_EXECUTION_PARAMETER.toString();
                break;
        }
        
        return retval;
    }

    private Row writeBaseEntryInformation(TestOperation op, Date startTime) {
        Row retval = wb.getSheetAt(0).createRow(++currentReportRow);

        // operation number
        Cell cell = retval.createCell(0);
        cell.setCellValue(op.getOperation().getIndex());
        cell.setCellStyle(cellStyleNormal);
        
        // checkpoint name
        cell = retval.createCell(1);
        cell.setCellValue(getOperationNameForOutput(op));
        cell.setCellStyle(cellStyleNormal);

        // checkpoint type
        cell = retval.createCell(2);
        cell.setCellValue(getOperationTypeForOutput(op));
        cell.setCellStyle(cellStyleNormal);

        // group
        cell = retval.createCell(3);
        cell.setCellStyle(cellStyleNormal);
        if (op.getOperationType().equals(TestOperationType.CHECKPOINT)) {
            int lines = op.getOperation().getCheckpointOperation().getCheckpointProperties().sizeOfCheckpointPropertyArray();
            if (lines > 1) {
                retval.setHeight((short)(lines * retval.getHeight()));
            }
            
            StringBuilder s = new StringBuilder(128);
            for (CheckpointProperty cp : op.getOperation().getCheckpointOperation().getCheckpointProperties().getCheckpointPropertyArray()) {
                if (StringUtils.isNotBlank(cp.getPropertyGroup())) {
                    s.append(cp.getPropertyGroup());
                    s.append("\n");
                }
            }
            cell.setCellValue(s.toString());
        } else {
            cell.setCellValue("");
        }

        // section
        cell = retval.createCell(4);
        cell.setCellStyle(cellStyleNormal);
        if (op.getOperationType().equals(TestOperationType.CHECKPOINT)) {
            StringBuilder s = new StringBuilder(128);
            for (CheckpointProperty cp : op.getOperation().getCheckpointOperation().getCheckpointProperties().getCheckpointPropertyArray()) {
                if (StringUtils.isNotBlank(cp.getPropertySection())) {
                    s.append(cp.getPropertySection().replaceAll(Constants.TAG_MATCH_REGEX_PATTERN, "").trim());
                }
                s.append("\n");
            }
            cell.setCellValue(s.toString());
        } else {
            cell.setCellValue("");
        }

        // start time
        cell = retval.createCell(5);
        cell.setCellValue(startTime);
        cell.setCellStyle(cellStyleTime);

        // endTime time
        long endts = System.currentTimeMillis();

        cell = retval.createCell(6);
        cell.setCellValue(new Date(endts));
        cell.setCellStyle(cellStyleTime);

        // run time
        cell = retval.createCell(7);
        cell.setCellValue((endts - startTime.getTime()) / 1000);
        cell.setCellStyle(cellStyleNormal);

        // expected values
        cell = retval.createCell(8);
        cell.setCellStyle(cellStyleNormal);
        if (op.getOperationType().equals(TestOperationType.CHECKPOINT)) {
            StringBuilder s = new StringBuilder(128);
            for (CheckpointProperty cp : op.getOperation().getCheckpointOperation().getCheckpointProperties().getCheckpointPropertyArray()) {
                if (StringUtils.isNotBlank(cp.getDisplayName())) {
                    s.append(cp.getDisplayName());
                    s.append(" ");
                    s.append(Utils.getOperatorFromEnumName(cp.getOperator()));
                    s.append(" ");
                    s.append(cp.getPropertyValue());
                    s.append("\n");
                }
            }
            cell.setCellValue(s.toString());
        } else {
            cell.setCellValue("");
        }

        // actual values
        cell = retval.createCell(9);
        cell.setCellStyle(cellStyleNormal);
        if (op.getOperationType().equals(TestOperationType.CHECKPOINT)) {
            StringBuilder s = new StringBuilder(128);
            for (CheckpointProperty cp : op.getOperation().getCheckpointOperation().getCheckpointProperties().getCheckpointPropertyArray()) {
                if (StringUtils.isNotBlank(cp.getDisplayName())) {
                    s.append(cp.getDisplayName());
                    s.append(" = ");
                    s.append(cp.getActualValue());
                    s.append("\n");
                }
            }
            cell.setCellValue(s.toString());

        } else {
            cell.setCellValue("");
        }

        return retval;
    }

    /**
     *
     * @param op
     * @param startTime
     */
    public void writeSuccessEntry(TestOperation op, Date startTime) {
        if (!TestOperationType.TEST_EXECUTION_PARAMETER.equals(op.getOperationType())) {
            Row row = writeBaseEntryInformation(op, startTime);

            // status
            Cell cell = row.createCell(10);
            cell.setCellValue("success");
            cell.setCellStyle(cellStyleSuccess);
        }
    }

    /**
     * 
     * @param op
     * @param comment 
     */
    public void writeCommentEntry(Operation op, boolean showIndex) {
        Sheet sheet = wb.getSheetAt(0);
        Row retval = sheet.createRow(++currentReportRow);

        // blank operation number
        Cell cell = retval.createCell(0);
        if (showIndex) {
            cell.setCellValue(op.getIndex());
        }
        cell.setCellStyle(cellStyleNormal);

        sheet.addMergedRegion(new CellRangeAddress(currentReportRow, currentReportRow, 1, HEADER_NAMES.length - 1));

        cell = retval.createCell(1);
        cell.setCellValue(op.getCommentOperation().getComment());
        cell.setCellStyle(cellStyleNormal);
    }

    /**
     * 
     * @param op
     * @param startTime
     * @param ex
     * @return true to continue test - false to halt
     */
    public boolean writeFailureEntry(TestOperation op, Date startTime, TestException ex) {
        boolean retval = true;
        Row row = writeBaseEntryInformation(op, startTime);

        // status
        Cell cell = row.createCell(10);

        FailureAction.Enum failureAction = ex.getFailureAction();
        
        if (failureAction == null) {
            failureAction = findMaxFailureAction(op, ex);
        }

        switch (failureAction.intValue()) {
            case FailureAction.INT_IGNORE:
                cell.setCellStyle(cellStyleIgnore);
                break;
            case FailureAction.INT_WARNING:
                cell.setCellStyle(cellStyleWarning);
                break;
            case FailureAction.INT_ERROR_CONTINUE:
                cell.setCellStyle(cellStyleError);
                break;
            case FailureAction.INT_ERROR_HALT_TEST:
                cell.setCellStyle(cellStyleError);
                retval = false;
                break;
        }

        cell.setCellValue(failureAction.toString());
        cell = row.createCell(11);
        cell.setCellStyle(cellStyleNormal);
        cell.setCellValue(ex.getMessage());
        
        return retval;
    }

    private FailureAction.Enum findMaxFailureAction(TestOperation op, TestException ex) {
        FailureAction.Enum retval = null;
        if (op.getOperationType().equals(TestOperationType.CHECKPOINT)) {
            if (op.getOperation().getCheckpointOperation().getCheckpointProperties() != null) {
                for (CheckpointProperty prop : op.getOperation().getCheckpointOperation().getCheckpointProperties().getCheckpointPropertyArray()) {
                    FailureAction.Enum fa = prop.getOnFailure();

                    if ((retval == null) && (fa != null)) {
                        retval = fa;
                    } else if ((fa != null) && (retval != null)) {
                        if (fa.intValue() > retval.intValue()) {
                            retval = fa;
                        }
                    }
                }
            }
        }
        
        if (retval == null) {
            if ((ex != null) && (ex.getCause() != null)) {
                retval = FailureAction.ERROR_HALT_TEST;
            }
        }
        
        return retval;
    }

    /**
     *
     * @param test
     */
    public void writeTestHeader(KualiTest test) {
        Sheet sheet = wb.getSheetAt(0);
        Row row = sheet.createRow(++currentReportRow);
        sheet.addMergedRegion(new CellRangeAddress(currentReportRow, currentReportRow, 0, HEADER_NAMES.length - 1));

        Cell cell = row.createCell(0);
        cell.setCellValue("Test: " + test.getTestHeader().getTestName());
        cell.setCellStyle(cellStyleTestHeader);
    }
    
    /**
     *
     * @param f
     */
    public void writeFile(File f) {
        writeFile(f, wb);
    }
    
    /**
     *
     * @param f
     * @param workbook
     */
    public void writeFile(File f, Workbook workbook) {

        FileOutputStream fos = null;

        try {
            if (!f.getParentFile().exists()) {
                f.getParentFile().mkdirs();
            }

            fos = new FileOutputStream(f);
            workbook.write(fos);
        } catch (IOException ex) {
            LOG.error(ex.toString(), ex);
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } 
            
            catch (Exception ex) {
            };
        }
    }

    /**
     *
     * @param fileName
     * @param inputFiles
     * @param deleteExistingFiles
     * @return
     */
    public File mergeWorkbookFiles(String fileName, List<File> inputFiles, boolean[] errorRuns, boolean deleteExistingFiles) {
        File retval = new File(fileName);
        XSSFWorkbook wbmerged = new XSSFWorkbook();
        createPoiCellStyles(wbmerged);
        int indx = 0;
        for (File f : inputFiles) {
            InputStream fs = null;

            try {
                fs = new FileInputStream(f);
                String nm = "";
                
                if (errorRuns[indx]) {
                    nm = "*run(" + (++indx) + ")";
                } else {
                    nm = "run(" + (++indx) + ")";
                }
                
                XSSFSheet newSheet = wbmerged.createSheet(nm);
                copySheets(newSheet, new XSSFWorkbook(fs).getSheetAt(0));
                
            } 
            
            catch (Exception ex) {
                LOG.error(ex.toString(), ex);
            } 
            
            finally {
                if (fs != null) {
                    try {
                        fs.close();
                    } 
                    
                    catch (Exception ex) {
                    };
                }
            }
        }
        
        writeFile(retval, wbmerged);
        
        if (deleteExistingFiles) {
            for (File f : inputFiles) {
                try {
                    FileUtils.forceDelete(f);
                } 
                
                catch (IOException ex) {}
            }
        }

        return retval;
    }


    private void copySheets(XSSFSheet newSheet, XSSFSheet sheet){  
        int maxColumnNum = 0;  
        Map<Integer, XSSFCellStyle> styleMap = new HashMap<Integer, XSSFCellStyle>();

        int mergedReqionsCount = sheet.getNumMergedRegions();
        
        for (int i = 0; i < mergedReqionsCount; ++i) {
            newSheet.addMergedRegion(sheet.getMergedRegion(i));
        }
        
        for (int i = sheet.getFirstRowNum(); i <= sheet.getLastRowNum(); i++) {  
            XSSFRow srcRow = sheet.getRow(i);  
            XSSFRow destRow = newSheet.createRow(i);  
            if (srcRow != null) {  
                copyRow(srcRow, destRow, styleMap);  
                if (srcRow.getLastCellNum() > maxColumnNum) {  
                    maxColumnNum = srcRow.getLastCellNum();  
                }  
            }  
        }  
        for (int i = 0; i <= maxColumnNum; i++) {  
            newSheet.setColumnWidth(i, sheet.getColumnWidth(i));  
        }  
    }  
  
    private void copyRow(XSSFRow srcRow, XSSFRow destRow, Map<Integer, XSSFCellStyle> styleMap) {  
        destRow.setHeight(srcRow.getHeight());  
        for (int j = srcRow.getFirstCellNum(); j <= srcRow.getLastCellNum(); j++) {  
            XSSFCell oldCell = srcRow.getCell(j);  
            XSSFCell newCell = destRow.getCell(j);  
            if (oldCell != null) {  
                if (newCell == null) {  
                    newCell = destRow.createCell(j);  
                }  
                copyCell(oldCell, newCell, styleMap);  
            }  
        }  
    }  
      
    private void copyCell(XSSFCell oldCell, XSSFCell newCell, Map<Integer, XSSFCellStyle> styleMap) {  
        if(styleMap != null) {  
            int stHashCode = oldCell.getCellStyle().hashCode();  
            XSSFCellStyle newCellStyle = styleMap.get(stHashCode);  
            if(newCellStyle == null) {  
                newCellStyle = newCell.getSheet().getWorkbook().createCellStyle();  
                newCellStyle.cloneStyleFrom(oldCell.getCellStyle());  
                newCellStyle.setFont(oldCell.getCellStyle().getFont());
                styleMap.put(stHashCode, newCellStyle);  
            }  
            
            newCell.setCellStyle(newCellStyle);  
        }  
        
        switch(oldCell.getCellType()) {  
            case XSSFCell.CELL_TYPE_STRING:  
                newCell.setCellValue(oldCell.getStringCellValue());  
                break;  
            case XSSFCell.CELL_TYPE_NUMERIC:  
                newCell.setCellValue(oldCell.getNumericCellValue());  
                break;  
            case XSSFCell.CELL_TYPE_BLANK:  
                newCell.setCellType(HSSFCell.CELL_TYPE_BLANK);  
                break;  
            case XSSFCell.CELL_TYPE_BOOLEAN:  
                newCell.setCellValue(oldCell.getBooleanCellValue());  
                break;  
            case XSSFCell.CELL_TYPE_ERROR:  
                newCell.setCellErrorValue(oldCell.getErrorCellValue());  
                break;  
            case XSSFCell.CELL_TYPE_FORMULA:  
                newCell.setCellFormula(oldCell.getCellFormula());  
                break;  
            default:  
                break;  
        }  
    }  
}
