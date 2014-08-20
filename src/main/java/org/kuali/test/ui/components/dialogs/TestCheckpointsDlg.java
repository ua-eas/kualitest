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

package org.kuali.test.ui.components.dialogs;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;
import org.kuali.test.Checkpoint;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.ui.base.BaseSetupDlg;
import org.kuali.test.ui.components.panels.TestCheckpointsPanel;
import org.kuali.test.utils.Constants;

/**
 *
 * @author rbtucker
 */
public class TestCheckpointsDlg extends BaseSetupDlg {
    /**
     * 
     * @param mainFrame
     * @param test 
     */
    public TestCheckpointsDlg(TestCreator mainFrame, List <Checkpoint> checkpoints) {
        super(mainFrame);
        setTitle("Test Checkpoints");
        initComponents(checkpoints);
    }

    private void initComponents(List <Checkpoint> checkpoints) {
        getContentPane().add(new TestCheckpointsPanel(getMainframe(), this, checkpoints), BorderLayout.CENTER);

        addStandardButtons();
        
        this.getSaveButton().setVisible(false);
        
        setDefaultBehavior();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(800, 400);
    }

    @Override
    protected String getCancelText() {
        return Constants.CLOSE_ACTION;
    }

    /**
     *
     * @return
     */
    @Override
    protected String getDialogName() {
        return "test-checkpoints";
    }

    @Override
    protected boolean save() {
        return false;
    }
}
