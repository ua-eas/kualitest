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
import org.kuali.test.CheckpointProperty;
import org.kuali.test.TagHandler;
import org.w3c.dom.Element;

/**
 *
 * @author rbtucker
 */
public interface HtmlTagHandler {

    /**
     *
     * @param tag
     * @return
     */
    public boolean isContainer(Element tag);

    /**
     *
     * @param tag
     * @return
     */
    public JComponent getContainerComponent(Element tag);

    /**
     *
     * @param tag
     * @return
     */
    public String getGroupName(Element tag);

    /**
     *
     * @param tag
     * @return
     */
    public String getSectionName(Element tag);

    /**
     *
     * @param tag
     * @return
     */
    public String getSubSectionName(Element tag);

    /**
     *
     * @param tag
     * @return
     */
    public String getSubSectionAdditional(Element tag);

    /**
     *
     * @param tag
     * @return
     */
    public CheckpointProperty getCheckpointProperty(Element tag);

    /**
     *
     * @return
     */
    public TagHandler getTagHandler();
    
    /**
     *
     * @param tagHandler
     */
    public void setTagHandler(TagHandler tagHandler);

}
