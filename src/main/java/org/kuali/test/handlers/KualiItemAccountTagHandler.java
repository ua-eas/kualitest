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

package org.kuali.test.handlers;

import org.kuali.test.CheckpointProperty;
import static org.kuali.test.handlers.DefaultHtmlTagHandler.LOG;
import org.kuali.test.utils.Constants;
import org.kuali.test.utils.Utils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


public class KualiItemAccountTagHandler extends DefaultHtmlTagHandler {
    @Override
    public CheckpointProperty getCheckpointProperty(Element node) {
        CheckpointProperty retval = super.getCheckpointProperty(node); 

        Element anchor = findAnchor(node);
        
        if (anchor != null) {
            retval.setPropertyValue(Utils.cleanDisplayText(anchor));
        } else {
            retval.setPropertyValue(Utils.cleanDisplayText(node));
        }
        
        return retval;
    }

    private Element findAnchor(Element node) {
        Element retval = null;
        
        Element cnode = null;
        for (Element child : Utils.getChildElements(node)) {
            if (Constants.HTML_TAG_TYPE_DIV.equalsIgnoreCase(child.getNodeName())
                || Constants.HTML_TAG_TYPE_SPAN.equalsIgnoreCase(child.getNodeName())) {
                cnode = child;
                 break;
            }
        }

        if (cnode != null) {
            for (Element child : Utils.getChildElements(cnode)) {
                if (Constants.HTML_TAG_TYPE_ANCHOR.equalsIgnoreCase(child.getNodeName())) {
                    retval = child;
                    break;
                }
            }
        }
            
        return retval;
    }
    
    @Override
    public String getSectionName(Element node) {
        String retval = null;
        if (getTagHandler().getSectionMatcher() != null) {
            retval = Utils.getMatchedNodeText(getTagHandler().getSectionMatcher().getTagMatcherArray(), node); 
        }
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("section: " + retval);
        }
        
        return retval;
    }

    @Override
    public String getSubSectionName(Element node) {
        String retval = null;
        if (getTagHandler().getSubSectionMatcher() != null) {
            retval = Utils.getMatchedNodeText(getTagHandler().getSubSectionMatcher().getTagMatcherArray(), node); 
        }
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("subsection: " + retval);
        }
        
        return retval;
    }

    @Override
    public String getSubSectionAdditional(Element node) {
        String retval = null;
        
        String data = Utils.cleanDisplayText(node);
        
        int pos1 = data.indexOf(Constants.SOURCE_ACCOUNTING_LINE_MATCH);
        
        if (pos1 < 0) {
            Node parent = node.getParentNode();
            while (Utils.isElement(parent)) {
                if (Constants.HTML_TAG_TYPE_TR.equalsIgnoreCase(parent.getNodeName())) {
                    data = Utils.cleanDisplayText(parent);
                    pos1 = data.indexOf(Constants.SOURCE_ACCOUNTING_LINE_MATCH);
                    break;
                }
                parent = parent.getParentNode();
            }
        }

        if (pos1 > -1) {
            int pos2 = data.indexOf("]", pos1);
            
            if ((pos2 > -1) && (pos2 > pos1)) {
                int indx = Integer.parseInt(data.substring(pos1 + Constants.SOURCE_ACCOUNTING_LINE_MATCH.length(), pos2));
                retval = Utils.buildHtmlStyle(Constants.HTML_DARK_RED_STYLE, "account[" + (indx+1) + "]");
            }
        }

        
        return retval;
    }
}
