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
import static org.kuali.test.handlers.htmltag.DefaultHtmlTagHandler.LOG;
import org.kuali.test.utils.Constants;
import org.kuali.test.utils.Utils;
import org.w3c.dom.Element;

/**
 *
 * @author rbtucker
 */
public class KualiItemAccountTagHandler extends TdTagHandler {

    /**
     *
     * @param node
     * @return
     */
    @Override
    public CheckpointProperty getCheckpointProperty(Element node) {
        CheckpointProperty retval = super.getCheckpointProperty(node); 

        Element anchor = findAnchor(node);
        
        if (anchor != null) {
            retval.setPropertyValue(Utils.cleanDisplayText(anchor));
        } 

        return retval;
    }

    private Element findAnchor(Element node) {
        Element retval = null;
        
        if (!isSelectWrapper(node)) {
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
        String retval = null;
        if (getTagHandler().getSectionMatcher() != null) {
            retval = Utils.getMatchedNodeText(getTagHandler().getSectionMatcher().getTagMatcherArray(), node); 
        }
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("section: " + retval);
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
        String retval = null;
        if (getTagHandler().getSubSectionMatcher() != null) {
            retval = Utils.getMatchedNodeText(getTagHandler().getSubSectionMatcher().getTagMatcherArray(), node); 
        }
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("subsection: " + retval);
        }
        
        return retval;
    }

    /**
     *
     * @param node
     * @return
     */
    @Override
    public String getSubSectionAdditional(Element node) {
        String retval = null;

        Element e = Utils.findFirstChildNode(node, Constants.HTML_TAG_TYPE_DIV);
        
        if (e != null) {
            String id = e.getAttribute(Constants.HTML_TAG_ATTRIBUTE_ID);
            if (StringUtils.isNotBlank(id)) {
                int pos = id.indexOf(Constants.SOURCE_ACCOUNTING_LINE_MATCH);
                if (pos > -1) {
                    int pos2 = id.indexOf("]", pos);

                    if ((pos2 > -1) && (pos2 > pos)) {
                        int indx = Integer.parseInt(id.substring(pos + Constants.SOURCE_ACCOUNTING_LINE_MATCH.length(), pos2));
                        retval = Utils.buildHtmlStyle(Constants.HTML_DARK_RED_STYLE, "account[" + (indx+1) + "]");
                    }
                }
            }
        }
        
        return retval;
    }
}
