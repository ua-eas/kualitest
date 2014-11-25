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

import org.w3c.dom.Node;


public abstract class AbstractParameterHandler implements ParameterHandler {
    public AbstractParameterHandler() {
    }

    @Override
    public boolean isAutoReplace() {
        return false;
    }

    public String getValue(String currentValue) {
        return currentValue;
    }

    public String getValue(Node currentNode) {
        return "";
    }
    
    public String toString() {
        String retval = getClass().getName();
        int pos1 = retval.lastIndexOf(".");
        int pos2 = retval.lastIndexOf("Handler");
        
        if ((pos1 > -1) && (pos2 > pos1)) {
            retval = retval.substring(pos1+1, pos2);
        } else if (pos1 > -1) {
            retval = retval.substring(pos1+1);
        } 
        
        return retval;
    }

    @Override
    public int compareTo(ParameterHandler o) {
        return toString().compareTo(o.toString());
    }
    
    public abstract String getDescription();
}
