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

package org.kuali.test.utils;

import org.apache.commons.lang3.StringUtils;
import org.htmlcleaner.TagNode;


public class KualiDatatableDisplayGenerator implements DisplayGenerator {
    @Override
    public String getDisplayString(Object o) {
        String retval = "";
        if ((o != null) && (o instanceof TagNode)) {
            TagNode tag = (TagNode)o;
            String s = tag.getAttributeByName("summary");
            if (StringUtils.isNotBlank(s)) {
                int pos = s.lastIndexOf(' ');
                
                if ((pos > -1) && "section".equals(s.substring(pos+1).toLowerCase().trim())) {
                    retval = s.substring(0, pos).trim();
                } else {
                    retval = s;
                }
            }
        }
        
        return retval;
    }
}
