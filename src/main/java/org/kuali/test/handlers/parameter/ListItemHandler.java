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

import org.kuali.test.CheckpointProperty;
import org.kuali.test.Parameter;
import org.kuali.test.TestExecutionParameter;
import org.kuali.test.runner.execution.TestExecutionContext;
import org.kuali.test.utils.Constants;
import org.kuali.test.utils.Utils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public abstract class ListItemHandler extends BaseParameterHandler {
    private int itemToSelect;
    public ListItemHandler(int itemToSelect) {
        this.itemToSelect = itemToSelect;
    }
    
    @Override
    public String getValue(TestExecutionContext tec, TestExecutionParameter tep, Document htmlDocument, CheckpointProperty cp) {
        String retval = "";
        
        Parameter param = Utils.getCheckpointPropertyTagParameter(cp, Constants.HTML_TAG_ATTRIBUTE_NAME);
        
        if (param != null) {
            Node node = tec.getWebClient().findHtmlElementByName(param.getValue());

            if ((node != null) && Constants.HTML_TAG_TYPE_SELECT.equalsIgnoreCase(node.getNodeName())) {
                NodeList nodeList = node.getChildNodes();

                if (nodeList.getLength() >= itemToSelect) {
                    Node value  = nodeList.item(itemToSelect).getAttributes().getNamedItem(Constants.HTML_TAG_ATTRIBUTE_VALUE);

                    if (value != null) {
                        retval = value.getNodeValue();
                    }
                }
            }
        }
        
        return retval;
    }
    
    public String toString() {
        return "ListItemHandler[" + itemToSelect + "]";
    }

    public int getItemToSelect() {
        return itemToSelect;
    }

    public void setItemToSelect(int itemToSelect) {
        this.itemToSelect = itemToSelect;
    }

}
