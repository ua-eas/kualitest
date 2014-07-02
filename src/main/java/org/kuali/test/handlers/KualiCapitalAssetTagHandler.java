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
import org.kuali.test.CheckpointProperty;
import org.kuali.test.utils.Constants;
import org.kuali.test.utils.Utils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author rbtucker
 */
public class KualiCapitalAssetTagHandler extends TdTagHandler {
    public static final String CAPITAL_ASSET_PROPERTY_GROUP = "CapitalAsset";
    public static final String CAPITAL_ASSET_ITEMS_TABLE_SUMMARY = "Capital Asset Items";

    /**
     *
     * @param node
     * @return
     */
    @Override
    public CheckpointProperty getCheckpointProperty(Element node) {
        CheckpointProperty retval = super.getCheckpointProperty(node); 
        retval.setPropertyGroup(CAPITAL_ASSET_PROPERTY_GROUP);
        return retval;
    }
    
    /**
     *
     * @param node
     * @return
     */
    @Override
    public String getSectionName(Element node) {
        StringBuilder retval = new StringBuilder(32);
        
        retval.append(CAPITAL_ASSET_PROPERTY_GROUP); 
        
        return retval.toString();
    }

    /**
     *
     * @param node
     * @return
     */
    @Override
    public String getSubSectionName(Element node) {
        StringBuilder retval = new StringBuilder(32);
        
        String itemNum = "";
        if (getTagHandler().getSectionMatcher() == null) {
            itemNum = getItemNumberForLocation(node);
        } else {
            itemNum = Utils.getMatchedNodeText(getTagHandler().getSectionMatcher().getTagMatcherArray(), node); 
        }
       
        if (StringUtils.isNotBlank(itemNum) && StringUtils.isNumeric(itemNum)) {
            retval.append(Utils.buildHtmlStyle(Constants.HTML_DARK_RED_STYLE, "[" + itemNum + "]"));
        }

        
        return retval.toString();
    }
    
    private String getItemNumberForLocation(Element node) {
        String retval = "";
        
        Node parent = node.getParentNode();
        while (Utils.isElement(parent)) {
            if (Utils.isElement(parent.getParentNode())) {
                if (Constants.HTML_TAG_TYPE_TABLE.equalsIgnoreCase(parent.getParentNode().getNodeName())
                    && Constants.HTML_TAG_TYPE_TR.equalsIgnoreCase(parent.getNodeName())) {

                    Element table = (Element)parent.getParentNode();

                    if (Constants.HTML_TAG_ATTRIBUTE_CLASS_DATATABLE.equalsIgnoreCase(table.getAttribute(Constants.HTML_TAG_ATTRIBUTE_CLASS))
                        && CAPITAL_ASSET_ITEMS_TABLE_SUMMARY.equalsIgnoreCase(table.getAttribute(Constants.HTML_TAG_ATTRIBUTE_SUMMARY))) {
                        
                        Element sibling = Utils.findPreviousSiblingNode((Element)parent, Constants.HTML_TAG_TYPE_TR);

                        if (sibling != null) {
                            Element td = Utils.findFirstChildNode(sibling, Constants.HTML_TAG_TYPE_TD);

                            if (td != null) {
                                retval = Utils.cleanDisplayText(td);
                                break;
                            }
                        }
                    }
                }
            }
            
            parent = parent.getParentNode();
        }
        
        return retval;
    }
}
