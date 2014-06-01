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

import org.kuali.test.CheckpointType;
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
        if (TestOperationType.CHECKPOINT.equals(op.getOperationType())) {
            switch(op.getOperation().getCheckpointOperation().getType().intValue()) {
                case CheckpointType.INT_FILE:
                    retval = new FileOperationExecution(op.getOperation());
                    break;
                case CheckpointType.INT_HTTP:
                    retval = new HttpCheckpointOperationExecution(op.getOperation());
                    break;
                case CheckpointType.INT_MEMORY:
                    retval = new MemoryOperationExecution(op.getOperation());
                    break;
                case CheckpointType.INT_SQL:
                    retval = new SqlOperationExecution(op.getOperation());
                    break;
                case CheckpointType.INT_WEB_SERVICE:
                    retval = new WebServiceOperationExecution(op.getOperation());
                    break;
            }
        } else {
            retval = new HttpRequestOperationExecution(op.getOperation());
        }
        
        return retval;
    }
}
