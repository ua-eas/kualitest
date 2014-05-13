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

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.kuali.test.CheckpointProperty;
import org.kuali.test.TagMatcher;
import org.kuali.test.utils.Utils;


public class TdTagHandler extends DefaultHtmlTagHandler {
    @Override
    public boolean isContainer(Node node) {
        return false;
    }

    @Override
    public CheckpointProperty getCheckpointProperty(Node node) {
        CheckpointProperty retval = super.getCheckpointProperty(node); 
        if (node instanceof Element) {
            retval.setPropertyValue(Utils.cleanDisplayText(node.toString()));
        }
        
        return retval;
    }

    @Override
    public String getSectionName(Node node) {
        String retval = super.getSectionName(node); 
        
        if (getTagHandler().getSectionMatcher() != null) {
            Node curnode = node;
            for (TagMatcher tm : getTagHandler().getSectionMatcher().getTagMatcherArray()) {
                curnode = Utils.getMatchingTagNode(tm, curnode);
                
                if (curnode == null) {
                    break;
                }
            }
            
            if (curnode != null) {
                retval = Utils.cleanDisplayText(curnode.toString());
            }
        }
        
        return retval;
    }
}
