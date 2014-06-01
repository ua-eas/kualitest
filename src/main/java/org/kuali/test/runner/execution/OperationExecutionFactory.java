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

import org.kuali.test.TestOperation;
import org.kuali.test.TestOperationType;


public class OperationExecutionFactory {
    private static OperationExecutionFactory instance;

    private OperationExecutionFactory() {};
    
    public static OperationExecutionFactory getInstance() {
        if (instance == null) {
            instance = new OperationExecutionFactory();
        }
        return instance;
    }
    
    public OperationExecution getOperationExecution(TestOperation op) {
        OperationExecution retval = null;
        switch(op.getOperationType().intValue()) {
            case TestOperationType.INT_CHECKPOINT:
                break;
            case TestOperationType.INT_FILE_INQUIRY:
                break;
            case TestOperationType.INT_HTTP_REQUEST:
                break;
            case TestOperationType.INT_MEMORY_INQUIRY:
                break;
            case TestOperationType.INT_SQL_SELECT:
                break;
            case TestOperationType.INT_WEB_SERVICE_REQUEST:
                break;
        }
        
        return retval;
    }
}
