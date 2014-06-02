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
import org.kuali.test.CheckpointProperty;
import org.kuali.test.Operation;
import org.kuali.test.Parameter;
import org.kuali.test.runner.output.TestOutput;


public abstract class AbstractOperationExecution implements OperationExecution {
    private Operation op;
    private Map<String, String> parameterMap = new HashMap<String, String>();
    private Map<String, CheckpointProperty> propertyMap = new HashMap<String, CheckpointProperty>();
 

    public AbstractOperationExecution (Operation op) {
        this.op = op;
       
        if (op.getCheckpointOperation().getInputParameters() != null) {
            for (Parameter param : op.getCheckpointOperation().getInputParameters().getParameterArray()) {
                parameterMap.put(param.getName(), param.getValue());
            }
        }
        
        if (op.getCheckpointOperation().getCheckpointProperties() != null) {
            for (CheckpointProperty property : op.getCheckpointOperation().getCheckpointProperties().getCheckpointPropertyArray()) {
                propertyMap.put(property.getPropertyName(), property);
            }
        }
    }
    
    protected String getParameter(String name) {
        return parameterMap.get(name);
    }
    
    protected TestOutput initTestOutput() {
        return new TestOutput(op);
    }

    protected CheckpointProperty getProperty(String name) {
        return propertyMap.get(name);
    }
    
    protected Operation getOperation() {
        return op;
    }
}
