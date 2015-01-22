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
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jetty.http.HttpHeaders;
import org.kuali.test.HtmlRequestOperation;
import org.kuali.test.KualiTestConfigurationDocument;
import org.kuali.test.Operation;
import org.kuali.test.Platform;
import org.kuali.test.RequestHeader;
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
                if (testWrapper.getUseTestEntryTimes()) {
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

            boolean requestSubmitted = false;

            if (request.getHttpMethod().equals(HttpMethod.POST)) {
                String params = Utils.getContentParameterFromRequestOperation(reqop);
                List <NameValuePair> nvplist = new ArrayList<NameValuePair>();
                
                if (StringUtils.isNotBlank(params)) {
                    if (urlFormEncoded) {
                        nvplist = tec.getWebClient().getUpdatedParameterList(Utils.getNameValuePairsFromUrlEncodedParams(params));
                    } else if (multiPart) {
                        nvplist = tec.getWebClient().getUpdatedParameterList(Utils.getNameValuePairsFromMultipartParams(params));
                    }
                }

                // see if we can find a submit element, if we can then 
                // use click() call to submit
                HtmlElement submit = tec.getWebClient().findFormSubmitElement(nvplist);

                if (submit != null) {
                    tec.getWebClient().populateFormElements(tec, nvplist);
                    submit.click();
                    requestSubmitted = true;
                } else {
                    request.setRequestParameters(nvplist);
                }
            } 

            // if we have not loaded web request to this point - load now
            if (!requestSubmitted) {
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
}

