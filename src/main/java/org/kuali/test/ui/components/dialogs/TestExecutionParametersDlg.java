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
import org.kuali.test.TestExecutionParameter;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.ui.base.BaseSetupDlg;
import org.kuali.test.ui.components.panels.TestExecutionParametersPanel;
import org.kuali.test.utils.Constants;

/**
 *
 * @author rbtucker
 */
public class TestExecutionParametersDlg extends BaseSetupDlg {
    /**
     * 
     * @param mainFrame
     * @param parameters 
     */
    public TestExecutionParametersDlg(TestCreator mainFrame, List <TestExecutionParameter> parameters) {
        super(mainFrame);
        setTitle("Test Execution Parameters");
        initComponents(parameters);
    }

    private void initComponents(List <TestExecutionParameter> parameters) {
        getContentPane().add(new TestExecutionParametersPanel(getMainframe(), this, parameters), BorderLayout.CENTER);

        addStandardButtons();
        
        this.getSaveButton().setVisible(false);
        
        setDefaultBehavior();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(1000, 400);
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
        return "test-execution-parameters";
    }

    @Override
    protected boolean save() {
        return false;
    }
}
