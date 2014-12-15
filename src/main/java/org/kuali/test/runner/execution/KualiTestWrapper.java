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

import java.util.HashMap;
import java.util.Map;
import org.kuali.test.FailureAction;
import org.kuali.test.KualiTestDocument.KualiTest;
import org.kuali.test.Operation;
import org.kuali.test.TestHeader;
import org.kuali.test.TestOperation;
import org.kuali.test.TestOperationType;
import org.kuali.test.TestType;


public class KualiTestWrapper {
    public KualiTest test;
    private int warningCount = 0;
    private int successCount = 0;
    private int errorCount = 0;
    private Map<Integer, Long> operationElapsedTime = new HashMap<Integer, Long>();
    
    public KualiTestWrapper(KualiTest test) {
        this.test = test;
    }
    
    public KualiTest getTest() {
        return test;
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

    public boolean getCollectPerformanceData() {
        return test.getTestHeader().getCollectPerformanceData();
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
    
    public String getTestName() {
        return test.getTestHeader().getTestName();
    }

    public String getPlatformName() {
        return test.getTestHeader().getPlatformName();
    }
    
    private int getOperationIndex(int curindx) {
        int retval = -1;
        TestOperation[] testOperations = test.getOperations().getOperationArray();
        for (int i = 0; i < testOperations.length; ++i) {
            if (testOperations[i].getOperation().getIndex() == curindx) {
                retval = i;
                break;
            }
        }
        
        return retval;
    }
    
    public Operation getNextHttpRequestOperation(int curop) {
        Operation retval = null;
        
        int curindx =  getOperationIndex(curop);
        
        if (curindx > -1) {
            int start = curindx+1;
            TestOperation[] testOperations = test.getOperations().getOperationArray();
            for (int i = start; i < testOperations.length; ++i) {
                if (TestOperationType.HTTP_REQUEST.equals(testOperations[i].getOperationType())) {
                    retval = testOperations[i].getOperation();
                    break;
                }
            }
        }
        
        return retval;
    }

    public Operation getPrevHttpRequestOperation(int curop) {
        Operation retval = null;
        int curindx =  getOperationIndex(curop);
        
        if (curindx > -1) {
            int start = curindx- 1;
            TestOperation[] testOperations = test.getOperations().getOperationArray();
            for (int i = start; i > -1; --i) {
                if (TestOperationType.HTTP_REQUEST.equals(testOperations[i].getOperationType())) {
                    retval = testOperations[i].getOperation();
                    break;
                }
            }
        }
        
        return retval;
    }
    
    public TestOperation getNextTestOperation(int curop) {
        TestOperation retval = null;
        int curindx =  getOperationIndex(curop);
        
        if (curindx > -1) {
            int indx = curindx + 1;
            TestOperation[] testOperations = test.getOperations().getOperationArray();
            if (indx < (testOperations.length)) {
                retval = testOperations[indx];
            }
        }
        
        return retval;
    }
    
    public void setElapedTime(Integer indx, Long elapsedTime) {
        operationElapsedTime.put(indx, elapsedTime);
    }

    public Long getElapedTime(Integer indx) {
        return operationElapsedTime.get(indx);
    }
}
