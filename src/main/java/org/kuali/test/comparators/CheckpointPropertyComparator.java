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
import org.apache.commons.lang3.StringUtils;
import org.kuali.test.CheckpointProperty;
import org.kuali.test.utils.Constants;

/**
 *
 * @author rbtucker
 */
public class CheckpointPropertyComparator implements Comparator <CheckpointProperty> {
    @Override
    public int compare(CheckpointProperty o1, CheckpointProperty o2) {
        String g1 = o1.getPropertyGroup();
        String g2 = o2.getPropertyGroup();
        
        if (StringUtils.isBlank(g1)) {
            g1 = Constants.DEFAULT_HTML_PROPERTY_GROUP;
        }
        
        if (StringUtils.isBlank(g2)) {
            g2 = Constants.DEFAULT_HTML_PROPERTY_GROUP;
        }

        int retval = g1.compareTo(g2);

        if (retval == 0) {
            String s1 = o1.getPropertySection();
            String s2 = o2.getPropertySection();
            
            if (StringUtils.isBlank(s1)) {
                s1 = Constants.DEFAULT_HTML_PROPERTY_SECTION;
            }

            if (StringUtils.isBlank(s2)) {
                s2 = Constants.DEFAULT_HTML_PROPERTY_SECTION;
            }
            
            
        
            // handle section names that end with index values enclosed in square brackets
            int pos1 = s1.indexOf("[");
            int pos2 = s1.indexOf("]");
            int pos3 = s2.indexOf("[");
            int pos4 = s2.indexOf("]");

            if ((pos1 > -1) && (pos2 > pos1) && (pos3 > -1) && (pos4 > pos3)) {
                retval = s1.substring(0, pos1).compareTo(s2.substring(0, pos3));

                if (retval == 0) {
                    try {
                        Integer indx1 = Integer.valueOf(s1.substring(pos1+1, pos2).trim());
                        Integer indx2 = Integer.valueOf(s1.substring(pos3+1, pos4).trim());

                        retval = indx1.compareTo(indx2);
                    }

                    catch (NumberFormatException ex) {};
                }
                
            } else {
                retval = s1.compareTo(s2);
            }
        }
        
        if (retval == 0) {
            String s1 = "";
            String s2 = "";
            
            if (StringUtils.isNotBlank(o1.getPropertyName())) {
                s1 = o1.getPropertyName().toLowerCase();
            }
            
            if (StringUtils.isNotBlank(o2.getPropertyName())) {
                s2 = o2.getPropertyName().toLowerCase();
            }
            
            retval = s1.compareTo(s2);
        }
        
        return retval;
    }
}
