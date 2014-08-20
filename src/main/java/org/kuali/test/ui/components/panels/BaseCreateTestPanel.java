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
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.kuali.test.Checkpoint;
import org.kuali.test.KualiTestDocument;
import org.kuali.test.Platform;
import org.kuali.test.PlatformTests;
import org.kuali.test.TestHeader;
import org.kuali.test.TestOperation;
import org.kuali.test.TestOperations;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.ui.base.BasePanel;
import org.kuali.test.ui.components.buttons.ExtraInfoToolbarButton;
import org.kuali.test.ui.components.buttons.ToggleToolbarButton;
import org.kuali.test.ui.components.buttons.ToolbarButton;
import org.kuali.test.ui.components.dialogs.TestCheckpointsDlg;
import org.kuali.test.ui.utils.UIUtils;
import org.kuali.test.utils.Constants;
import org.kuali.test.utils.Utils;

/**
 *
 * @author rbtucker
 */
public abstract class BaseCreateTestPanel extends BasePanel implements ActionListener {
    private static final Logger LOG = Logger.getLogger(BaseCreateTestPanel.class);
    
    private final Platform platform;
    private final TestHeader testHeader;
    private ToggleToolbarButton startTest;
    private ExtraInfoToolbarButton createCheckpoint;
    private ToolbarButton saveTest;
    
    /**
     *
     * @param mainframe
     * @param platform
     * @param testHeader
     */
    public BaseCreateTestPanel(TestCreator mainframe, Platform platform, TestHeader testHeader) {
        super(mainframe);
        this.platform = platform;
        this.testHeader = testHeader;
    
        if (LOG.isDebugEnabled()) {
            LOG.debug("creating " + testHeader.getTestType() + " test for platform: " + platform.getName());
        }
    }

    /**
     *
     */
    protected void initComponents() {
        JToolBar tb = createToolbar();
        
        if (tb != null) {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            p.add(tb);
            add(p, BorderLayout.NORTH);
        }
    }
    
    /**
     *
     * @return
     */
    protected JToolBar createToolbar() {
        JToolBar retval = new JToolBar();
        retval.setFloatable(false);
        
        if (isStartTestRequired()) {
            startTest = new ToggleToolbarButton(Constants.START_TEST_ACTION, Constants.START_TEST_ICON);
            startTest.addActionListener(this);
            retval.add(startTest);
            retval.addSeparator();
        } else {
            ToolbarButton tb = new ToolbarButton(Constants.CANCEL_TEST_ACTION, Constants.CANCEL_TEST_ICON);
            tb.addActionListener(this);
            retval.add(tb);
            retval.addSeparator();
        }
        
        createCheckpoint = new ExtraInfoToolbarButton(Constants.CREATE_CHECKPOINT_ACTION, Constants.CREATE_CHECKPOINT_ICON) {
            @Override
            public void showExtraInfo() {
                new TestCheckpointsDlg(getMainframe(), getCheckpoints());
            }
        };
        
        createCheckpoint.addActionListener(this);
        createCheckpoint.setEnabled(false);
        retval.add(createCheckpoint);
        retval.addSeparator();
        
        List <JComponent> customButtons = getCustomButtons();

        if (customButtons != null) {
            for (JComponent tb : customButtons) {
                retval.add(tb);
                retval.addSeparator();
            }
        } else {
            retval.addSeparator();
        }
        
        saveTest = new ToolbarButton(Constants.SAVE_TEST_ACTION, Constants.SAVE_TEST_ICON);
        saveTest.addActionListener(this);
        saveTest.setEnabled(false);
        retval.add(saveTest);

        StringBuilder txt = new StringBuilder(128);
        
        txt.append("<html><span style='font-weight: 700;'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Platform: </span><span style='color: ");
        txt.append(Constants.COLOR_DARK_BLUE);
        txt.append(";  font-weight: normal;'>");
        txt.append(platform.getName());
        txt.append("</span><span style='font-weight: 700; padding-left: 30px;'>, Test Type: </span><span style='color: ");
        txt.append(Constants.COLOR_DARK_BLUE);
        txt.append(";  font-weight: normal;'>");
        txt.append(testHeader.getTestType().toString());
        txt.append("</span><span style='font-weight: 700; padding-left: 30px;'>, Test Name: </span><span style='color: ");
        txt.append(Constants.COLOR_DARK_BLUE);
        txt.append("; font-weight: normal;'>");
        txt.append(testHeader.getTestName());
        txt.append("</span></html>");
        
        retval.add(new JLabel(txt.toString()));
        
        return retval;
    }
    
