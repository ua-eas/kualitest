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

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import org.htmlcleaner.TagNode;
import org.kuali.test.CheckpointProperty;
import org.kuali.test.TagHandler;
import org.kuali.test.utils.Constants;


public class DefaultHtmlTagHandler implements HtmlTagHandler {
    private TagHandler tagHandler;
    
    @Override
    public boolean isContainer() {
        return false;
    }

    @Override
    public JComponent getContainerComponent(TagNode tag) {
        return null;
    }

    @Override
    public CheckpointProperty getCheckpointProperty(TagNode tag) {
        CheckpointProperty retval = CheckpointProperty.Factory.newInstance();
        retval.setKey(tag.getAttributeByName("test-key"));
        retval.setPropertyGroup(getGroupName(tag));
        retval.setPropertySection(getSectionName(tag));

        return retval;
    }
    
    protected JPanel getNewPanel(TagNode tag) {
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
    public String getGroupName(TagNode tag) {
        return Constants.DEFAULT_HTML_PROPERTY_GROUP;
    }

    @Override
    public String getSectionName(TagNode tag) {
        return Constants.DEFAULT_HTML_PROPERTY_SECTION;
    }
}
