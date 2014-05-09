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
import org.htmlcleaner.TagNode;
import org.kuali.test.CheckpointProperty;


public class RadioInputTagHandler extends DefaultHtmlTagHandler {
    @Override
    public CheckpointProperty getCheckpointProperty(TagNode tag) {
        CheckpointProperty retval = super.getCheckpointProperty(tag);
        retval.setPropertyName(tag.getAttributeByName("name"));
        retval.setPropertyValue(getSelectedRadioValue(tag, retval.getPropertyName()));
        return retval;
    }
    
    private String getSelectedRadioValue(TagNode tag, String name) {
        String retval = "";
        
        TagNode[] childTags = tag.getParent().getChildTags();
        
        for (int i = 0; i < childTags.length; ++i) {
            if (name.equals(childTags[i].getAttributeByName("name"))) {
                if (StringUtils.isNotBlank(childTags[i].getAttributeByName("checked"))) {
                    retval = childTags[i].getAttributeByName("value");
                    break;
                }
            }
        }
        
        return retval;
    }

}
