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

import org.kuali.test.utils.Constants;
import org.w3c.dom.Element;

/**
 *
 * @author rbtucker
 */
public class KualiTabTagHandler extends DefaultHtmlTagHandler {

    /**
     *
     * @param node
     * @return
     */
    @Override
    public boolean isContainer(Element node) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("tag: " + node.getNodeName() + ", id=" + node.getAttribute("id") + ", name=" + node.getAttribute("name"));
        }
        return true;
    }

    /**
     *
     * @param node
     * @return
     */
    @Override
    public String getGroupName(Element node) {
        String id = node.getAttribute(Constants.HTML_TAG_ATTRIBUTE_ID);
        String retval = id;
        int pos1 = id.indexOf("-");
        int pos2 = id.lastIndexOf("-");
        
        if ((pos1 > -1) && (pos2 > -1) && (pos2 > pos1)) {
            retval = id.substring(pos1+1, pos2);
        }
        
        // hack to handle generated tab names in kuali
        if (retval.startsWith("ID")) {
            retval = null;
        }
        
        return retval;
    }
}
