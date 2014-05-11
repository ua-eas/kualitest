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
import org.kuali.test.utils.Utils;


public class KualiTabTagHandler extends DefaultHtmlTagHandler {
    @Override
    public boolean isContainer(Node node) {
        return true;
    }

    @Override
    public String getGroupName(Node node) {
        String retval = super.getGroupName(node);
        if (getTagHandler().getLabelMatcher() != null) {
            retval = Utils.getLabelText(getTagHandler().getLabelMatcher(), node);
        }
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("group: " + retval);
        }
        
        return retval;
    }
}
