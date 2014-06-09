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
import org.kuali.test.utils.Constants;
import org.kuali.test.utils.Utils;


public class TdTagHandler extends DefaultHtmlTagHandler {
    @Override
    public CheckpointProperty getCheckpointProperty(Node node) {
        CheckpointProperty retval = super.getCheckpointProperty(node); 
        
        if (isSelectWrapper(node)) {
            retval.setPropertyValue(getSelectedOption(node.childNode(0)));
        } else if (isRadioWrapper(node)) {
            retval.setPropertyValue(getSelectedRadioValue(node.childNode(0), node.childNode(0).attr(Constants.HTML_TAG_ATTRIBUTE_NAME)));
        } else if (isCheckboxWrapper(node)) {
            retval.setPropertyValue(getSelectedCheckboxValues(node.childNode(0), node.childNode(0).attr(Constants.HTML_TAG_ATTRIBUTE_NAME)));
        } else {
            retval.setPropertyValue(Utils.cleanDisplayText(node.toString()));
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug(retval.getPropertyName() + "=" + retval.getPropertyValue());
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
    
    private boolean isSelectWrapper(Node node) {
        return ((node.childNodeSize() > 0) && Constants.HTML_TAG_TYPE_SELECT.equalsIgnoreCase(node.childNode(0).nodeName()));
    }

    private boolean isRadioWrapper(Node node) {
        boolean retval = false;
        
        if (isInputWrapper(node)) {
            Node child = node.childNode(0);
            retval = Constants.HTML_INPUT_ATTRIBUTE_TYPE_RADIO.equalsIgnoreCase(child.attr(Constants.HTML_TAG_ATTRIBUTE_TYPE));
        }
        
        return retval;
    }
        
    private boolean isCheckboxWrapper(Node node) {
        boolean retval = false;
        
        if (isInputWrapper(node)) {
            Node child = node.childNode(0);
            retval = Constants.HTML_INPUT_ATTRIBUTE_TYPE_CHECKBOX.equalsIgnoreCase(child.attr(Constants.HTML_TAG_ATTRIBUTE_TYPE));
        }
        
        return retval;
    }

    private boolean isInputWrapper(Node node) {
        return ((node.childNodeSize() > 0)  && Constants.HTML_TAG_TYPE_INPUT.equalsIgnoreCase(node.childNode(0).nodeName()));
    }
}
