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

import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.WebRequest;
import org.kuali.test.runner.execution.TestExecutionContext;
import org.kuali.test.runner.execution.TestWebClient;


public class KualiPortalDoProcessor extends AbstractRequestProcessor {
    @Override
    public void preProcess(TestWebClient webClient, TestExecutionContext tec, WebRequest request) throws HttpRequestProcessorException {
        if (HttpMethod.GET.equals(request.getHttpMethod())) {
            String surl = request.getUrl().toExternalForm();
        }
    }
}
