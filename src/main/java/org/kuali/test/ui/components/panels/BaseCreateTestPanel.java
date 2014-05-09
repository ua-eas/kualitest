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

import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.kuali.test.KualiTestDocument;
import org.kuali.test.Platform;
import org.kuali.test.PlatformTests;
import org.kuali.test.TestHeader;
import org.kuali.test.TestOperation;
import org.kuali.test.TestOperations;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.ui.base.BasePanel;
import org.kuali.test.ui.components.buttons.ToggleToolbarButton;
import org.kuali.test.ui.components.buttons.ToolbarButton;
import org.kuali.test.ui.utils.UIUtils;
import org.kuali.test.utils.Constants;
import org.kuali.test.utils.Utils;


public abstract class BaseCreateTestPanel extends BasePanel implements ActionListener {
    private static final Logger LOG = Logger.getLogger(BaseCreateTestPanel.class);
    
    private Platform platform;
    private TestHeader testHeader;
    private ToggleToolbarButton startTest;
    private ToolbarButton createCheckpoint;
    private ToolbarButton saveTest;
    
    public BaseCreateTestPanel(TestCreator mainframe, Platform platform, TestHeader testHeader) {
        super(mainframe);
        this.platform = platform;
        this.testHeader = testHeader;
    
        if (LOG.isDebugEnabled()) {
            LOG.debug("creating " + testHeader.getTestType() + " test for platform: " + platform.getName());
        }
        
        add(createToolbar(), BorderLayout.NORTH);
    }

    protected JToolBar createToolbar() {
        JToolBar retval = new JToolBar();
        retval.setFloatable(false);
        
        startTest = new ToggleToolbarButton(Constants.START_TEST_ACTION, Constants.START_TEST_ICON);
        startTest.addActionListener(this);
        retval.add(startTest);
        retval.addSeparator();
        
        createCheckpoint = new ToolbarButton(Constants.CREATE_CHECKPOINT_ACTION, Constants.CREATE_CHECKPOINT_ICON);
        createCheckpoint.addActionListener(this);
        createCheckpoint.setEnabled(false);
        retval.add(createCheckpoint);
        retval.addSeparator();
        
        saveTest = new ToolbarButton(Constants.SAVE_TEST_ACTION, Constants.SAVE_TEST_ICON);
        saveTest.addActionListener(this);
        saveTest.setEnabled(false);
        retval.add(saveTest);

        retval.add(new JLabel("        Platform: " + platform.getName()));
        retval.add(new JLabel("  Test Name: " + testHeader.getTestName()));
        
        return retval;
    }
    
    public Platform getPlatform() {
        return platform;
    }

    public TestHeader getTestHeader() {
        return testHeader;
    }

    public Insets getInsets() {
        return new Insets(3, 3, 3, 3);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals(Constants.START_TEST_ACTION)
            || e.getActionCommand().equals(Constants.CANCEL_TEST_ACTION)) {
            if (startTest.isSelected()) {
                handleStartTest();
                startTest.setText(Constants.CANCEL_TEST_ACTION);
                startTest.setIcon(Constants.CANCEL_TEST_ICON);
            } else if (UIUtils.promptForCancel(this, "Cancel Test Creation", 
                "Are you sure you want to cancel the new test creation?")) {
                handleCancelTest();
                startTest.setText(Constants.START_TEST_ACTION);
                startTest.setIcon(Constants.START_TEST_ICON);
            }
        } else if (e.getActionCommand().equals(Constants.CREATE_CHECKPOINT_ACTION)) {
            handleCreateCheckpoint();
        } else if (e.getActionCommand().equals(Constants.SAVE_TEST_ACTION)) {
            handleSaveTest();
        }
        
        createCheckpoint.setEnabled(startTest.isSelected());
        saveTest.setEnabled(startTest.isSelected());
    }

    public ToggleToolbarButton getStartTest() {
        return startTest;
    }

    public void setStartTest(ToggleToolbarButton startTest) {
        this.startTest = startTest;
    }

    public ToolbarButton getCreateCheckpoint() {
        return createCheckpoint;
    }

    public void setCreateCheckpoint(ToolbarButton createCheckpoint) {
        this.createCheckpoint = createCheckpoint;
    }

    public ToolbarButton getSaveTest() {
        return saveTest;
    }

    public void setSaveTest(ToolbarButton saveTest) {
        this.saveTest = saveTest;
    }
    
    protected boolean saveTest(String repositoryLocation, TestHeader header, List <TestOperation> testOperations) {
        boolean retval = false;
        KualiTestDocument.KualiTest test = KualiTestDocument.KualiTest.Factory.newInstance();
        header.setDateCreated(Calendar.getInstance());
        TestOperations ops = test.addNewOperations();
        if ((testOperations != null) && !testOperations.isEmpty()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("testOperations.size(): " + testOperations.size());
            }
            
            ops.setOperationArray(testOperations.toArray(new TestOperation[testOperations.size()]));
            
            File f = Utils.buildTestFile(repositoryLocation, header);
            
            if (!f.getParentFile().exists()) {
                try {
                    FileUtils.forceMkdir(f.getParentFile());
                } 
                
                catch (IOException ex) {
                    UIUtils.showError(this, "Error creating test file", "An error occured while attempting to create test file parent directory - " + ex.toString());
                    LOG.error(ex.toString(), ex);
                }
            }

            if (f.getParentFile().exists()) {
                header.setTestFileName(f.getPath());
                PlatformTests platformTests = platform.getPlatformTests();
                if (platform.getPlatformTests() == null) {
                    platformTests = platform.addNewPlatformTests();
                }
                
                KualiTestDocument doc = KualiTestDocument.Factory.newInstance();
                doc.setKualiTest(test);
                
                try {
                    doc.save(f, Utils.getSaveXmlOptions());
                    platformTests.addNewTestHeader();
                    platformTests.setTestHeaderArray(platformTests.getTestHeaderArray().length-1, testHeader);
                    retval = true;
                    
                } 
                
                catch (IOException ex) {
                    UIUtils.showError(this, "Error creating test file", "An error occured while attempting to create test file - " + ex.toString());
                    LOG.error(ex.toString(), ex);
                }
                
            }
        } else {
            JOptionPane.showMessageDialog(this, "No test operations - test will not be saved");
        }
        
        return retval;
    }

    protected void setInitialButtonState() {
        startTest.setSelected(false);
        startTest.setText(Constants.START_TEST_ACTION);
        startTest.setIcon(Constants.START_TEST_ICON);
        createCheckpoint.setEnabled(false);
        saveTest.setEnabled(false);
    }

    protected abstract void handleStartTest();
    protected abstract void handleCancelTest();
    protected abstract void handleCreateCheckpoint();
    protected abstract boolean handleSaveTest();
}
