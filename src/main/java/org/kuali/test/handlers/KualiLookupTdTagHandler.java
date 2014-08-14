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
import org.kuali.test.utils.Utils;
import org.w3c.dom.Element;

/**
 *
 * @author rbtucker
 */
public class KualiLookupTdTagHandler extends TdTagHandler {
    @Override
    public String getSectionName(Element node) {
        return "lookup result";
    }

    @Override
    public String getSubSectionName(Element node) {
        String retval = null;
        Element pnode = (Element)node.getParentNode();
        
        if ((pnode != null) && Constants.HTML_TAG_TYPE_TR.equals(pnode.getTagName())) {
            retval = Utils.buildHtmlStyle(Constants.HTML_DARK_RED_STYLE, "row[" + (Utils.getChildNodeIndex(pnode)+1) + "]");
        }

        return retval;
    }
}
