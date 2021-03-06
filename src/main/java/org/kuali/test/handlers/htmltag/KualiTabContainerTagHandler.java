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

package org.kuali.test.handlers.htmltag;

import org.apache.commons.lang3.StringUtils;
import org.kuali.test.utils.Constants;
import org.kuali.test.utils.Utils;
import org.w3c.dom.Element;

/**
 *
 * @author rbtucker
 */
public class KualiTabContainerTagHandler extends DefaultHtmlTagHandler {
    /**
     *
     * @param node
     * @return
     */
    @Override
    public boolean isContainer(Element node) {
        return true;
    }

    /**
     *
     * @param node
     * @return
     */
    @Override
    public String getGroupContainerName(Element node) {
        String retval = findTabName(node);

        if (StringUtils.isBlank(retval)) {
            retval = getTabNameFromId(node);
        }
        
        return retval;
    }

    private String findTabName(Element node) {
        String retval = null;
        
        Element prev = Utils.getPreviousSiblingElement(node);
        
        if (prev != null) {
            String cname = prev.getAttribute(Constants.HTML_TAG_ATTRIBUTE_CLASS);
            
            if (Constants.TAB.equals(cname) && Constants.HTML_TAG_TYPE_TABLE.equals(prev.getTagName())) {
                Element child = Utils.getFirstChildNodeByNodeName(prev, Constants.HTML_TAG_TYPE_TBODY);
                
                if (child != null) {
                    child = Utils.getFirstChildNodeByNodeName(child, Constants.HTML_TAG_TYPE_TR);
                    
                    if (child != null) {
                        child = Utils.getFirstChildNodeByNodeName(child, Constants.HTML_TAG_TYPE_TD);
                        
                        if (child != null) {
                            retval = Utils.cleanDisplayText(child);
                        }
                    }
                }
            }
        }
        
        return retval;
    }
    
    private String getTabNameFromId(Element node) {
        String id = node.getAttribute(Constants.HTML_TAG_ATTRIBUTE_ID);
        String retval = id;
        
        int pos1 = id.indexOf("-");
        int pos2 = id.lastIndexOf("-");
        
        if ((pos1 > -1) && (pos2 > -1) && (pos2 > pos1)) {
            retval = id.substring(pos1+1, pos2);
        }
        
        // hack to handle generated tab names in kuali
        if (retval.startsWith("ID")) {
            retval = null;
        }

        return retval;
    }
}
