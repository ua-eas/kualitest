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

package org.kuali.test.runner.exceptions;

import org.kuali.test.FailureAction;
import org.kuali.test.Operation;

/**
 *
 * @author rbtucker
 */
public class TestException extends Exception {
    private Operation op;
    private String message;
    private FailureAction.Enum failureAction;
    
    /**
     *
     * @param message
     * @param op
     */
    public TestException(String message, Operation op) {
        super(message);
        this.op = op;
        this.message = message;
    }

    /**
     * 
     * @param message
     * @param op
     * @param failureAction 
     */
    public TestException(String message, Operation op, FailureAction.Enum failureAction) {
        super(message);
        this.op = op;
        this.message = message;
        this.failureAction = failureAction;
    }

    /**
     *
     * @param message
     * @param op
     * @param cause
     */
    public TestException(String message, Operation op, Throwable cause) {
        super(message, cause);
        this.op = op;
        this.message = message;
    }

    @Override
    public String toString() {
        return message; 
    }


    @Override
    public String getMessage() {
        return message;
    }

    public FailureAction.Enum getFailureAction() {
        return failureAction;
    }
    
    public boolean isError() {
        boolean retval = false;
        
        if (failureAction != null) {
            retval = (failureAction.equals(FailureAction.ERROR_CONTINUE) || failureAction.equals(FailureAction.ERROR_HALT_TEST));
        }
        
        return retval;
    }

    public boolean isWarning() {
        boolean retval = false;
        
        if (failureAction != null) {
            retval = failureAction.equals(FailureAction.WARNING);
        }
        
        return retval;
    }
}
