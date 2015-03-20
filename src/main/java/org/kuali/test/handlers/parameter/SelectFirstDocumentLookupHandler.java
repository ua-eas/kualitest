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

import org.apache.log4j.Logger;
import org.kuali.test.CheckpointProperty;
import org.kuali.test.TestExecutionParameter;
import org.kuali.test.runner.execution.TestExecutionContext;
import org.kuali.test.utils.Constants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


public class SelectFirstDocumentLookupHandler extends RandomSelectDocumentLookupHandler  {
    private static final Logger LOG = Logger.getLogger(SelectFirstDocumentLookupHandler.class);

    @Override
    public String getDescription() {
        return "This handler will select the first document from a document lookup list";
    }
    
    @Override
    public String getValue(TestExecutionContext tec, TestExecutionParameter tep, Document htmlDocument, CheckpointProperty cp) {
        String retval = null;
        
        Element table = findLookupTable(htmlDocument);
        
        if (table != null) {
            NodeList rows = table.getElementsByTagName(Constants.HTML_TAG_TYPE_TR);
            
            if ((rows != null) && (rows.getLength() > 1)) {
                Element selrow = (Element)rows.item(1); 
                NodeList cols = selrow.getElementsByTagName(Constants.HTML_TAG_TYPE_TD);
                
                if ((cols != null) && (cols.getLength() > 0)) {
                    retval = getValue(tep, cp, (Element)cols.item(0));
                    setCommentText("selected value " + retval + " from row 0");
                }
            }
        }
        
        return retval;
    }
}
