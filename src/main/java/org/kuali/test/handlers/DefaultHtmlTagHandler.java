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
import org.jsoup.nodes.Node;
import org.kuali.test.CheckpointProperty;
import org.kuali.test.TagHandler;
import org.kuali.test.utils.Constants;


public class DefaultHtmlTagHandler implements HtmlTagHandler {
    protected static final Logger LOG = Logger.getLogger(DefaultHtmlTagHandler.class);
    private TagHandler tagHandler;
    
    @Override
    public boolean isContainer(Node node) {
        return false;
    }

    @Override
    public JComponent getContainerComponent(Node node) {
        return null;
    }

    @Override
    public CheckpointProperty getCheckpointProperty(Node node) {
        CheckpointProperty retval = CheckpointProperty.Factory.newInstance();

        if (node.hasAttr("value")) {
            retval.setPropertyValue(node.attr("value"));
        }
        
        if (node.hasAttr("id")) {
            retval.setPropertyName(node.attr("id"));
        } else if (node.hasAttr("name")) {
            retval.setPropertyName(node.attr("name"));
        } else {
            retval.setPropertyName(node.attr("test-id"));
        }
        
        retval.setDisplayName(retval.getPropertyName());

        return retval;
    }
    
    protected JPanel getNewPanel(Node tag) {
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
    public String getGroupName(Node node) {
        return null;
    }

    @Override
    public String getSectionName(Node node) {
        return null;
    }

    @Override
    public String getSubSectionName(Node node) {
        return null;
    }

    @Override
    public String getSubSectionAdditional(Node node) {
        return null;
    }
    
    protected String getSelectedRadioValue(Node node, String name) {
        String retval = "";
        
        for (Node sibling : node.siblingNodes()) {
            if (name.equals(sibling.attr("name"))) {
                if (StringUtils.isNotBlank(sibling.attr("checked"))) {
                    retval = sibling.attr("value");
                    break;
                }
            }
        }
        
        return retval;
    }
    
    protected String getSelectedOption(Node node) {
        String retval = "";
        
        for (Node sibling : node.siblingNodes()) {
            if (Constants.HTML_TAG_TYPE_OPTION.equalsIgnoreCase(sibling.nodeName())) {
                if (StringUtils.isNotBlank(sibling.attr("selected"))) {
                    retval = sibling.attr("value");
                    break;
                }
            }
        }
        
        return retval;
    }

    protected String getSelectedCheckboxValues(Node node, String name) {
        String retval = "";
        
        List <String> l = new ArrayList<String>();

        
        for (Node sibling : node.siblingNodes()) {
            if (name.equals(sibling.attr("name"))) {
                if (StringUtils.isNotBlank(sibling.attr("selected"))) {
                    l.add(sibling.attr("value"));
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
