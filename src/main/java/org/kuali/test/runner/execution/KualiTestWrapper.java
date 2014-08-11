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

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import org.kuali.test.KualiTestDocument.KualiTest;
import org.kuali.test.utils.Constants;


public class KualiTestWrapper {
    public Stack<String> httpResponseStack = new Stack<String>();
    public KualiTest test;
    
    public KualiTestWrapper(KualiTest test) {
        this.test = test;
    }
    
    public KualiTest getTest() {
        return test;
    }
    
    /**
     * 
     * @param html 
     */
    public void pushHttpResponse(String html) {
        httpResponseStack.push(html);
        if (httpResponseStack.size() > Constants.LAST_RESPONSE_STACK_SIZE) {
            httpResponseStack.remove(0);
        }
    }

    /**
     * 
     * @return 
     */
    public List<String> getRecentHttpResponseData() {
        List<String> retval = new ArrayList<String>();
        while (!httpResponseStack.empty()) {
            retval.add(httpResponseStack.pop());
        }

        return retval;
    }

    public Stack<String> getHttpResponseStack() {
        return httpResponseStack;
    }
    
    
}
