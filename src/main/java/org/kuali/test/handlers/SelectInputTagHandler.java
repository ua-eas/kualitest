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

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Node;
import org.kuali.test.CheckpointProperty;
import org.kuali.test.utils.Constants;


public class SelectInputTagHandler extends DefaultHtmlTagHandler {
    @Override
    public CheckpointProperty getCheckpointProperty(Node node) {
        CheckpointProperty retval = super.getCheckpointProperty(node);

        retval.setPropertyName(node.attributes().get("id"));
        
        if (StringUtils.isBlank(retval.getPropertyName())) {
            retval.setPropertyName(node.attributes().get("name"));
        }
        
        retval.setPropertyValue(getSelectedOption(node));
        return retval;
    }
    
    private String getSelectedOption(Node node) {
        String retval = "";
        
        for (Node sibling : node.siblingNodes()) {
            if (Constants.HTML_TAG_TYPE_OPTION.equalsIgnoreCase(sibling.nodeName())) {
                if (StringUtils.isNotBlank(sibling.attributes().get("selected"))) {
                    retval = sibling.attributes().get("value");
                    break;
                }
            }
        }
        
        return retval;
    }
}
