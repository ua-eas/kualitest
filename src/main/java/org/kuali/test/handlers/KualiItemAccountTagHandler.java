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

import org.jsoup.nodes.Node;
import org.kuali.test.CheckpointProperty;
import static org.kuali.test.handlers.DefaultHtmlTagHandler.LOG;
import org.kuali.test.utils.Constants;
import org.kuali.test.utils.Utils;


public class KualiItemAccountTagHandler extends DefaultHtmlTagHandler {
    @Override
    public CheckpointProperty getCheckpointProperty(Node node) {
        CheckpointProperty retval = super.getCheckpointProperty(node); 

        Node anchor = findAnchor(node);
        
        if (anchor != null) {
            retval.setPropertyValue(Utils.cleanDisplayText(anchor.toString()));
        } else {
            retval.setPropertyValue(Utils.cleanDisplayText(node.toString()));
        }
        
        return retval;
    }

    private Node findAnchor(Node node) {
        Node retval = null;
        
        Node cnode = null;
        for (Node child : node.childNodes()) {
            if (Constants.HTML_TAG_TYPE_DIV.equalsIgnoreCase(child.nodeName())
                || Constants.HTML_TAG_TYPE_SPAN.equalsIgnoreCase(child.nodeName())) {
                cnode = child;
                 break;
            }
        }

        if (cnode != null) {
            for (Node child : cnode.childNodes()) {
                if (Constants.HTML_TAG_TYPE_ANCHOR.equalsIgnoreCase(child.nodeName())) {
                    retval = child;
                    break;
                }
            }
        }
            
        return retval;
    }
    
    @Override
    public String getSectionName(Node node) {
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
    public String getSubSectionName(Node node) {
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
    public String getSubSectionAdditional(Node node) {
        String retval = null;
        
        String data = node.toString();
        
        int pos1 = data.indexOf(Constants.SOURCE_ACCOUNTING_LINE_MATCH);
        
        if (pos1 < 0) {
            Node parent = node.parentNode();
            while (parent != null) {
                if (Constants.HTML_TAG_TYPE_TR.equalsIgnoreCase(parent.nodeName())) {
                    data = parent.toString();
                    pos1 = data.indexOf(Constants.SOURCE_ACCOUNTING_LINE_MATCH);
                    break;
                }
                parent = parent.parentNode();
            }
        }

        if (pos1 > -1) {
            int pos2 = data.indexOf("]", pos1);
            
            if ((pos2 > -1) && (pos2 > pos1)) {
                int indx = Integer.parseInt(data.substring(pos1 + Constants.SOURCE_ACCOUNTING_LINE_MATCH.length(), pos2));
                retval = "<span style='color: " + Constants.COLOR_DARK_RED + ";'>account[" + (indx+1) + "]</span>";
            }
        }

        
        return retval;
    }
}
