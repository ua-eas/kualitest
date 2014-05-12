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


public class KualiTabTagHandler extends DefaultHtmlTagHandler {
    @Override
    public boolean isContainer(Node node) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("tag: " + node.nodeName() + ", id=" + node.attr("id") + ", name=" + node.attr("name"));
        }
        return true;
    }

    @Override
    public String getGroupName(Node node) {
        String id = node.attr("id");
        String retval = id;
        int pos1 = id.indexOf("-");
        int pos2 = id.lastIndexOf("-");
        
        if ((pos1 > -1) && (pos2 > -1) && (pos2 > pos1)) {
            retval = id.substring(pos1+1, pos2);
        }
        
        return retval;
    }
}
