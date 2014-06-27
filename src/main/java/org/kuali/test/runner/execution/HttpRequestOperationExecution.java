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
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Consts;
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
                String url = getTestExecutionContext().replaceTestExecutionParameters(reqop.getUrl());
                HttpPost postRequest = new HttpPost(url);
                request = postRequest;
                String params = getTestExecutionContext().replaceTestExecutionParameters(getContentParameterFromRequestOperation(reqop));
                List <NameValuePair> nvps = URLEncodedUtils.parse(params, Consts.UTF_8);
                postRequest.setEntity(new UrlEncodedFormEntity(nvps));
                request.addHeader(Constants.HTTP_HEADER_CONTENT_TYPE, Constants. CONTENT_TYPE_FORM_URL_ENCODED);
            }
            
            if (request != null) {
                if (reqop.getRequestHeaders() != null) {
                    for (RequestHeader hdr : reqop.getRequestHeaders().getHeaderArray()) {
                        request.addHeader(hdr.getName(), hdr.getValue());
                    }
                }

                TestExecutionContext tec = getTestExecutionContext();
                response = tec.getHttpClient().execute(request);
    
                // clear last response storage
                tec.setLastHttpResponse("");

                if (response != null) {
                    BufferedReader reader = null; 
                    StringBuilder responseBuffer = new StringBuilder(Constants.DEFAULT_HTTP_RESPONSE_BUFFER_SIZE);
                    try {
                        reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

                         String line = "";
                         while ((line = reader.readLine()) != null) {
                             responseBuffer.append(line);
                         }                        
                     }

                     finally {
                         if (reader != null) {
                             reader.close();
                         }
                     }

                    if (response.getStatusLine().getStatusCode() == HttpURLConnection.HTTP_OK) {
                        tec.setLastHttpResponse(responseBuffer.toString());
                        tec.updateAutoReplaceMap();
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
