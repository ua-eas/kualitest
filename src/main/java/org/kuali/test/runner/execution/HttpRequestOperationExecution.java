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

import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.FormEncodingType;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.WebWindow;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.FrameWindow;
import com.gargoylesoftware.htmlunit.html.HtmlCheckBoxInput;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlOption;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlRadioButtonInput;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jetty.http.HttpHeaders;
import org.kuali.test.HtmlRequestOperation;
import org.kuali.test.KualiTestConfigurationDocument;
import org.kuali.test.Operation;
import org.kuali.test.Platform;
import org.kuali.test.RequestHeader;
import org.kuali.test.TestOperation;
import org.kuali.test.TestOperationType;
import org.kuali.test.runner.exceptions.TestException;
import org.kuali.test.utils.Constants;
import org.kuali.test.utils.Utils;

/**
 *
 * @author rbtucker
 */
public class HttpRequestOperationExecution extends AbstractOperationExecution {
    private static final Logger LOG = Logger.getLogger(HttpRequestOperationExecution.class);

    /**
     *
     * @param context
     * @param op
     */
    public HttpRequestOperationExecution(TestExecutionContext context, Operation op) {
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
        Platform platform, KualiTestWrapper testWrapper) throws TestException {
        HtmlRequestOperation reqop = getOperation().getHtmlRequestOperation();
        
        try {
            try {
                int delay = configuration.getDefaultTestWaitInterval();
                // we will always use user entry times for pre-checkpoint request - trying to
                // ensure everything has completed on backend prior to checkpoint
                if (testWrapper.getUseTestEntryTimes() || isNextOperationCheckpoint(testWrapper)) {
                    delay = reqop.getDelay();
                }
                
                Thread.sleep(delay);
            } 
            
            catch (InterruptedException ex) {};
          
           
            TestExecutionContext tec = getTestExecutionContext();

            tec.setCurrentOperationIndex(Integer.valueOf(getOperation().getIndex()));
            tec.setCurrentTest(testWrapper);

            WebRequest request = new WebRequest(new URL(reqop.getUrl()), HttpMethod.valueOf(reqop.getMethod()));
            request.setAdditionalHeader(Constants.TEST_OPERATION_INDEX, "" + getOperation().getIndex());

            boolean multiPart = Utils.isMultipart(reqop);
            boolean urlFormEncoded = Utils.isUrlFormEncoded(reqop);

            if (reqop.getRequestHeaders() != null) {
                for (RequestHeader hdr : reqop.getRequestHeaders().getHeaderArray()) {
                    if (HttpHeaders.CONTENT_TYPE.equals(hdr.getName())) {
                        if (!multiPart) {
                            request.setAdditionalHeader(hdr.getName(), hdr.getValue());
                        }
                    } else {
                        request.setAdditionalHeader(hdr.getName(), hdr.getValue());
                    }
                }
            }

            boolean ispost = request.getHttpMethod().equals(HttpMethod.POST);

            if (ispost) {
                ispost = true;
                String params = Utils.getContentParameterFromRequestOperation(reqop);

                if (StringUtils.isNotBlank(params)) {
                    if (urlFormEncoded) {
                        request.setEncodingType(FormEncodingType.URL_ENCODED);
                        request.setRequestParameters(Utils.getNameValuePairsFromUrlEncodedParams(params));
                    } else if (multiPart) {
                        request.setEncodingType(FormEncodingType.MULTIPART);
                        request.setRequestParameters(Utils.getNameValuePairsFromMultipartParams(params));
                    }
                }
            }    
            
            
            if (ispost && (tec.getConfiguration().getFormSubmitElementNames() != null)) {
                HtmlElement submit = getFormSubmitElement(request.getRequestParameters());
                if (submit != null) {
                    submit.click();
                } else {
                    tec.getWebClient().getPage(request);
                }
            } else {
                tec.getWebClient().getPage(request);
            }
        } 

        catch (IOException ex) {
            Throwable t = ex.getCause();
            LOG.error(ex.toString(), ex);
            
            if ((t != null) && (t instanceof TestException)) {
                throw (TestException)t;
            } else {
                String uri = Constants.UNKNOWN;
                if (reqop != null) {
                  uri = reqop.getUrl();
                }

                throw new TestException("An IOException occured while processing http request: " 
                    + uri + ", error: " +  ex.toString(), getOperation(), ex);
            }
        }
    }
    
