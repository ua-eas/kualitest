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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.log4j.Logger;
import org.kuali.test.HtmlRequestOperation;
import org.kuali.test.KualiTestConfigurationDocument;
import org.kuali.test.Operation;
import org.kuali.test.Platform;
import org.kuali.test.RequestHeader;
import org.kuali.test.RequestParameter;
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

    private String handleJsessionId(String url, List <String> cookies) {
        String retval = url;
        int[] parameterPos = Utils.getParameterPosition(url, Constants.JSESSIONID_PARAMETER_NAME, Constants.SEPARATOR_QUESTION);
                
        if (parameterPos != null) {
            String[] parameterData = Utils.getParameterData(url, parameterPos);

            // if we have a jsessionid the check the cookies for the latest one
            if (parameterData != null) {
                for (int i = cookies.size() - 1; i > -1; --i) {
                    String cookie = cookies.get(i);

                    // if we find a cookie with jsessionid the grab the jsessionid
                    if (cookie.toLowerCase().contains(Constants.JSESSIONID_PARAMETER_NAME)) {
                        int[] parameterPos2 = Utils.getParameterPosition(cookie, Constants.JSESSIONID_PARAMETER_NAME, Constants.SEPARATOR_SEMICOLON);

                        if (parameterPos2 != null) {
                            String[] parameterData2 = Utils.getParameterData(cookie, parameterPos2);
                            if (parameterData2 != null) {
                                parameterData[1] = parameterData2[1];
                                retval = Utils.replaceParameterString(url, parameterPos, parameterData);
                                break;
                            }
                        }
                    }
                }
            }
        }

        return retval;
    }
    
    /**
     *
     * @param configuration
     * @param platform
     * @throws TestException
     */
    @Override
    public void execute(KualiTestConfigurationDocument.KualiTestConfiguration configuration, Platform platform) throws TestException {
        HtmlRequestOperation reqop = null;
        CloseableHttpResponse response = null;

        try {
            reqop = getOperation().getHtmlRequestOperation();
            
            HttpRequestBase request = null;
            
            if (HttpGet.METHOD_NAME.equals(reqop.getMethod())) {
                request = new HttpGet(getTestExecutionContext().replaceTestExecutionParameters(reqop.getUrl()));
            } else if (HttpPost.METHOD_NAME.equals(reqop.getMethod())) {
                request = new HttpPost(getTestExecutionContext().replaceTestExecutionParameters(reqop.getUrl()));
                List <NameValuePair> nvps = URLEncodedUtils.parse(getContentParameterFromRequestOperation(reqop), Charset.defaultCharset());
                ((HttpPost)request).setEntity(new UrlEncodedFormEntity(nvps));
                request.addHeader(Constants.HTTP_HEADER_CONTENT_TYPE, Constants. CONTENT_TYPE_FORM_URL_ENCODED);
            }
            
            if (request != null) {
                TestExecutionContext tec = getTestExecutionContext();
                
                if (reqop.getRequestHeaders() != null) {
                    for (RequestHeader hdr : reqop.getRequestHeaders().getHeaderArray()) {
                        request.addHeader(hdr.getName(), hdr.getValue());
                    }
                }

                response = tec.getHttpClient().execute(request);

                // clear last response storage
                tec.clearLastHttpResponse();

                if (response != null) {
                    if (response.getStatusLine().getStatusCode() == HttpURLConnection.HTTP_OK) {
                        BufferedReader reader = null; 
                        
                        try {
                            reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
 
                            String line = "";
                            while ((line = reader.readLine()) != null) {
                                tec.getLastHttpResponseData().append(line);
                            }                        
                        }
                        
                        finally {
                            reader.close();
                        }
                        
System.out.println("---------------------------------------------------------->");
System.out.println(tec.getLastHttpResponseData().toString());
System.out.println("---------------------------------------------------------->");
                        Header[] headers = response.getAllHeaders();

                        tec.updateAutoReplaceMap();

                        if (LOG.isDebugEnabled()) {
                            LOG.debug("********************************* http response ***********************************");
                            LOG.debug(tec.getLastHttpResponseData().toString());
                            LOG.debug("***********************************************************************************");
                        }
                    }
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
            HttpClientUtils.closeQuietly(response);
        }
    }
    
    private String getContentParameterFromRequestOperation(HtmlRequestOperation reqop) {
        String retval = null;
        
        for (RequestParameter param : reqop.getRequestParameters().getParameterArray()) {
            if (Constants.PARAMETER_NAME_CONTENT.equals(param.getName())) {
                if (StringUtils.isNotBlank(param.getValue())) {
                    retval = param.getValue();
                    break;
                }
            }
        }
        
        return retval;
    }
}
