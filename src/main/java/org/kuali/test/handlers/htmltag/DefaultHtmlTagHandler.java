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

package org.kuali.test.handlers.htmltag;

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
            retval = (Utils.containsChildNode(node, Constants.HTML_TAG_TYPE_TABLE) 
                || Utils.containsChildNode(node, Constants.HTML_TAG_TYPE_IFRAME));
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

        if (StringUtils.isNotBlank(node.getAttribute(Constants.HTML_TAG_ATTRIBUTE_VALUE))) {
            retval.setPropertyValue(node.getAttribute(Constants.HTML_TAG_ATTRIBUTE_VALUE));
        }
        
        if (StringUtils.isNotBlank(node.getAttribute(Constants.HTML_TAG_ATTRIBUTE_ID))) {
            String id = Utils.trimString(node.getAttribute(Constants.HTML_TAG_ATTRIBUTE_ID));
            
            if (id.contains(".")) {
                int pos = id.lastIndexOf(".");
                retval.setDisplayName(id.substring(pos+1));
                
            }
            
            retval.setPropertyName(id);
        } else if (StringUtils.isNotBlank(node.getAttribute(Constants.HTML_TAG_ATTRIBUTE_NAME))) {
            String nm = Utils.trimString(node.getAttribute(Constants.HTML_TAG_ATTRIBUTE_NAME));
            
            if (nm.contains(".")) {
                int pos = nm.lastIndexOf(".");
                retval.setDisplayName(nm.substring(pos+1));
            }
            
            retval.setPropertyName(nm);
        } 

        if (StringUtils.isBlank(retval.getDisplayName())) {
            retval.setDisplayName(Utils.trimString(retval.getPropertyName()));
        }

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
            if (name.equals(sibling.getAttribute(Constants.HTML_TAG_ATTRIBUTE_NAME))) {
                if (StringUtils.isNotBlank(sibling.getAttribute(Constants.HTML_TAG_ATTRIBUTE_CHECKED))) {
                    retval = sibling.getAttribute(Constants.HTML_TAG_ATTRIBUTE_VALUE);
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
        
        List <Element> children = Utils.getChildElements(node);
        
        for (Element child : children) {
            if (Constants.HTML_TAG_TYPE_OPTION.equalsIgnoreCase(child.getNodeName())) {
                if (StringUtils.isNotBlank(child.getAttribute(Constants.HTML_TAG_ATTRIBUTE_SELECTED))) {
                    retval = child.getAttribute(Constants.HTML_TAG_ATTRIBUTE_VALUE);
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
            if (name.equals(sibling.getAttribute(Constants.HTML_TAG_ATTRIBUTE_NAME))) {
                if (StringUtils.isNotBlank(sibling.getAttribute(Constants.HTML_TAG_ATTRIBUTE_SELECTED))) {
                    l.add(sibling.getAttribute(Constants.HTML_TAG_ATTRIBUTE_VALUE));
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
    
    @Override
    public boolean isInputWrapper(Element node) {
        return (!isInput(node) && (isTextInputWrapper(node) || isSelectWrapper(node) || isRadioWrapper(node) || isCheckboxWrapper(node)));
    }
    
    @Override
    public boolean isInput(Element node) {
        return Utils.isFormInputTag(node.getTagName());
    }

    @Override
    public Element getInputElement(Element node) {
        Element retval = null;

        if (isInput(node)) {
            retval = node;
        } else {
            retval = Utils.getFirstChildNodeByNodeNameAndAttribute(node, Constants.HTML_TAG_TYPE_INPUT, 
                Constants.HTML_TAG_ATTRIBUTE_TYPE, Constants.HTML_INPUT_ATTRIBUTE_TYPE_TEXT);
            
            if (retval == null) {
                retval = Utils.getFirstChildNodeByNodeName(node, Constants.HTML_TAG_TYPE_SELECT);
                
                if (retval == null) {
                    retval = Utils.getFirstChildNodeByNodeNameAndAttribute(node, Constants.HTML_TAG_TYPE_INPUT, 
                        Constants.HTML_TAG_ATTRIBUTE_TYPE, Constants.HTML_INPUT_ATTRIBUTE_TYPE_RADIO);
                    
                    if (retval == null) {
                        retval = Utils.getFirstChildNodeByNodeNameAndAttribute(node, Constants.HTML_TAG_TYPE_INPUT, 
                            Constants.HTML_TAG_ATTRIBUTE_TYPE, Constants.HTML_INPUT_ATTRIBUTE_TYPE_CHECKBOX);
                    }
                }
            }
        }
        
        return retval;
    }
    
    protected boolean isSelectWrapper(Element node) {
        return Utils.hasChildNodeWithNodeName(node, Constants.HTML_TAG_TYPE_SELECT);
    }

    protected boolean isRadioWrapper(Element node) {
        return Utils.hasChildNodeWithNodeNameAndAttribute(node, Constants.HTML_TAG_TYPE_INPUT, 
            Constants.HTML_TAG_ATTRIBUTE_TYPE, Constants.HTML_INPUT_ATTRIBUTE_TYPE_RADIO);
    }
        
    protected boolean isCheckboxWrapper(Element node) {
        return Utils.hasChildNodeWithNodeNameAndAttribute(node, Constants.HTML_TAG_TYPE_INPUT, 
            Constants.HTML_TAG_ATTRIBUTE_TYPE, Constants.HTML_INPUT_ATTRIBUTE_TYPE_CHECKBOX);
    }

    protected boolean isTextInputWrapper(Element node) {
        boolean retval = Utils.hasChildNodeWithNodeNameAndAttribute(node, Constants.HTML_TAG_TYPE_INPUT, 
            Constants.HTML_TAG_ATTRIBUTE_TYPE, Constants.HTML_INPUT_ATTRIBUTE_TYPE_TEXT);
        
        if (!retval) {
            if (Utils.hasChildNodeWithNodeName(node, Constants.HTML_TAG_TYPE_SPAN)) {
                Element e = Utils.getFirstChildNodeByNodeName(node, Constants.HTML_TAG_TYPE_SPAN);
                
                if (e != null) {
                     retval = Utils.hasChildNodeWithNodeNameAndAttribute(e, Constants.HTML_TAG_TYPE_INPUT, 
                         Constants.HTML_TAG_ATTRIBUTE_TYPE, Constants.HTML_INPUT_ATTRIBUTE_TYPE_TEXT);

                    if (!retval) {
                        e = Utils.getFirstChildNodeByNodeName(e, Constants.HTML_TAG_TYPE_DIV);

                        if (e != null) {
                             retval = Utils.hasChildNodeWithNodeNameAndAttribute(e, Constants.HTML_TAG_TYPE_INPUT, 
                                 Constants.HTML_TAG_ATTRIBUTE_TYPE, Constants.HTML_INPUT_ATTRIBUTE_TYPE_TEXT);
                        }
                    }
                }
            }
        }
        
        return retval;
    }
}