    /**
     *
     * @return
     */
    protected List <JComponent> getCustomButtons() {
        return null;
    }
    
    protected abstract List <Checkpoint> getCheckpoints();
    
    /**
     *
     * @return
     */
    public Platform getPlatform() {
        return platform;
    }

    /**
     *
     * @return
     */
    public TestHeader getTestHeader() {
        return testHeader;
    }

    @Override
    public Insets getInsets() {
        return new Insets(3, 3, 3, 3);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals(Constants.START_TEST_ACTION)) {
            handleStartTest();
            startTest.setText(Constants.CANCEL_TEST_ACTION);
            startTest.setIcon(Constants.CANCEL_TEST_ICON);
            startTest.setActionCommand(Constants.CANCEL_TEST_ACTION);
            createCheckpoint.setEnabled(true);
            saveTest.setEnabled(true);
        } else if (e.getActionCommand().equals(Constants.CANCEL_TEST_ACTION)) {
            if (UIUtils.promptForCancel(this, "Cancel Test Creation", 
                "Cancel test '" + testHeader.getTestName() + "'?")) {
                handleCancelTest();
                if (startTest != null) {
                    startTest.setText(Constants.START_TEST_ACTION);
                    startTest.setIcon(Constants.START_TEST_ICON);
                    startTest.setActionCommand(Constants.START_TEST_ACTION);
                    createCheckpoint.setEnabled(false);
                    saveTest.setEnabled(false);
                }
            }
        } else if (e.getActionCommand().equals(Constants.CREATE_CHECKPOINT_ACTION)) {
            handleCreateCheckpoint();
        } else if (e.getActionCommand().equals(Constants.SAVE_TEST_ACTION)) {
            if (handleSaveTest()) {
                getMainframe().getPlatformTestsPanel().populateList(platform);
            }
        } else {
            handleUnprocessedActionEvent(e);
        }
    }

    /**
     *
     * @return
     */
    public ToggleToolbarButton getStartTest() {
        return startTest;
    }

    /**
     *
     * @param startTest
     */
    public void setStartTest(ToggleToolbarButton startTest) {
        this.startTest = startTest;
    }

    /**
     *
     * @return
     */
    public ExtraInfoToolbarButton getCreateCheckpoint() {
        return createCheckpoint;
    }

    /**
     *
     * @param createCheckpoint
     */
    public void setCreateCheckpoint(ExtraInfoToolbarButton createCheckpoint) {
        this.createCheckpoint = createCheckpoint;
    }

    /**
     *
     * @return
     */
    public ToolbarButton getSaveTest() {
        return saveTest;
    }

    /**
     *
     * @param saveTest
     */
    public void setSaveTest(ToolbarButton saveTest) {
        this.saveTest = saveTest;
    }
    
    /**
     *
     * @param repositoryLocation
     * @param header
     * @param testOperations
     * @return
     */
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

    /**
     *
     */
    protected void setInitialButtonState() {
        startTest.setSelected(false);
        startTest.setText(Constants.START_TEST_ACTION);
        startTest.setIcon(Constants.START_TEST_ICON);
        createCheckpoint.setEnabled(false);
        saveTest.setEnabled(false);
    }

    /**
     *
     * @return
     */
    protected boolean isStartTestRequired() { return false; }

    /**
     *
     */
    protected abstract void handleStartTest();

    /**
     *
     */
    protected abstract void handleCancelTest();

    /**
     *
     */
    protected abstract void handleCreateCheckpoint();

    /**
     *
     * @return
     */
    protected abstract boolean handleSaveTest();

    /**
     *
     * @param e
     */
    protected void handleUnprocessedActionEvent(ActionEvent e) {};
}
