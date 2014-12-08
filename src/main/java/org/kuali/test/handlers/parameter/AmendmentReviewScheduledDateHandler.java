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

package org.kuali.test.handlers.parameter;

import org.apache.commons.lang3.StringUtils;
import org.kuali.test.CheckpointProperty;
import org.kuali.test.KualiApplication;
import org.kuali.test.Parameter;
import org.kuali.test.runner.execution.TestExecutionContext;
import org.kuali.test.utils.Constants;
import org.kuali.test.utils.Utils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class AmendmentReviewScheduledDateHandler extends AbstractParameterHandler {
    @Override
    public String getDescription() {
        return "This handler will save the selected amendment review date value for use later";
    }

    @Override
    public String getValue(TestExecutionContext tec, Document htmlDocument, CheckpointProperty cp, String inputValue) {
        String retval = "";
        
        if (StringUtils.isNotBlank(inputValue)) {
            Parameter param = Utils.getCheckpointPropertyTagParameter(cp, Constants.HTML_TAG_ATTRIBUTE_NAME);
        
            if (param != null) {
                Node node = tec.getWebClient().findHtmlElementByName(inputValue);

                if ((node != null) && Constants.HTML_TAG_TYPE_SELECT.equalsIgnoreCase(node.getNodeName())) {
                    NodeList nodeList = node.getChildNodes();

                    for (int i = 0; i < nodeList.getLength(); ++i) {
                        Node childNode = nodeList.item(i);

                        if (childNode.getNodeName().equalsIgnoreCase(Constants.HTML_TAG_TYPE_OPTION)) {
                            Node att = childNode.getAttributes().getNamedItem(Constants.HTML_TAG_ATTRIBUTE_VALUE);

                            if ((att != null) && inputValue.equals(att.getNodeValue())) {
                                retval = findDate(Utils.cleanDisplayText(childNode));
                                break;
                            }
                        }
                    }
                }
            }
        }
        
        return retval;
    }

    private String findDate(String in) {
        String retval = "";
        
        if (StringUtils.isNotBlank(in)) {
            int pos = in.indexOf(",");
            if ((pos > -1) && in.substring(0, pos).contains("/")) {
                retval = in.substring(0, pos).trim();
            }
        }
        
        return retval;
    }
    
    /**
     *
     * @return
     */
    @Override
    public KualiApplication.Enum getApplication() {
        return KualiApplication.KC;
    }
}
