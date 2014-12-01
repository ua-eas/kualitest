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
import org.kuali.test.CheckpointProperty;
import org.kuali.test.utils.Constants;
import org.kuali.test.utils.Utils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author rbtucker
 */
public class NotesAndAttachmentsTagHandler extends DefaultHtmlTagHandler {
    /**
     *
     * @param node
     * @return
     */
    @Override
    public CheckpointProperty getCheckpointProperty(Element node) {
        CheckpointProperty retval = null;
        if (StringUtils.isNotBlank(getRowNumber(node))) {
            retval = super.getCheckpointProperty(node); 
            retval.setPropertyValue(Utils.cleanDisplayText(node));
            
        }
        
        return retval;
    }

    /**
     *
     * @param node
     * @return
     */
    @Override
    public String getSectionName(Element node) {
        String retval = "";
        
        if (StringUtils.isNotBlank(getRowNumber(node))) {
            retval = "Row";
        }
        
        return retval;
    }

    /**
     *
     * @param node
     * @return
     */
    @Override
    public String getSubSectionName(Element node) {
        String retval = "";
        
        String rownum = getRowNumber(node);
        
        if (StringUtils.isNotBlank(rownum)) {
            retval = rownum;
        }
        
        return retval;
    }

    private String getRowNumber(Element node) {
        String retval = "";
        
        Node prev = node.getPreviousSibling();
        while (prev != null) {
            if (Constants.HTML_TAG_TYPE_TH.equalsIgnoreCase(prev.getNodeName())) {
                try {
                    String s = Utils.cleanDisplayText(prev);
                    Integer.parseInt(s);
                    retval = s;
                    break;
                }
                
                catch (NumberFormatException ex) {}
            }
            
            prev = prev.getPreviousSibling();
        }
        
        return retval;
    }
}
