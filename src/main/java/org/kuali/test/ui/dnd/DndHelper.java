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

package org.kuali.test.ui.dnd;

import java.awt.datatransfer.DataFlavor;
import org.apache.log4j.Logger;


public class DndHelper {
    private static final Logger LOG = Logger.getLogger(DndHelper.class);
    public static final String TEST_MIME_TYPE = "text/kuali-test-list";
    public static final String TEST_ORDER_MIME_TYPE = "text/kuali-test-order";
    
    public static DataFlavor getTestDataFlavor() {
        return getRepositoryStringDataFlavor(TEST_MIME_TYPE);
    }
    
    public static DataFlavor getTestOrderDataFlavor() {
        return getRepositoryStringDataFlavor(TEST_ORDER_MIME_TYPE);
    }
    
    private static DataFlavor getRepositoryStringDataFlavor(String type) {
        DataFlavor retval = null;
        try {
            retval  = new DataFlavor(type + ";class=java.lang.String");
        }
        
        catch (ClassNotFoundException ex) {
            LOG.warn("error creating DataFlavor of type: " + type, ex);
        };
        
        return retval;
    }
}
