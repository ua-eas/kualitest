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
import java.net.URL;
import java.net.URLConnection;
import org.apache.log4j.Logger;
import org.kuali.test.HtmlRequestOp;
import org.kuali.test.KualiTestConfigurationDocument;
import org.kuali.test.Operation;
import org.kuali.test.Platform;
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
        HtmlRequestOp reqop = null;
        try {
            reqop = getOperation().getHtmlRequestOperation();
            URL url = new URL(reqop.getUri());
            URLConnection conn = url.openConnection();
            
            if (Constants.HTTP_REQUEST_METHOD_POST.equals(reqop.getMethod())) {
                conn.setDoOutput(true);
                writer = new OutputStreamWriter(conn.getOutputStream());
                //writer.write(reqop.getRequestParameters());
                writer.flush();
            }

            TestExecutionContext context = getTestExecutionContext();
            // clear last response stotage
            context.getLastHttpResponseData(true);

            // Get the response
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                context.getLastHttpResponseData().append(line);
            }
            
            if (LOG.isDebugEnabled()) {
                LOG.debug(context.getLastHttpResponseData().toString());
            }
        } 
        
        catch (IOException ex) {
            String uri = "unknown";
            if (reqop != null) {
              uri = reqop.getUri();
            }
            throw new TestException("an IOException occured while attempting to make an http request to uri: " + uri, getOperation(), ex);
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
