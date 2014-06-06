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
import org.kuali.test.TestOperation;
import org.kuali.test.TestSuite;
import org.kuali.test.runner.exceptions.TestException;
import org.kuali.test.utils.Constants;

public class PoiHelper {

    private static final Logger LOG = Logger.getLogger(PoiHelper.class);
    int currentReportRow = 0;

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

    private CellStyle cellStyleNormal = null;
    private CellStyle cellStyleBold = null;
    private CellStyle cellStyleTestHeader = null;
    private CellStyle cellStyleIgnore = null;
    private CellStyle cellStyleSuccess = null;
    private CellStyle cellStyleWarning = null;
    private CellStyle cellStyleError = null;
    private CellStyle cellStyleTimestamp = null;
    private CellStyle cellStyleHeader = null;
    private Workbook wb;

    public PoiHelper() {
        this(true);
    }

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
        font.setFontHeightInPoints((short) 12);
        cellStyleBold = workbook.createCellStyle();
        cellStyleBold.setFont(font);

        // create standard cell style
        font = workbook.createFont();
        font.setBoldweight(Font.BOLDWEIGHT_NORMAL);
        font.setFontHeightInPoints((short) 12);
        cellStyleNormal = workbook.createCellStyle();
        cellStyleNormal.setFont(font);

