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

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import org.apache.commons.lang3.StringUtils;
import org.kuali.test.CheckpointProperty;
import org.kuali.test.TestExecutionParameter;
import org.kuali.test.runner.execution.TestExecutionContext;
import org.w3c.dom.Document;


public class RandomListValueHandler extends BaseParameterHandler {
    @Override
    public String getDescription() {
        return "This handler will return a random selection from a comma-delimited list of values";
    }

    @Override
    public String getValue(TestExecutionContext tec, TestExecutionParameter tep, Document htmlDocument, CheckpointProperty cp) {
        String retval = "";
        
        if (StringUtils.isNotBlank(tep.getAdditionalInfo())) {
            StringTokenizer st = new StringTokenizer(tep.getAdditionalInfo(), ",");
            List <String> l = new ArrayList<String>();
            while (st.hasMoreTokens()) {
                l.add(st.nextToken());
            }
            
            if (!l.isEmpty()) {
                int indx = this.getRandomIndex(l.size());
                if ((indx >= 0) && (indx < l.size())) {
                    retval = l.get(indx);
                } else {
                    retval = l.get(0);
                }
            }
        }
        
        return retval;
    }

    @Override
    public boolean isFreeformEntryRequired() {
        return true;
    }
}
