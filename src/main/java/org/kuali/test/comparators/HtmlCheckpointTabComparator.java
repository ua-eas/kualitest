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
import org.kuali.test.utils.Constants;

/**
 *
 * @author rbtucker
 */
public class HtmlCheckpointTabComparator implements Comparator<String>{

    @Override
    public int compare(String o1, String o2) {
        int retval = 0;
        if (Constants.DEFAULT_HTML_PROPERTY_GROUP.equals(o1)) {
            retval = -1;
        } else if (Constants.DEFAULT_HTML_PROPERTY_GROUP.equals(o2)) {
            retval = 1;
        } else {
            retval = o1.compareTo(o2);
        }
        
        return retval;
    }
}
