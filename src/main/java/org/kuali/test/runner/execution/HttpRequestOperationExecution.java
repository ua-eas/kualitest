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

import com.gargoylesoftware.htmlunit.FormEncodingType;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.WebResponse;
import java.io.IOException;
import java.net.URL;
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
        WebResponse response = null;
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

            tec.setCurrentOperationIndex(getOperation().getIndex());
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
            
            if (request.getHttpMethod().equals(HttpMethod.POST)) {
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
            
            tec.getWebClient().getPage(request); //.getWebResponse();
        } 

        catch (IOException ex) {
            String uri = Constants.UNKNOWN;
            if (reqop != null) {
              uri = reqop.getUrl();
            }

            LOG.error(ex.toString(), ex);

            throw new TestException("An IOException occured while processing http request: " 
                + uri + ", error: " +  ex.toString(), getOperation(), ex);
        }

        finally {
            if (response != null) {
                response.cleanUp();
            }
        }
    }
}
