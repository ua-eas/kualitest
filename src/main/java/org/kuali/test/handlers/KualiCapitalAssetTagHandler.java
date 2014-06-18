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


public class KualiCapitalAssetTagHandler extends DefaultHtmlTagHandler {
    public static final String CAPITAL_ASSET_PROPERTY_GROUP = "CapitalAsset";
    public static final String CAPITAL_ASSET_ITEMS_SECTION = "Capital Asset Item";
    public static final String ASSET_LOCATION_SUB_SECTION = "Location";

    @Override
    public CheckpointProperty getCheckpointProperty(Element node) {
        CheckpointProperty retval = super.getCheckpointProperty(node); 
        retval.setPropertyValue(Utils.cleanDisplayText(node));
        retval.setPropertyGroup(CAPITAL_ASSET_PROPERTY_GROUP);
        return retval;
    }
    
    @Override
    public String getSectionName(Element node) {
        StringBuilder retval = new StringBuilder(32);
        retval.append(CAPITAL_ASSET_ITEMS_SECTION); 
        
        return retval.toString();
    }

    @Override
    public String getSubSectionName(Element node) {
        StringBuilder retval = new StringBuilder(64);
        
        String ss = null;
        
        if (getTagHandler().getSubSectionMatcher() != null) {
            ss = Utils.getMatchedNodeText(getTagHandler().getSubSectionMatcher().getTagMatcherArray(), node); 
        }

        if (StringUtils.isNotBlank(ss)) {
            if (ASSET_LOCATION_SUB_SECTION.equalsIgnoreCase(ss)) {
                retval.append(getItemNumberForLocation(node));
                retval.append(" ");
            }                 
            
            retval.append(ss);
        }
        
        return retval.toString();
    }
    
    private String getItemNumberForLocation(Element node) {
        String retval = "";
        
        Node parent = node.getParentNode();
        while (Utils.isElement(parent)) {
            if (Constants.HTML_TAG_TYPE_TR.equalsIgnoreCase(parent.getNodeName())) {
                Element table = Utils.findFirstParentNode((Element)parent, Constants.HTML_TAG_TYPE_TABLE);
                
                if ((table != null) 
                    && "datatable".equalsIgnoreCase(table.getAttribute(Constants.HTML_TAG_ATTRIBUTE_CLASS))
                    && "Capital Asset Items".equalsIgnoreCase(table.getAttribute(Constants.HTML_TAG_ATTRIBUTE_SUMMARY))) {
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
            
            
            parent = parent.getParentNode();
        }
        
        return retval;
    }
}
