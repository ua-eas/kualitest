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
import org.kuali.test.runner.execution.TestExecutionContext;
import org.kuali.test.runner.execution.TestWebClient;

/**
 *
 * @author rbtucker
 */
public interface HttpRequestProcessor {
    public void preProcess(TestWebClient webClient, 
        TestExecutionContext tec, WebRequest request) throws HttpRequestProcessorException;

    public WebResponse postProcess(TestWebClient webClient, 
        TestExecutionContext tec, WebRequest request, WebResponse response) throws HttpRequestProcessorException;
}
