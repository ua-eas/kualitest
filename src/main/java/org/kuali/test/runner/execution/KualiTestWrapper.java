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

import java.util.Stack;
import org.kuali.test.FailureAction;
import org.kuali.test.KualiTestDocument.KualiTest;
import org.kuali.test.TestHeader;
import org.kuali.test.TestOperation;
import org.kuali.test.TestType;
import org.kuali.test.utils.Constants;


public class KualiTestWrapper {
    public Stack<String> httpResponseStack = new Stack<String>();
    public KualiTest test;
    private int warningCount = 0;
    private int successCount = 0;
    private int errorCount = 0;
    
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

    public Stack<String> getHttpResponseStack() {
        return httpResponseStack;
    }
    
    public TestHeader getTestHeader() {
        return test.getTestHeader();
    }

    public TestOperation[] getOperations() {
        if (test.getOperations() == null) {
            return new TestOperation[0];
        } else {
            return test.getOperations().getOperationArray();
        }
    }
    
    public int getMaxRunTime() {
        return test.getTestHeader().getMaxRunTime();
    }
    
    public TestType.Enum getTestType() {
        return test.getTestHeader().getTestType();
    }
    
    public boolean getUseTestEntryTimes() {
        return test.getTestHeader().getUseTestEntryTimes();
    }

    public void incrementErrorCount() {
        errorCount++;
    }

    public void incrementWarningCount() {
        warningCount++;
    }

    public void incrementSuccessCount() {
        successCount++;
    }

    public int getWarningCount() {
        return warningCount;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public void updateCounts(FailureAction.Enum failureAction) {
        if (failureAction != null) {
            switch (failureAction.intValue()) {
                case FailureAction.INT_ERROR_CONTINUE:
                case FailureAction.INT_ERROR_HALT_TEST:
                    incrementErrorCount();
                    break;
                case FailureAction.INT_WARNING:
                    incrementWarningCount();
                    break;
            }
        } else {
            incrementSuccessCount();
        }
    }
    
    public void cleanup() {
        httpResponseStack.clear();
    }
    
    public String getTestName() {
        return test.getTestHeader().getTestName();
    }

    public String getPlatformName() {
        return test.getTestHeader().getPlatformName();
    }
}
