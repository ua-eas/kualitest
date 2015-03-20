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

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.kuali.test.CheckpointProperty;
import org.kuali.test.TestExecutionParameter;
import org.kuali.test.runner.execution.TestExecutionContext;
import org.kuali.test.utils.Constants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class ParseTextValueHandler extends BaseParameterHandler {
    @Override
    public String getDescription() {
        return "This handle will parse a designated value from html text on current page - the value is to parse is identified by placeholder " 
            + Constants.TEXT_SEARCH_PLACE_HOLDER + " in the search string definition. For exampe \"looking for id " 
            + Constants.TEXT_SEARCH_PLACE_HOLDER + " in string\".";
    }

    @Override
    public String getValue(TestExecutionContext tec, TestExecutionParameter tep, Document htmlDocument, CheckpointProperty cp) {
        String retval = findText(tep.getAdditionalInfo(), htmlDocument);
        setCommentText("parsing html for \"" + tep.getAdditionalInfo() + "\"  found - \"" + retval + "\"");
        return retval;
    }
    
    @Override
    public boolean isExistingPropertyValueRequired() {
        return false;
    }
    
    
    @Override
    public boolean isResubmitRequestAllowed() {
        return false;
    }

    @Override
    public boolean isTextEntryRequired() {
        return true;
    }

    @Override
    public boolean isTextEntryAllowed() {
        return true;
    }

    private void loadTextNodes(Node curnode, List<Node> textNodes) {
        if (curnode.getNodeType() == Node.TEXT_NODE) {
            textNodes.add(curnode);
        } else {
            NodeList childnodes = curnode.getChildNodes();
            
            for (int i = 0; i < childnodes.getLength(); ++i) {
                loadTextNodes(childnodes.item(i), textNodes); 
            }
        }
    }
    
    private String findText(String searchString, Document htmlDocument) {
        String retval = null;
        
        Element element = htmlDocument.getDocumentElement();
        int rpos = searchString.indexOf(Constants.TEXT_SEARCH_PLACE_HOLDER);
        
        List <Node> textNodes = new ArrayList<Node>();
        
        // recursively load all text nodes in current document
        loadTextNodes(element, textNodes);
        

        // look in the text nodes for a string that matches our search string
        if (!textNodes.isEmpty() && (rpos > -1)) {
            for (Node n : textNodes) {
                String s = n.getNodeValue();
                if (StringUtils.isNotBlank(s)) {
                    s = s.trim();
                    if (rpos == 0) {
                        rpos = s.indexOf(searchString.substring(Constants.TEXT_SEARCH_PLACE_HOLDER.length()));

                        if (rpos > 0) {
                            retval = s.substring(0, rpos);
                        }
                    } else if (rpos == (searchString.length() - Constants.TEXT_SEARCH_PLACE_HOLDER.length())) {
                        retval = s.substring(searchString.length() - Constants.TEXT_SEARCH_PLACE_HOLDER.length());
                    } else {
                        String ss1 = searchString.substring(0, rpos);
                        String ss2 = searchString.substring(rpos + Constants.TEXT_SEARCH_PLACE_HOLDER.length());
                        int pos1 = s.indexOf(ss1) + ss1.length();
                        int pos2 = s.indexOf(ss2);

                        if ((pos1 > -1) && (pos2 > pos1)) {
                            retval = s.substring(pos1, pos2).trim();
                        }
                    }

                    if (StringUtils.isNotBlank(retval)) {
                        break;
                    }
                }
            }
        }
        
        return retval;
    }
}
