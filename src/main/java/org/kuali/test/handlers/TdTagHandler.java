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
            retval.setPropertyValue(getSelectedOption((Element)node.getFirstChild()));
        } else if (isRadioWrapper(node)) {
            Element c = (Element)node.getFirstChild();
            retval.setPropertyValue(getSelectedRadioValue((Element)node.getFirstChild(), c.getAttribute(Constants.HTML_TAG_ATTRIBUTE_NAME)));
        } else if (isCheckboxWrapper(node)) {
            Element c = (Element)node.getFirstChild();
            retval.setPropertyValue(getSelectedCheckboxValues(c, c.getAttribute(Constants.HTML_TAG_ATTRIBUTE_NAME)));
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
    
    private boolean isSelectWrapper(Element node) {
        return (node.hasChildNodes() && Constants.HTML_TAG_TYPE_SELECT.equalsIgnoreCase(node.getFirstChild().getNodeName()));
    }

    private boolean isRadioWrapper(Element node) {
        boolean retval = false;
        
        if (isInputWrapper(node)) {
            Element child = (Element)node.getFirstChild();
            retval = Constants.HTML_INPUT_ATTRIBUTE_TYPE_RADIO.equalsIgnoreCase(child.getAttribute(Constants.HTML_TAG_ATTRIBUTE_TYPE));
        }
        
        return retval;
    }
        
    private boolean isCheckboxWrapper(Element node) {
        boolean retval = false;
        
        if (isInputWrapper(node)) {
            Element child = (Element)node.getFirstChild();
            retval = Constants.HTML_INPUT_ATTRIBUTE_TYPE_CHECKBOX.equalsIgnoreCase(child.getAttribute(Constants.HTML_TAG_ATTRIBUTE_TYPE));
        }
        
        return retval;
    }

    private boolean isInputWrapper(Element node) {
        return (node.hasChildNodes()  && Constants.HTML_TAG_TYPE_INPUT.equalsIgnoreCase(node.getFirstChild().getNodeName()));
    }
}
