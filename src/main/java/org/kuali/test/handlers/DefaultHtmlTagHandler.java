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
import org.dom4j.Node;
import org.kuali.test.CheckpointProperty;
import org.kuali.test.TagHandler;
import org.kuali.test.utils.Constants;
import org.kuali.test.utils.Utils;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author rbtucker
 */
public class DefaultHtmlTagHandler implements HtmlTagHandler {
    protected static final Logger LOG = Logger.getLogger(DefaultHtmlTagHandler.class);
    private TagHandler tagHandler;

    /**
     *
     * @param node
     * @return
     */
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

    /**
     *
     * @param node
     * @return
     */
    @Override
    public JComponent getContainerComponent(Element node) {
        return null;
    }

    /**
     *
     * @param node
     * @return
     */
    @Override
    public CheckpointProperty getCheckpointProperty(Element node) {
        CheckpointProperty retval = CheckpointProperty.Factory.newInstance();

        if (StringUtils.isNotBlank(node.getAttribute("value"))) {
            retval.setPropertyValue(node.getAttribute("value"));
        }
        
        if (StringUtils.isNotBlank(node.getAttribute("id"))) {
            retval.setPropertyName(Utils.trimString(node.getAttribute("id")));
        } else if (StringUtils.isNotBlank(node.getAttribute("name"))) {
            retval.setPropertyName(Utils.trimString(node.getAttribute("name")));
        } 
        
        retval.setDisplayName(Utils.trimString(retval.getPropertyName()));

        return retval;
    }
    
    /**
     *
     * @param tag
     * @return
     */
    protected JPanel getNewPanel(Element tag) {
        JPanel retval = new JPanel();
        retval.setName(getGroupName(tag));
        return retval;
    }

    /**
     *
     * @return
     */
    protected JTabbedPane getNewTabbedPane() {
        return new JTabbedPane();
    }

    /**
     *
     * @return
     */
    @Override
    public TagHandler getTagHandler() {
        return tagHandler;
    }

    /**
     *
     * @param tagHandler
     */
    @Override
    public void setTagHandler(TagHandler tagHandler) {
        this.tagHandler = tagHandler;
    }
    
    /**
     *
     * @param node
     * @return
     */
    @Override
    public String getGroupName(Element node) {
        return null;
    }

    /**
     *
     * @param node
     * @return
     */
    @Override
    public String getSectionName(Element node) {
        return null;
    }

    /**
     *
     * @param node
     * @return
     */
    @Override
    public String getSubSectionName(Element node) {
        return null;
    }

    /**
     *
     * @param node
     * @return
     */
    @Override
    public String getSubSectionAdditional(Element node) {
        return null;
    }
    
    /**
     *
     * @param node
     * @param name
     * @return
     */
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
    
    /**
     *
     * @param node
     * @return
     */
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

    /**
     *
     * @param node
     * @param name
     * @return
     */
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
    
    protected boolean hasChildNodeWithNodeName(Element parent, String nodeName) {
        return (getFirstChildNodeByNodeName(parent, nodeName) != null);         
    }

    protected boolean hasChildNodeWithNodeNameAndAttribute(Element parent, String nodeName, String attributeName, String attributeValue) {
        return (getFirstChildNodeByNodeNameAndAttribute(parent, nodeName, attributeName, attributeValue) != null);         
    }

    protected Element getFirstChildNodeByNodeName(Element parent, String nodeName) {
        return getFirstChildNodeByNodeNameAndAttribute(parent, nodeName, null, null);
    }
    
    protected Element getFirstChildNodeByNodeNameAndAttribute(Element parent, String nodeName, String attributeName, String attributeValue) {
        Element retval = null;
        
        if (parent.hasChildNodes()) {
            NodeList nl = parent.getChildNodes();
            
            for (int i = 0; i < nl.getLength(); ++ i) {
                if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    Element curElement = (Element) nl.item(i);
                    if (curElement.getNodeName().equalsIgnoreCase(nodeName)) {
                        if (StringUtils.isBlank(attributeName) && StringUtils.isBlank(attributeValue)) {
                            retval = curElement;
                            break;
                        } else if (attributeValue.equalsIgnoreCase(curElement.getAttribute(attributeName))) {
                            retval = curElement;
                            break;
                        }
                    }
                }
            }
        }

        return retval;
    }

    protected boolean isSelectWrapper(Element node) {
        if (hasChildNodeWithNodeName(node, Constants.HTML_TAG_TYPE_SELECT)) {
            return true;
        } else {
            return false;
        }
    }

    protected boolean isRadioWrapper(Element node) {
        if (hasChildNodeWithNodeNameAndAttribute(node, Constants.HTML_TAG_TYPE_INPUT, Constants.HTML_TAG_ATTRIBUTE_TYPE, Constants.HTML_INPUT_ATTRIBUTE_TYPE_RADIO)) {
            return true;
        } else {
            return false;
        }
    }
        
    protected boolean isCheckboxWrapper(Element node) {
        if (hasChildNodeWithNodeNameAndAttribute(node, Constants.HTML_TAG_TYPE_INPUT, Constants.HTML_TAG_ATTRIBUTE_TYPE, Constants.HTML_INPUT_ATTRIBUTE_TYPE_CHECKBOX)) {
            return true;
        } else {
            return false;
        }
    }
}
