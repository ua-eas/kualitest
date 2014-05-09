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

package org.kuali.test.comparators;

import java.util.Comparator;
import org.kuali.test.CheckpointProperty;


public class CheckpointPropertyComparator implements Comparator <CheckpointProperty> {
    @Override
    public int compare(CheckpointProperty o1, CheckpointProperty o2) {
        
        int retval = o1.getPropertyGroup().compareTo(o2.getPropertyGroup());

        if (retval == 0) {
            retval = o1.getPropertySection().compareTo(o2.getPropertySection());
        }
        
        if (retval == 0) {
            retval = o1.getPropertyName().toLowerCase().compareTo(o2.getPropertyName().toLowerCase());
        }
        
        return retval;
    }
}
