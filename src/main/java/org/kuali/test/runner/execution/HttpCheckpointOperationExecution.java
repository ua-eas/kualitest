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
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.test.Checkpoint;
import org.kuali.test.CheckpointProperty;
import org.kuali.test.FailureAction;
import org.kuali.test.KualiTestConfigurationDocument;
import org.kuali.test.Operation;
import org.kuali.test.Parameter;
import org.kuali.test.Platform;
import org.kuali.test.runner.exceptions.TestException;
import org.kuali.test.utils.Constants;
import org.kuali.test.utils.HtmlDomProcessor;
import org.kuali.test.utils.Utils;
import org.w3c.dom.Document;
import org.xhtmlrenderer.pdf.ITextRenderer;

/**
 *
 * @author rbtucker
 */
public class HttpCheckpointOperationExecution extends AbstractOperationExecution {
    private static Logger LOG = Logger.getLogger(HttpCheckpointOperationExecution.class);
    /**
     *
     * @param context
     * @param op
     */
    public HttpCheckpointOperationExecution(TestExecutionContext context, Operation op) {
        super(context, op);
    }
    
    private List <CheckpointProperty> findCurrentProperties(Checkpoint cp, HtmlDomProcessor.DomInformation dominfo) {
        List <CheckpointProperty> retval = new ArrayList<CheckpointProperty>();
        for (CheckpointProperty originalProperty : cp.getCheckpointProperties().getCheckpointPropertyArray()) {
            for (CheckpointProperty currentProperty : dominfo.getCheckpointProperties()) {
                if (Utils.isCheckPointPropertyMatch(currentProperty, originalProperty)) {
                    retval.add(currentProperty);
                }
            }
        }
        
        return retval;
    }

   private String getSaveScreenFileName(KualiTestConfigurationDocument.KualiTestConfiguration configuration, 
        Platform platform) {
        StringBuilder retval = new StringBuilder(256);
        
        retval.append(configuration.getTestResultLocation());
        retval.append(Constants.FORWARD_SLASH);
        retval.append(platform.getName());
        retval.append(Constants.FORWARD_SLASH);
        retval.append(Constants.SCREEN_CAPTURE_DIR);
        retval.append(Constants.FORWARD_SLASH);
        retval.append(Constants.DEFAULT_DATE_FORMAT.format(new Date()));
        retval.append(Constants.FORWARD_SLASH);
        retval.append(getOperation().getCheckpointOperation().getTestName().toLowerCase().replace(" ", "-"));
        retval.append("_");
        retval.append(getOperation().getCheckpointOperation().getName().toLowerCase().replace(" ", "-"));
        retval.append("_");
        retval.append(Constants.FILENAME_TIMESTAMP_FORMAT.format(new Date()));
        retval.append("_");
        retval.append(getTestExecutionContext().getTestRun());
        retval.append(Constants.PDF_SUFFIX);
        
        return retval.toString();
    }

