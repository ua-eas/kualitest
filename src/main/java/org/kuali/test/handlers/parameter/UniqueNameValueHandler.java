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

import org.apache.commons.lang3.StringUtils;
import org.kuali.test.CheckpointProperty;
import org.kuali.test.TestExecutionParameter;
import org.kuali.test.runner.execution.TestExecutionContext;
import org.w3c.dom.Document;


public class UniqueNameValueHandler extends BaseParameterHandler {
    @Override
    public String getDescription() {
        return "this handler will return the entered text + a unique number";
    }

    @Override
    public String getValue(TestExecutionContext tec, TestExecutionParameter tep, Document htmlDocument, CheckpointProperty cp) {
        String retval = "";
        
        if (StringUtils.isNotBlank(tep.getAdditionalInfo())) {
            retval = tep.getAdditionalInfo() + " " + System.currentTimeMillis();
        }
        
        return retval;
    }

    @Override
    public boolean isTextEntryRequired() {
        return true;
    }
}
