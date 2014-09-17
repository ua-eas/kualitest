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

package org.kuali.test.runner.requestprocessors;

import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.WebResponse;
import org.apache.log4j.Logger;
import org.kuali.test.HtmlRequestOperation;
import org.kuali.test.TestOperation;
import org.kuali.test.TestOperationType;
import org.kuali.test.runner.execution.AbstractOperationExecution;
import org.kuali.test.runner.execution.TestExecutionContext;
import org.kuali.test.runner.execution.TestWebClient;
import org.kuali.test.utils.Utils;

/**
 *
 * @author rbtucker
 */
public abstract class AbstractRequestProcessor implements HttpRequestProcessor {
    private static final Logger LOG = Logger.getLogger(AbstractOperationExecution.class);

    protected TestOperation getCurrentTestOperation(TestExecutionContext tec) {
        return tec.getCurrentTest().getTest().getOperations().getOperationArray()[tec.getCurrentOperationIndex()];
    }
    
    protected String getContentParameterValue(TestExecutionContext tec) {
        String retval = null;
        
        if (isHttpRequest(tec)) {
            retval = Utils.getContentParameterFromRequestOperation(getHtmlRequestOperation(tec));
        }
        
        return retval;
    }
    
    protected boolean isHttpRequest(TestExecutionContext tec) {
        return getCurrentTestOperation(tec).getOperationType().equals(TestOperationType.HTTP_REQUEST);
    }
    
    protected HtmlRequestOperation getHtmlRequestOperation(TestExecutionContext tec) {
        HtmlRequestOperation retval = null;
        
        if (isHttpRequest(tec)) {
            retval = getCurrentTestOperation(tec).getOperation().getHtmlRequestOperation();
        }
        
        return retval; 
    }
    
    public Logger getLogger() {
        return LOG;
    }
    
    @Override
    public WebResponse postProcess(TestWebClient webClient, TestExecutionContext tec, 
        WebRequest request, WebResponse response) throws HttpRequestProcessorException {
        return response;
    }

}
