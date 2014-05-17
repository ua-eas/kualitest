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
import org.jsoup.nodes.Node;
import org.kuali.test.CheckpointProperty;
import org.kuali.test.TagHandler;


public interface HtmlTagHandler {
    public boolean isContainer(Node tag);
    public JComponent getContainerComponent(Node tag);
    public String getGroupName(Node tag);
    public String getSectionName(Node tag);
    public String getSubSectionName(Node tag);
    public String getSubSectionAdditional(Node tag);
    public CheckpointProperty getCheckpointProperty(Node tag);
    public TagHandler getTagHandler();
    public void setTagHandler(TagHandler tagHandler);
}
