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

import com.gargoylesoftware.htmlunit.HttpMethod;
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
import org.kuali.test.HtmlRequestOperation;
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

        // try this a few time to account for asynchronous page loading
        for(int i = 0; i < Constants.HTML_TEST_RETRY_COUNT; i++) {
            try {
                List <CheckpointProperty> matchingProperties = null;
                if (cp.getCheckpointProperties() != null) {
                    html = tec.getWebClient().getCurrentWindow().getEnclosedPage().getWebResponse().getContentAsString();
                    HtmlDomProcessor domProcessor = HtmlDomProcessor.getInstance();
                    for (String curhtml : testWrapper.getHttpResponseStack()) {
                        if (StringUtils.isNotBlank(curhtml)) {
                            Document doc = Utils.tidify(curhtml);
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
                                    failureAction = properties[j].getOnFailure();
                                    break;
                                }
                            }
                        }

                        if (!success) {
                            throw new TestException("Current web document values do not match test criteria", getOperation(), failureAction);
                        } else {
                            writeHtmlIfRequired(cp, configuration, platform,  Utils.tidify(formatForPdf(html), false));
                            break;
                        }
                    } else {
                        throw new TestException("Expected checkpoint property count mismatch: expected " 
                            + properties.length 
                            + " found " 
                            + matchingProperties.size(), getOperation(), FailureAction.ERROR_HALT_TEST);
                    }
                } else {
                    throw new TestException("No matching properties found", getOperation(), FailureAction.ERROR_HALT_TEST);
                }
            }
            
            catch (TestException ex) {
                if (i == (Constants.HTML_TEST_RETRY_COUNT-1)) {
                    writeHtmlIfRequired(cp, configuration, platform,  Utils.tidify(formatForPdf(html), false));
                    throw ex;
                } else {
                    try {
                        Thread.sleep(Constants.HTML_TEST_RETRY_SLEEP_INTERVAL);
                    } catch (InterruptedException ex1) {
                        LOG.warn(ex.toString(), ex1);
                    }

                    // if we did not find matching checkpoint and if the last http request 
                    // we will try reloading the page if possible to account for asynchronous 
                    // processing
                    resubmitHttpRequestIfPossible(testWrapper);
                }
            }
        }
    }
    
    private void resubmitHttpRequestIfPossible(KualiTestWrapper testWrapper) {
        try {
            TestExecutionContext tec = getTestExecutionContext();
            Operation op = testWrapper.getPrevHttpRequestOperation(getOperation().getIndex());

            if (op != null) {
                if (HttpMethod.GET.toString().equalsIgnoreCase(op.getHtmlRequestOperation().getMethod())
                    || isSearch(op.getHtmlRequestOperation())) {
System.out.println("--------------------------->" + op.getHtmlRequestOperation().getUrl());
                    new HttpRequestOperationExecution(tec, op).execute(tec.getConfiguration(), tec.getPlatform(), testWrapper);
                }
            }
        }
        
        catch (TestException ex) {
            LOG.warn(ex.toString(), ex);
        }
    }
    
    private boolean isSearch(HtmlRequestOperation reqop) {
        boolean retval = false;

        String params = Utils.getContentParameterFromRequestOperation(reqop);
        if (StringUtils.isNotBlank(params)) {
            TestExecutionContext tec = getTestExecutionContext();
            
            if (tec.getConfiguration().getSearchSubmitElementNames() != null) {
                for (String ename : tec.getConfiguration().getSearchSubmitElementNames().getElementNameArray()) {
                    if (params.contains(ename)) {
                        retval = true;
                        break;
                    }
                }
            }
            
        }
        
        return retval;
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
            // add this css lanscape tab to ensure page is not truncated on right
            buf.append(html.substring(0, pos));
            buf.append("<style> @page {size: landscape;} </style>");
            buf.append(html.substring(pos));
            retval = buf.toString();
        }
        
        return retval;
    }
}