   /**
    * 
    * @param configuration
    * @param platform
    * @param testWrapper
    * @throws TestException 
    */
    @Override
    public void execute(KualiTestConfigurationDocument.KualiTestConfiguration configuration, Platform platform, 
        KualiTestWrapper testWrapper) throws TestException {
        TestExecutionContext tec = getTestExecutionContext();
        Checkpoint cp = getOperation().getCheckpointOperation();
        String html = null;

        tec.setCurrentOperationIndex(Integer.valueOf(getOperation().getIndex()));
        tec.setCurrentTest(testWrapper);
        
        TestException lastTestException = null;
        long start = System.currentTimeMillis();
        
        // if the source page was loaded from a get request try reload a few times if required 
        // to handle asynchronous processing that may affect checkpoint fields
        while ((System.currentTimeMillis() - start) < Constants.HTML_TEST_RETRY_TIMESPAN) {
            List <CheckpointProperty> matchingProperties = null;
            if (cp.getCheckpointProperties() != null) {
                html = testWrapper.getHttpResponseStack().peek();
                HtmlDomProcessor domProcessor = HtmlDomProcessor.getInstance();
                for (String curhtml : testWrapper.getHttpResponseStack()) {
                    if (StringUtils.isNotBlank(curhtml)) {
                        Document doc = Utils.cleanHtml(curhtml);
                        HtmlDomProcessor.DomInformation dominfo = domProcessor.processDom(platform, doc);
                        matchingProperties = findCurrentProperties(cp, dominfo);
                        if (matchingProperties.size() == cp.getCheckpointProperties().sizeOfCheckpointPropertyArray()) {
                            html = curhtml;
                            break;
                        }
                    }
                }
            }

            if (matchingProperties != null) {
                CheckpointProperty[] properties = cp.getCheckpointProperties().getCheckpointPropertyArray();
                if (matchingProperties.size() == properties.length) {
                    boolean success = true;

                    FailureAction.Enum failureAction = FailureAction.IGNORE;

                    for (int j = 0; j < properties.length; ++j) {
                        if (j < matchingProperties.size()) {
                            properties[j].setActualValue(matchingProperties.get(j).getPropertyValue());
                            if (!evaluateCheckpointProperty(testWrapper, properties[j])) {
                                success = false;

                                // use the most severe action in the group
                                if (properties[j].getOnFailure().intValue() > failureAction.intValue()) {
                                    failureAction = properties[j].getOnFailure();
                                }
                            }
                        }
                    }

                    if (!success) {
                        lastTestException = new TestException("Current web document values do not match test criteria", getOperation(), failureAction);
                    } else {
                        lastTestException = null;
                        break;
                    }
                } else {
                    lastTestException = new TestException("Expected checkpoint property count mismatch: expected " 
                        + properties.length 
                        + " found " 
                        + matchingProperties.size(), getOperation(), FailureAction.ERROR_HALT_TEST);
                }
            } else {
                lastTestException =  new TestException("No matching properties found", getOperation(), FailureAction.ERROR_HALT_TEST);
            }
           
            try {
                Thread.sleep(Constants.HTML_TEST_RETRY_SLEEP_INTERVAL);
            } catch (InterruptedException ex) {}
            
            try {
                tec.resubmitLastGetRequest();
            }
            
            catch (Exception ex) {
                lastTestException = new TestException(ex.toString(), getOperation(), ex);
                break;
            }
        }
        
        writeHtmlIfRequired(cp, configuration, platform,  Utils.cleanHtml(formatForPdf(html), new String[] {"input.type=hidden,name=script"}));

        if (lastTestException != null) {
            throw lastTestException;
        }
    }

    private void writeHtmlIfRequired(Checkpoint cp, 
        KualiTestConfigurationDocument.KualiTestConfiguration configuration, Platform platform, Document doc) {
        boolean saveScreen = false;

        if (cp.getInputParameters() != null) {
            for (Parameter param : cp.getInputParameters().getParameterArray()) {
                if (Constants.SAVE_SCREEN.equalsIgnoreCase(param.getName())) {
                    saveScreen = "true".equalsIgnoreCase(param.getValue());
                    break;
                }
            }
        }

        if (saveScreen) {
            File f = new File(getSaveScreenFileName(configuration, platform));
            
            if (!f.getParentFile().exists()) {
                f.getParentFile().mkdirs();
            }

            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(f);
                ITextRenderer renderer = new ITextRenderer();
        		renderer.setDocument(doc, platform.getWebUrl());
                renderer.layout();
                renderer.createPDF(fos);
                getTestExecutionContext().getGeneratedCheckpointFiles().add(f);
            }

            catch (Exception ex) {
                LOG.error(ex.toString(), ex);
            }

            finally {
                try {
                    if (fos != null) {
                        fos.close();
                    }
                }

                catch (Exception ex) {};
            }
        }
    }
    
    private String formatForPdf(String html) {
        String retval = html;
        StringBuilder buf = new StringBuilder(html.length());
        
        int pos = html.indexOf("</head>");
        
        if (pos > -1) {
            // add this css landscape to ensure page is not truncated on right
            buf.append(html.substring(0, pos));
            buf.append("<style> @page {size: landscape;} </style>");
            buf.append(html.substring(pos));
            retval = buf.toString();
        }
        
        return retval;
    }
}
