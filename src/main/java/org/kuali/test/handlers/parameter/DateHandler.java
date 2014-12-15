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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import org.kuali.test.CheckpointProperty;
import org.kuali.test.TestExecutionParameter;
import org.kuali.test.runner.execution.TestExecutionContext;
import org.w3c.dom.Document;


public class DateHandler extends BaseParameterHandler {
    private static String DATE_FORMAT = "MM/dd/yyyy";
    private static final SimpleDateFormat DF = new SimpleDateFormat(DATE_FORMAT);
    private int plusdays;
    
    public DateHandler(int plusdays) {
        this.plusdays=plusdays;
    }
    
    @Override
    public String getValue(TestExecutionContext tec, TestExecutionParameter tep, Document htmlDocument, CheckpointProperty cp) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, plusdays);
        String retval = DF.format(c.getTime());
        setCommentText("using date value " + retval + " for " + cp.getDisplayName());
        return retval;

    }
    
    public String toString() {
        StringBuilder retval = new StringBuilder(64);
        
        retval.append("CurrentDate");
        
        if (plusdays != 0) {
            retval.append("(");
            
            if (plusdays > 0) {
                retval.append("+");
            } 
            retval.append(plusdays);
            retval.append(")");
        }
        
        retval.append(": [");
        retval.append(DATE_FORMAT);
        retval.append("]");
        
        return retval.toString();
    }

    @Override
    public String getDescription() {
        StringBuilder retval = new StringBuilder(256);
        
        retval.append("This handler will replace the date value in the selected field with the current date ");
        
        if (plusdays != 0) {
            if (plusdays > 0) {
                retval.append("+");
            } 
            
            retval.append(plusdays);
            retval.append(" days ");
        }
        
        retval.append("in the format '");
        retval.append(DATE_FORMAT);
        retval.append("' - for example 01/10/2014");
        
        return retval.toString();
    }
}
