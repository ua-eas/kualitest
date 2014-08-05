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
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jetty.http.HttpHeaders;
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
    
    private List<NameValuePair> getUpdatedParameterList(List <NameValuePair> nvplist) throws UnsupportedEncodingException {
        TestExecutionContext tec = getTestExecutionContext();
        List <NameValuePair> retval = tec.decryptHttpParameters(nvplist);
        retval = tec.replaceRequestParameterValues(retval, tec.getAutoReplaceParameterMap());
        return retval;
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

            String[] urlparts = Utils.getUrlParts(reqop.getUrl());
            StringBuilder url = new StringBuilder(512);
            url.append(urlparts[0]);
            
            if (StringUtils.isNotBlank(urlparts[1])) {
                url.append(replaceJsessionId(Utils.buildUrlEncodedParameterString(getUpdatedParameterList(Utils.getNameValuePairsFromUrlEncodedParams(urlparts[1])))));
            }
            
            WebRequest request = new WebRequest(new URL(url.toString()), HttpMethod.valueOf(reqop.getMethod()));
            
            if (reqop.getRequestHeaders() != null) {
                request.setAdditionalHeader(HttpHeaders.USER_AGENT, Constants.DEFAULT_USER_AGENT);
                
                for (RequestHeader hdr : reqop.getRequestHeaders().getHeaderArray()) {
                    request.setAdditionalHeader(hdr.getName(), hdr.getValue());
                }
            }

            if (request.getHttpMethod().equals(HttpMethod.POST)) {
                String params = Utils.getContentParameterFromRequestOperation(reqop);

                if (StringUtils.isNotBlank(params)) {
                    if (Utils.isUrlFormEncoded(reqop)) {
                        request.setEncodingType(FormEncodingType.URL_ENCODED);
                        request.setRequestParameters(getUpdatedParameterList(Utils.getNameValuePairsFromUrlEncodedParams(params)));
                    } else if (Utils.isMultipart(reqop)) {
                        request.setEncodingType(FormEncodingType.MULTIPART);
                        request.setRequestParameters(getUpdatedParameterList(Utils.getNameValuePairsFromMultipartParams(params)));                        
                    }
                }
            }
            
            response = tec.getWebClient().getPage(request).getWebResponse();
            int status = response.getStatusCode();
            
            if (Utils.isRedirectResponse(status)) {
                request = new WebRequest(new URL(response.getResponseHeaderValue(HttpHeaders.LOCATION)), HttpMethod.GET);
                response =  tec.getWebClient().getPage(request).getWebResponse();
            }
                
            String results = response.getContentAsString(CharEncoding.UTF_8);

            if (StringUtils.isNotBlank(results)) {
//                System.out.println("-------------------------------------------------------------->status=" + status);
  //              System.out.println(results);
                if (status == HttpURLConnection.HTTP_OK) {
                    tec.pushHttpResponse(results);
                    tec.updateAutoReplaceMap();
                    tec.updateTestExecutionParameters(test, getOperation().getHtmlRequestOperation(), results);
                } else {
                    throw new TestException("server returned bad status - " 
                        + status 
                        + ", content-type="
                        + Utils.getRequestHeader(reqop, HttpHeaders.CONTENT_TYPE)
                        + ", url=" 
                        + request.getUrl().toString(), getOperation(), FailureAction.IGNORE);
                }
            }
        } 

        catch (IOException ex) {
            String uri = Constants.UNKNOWN;
            if (reqop != null) {
              uri = reqop.getUrl();
            }

            LOG.error(ex.toString(), ex);

            throw new TestException("an IOException occured while processing http request: " 
                + uri + ", error: " +  ex.toString(), getOperation(), ex);
        }

        finally {
            if (response != null) {
                response.cleanUp();
            }
        }
    }
    
    private String replaceJsessionId(String input) {
        StringBuilder retval = new StringBuilder(input.length());

        int pos = input.toLowerCase().indexOf(Constants.JSESSIONID_PARAMETER_NAME);
        if (pos > -1) {
            Cookie cookie = findJSessionIdCookie(Utils.getHostFromUrl(input, false));
            if (cookie != null) {
                int pos2 = input.indexOf(Constants.SEPARATOR_QUESTION);
                retval.append(input.subSequence(0, pos));
                retval.append(Constants.JSESSIONID_PARAMETER_NAME);
                retval.append(Constants.SEPARATOR_EQUALS);
                retval.append(cookie.getValue());

                if (pos2 > -1) {
                    retval.append(input.substring(pos2));
                }
            } else {
                retval.append(input);
            }
        } else {
            retval.append(input);
        }
        
        return retval.toString();
    }

    private Cookie findJSessionIdCookie(String host) {
        Cookie retval = null;
            
        for (Cookie c : getTestExecutionContext().getWebClient().getCookieManager().getCookies()) {
            if (c.getDomain().equalsIgnoreCase(host) && c.getName().equalsIgnoreCase(Constants.JSESSIONID_PARAMETER_NAME)) {
                retval = c;
                break;
            }
        }

        return retval;
    }
}
