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
import com.gargoylesoftware.htmlunit.util.Cookie;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.http.HttpStatus;
import org.kuali.test.FailureAction;
import org.kuali.test.HtmlRequestOperation;
import org.kuali.test.KualiTestConfigurationDocument;
import org.kuali.test.KualiTestDocument.KualiTest;
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
     * @param test
     * @throws TestException 
     */
    @Override
    public void execute(KualiTestConfigurationDocument.KualiTestConfiguration configuration, 
        Platform platform, KualiTest test) throws TestException {
        WebResponse response = null;
        HtmlRequestOperation reqop = getOperation().getHtmlRequestOperation();
        try {
            try {
                int delay = configuration.getDefaultTestWaitInterval();
                
                if (getTestExecutionContext().getKualiTest().getTestHeader().getUseTestEntryTimes()) {
                    delay = reqop.getDelay();
                }
                
                Thread.sleep(delay);
            } 
            
            catch (InterruptedException ex) {};
            
            TestExecutionContext tec = getTestExecutionContext();

            tec.getWebClient().setCurrentOperationIndex(getOperation().getIndex());
            tec.getWebClient().setCurrentTest(test);
            
            WebRequest request = new WebRequest(new URL(reqop.getUrl()), HttpMethod.valueOf(reqop.getMethod()));
            boolean multiPart = Utils.isMultipart(reqop);
            boolean urlFormEncoded = Utils.isUrlFormEncoded(reqop);
            
            if (reqop.getRequestHeaders() != null) {
                request.setAdditionalHeader(HttpHeaders.USER_AGENT, Constants.DEFAULT_USER_AGENT);
                
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
            
            response = tec.getWebClient().getPage(request).getWebResponse();

            int status = response.getStatusCode();
            String results = response.getContentAsString(CharEncoding.UTF_8);
            
            if (status == HttpStatus.OK_200) {
                if (StringUtils.isNotBlank(results)) {
                    tec.pushHttpResponse(results);
                    tec.updateAutoReplaceMap();
                    tec.updateTestExecutionParameters(test, results);
                }
            } else {
            System.out.println("---------------------------------------------------------------------->");
            System.out.println("url=" + request.getUrl().toExternalForm());
            System.out.println("index=" + getOperation().getIndex());
            System.out.println("status=" + status);
            System.out.println("----------------------------cookies---------------------------------->");
            for (Cookie c : tec.getWebClient().getCookies()) {
                System.out.println(c.getName() + "=" + c.getValue());
            }

            System.out.println("----------------------------request headers---------------------------------->");
            for (Entry e : request.getAdditionalHeaders().entrySet()) {
                System.out.println(e);
            }

            System.out.println("----------------------------request parameters---------------------------------->");
            if (StringUtils.isNotBlank(request.getRequestBody())) {
                System.out.println(request.getRequestBody());
            } else {
                for (NameValuePair nvp : request.getRequestParameters()) {
                    System.out.println(nvp.getName() + "=" + nvp.getValue());
                }
            }
            
            System.out.println("----------------------------response parameters---------------------------------->");
            for (NameValuePair nvp : response.getResponseHeaders()) {
                System.out.println(nvp.getName() + "=" + nvp.getValue());
            }

            System.out.println("----------------------------results---------------------------------->");
            System.out.println(results);

            throw new TestException("server returned bad status - " 
                    + status 
                    + ", url=" 
                    + request.getUrl().toString(), getOperation(), FailureAction.IGNORE);
            }
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
    
    private MultipartRequestEntity getMultipartRequestEntity(List <NameValuePair> params) {
        List <StringPart> parts = new ArrayList<StringPart>();
        
        for (NameValuePair nvp : params) {
            parts.add(new StringPart(nvp.getName(), nvp.getValue(), CharEncoding.UTF_8));
        }
        HttpMethodParams methodParams = new HttpMethodParams();
        methodParams.setContentCharset(CharEncoding.UTF_8);
        
        return new MultipartRequestEntity(parts.toArray(new StringPart[parts.size()]), methodParams);
    }
}
