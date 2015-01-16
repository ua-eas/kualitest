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
import java.io.FilenameFilter;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.kuali.test.CheckpointProperty;
import org.kuali.test.FailureAction;
import org.kuali.test.KualiTestConfigurationDocument;
import org.kuali.test.Operation;
import org.kuali.test.Platform;
import org.kuali.test.runner.exceptions.TestException;
import org.kuali.test.utils.Constants;
import org.kuali.test.utils.Utils;

/**
 *
 * @author rbtucker
 */
public class FileOperationExecution extends AbstractOperationExecution {

    /**
     *
     * @param context
     * @param op
     */
    public FileOperationExecution(TestExecutionContext context, Operation op) {
        super(context, op);
    }
    
    /**
     * 
     * @param configuration
     * @param platform
     * @param testWrapper
     * @throws TestException 
     */
    @Override
    public void execute(KualiTestConfigurationDocument.KualiTestConfiguration configuration, 
        Platform platform,KualiTestWrapper testWrapper) throws TestException {
        File dir = new File(getParameter(Constants.FILE_DIRECTORY));
        String fileNamePattern = getParameter(Constants.FILE_NAME_PATTERN);
        
        TestExecutionContext tec = getTestExecutionContext();
        tec.setCurrentOperationIndex(Integer.valueOf(getOperation().getIndex()));
        tec.setCurrentTest(testWrapper);

        
        CheckpointProperty fileDoesNotExist = getProperty(Constants.FILE_DOES_NOT_EXIST.toLowerCase().replace(" ", "-"));
        File[] targetFiles = null;
        
        if (dir.exists() && dir.isDirectory()) {
            if (StringUtils.isNotBlank(fileNamePattern)) {
               targetFiles = dir.listFiles((FilenameFilter)new WildcardFileFilter(fileNamePattern));
            }
        }
        
        CheckpointProperty cp = null;
        if ((fileDoesNotExist != null) && (targetFiles != null) && (targetFiles.length > 0)) {
            throw new TestException("file with name pattern '" + fileNamePattern + "' exists", getOperation(), fileDoesNotExist.getOnFailure());
        } else {
            String errorMessage = null;
            List <File> filteredFiles = new ArrayList<File>();
            
            CheckpointProperty cp1 = getProperty(Constants.FILE_CREATED_TODAY.toLowerCase().replace(" ", "-"));
            CheckpointProperty cp2 = getProperty(Constants.FILE_CREATED_YESTERDAY.toLowerCase().replace(" ", "-"));
            if (cp1 != null) {
                filteredFiles.addAll(getFilesCreatedToday(targetFiles));
                if (filteredFiles.isEmpty()) {
                    errorMessage = "no file with name pattern '" 
                        + fileNamePattern 
                        + "' created " 
                        + Constants.DEFAULT_DATE_FORMAT.format(new Date()) 
                        + " found";
                }
            } else if (cp2 != null) {
                filteredFiles.addAll(getFilesCreatedYesterday(targetFiles));

                if (filteredFiles.isEmpty()) {
                    Calendar cal = Calendar.getInstance();
                    cal.add(Calendar.DATE, -1);
                    errorMessage = "no file with name pattern '" 
                        + fileNamePattern 
                        + "' created " 
                        + Constants.DEFAULT_DATE_FORMAT.format(cal.getTime()) 
                        + " found";
                }
            }
            
            CheckpointProperty cp3 = getProperty(Constants.FILE_SIZE_GREATER_THAN_ZERO.toLowerCase().replace(" ", "-"));
            if (StringUtils.isBlank(errorMessage)) {
                if (cp3 != null) {
                    Iterator <File> it = filteredFiles.iterator();

                    while (it.hasNext()) {
                        if (it.next().length() == 0) {
                            it.remove();
                        }
                    }

                    if (filteredFiles.isEmpty()) {
                        errorMessage = "no non-zero size file found with name pattern '" 
                            + fileNamePattern + "'";
                    }
                }
            }
            
            CheckpointProperty cp4 = getProperty(Constants.CONTAINING_TEXT);
            if (StringUtils.isBlank(errorMessage)) {
                if (cp4 != null) {
                    String txt = cp4.getPropertyValue();

                    if (StringUtils.isNotEmpty(txt)) {
                        filteredFiles = findFilesContainingText(filteredFiles, txt);
                    }

                    if (filteredFiles.isEmpty()) {
                        errorMessage = "no file containing text '" + txt + "' found";
                    }
                }
            }
            
            if (StringUtils.isNotBlank(errorMessage)) {
                FailureAction.Enum failureAction = null;
                
                if (cp1 != null) {
                    failureAction = cp1.getOnFailure();
                } else if (cp2 != null) {
                    failureAction = cp2.getOnFailure();
                } else if (cp3 != null) {
                    failureAction = cp3.getOnFailure();
                } else if (cp4 != null) {
                    failureAction = cp4.getOnFailure();
                }
                
                throw new TestException(errorMessage, getOperation(), failureAction);
            }
        }
    }
    
    private List <File> getFilesCreatedToday(File[] targetFiles) {
        List <File> retval = new ArrayList<File>();
        long start = Utils.truncate(Calendar.getInstance()).getTimeInMillis();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, 1);
        long end = Utils.truncate(cal).getTimeInMillis();
        
        for (File f : targetFiles) {
            if ((f.lastModified() >= start) && (f.lastModified() < end)) {
                retval.add(f);
            }
        }
        
        return retval;
    }

    private List <File> getFilesCreatedYesterday(File[] targetFiles) {
        List <File> retval = new ArrayList<File>();

        long end = Utils.truncate(Calendar.getInstance()).getTimeInMillis();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        long start = Utils.truncate(cal).getTimeInMillis();
        
        for (File f : targetFiles) {
            if ((f.lastModified() >= start) && (f.lastModified() < end)) {
                retval.add(f);
            }
        }
        
        return retval;
    }
    
    private List <File> findFilesContainingText(List <File> inputFiles, String txt) {
        List <File> retval = new ArrayList<File>();
        LineNumberReader lnr = null;
        
        for (File f : inputFiles) {
            try {
                lnr = new LineNumberReader(new FileReader(f));
                String line = null;
                while ((line = lnr.readLine()) != null) {
                    if (line.contains(txt)) {
                        retval.add(f);
                        break;
                    }
                }
            }
            
            catch (Exception ex) {
                
            }
            
            finally {
                try {
                    if (lnr != null) {
                        lnr.close();
                    }
                }
                
                catch (Exception ex) {};
            }
        }
        
        
        return retval;
    }
}
