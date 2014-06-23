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
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.test.HtmlRequestOperation;
import org.kuali.test.KualiTestConfigurationDocument;
import org.kuali.test.Operation;
import org.kuali.test.Platform;
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
        OutputStreamWriter writer = null;
        BufferedReader reader = null;
        HtmlRequestOperation reqop = null;
        try {
            reqop = getOperation().getHtmlRequestOperation();
            
            TestExecutionContext tec = getTestExecutionContext();
            List <String> cookies = tec.getCookies(reqop.getUrl());

            // if we have cookies then lets check the url for a
            // jsessionid, if it has a jsessionid then we will see if
            // there is a jsessionid cookie for this call then we will
            // blow it in
            if ((cookies != null) && !cookies.isEmpty()) {
                reqop.setUrl(handleJsessionId(reqop.getUrl(), cookies));
            }
            
            URL url = new URL(getTestExecutionContext().replaceTestExecutionParameters(reqop.getUrl()));
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            
            conn.setReadTimeout(Constants.DEFAULT_HTTP_REQUEST_READ_TIMEOUT);
            conn.addRequestProperty(Constants.HTTP_HEADER_ACCEPT_LANGUAGE, Constants.HTTP_HEADER_ACCEPT_LANGUAGE_US);
            conn.addRequestProperty(Constants.HTTP_HEADER_USER_AGENT, Constants.HTTP_HEADER_USER_AGENT_MOZILLA);
            conn.setDoInput(true);
            conn.setInstanceFollowRedirects(true);

            // add cookies for this path
            if (cookies != null) {
                for (String cookie : cookies) {
                    conn.addRequestProperty(Constants.HTTP_HEADER_COOKIE, cookie);
                }
            }

            if (Constants.HTTP_REQUEST_METHOD_POST.equals(reqop.getMethod())) {
                conn.setDoOutput(true);
                if (reqop.getRequestParameters()!= null) {
                    conn.addRequestProperty(Constants. HTTP_HEADER_CONTENT_TYPE, Constants. CONTENT_TYPE_FORM_URL_ENCODED);
                    for (RequestParameter param : reqop.getRequestParameters().getParameterArray()) {
                        if (Constants.PARAMETER_NAME_CONTENT.equals(param.getName())) {
                            if (StringUtils.isNotBlank(param.getValue())) {
                                writer = new OutputStreamWriter(conn.getOutputStream());
                                writer.write(getTestExecutionContext().replaceTestExecutionParameters(param.getValue()));
                                writer.flush();
                                break;
                            }
                        }
                    }
                }
            }
            
            // clear last response storage
            tec.clearLastHttpResponse();

            // Get the response
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                tec.getLastHttpResponseData().append(line);
            }

            int status = conn.getResponseCode();
            
            if (status == HttpURLConnection.HTTP_OK) {
                Map <String, List<String>> headers = conn.getHeaderFields();

                for (String key : headers.keySet()) {
                    if (Constants.HTTP_HEADER_SET_COOKIE.equalsIgnoreCase(key)) {
                        List <String> l = headers.get(key);
                        for (String s : l) {
                            tec.addCookie(s);
                        }
                    }
                }

                if (LOG.isDebugEnabled()) {
                    LOG.debug("********************************* http response ***********************************");
                    LOG.debug(tec.getLastHttpResponseData().toString());
                    LOG.debug("***********************************************************************************");
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
            if (writer != null) {
                try {
                    writer.close();
                }
                
                catch (IOException ex) {};
            }
            
            if (reader != null) {
                try {
                    reader.close();
                }
                
                catch (IOException ex) {};
            }
        }
    }
}
