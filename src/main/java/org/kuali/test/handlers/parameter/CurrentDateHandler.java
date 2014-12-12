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


public class CurrentDateHandler extends CurrentDatePlusHandler {
    public CurrentDateHandler() {
        super(0);
    }

    @Override
    public String getDescription() {
        return "This handler will replace the date value in the selected field with the current date in the format 'MM/DD/YYYY' - for example 01/10/2014";
    }
}
