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

package org.kuali.test.handlers.parameter;

import org.kuali.test.CheckpointProperty;
import org.kuali.test.TestExecutionParameter;
import org.kuali.test.runner.execution.TestExecutionContext;
import org.w3c.dom.Document;


public class GeneratedIdHandler extends BaseParameterHandler {
    @Override
    public String getDescription() {
        return "This handler will save the current value from this field and replace all instances of this value found in future test runs with the current run value";
    }

    @Override
    public String getValue(TestExecutionContext tec, TestExecutionParameter tep, Document htmlDocument, CheckpointProperty cp) {
        setCommentText("using current test execution value " + cp.getPropertyValue() + " for \"" + cp.getDisplayName() + "\"");
        return cp.getPropertyValue();
    }
    
    public boolean isReplaceByValue() {
        return true;
    }
    
    @Override
    public boolean isExistingPropertyValueRequired() {
        return true;
    }
}
