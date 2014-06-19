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
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.kuali.test.Platform;
import org.kuali.test.TestSuite;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.ui.base.BaseSetupDlg;
import org.kuali.test.ui.components.panels.PlatformTestsPanel;
import org.kuali.test.utils.Constants;
import org.kuali.test.utils.Utils;

/**
 *
 * @author rbtucker
 */
public class AddTestsDlg extends BaseSetupDlg implements ListSelectionListener {
    private Platform platform;
    private TestSuite testSuite;
    private PlatformTestsPanel testsPanel;
    
    public AddTestsDlg(TestCreator mainFrame, TestSuite testSuite) {
        super(mainFrame);
        this.platform = Utils.findPlatform(getConfiguration(), testSuite.getPlatformName());
        this.testSuite = testSuite;
        setTitle("Add Tests");
        initComponents();
    }

    private void initComponents() {
        getContentPane().add(testsPanel = new PlatformTestsPanel(getMainframe(), platform), BorderLayout.CENTER);
        testsPanel.addListSelectionListener(this);
        addStandardButtons();
        setDefaultBehavior();
    }

    @Override
    protected boolean getInitialSavedState() {
        return false;
    }
    
    @Override
    protected boolean save() {
        boolean retval = !getSelectedTests().isEmpty();
        setSaved(retval);
        dispose();
        return retval;
    }
    
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(300, 350);
    }

    @Override
    protected String getDialogName() {
        return "add-platform-tests";
    }

    @Override
    protected String getSaveText() {
        return Constants.OK_ACTION;
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        getSaveButton().setEnabled(!getSelectedTests().isEmpty());
    }
    
    public List <String> getSelectedTests() {
        return testsPanel.getSelectedTests();
    }

    public TestSuite getTestSuite() {
        return testSuite;
    }

    public Platform getPlatform() {
        return platform;
    }
}
