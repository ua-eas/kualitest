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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Node;
import org.kuali.test.CheckpointProperty;


public class CheckboxInputTagHandler extends DefaultHtmlTagHandler {
    @Override
    public CheckpointProperty getCheckpointProperty(Node node) {
        CheckpointProperty retval = super.getCheckpointProperty(node);
        retval.setPropertyName(node.attributes().get("name"));
        retval.setPropertyValue(getSelectedCheckboxValues(node, retval.getPropertyName()));
        return retval;
    }
    
    private String getSelectedCheckboxValues(Node node, String name) {
        String retval = "";
        
        List <String> l = new ArrayList<String>();

        
        for (Node sibling : node.siblingNodes()) {
            if (name.equals(sibling.attributes().get("name"))) {
                if (StringUtils.isNotBlank(sibling.attributes().get("selected"))) {
                    l.add(sibling.attributes().get("value"));
                    break;
                }
            }
        }

        if (!l.isEmpty()) {
            StringBuilder buf = new StringBuilder(64);
            Collections.sort(l);
            String comma = "";
            for (String s : l) {
              buf.append(comma);
              buf.append(s);
              comma = ",";
            }
            retval = buf.toString();
        }
        
        return retval;
    }

}
