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

package org.kuali.test.ui.components.panels;

import javax.swing.JLabel;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.ui.base.BasePanel;

/**
 *
 * @author rbtucker
 */
public class RunningTestPanel extends BasePanel {

    /**
     *
     * @param mainframe
     */
    public RunningTestPanel(TestCreator mainframe) {
        super(mainframe);
        add(new JLabel("inactive", JLabel.CENTER));
    }
}