        // create test header cell style
        font = workbook.createFont();
        font.setBoldweight(Font.BOLDWEIGHT_NORMAL);
        font.setFontHeightInPoints((short) 12);
        cellStyleTestHeader = workbook.createCellStyle();
        cellStyleTestHeader.setFont(font);
        cellStyleTestHeader.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.index);
        cellStyleTestHeader.setFillPattern(CellStyle.SOLID_FOREGROUND);

        // create timestamp cell style
        font = workbook.createFont();
        font.setBoldweight(Font.BOLDWEIGHT_NORMAL);
        font.setFontHeightInPoints((short) 12);
        cellStyleTimestamp = workbook.createCellStyle();
        cellStyleTimestamp.setFont(font);
        cellStyleTimestamp.setDataFormat(workbook.createDataFormat().getFormat("yyyy-mm-dd hh:mm:ss"));

        // create success cell style
        font = workbook.createFont();
        font.setColor(IndexedColors.DARK_GREEN.index);
        font.setBoldweight(Font.BOLDWEIGHT_NORMAL);
        font.setFontHeightInPoints((short) 12);
        cellStyleSuccess = workbook.createCellStyle();
        cellStyleSuccess.setFont(font);

        // create ignore cell style
        font = workbook.createFont();
        font.setColor(IndexedColors.BROWN.index);
        font.setBoldweight(Font.BOLDWEIGHT_BOLD);
        font.setFontHeightInPoints((short) 12);
        cellStyleIgnore = workbook.createCellStyle();
        cellStyleIgnore.setFont(font);

        // create warning cell style
        font = workbook.createFont();
        font.setColor(IndexedColors.DARK_YELLOW.index);
        font.setBoldweight(Font.BOLDWEIGHT_BOLD);
        font.setFontHeightInPoints((short) 12);
        cellStyleWarning = workbook.createCellStyle();
        cellStyleWarning.setFont(font);

        // create error cell style
        font = workbook.createFont();
        font.setColor(IndexedColors.DARK_RED.index);
        font.setBoldweight(Font.BOLDWEIGHT_BOLD);
        font.setFontHeightInPoints((short) 12);
        cellStyleError = workbook.createCellStyle();
        cellStyleError.setFont(font);

        // create header cell style
        font = workbook.createFont();
        font.setBoldweight(Font.BOLDWEIGHT_BOLD);
        font.setFontHeightInPoints((short) 12);
        cellStyleHeader = (XSSFCellStyle) workbook.createCellStyle();
        cellStyleHeader.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.index);
        cellStyleHeader.setFillPattern(CellStyle.SOLID_FOREGROUND);
        cellStyleHeader.setFont(font);
    }

    public void writeReportHeader(TestSuite testSuite, KualiTest kualiTest) {
        Sheet sheet = wb.getSheetAt(0);
        Row row = sheet.createRow(currentReportRow);
        sheet.addMergedRegion(new CellRangeAddress(currentReportRow, currentReportRow + 2, 0, HEADER_NAMES.length - 1));
        currentReportRow += 2;

        Cell cell = row.createCell(0);

        StringBuilder headerString = new StringBuilder(128);

        headerString.append("  Platform: ");

        if (testSuite != null) {
            headerString.append(testSuite.getPlatformName());
            headerString.append("\n");
            headerString.append("Test Suite: ");
            headerString.append(testSuite.getName());
            headerString.append("\n");
        } else {
            headerString.append(kualiTest.getTestHeader().getPlatformName());
            headerString.append("\n");
            headerString.append("          Test: ");
            headerString.append(kualiTest.getTestHeader().getTestName());
            headerString.append("\n");
        }

        headerString.append(" Run Date: ");
        headerString.append(Constants.DEFAULT_TIMESTAMP_FORMAT.format(new Date()));
        cell.setCellValue(headerString.toString());
        cell.setCellStyle(cellStyleHeader);

    }

    public void writeColumnHeaders() {
        Row row = wb.getSheetAt(0).createRow(++currentReportRow);

        for (int i = 0; i < HEADER_NAMES.length; ++i) {
            Cell cell = row.createCell(i);
            cell.setCellValue(HEADER_NAMES[i]);
            cell.setCellStyle(cellStyleBold);
        }
    }

    private Row writeBaseEntryInformation(TestOperation op, Date startTime) {
        Row retval = wb.getSheetAt(0).createRow(++currentReportRow);
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
        cell.setCellStyle(cellStyleTimestamp);

        // endTime time
        long endts = System.currentTimeMillis();

        cell = retval.createCell(3);
        cell.setCellValue(new Date(endts));
        cell.setCellStyle(cellStyleTimestamp);

        // run time
        cell = retval.createCell(4);
        cell.setCellValue((endts - startTime.getTime()) / 1000);
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

        cell.setCellValue(s.toString());
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
        cell.setCellValue(s.toString());
        cell.setCellStyle(cellStyleNormal);

        return retval;
    }

    public void writeSuccessEntry(TestOperation op, Date startTime) {
        Row row = writeBaseEntryInformation(op, startTime);

        // status
        Cell cell = row.createCell(7);
        cell.setCellValue("success");
        cell.setCellStyle(cellStyleSuccess);

    }

    public void writeFailureEntry(TestOperation op, Date startTime, TestException ex) {
        Row row = writeBaseEntryInformation(op, startTime);

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
                cell.setCellStyle(cellStyleError);
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

    public void writeTestHeader(KualiTest test) {
        Sheet sheet = wb.getSheetAt(0);
        Row row = sheet.createRow(++currentReportRow);
        sheet.addMergedRegion(new CellRangeAddress(currentReportRow, currentReportRow, 0, HEADER_NAMES.length - 1));

        Cell cell = row.createCell(0);
        cell.setCellValue("Test: " + test.getTestHeader().getTestName());
        cell.setCellStyle(cellStyleTestHeader);
    }
    
    public void writeFile(File f) {
        writeFile(f, wb);
    }
    
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
            } catch (Exception ex) {
            };
        }
    }

    public File mergeWorkbookFiles(String fileName, List<File> inputFiles, boolean deleteExistingFiles) {
        File retval = new File(fileName);
        XSSFWorkbook wbmerged = new XSSFWorkbook();
        createPoiCellStyles(wbmerged);
        int indx = 1;
        for (File f : inputFiles) {
            InputStream fs = null;

            try {
                fs = new FileInputStream(f);
                XSSFSheet newSheet = wbmerged.createSheet("run(" + indx++ + ")");
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
                
                catch (IOException ex) {
                    LOG.warn(ex.toString(), ex);
                }
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
                copyRow(sheet, newSheet, srcRow, destRow, styleMap);  
                if (srcRow.getLastCellNum() > maxColumnNum) {  
                    maxColumnNum = srcRow.getLastCellNum();  
                }  
            }  
        }  
        for (int i = 0; i <= maxColumnNum; i++) {  
            newSheet.setColumnWidth(i, sheet.getColumnWidth(i));  
        }  
    }  
  
    private void copyRow(XSSFSheet srcSheet, XSSFSheet destSheet, XSSFRow srcRow, XSSFRow destRow, Map<Integer, XSSFCellStyle> styleMap) {  
        Map<String, CellRangeAddress> mergedRegions = new HashMap<String, CellRangeAddress>();  
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
            if(oldCell.getSheet().getWorkbook() == newCell.getSheet().getWorkbook()){  
                newCell.setCellStyle(oldCell.getCellStyle());  
            } else{  
                int stHashCode = oldCell.getCellStyle().hashCode();  
                XSSFCellStyle newCellStyle = styleMap.get(stHashCode);  
                if(newCellStyle == null){  
                    newCellStyle = newCell.getSheet().getWorkbook().createCellStyle();  
                    newCellStyle.cloneStyleFrom(oldCell.getCellStyle());  
                    styleMap.put(stHashCode, newCellStyle);  
                }  
                newCell.setCellStyle(newCellStyle);  
            }  
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
