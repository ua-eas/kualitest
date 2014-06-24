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

import java.io.File;
import java.util.Comparator;

/**
 *
 * @author rbtucker
 */
public class TagHandlerFileComparator implements Comparator <File> {
    @Override
    public int compare(File o1, File o2) {
        int retval = 0;
        if (o1.getName().startsWith("custom-")) {
            retval = -1;
        } else if (o1.getName().startsWith("general-")) {
            retval = 1;
        } else if (o1.getName().startsWith("kfs-") || o1.getName().startsWith("kc-")) {
            retval = -1;
        } else {
            retval = o1.compareTo(o2);
        }
        
        return retval;
    }
}
