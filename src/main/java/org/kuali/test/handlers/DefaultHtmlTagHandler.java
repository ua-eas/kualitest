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
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.test.CheckpointProperty;
import org.kuali.test.TagHandler;
import org.kuali.test.utils.Constants;
import org.kuali.test.utils.Utils;
import org.w3c.dom.Element;


public class DefaultHtmlTagHandler implements HtmlTagHandler {
    protected static final Logger LOG = Logger.getLogger(DefaultHtmlTagHandler.class);
    private TagHandler tagHandler;
    
    @Override
    public boolean isContainer(Element node) {
        boolean retval = false;
        if (Constants.HTML_TAG_TYPE_DIV.equalsIgnoreCase(node.getNodeName()) 
            || Constants.HTML_TAG_TYPE_TD.equalsIgnoreCase(node.getNodeName()) 
            || Constants.HTML_TAG_TYPE_TH.equalsIgnoreCase(node.getNodeName())) {
            retval = Utils.containsChildNode(node, Constants.HTML_TAG_TYPE_TABLE);
        }
        
        return retval;
    }

    @Override
    public JComponent getContainerComponent(Element node) {
        return null;
    }

    @Override
    public CheckpointProperty getCheckpointProperty(Element node) {
        CheckpointProperty retval = CheckpointProperty.Factory.newInstance();

        if (node.hasAttribute("value")) {
            retval.setPropertyValue(node.getAttribute("value"));
        }
        
        if (node.hasAttribute("id")) {
            retval.setPropertyName(Utils.trimString(node.getAttribute("id")));
        } else if (node.hasAttribute("name")) {
            retval.setPropertyName(Utils.trimString(node.getAttribute("name")));
        } 
        
        retval.setDisplayName(Utils.trimString(retval.getPropertyName()));

        return retval;
    }
    
    protected JPanel getNewPanel(Element tag) {
        JPanel retval = new JPanel();
        retval.setName(getGroupName(tag));
        return retval;
    }

    protected JTabbedPane getNewTabbedPane() {
        return new JTabbedPane();
    }

    @Override
    public TagHandler getTagHandler() {
        return tagHandler;
    }

    @Override
    public void setTagHandler(TagHandler tagHandler) {
        this.tagHandler = tagHandler;
    }
    
    @Override
    public String getGroupName(Element node) {
        return null;
    }

    @Override
    public String getSectionName(Element node) {
        return null;
    }

    @Override
    public String getSubSectionName(Element node) {
        return null;
    }

    @Override
    public String getSubSectionAdditional(Element node) {
        return null;
    }
    
    protected String getSelectedRadioValue(Element node, String name) {
        String retval = "";
        
        for (Element sibling : Utils.getSiblingElements(node)) {
            if (name.equals(sibling.getAttribute("name"))) {
                if (StringUtils.isNotBlank(sibling.getAttribute("checked"))) {
                    retval = sibling.getAttribute("value");
                    break;
                }
            }
        }
        
        return retval;
    }
    
    protected String getSelectedOption(Element node) {
        String retval = "";
        
        for (Element sibling : Utils.getSiblingElements(node)) {
            if (Constants.HTML_TAG_TYPE_OPTION.equalsIgnoreCase(sibling.getNodeName())) {
                if (StringUtils.isNotBlank(sibling.getAttribute("selected"))) {
                    retval = sibling.getAttribute("value");
                    break;
                }
            }
        }
        
        return retval;
    }

    protected String getSelectedCheckboxValues(Element node, String name) {
        String retval = "";
        
        List <String> l = new ArrayList<String>();

        
        for (Element sibling : Utils.getSiblingElements(node)) {
            if (name.equals(sibling.getAttribute("name"))) {
                if (StringUtils.isNotBlank(sibling.getAttribute("selected"))) {
                    l.add(sibling.getAttribute("value"));
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