    private boolean isNextOperationCheckpoint(KualiTestWrapper testWrapper) {
        boolean retval = false;
        TestOperation op = testWrapper.getNextTestOperation(getOperation().getIndex());
        
        if ((op != null) && TestOperationType.CHECKPOINT.equals(op.getOperationType())) {
            retval = true;
        }
        
        return retval;
    }
    
    private HtmlElement getFormSubmitElement(List <NameValuePair> nvplist) throws IOException {
        HtmlElement retval = null;
        TestExecutionContext tec = getTestExecutionContext();
        String elementName = null;
        for (NameValuePair nvp : nvplist) {
            for (String nm : tec.getConfiguration().getFormSubmitElementNames().getElementNameArray()) {
                if (nvp.getName().startsWith(nm)) {
                    elementName = nm;
                    break;
                }
            }
            
            if (StringUtils.isNotBlank(elementName)) {
                break;
            }
        }
        
        WebWindow window = tec.getWebClient().getCurrentWindow();
        Page page = window.getEnclosedPage();
        
        if (StringUtils.isNotBlank(elementName)) {
            int cnt = 0;
            while ((retval == null) && (cnt < (Constants.HTML_TEST_RETRY_COUNT))) {
                retval = findHtmlElementByName(page, elementName);
            
                if (retval == null) {
                    page.getWebResponse().cleanUp();
                    page = tec.getWebClient().getPage(window.getEnclosedPage().getWebResponse().getWebRequest());
                }

                cnt++;
            }
        }    
             
        if ((retval != null) && page.isHtmlPage()) {
            HtmlPage hpage = (HtmlPage)page;
            // update parameter elements
            for (NameValuePair nvp : tec.getWebClient().getUpdatedParameterList(nvplist)) {
                try {
                    HtmlElement e = hpage.getElementByName(nvp.getName());

                    if (e instanceof HtmlTextInput) {
                        HtmlTextInput ti = (HtmlTextInput)e;
                        ti.setText(nvp.getValue());
                    } else if (e instanceof HtmlRadioButtonInput) {
                        for (DomElement de : hpage.getElementsByName(nvp.getName())) {
                            HtmlRadioButtonInput rbi = (HtmlRadioButtonInput)de;
                            if (rbi.getValueAttribute().equals(nvp.getValue())) {
                                rbi.setChecked(true);
                                break;
                            }
                        }
                    } else if (e instanceof HtmlCheckBoxInput) {
                        for (DomElement de : hpage.getElementsByName(nvp.getName())) {
                            HtmlCheckBoxInput cbi = (HtmlCheckBoxInput)de;
                            if (cbi.getValueAttribute().equals(nvp.getValue())) {
                                cbi.setChecked(true);
                                break;
                            }
                        }
                    } else if (e instanceof HtmlSelect) {
                        for (DomElement de : hpage.getElementsByName(nvp.getName())) {
                            HtmlSelect sel = (HtmlSelect)de;
                            HtmlOption option = sel.getOptionByValue(nvp.getValue());

                            if (option != null) {
                                sel.setSelectedAttribute(option, true);
                                break;
                            }
                        }
                    }
                }

                catch (ElementNotFoundException ex) {};
            }
        }
        
        return retval;
    }
    
    private HtmlElement findHtmlElementByName(Page page, String elementName) {
        HtmlElement retval = null;
        
        if (page.isHtmlPage()) {
            try {
                retval = ((HtmlPage)page).getElementByName(elementName);
            }

            catch (ElementNotFoundException ex) {};
            
            if (retval == null) {
                for (FrameWindow window : ((HtmlPage)page).getFrames()) {
                    Page pg = window.getFrameElement().getEnclosedPage();
                    if (pg.isHtmlPage()) {
                        try {
                            retval = ((HtmlPage)pg).getElementByName(elementName);
                        }
                        
                        catch (ElementNotFoundException ex) {};
                        
                        if (retval == null) {
                            retval = findHtmlElementByName(pg, elementName);
                        }
                    }
                    
                    if (retval != null) {
                        break;
                    }
                }
            }
        }
        
        return retval;
    }
}

