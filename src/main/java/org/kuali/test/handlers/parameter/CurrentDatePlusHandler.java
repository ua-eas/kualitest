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
import org.kuali.test.runner.execution.TestExecutionContext;
import org.w3c.dom.Document;


public class CurrentDatePlusHandler extends AbstractParameterHandler {
    private static String DATE_FORMAT = "MM/dd/yyyy";
    private static final SimpleDateFormat DF = new SimpleDateFormat(DATE_FORMAT);
    private int plusdays;
    
    public CurrentDatePlusHandler(int plusdays) {
        this.plusdays=plusdays;
    }
    
    @Override
    public String getValue(TestExecutionContext tec, Document htmlDocument, CheckpointProperty cp, String inputValue) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, plusdays);
        return DF.format(c.getTime());
    }
    
    public String toString() {
        return "CurrentDate(+" + plusdays + "): [" + DATE_FORMAT + "]" ;
    }

    @Override
    public String getDescription() {
        return "This handler will replace the date value in the selected field with the current date + " + plusdays + " days  in the format 'MM/DD/YYYY' - for example 01/10/2014";
    }
}
