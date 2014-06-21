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

public class HttpRequestOperationExecution extends AbstractOperationExecution {
    private static final Logger LOG = Logger.getLogger(HttpRequestOperationExecution.class);
    
    public HttpRequestOperationExecution(TestExecutionContext context, Operation op) {
        super(context, op);
    }

    @Override
    public void execute(KualiTestConfigurationDocument.KualiTestConfiguration configuration, Platform platform) throws TestException {
        OutputStreamWriter writer = null;
        BufferedReader reader = null;
        HtmlRequestOperation reqop = null;
        try {
            reqop = getOperation().getHtmlRequestOperation();

            URL url = new URL(getTestExecutionContext().replaceTestExecutionParameters(reqop.getUri()));
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setDoInput(true);
            conn.setInstanceFollowRedirects(true);
            
            TestExecutionContext tec = getTestExecutionContext();
            List <String> cookies = tec.getCookies(url.getPath());
            
            if (cookies != null) {
                for (String cookie : cookies) {
                    conn.addRequestProperty("Cookie", cookie);
                }
            }
            
            if (Constants.HTTP_REQUEST_METHOD_POST.equals(reqop.getMethod())) {
                if (reqop.getRequestParameters()!= null) {
                    for (RequestParameter param : reqop.getRequestParameters().getParameterArray()) {
                        if (Constants.PARAMETER_NAME_CONTENT.equals(param.getName())) {
                            if (StringUtils.isNotBlank(param.getValue())) {
                                conn.setDoOutput(true);
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
            
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                Map <String, List<String>> headers = conn.getHeaderFields();
                
                for (String key : headers.keySet()) {
                    if ("Set-Cookie".equalsIgnoreCase(key)) {
                        List <String> l = headers.get(key);
                        for (String s : l) {
                            tec.addCookie(s);
                        }
                    }
                }
            }
            
            if (LOG.isDebugEnabled()) {
                LOG.debug("********************************* http response ***********************************");
                LOG.debug(tec.getLastHttpResponseData().toString());
                LOG.debug("***********************************************************************************");
            }
        } 
        
        catch (IOException ex) {
            String uri = "unknown";
            if (reqop != null) {
              uri = reqop.getUri();
            }
            
            LOG.error(ex.toString(), ex);
            
            throw new TestException("an IOException occured while for http request: " 
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
