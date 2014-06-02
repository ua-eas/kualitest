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

import org.kuali.test.KualiTestConfigurationDocument;
import org.kuali.test.Operation;
import org.kuali.test.Platform;
import org.kuali.test.runner.exceptions.TestException;
import org.kuali.test.runner.output.TestOutput;


public class MemoryOperationExecution extends AbstractOperationExecution {
    public MemoryOperationExecution(Operation op) {
        super(op);
    }
    
    @Override
    public TestOutput execute(KualiTestConfigurationDocument.KualiTestConfiguration configuration, Platform platform) throws TestException {
        TestOutput retval = initTestOutput();
        
        return retval;
    }
}
