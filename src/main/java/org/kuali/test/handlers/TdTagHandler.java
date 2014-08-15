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
import org.kuali.test.utils.Constants;
import org.kuali.test.utils.Utils;
import org.w3c.dom.Element;

/**
 *
 * @author rbtucker
 */
public class TdTagHandler extends DefaultHtmlTagHandler {
    
    /**
     *
     * @param node
     * @return
     */
    @Override
    public CheckpointProperty getCheckpointProperty(Element node) {
        CheckpointProperty retval = super.getCheckpointProperty(node); 
        
        if (isSelectWrapper(node)) {
            Element c = Utils.getFirstChildNodeByNodeName(node, Constants.HTML_TAG_TYPE_SELECT);
            
            if (c != null) {
               retval.setPropertyValue(getSelectedOption(c));
            }
        } else if (isRadioWrapper(node)) {
            Element c = Utils.getFirstChildNodeByNodeNameAndAttribute(node, Constants.HTML_TAG_TYPE_INPUT, 
                Constants.HTML_TAG_ATTRIBUTE_TYPE, Constants.HTML_INPUT_ATTRIBUTE_TYPE_RADIO);
            
            if (c != null) {
                retval.setPropertyValue(getSelectedRadioValue(c, c.getAttribute(Constants.HTML_TAG_ATTRIBUTE_NAME)));
            }
        } else if (isCheckboxWrapper(node)) {
            Element c = Utils.getFirstChildNodeByNodeNameAndAttribute(node, Constants.HTML_TAG_TYPE_INPUT, 
                Constants.HTML_TAG_ATTRIBUTE_TYPE, Constants.HTML_INPUT_ATTRIBUTE_TYPE_CHECKBOX);
            if (c != null) {
                retval.setPropertyValue(getSelectedCheckboxValues(c, c.getAttribute(Constants.HTML_TAG_ATTRIBUTE_NAME)));
            }
        } else if (isTextInputWrapper(node)) {
            Element c = Utils.getFirstChildNodeByNodeNameAndAttribute(node, Constants.HTML_TAG_TYPE_INPUT, 
                Constants.HTML_TAG_ATTRIBUTE_TYPE, Constants.HTML_INPUT_ATTRIBUTE_TYPE_TEXT);
            if (c != null) {
                retval.setPropertyValue(node.getAttribute(Constants.HTML_TAG_ATTRIBUTE_VALUE));
            }
        } else {
            retval.setPropertyValue(Utils.cleanDisplayText(node));
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug(retval.getPropertyName() + "=" + retval.getPropertyValue());
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
}
