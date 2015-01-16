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

import org.kuali.test.CheckpointProperty;
import org.kuali.test.KualiTestConfigurationDocument;
import org.kuali.test.Operation;
import org.kuali.test.Platform;
import org.kuali.test.TestOperation;
import org.kuali.test.runner.exceptions.TestException;
import org.kuali.test.utils.Constants;

/**
 *
 * @author rbtucker
 */
public class ResponseTimeOperationExecution extends AbstractOperationExecution {
    /**
     *
     * @param context
     * @param op
     */
    public ResponseTimeOperationExecution(TestExecutionContext context, Operation op) {
        super(context, op);
    }
    
    /**
     * 
     * @param configuration
     * @param platform
     * @param testWrapper
     * @throws TestException 
     */
    @Override
    public void execute(KualiTestConfigurationDocument.KualiTestConfiguration configuration, Platform platform, KualiTestWrapper testWrapper) throws TestException {
        TestExecutionContext tec = getTestExecutionContext();
        tec.setCurrentOperationIndex(Integer.valueOf(getOperation().getIndex()));
        tec.setCurrentTest(testWrapper);
        
        int curindx = tec.getCurrentOperationIndex();
        
        for(int i = curindx-1; i >= 0; i--) {
            TestOperation top = tec.getTestOperation(i);
            if (top != null) {
                if (top.getOperation().getHtmlRequestOperation() != null) {
                    Long elapsedTime = testWrapper.getElapedTime(i);

                    if (elapsedTime != null) {
                        CheckpointProperty cp = getProperty(Constants.MAX_RUNTIME_PROPERTY_NAME);
                        cp.setActualValue("" + (elapsedTime/1000));
                        
                        if (Integer.valueOf(cp.getActualValue()) > Integer.valueOf(cp.getPropertyValue())) {
                            throw new TestException("max response time exceeded", getOperation(), cp.getOnFailure());
                        }
                        break;
                    }
                }
            }   
        }
    }
}
