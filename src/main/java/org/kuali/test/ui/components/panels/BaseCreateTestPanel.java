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
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.test.Checkpoint;
import org.kuali.test.CheckpointProperty;
import org.kuali.test.CommentOperation;
import org.kuali.test.Operation;
import org.kuali.test.Platform;
import org.kuali.test.TestExecutionParameter;
import org.kuali.test.TestHeader;
import org.kuali.test.TestOperation;
import org.kuali.test.TestOperationType;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.ui.base.BasePanel;
import org.kuali.test.ui.components.dialogs.TestCheckpointsDlg;
import org.kuali.test.ui.components.dialogs.TestCommentsDlg;
import org.kuali.test.ui.components.dialogs.TestExecutionParametersDlg;
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
    private String testDescription;
    private JMenuItem cancelTest;
    private JMenuItem saveTest;
    private JMenuItem createCheckpoint;
    private JMenuItem createComment;
    private JMenuItem createParameter;
    private JMenuItem viewCheckpoints;
    private JMenuItem viewComments;
    private JMenuItem viewParameters;
    
    /**
     * 
     * @param mainframe
     * @param platform
     * @param testHeader
     * @param testDescription 
     */
    public BaseCreateTestPanel(TestCreator mainframe, Platform platform, TestHeader testHeader, String testDescription) {
        super(mainframe);
        this.platform = platform;
        this.testHeader = testHeader;
        this.testDescription = testDescription;
    
        if (LOG.isDebugEnabled()) {
            LOG.debug("creating " + testHeader.getTestType() + " test for platform: " + platform.getName());
        }
    }

    protected void initComponents() {
        if (!isForCheckpoint()) {
            add(createOperationPanel(), BorderLayout.NORTH);
            handleStartTest();
            setMenuState(true);
        }
    }

    /**
     *
     * @return
     */
    protected JMenuBar createOperationPanel() {
        JMenuBar retval = new JMenuBar();
        
        JMenu menu = new JMenu(Constants.OPERATION_ACTION) {

            @Override
            public Insets getInsets() {
                return new Insets(1, 5, 1, 200);
            }
            
        };
        
        menu.setMnemonic('o');
        
        cancelTest = new JMenuItem(Constants.CANCEL_TEST_ACTION);
        cancelTest.setMnemonic('c');
        
        saveTest = new JMenuItem(Constants.SAVE_TEST_ACTION);
        
        createCheckpoint = new JMenuItem(Constants.CREATE_CHECKPOINT_ACTION);
        createComment = new JMenuItem(Constants.CREATE_COMMENT_ACTION);
        createParameter = new JMenuItem(Constants.CREATE_PARAMETER_ACTION);
        
        viewCheckpoints = new JMenuItem(Constants.VIEW_CHECKPOINTS_ACTION);
        viewComments = new JMenuItem(Constants.VIEW_COMMENTS_ACTION);
        viewParameters = new JMenuItem(Constants.VIEW_PARAMETERS_ACTION);

        menu.add(cancelTest);
        menu.add(saveTest);
        
        menu.addSeparator();
        
        menu.add(createCheckpoint);
        menu.add(createComment);
        if (isParameterOperationRequired()) {
            menu.add(createParameter);
        }
        
        menu.addSeparator();
        
        menu.add(viewCheckpoints);
        menu.add(viewComments);
        if (isParameterOperationRequired()) {
            menu.add(viewParameters);
        }
        
        cancelTest.addActionListener(this);
        saveTest.addActionListener(this);
        createCheckpoint.addActionListener(this);
        createComment.addActionListener(this);
        if (isParameterOperationRequired()) {
            createParameter.addActionListener(this);
        }
        viewCheckpoints.addActionListener(this);
        viewComments.addActionListener(this);
        if (isParameterOperationRequired()) {
            viewParameters.addActionListener(this);
        }

        
        retval.add(menu);
        
        StringBuilder txt = new StringBuilder(128);
        
        txt.append("<html><span style='font-weight: 700;'>Platform: </span><span style='color: ");
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
        if (e.getActionCommand().equals(Constants.CANCEL_TEST_ACTION)) {
            if (UIUtils.promptForCancel(this, "Cancel Test Creation", 
                "Cancel test '" + testHeader.getTestName() + "'?")) {
                handleCancelTest();
                setMenuState(false);
            }
        } else if (e.getActionCommand().equals(Constants.CREATE_CHECKPOINT_ACTION)) {
            getMainframe().startSpinner("Loading available checkpoint parameters...");
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    handleCreateCheckpoint();
                }
            });
        } else if (e.getActionCommand().equals(Constants.VIEW_CHECKPOINTS_ACTION)) {
            handleViewCheckpoints();
        } else if (e.getActionCommand().startsWith(Constants.CREATE_PARAMETER_ACTION)) {
            getMainframe().startSpinner("Loading available test execution parameters...");

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    handleCreateParameter();
                };
            });
        } else if (e.getActionCommand().equals(Constants.VIEW_PARAMETERS_ACTION)) {
            handleViewParameters();
        } else if (e.getActionCommand().equals(Constants.CREATE_COMMENT_ACTION)) {
            handleCreateComment();
        } else if (e.getActionCommand().equals(Constants.VIEW_COMMENTS_ACTION)) {
            handleViewComments();
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
    public JMenuItem getSaveTest() {
        return saveTest;
    }

    /**
     *
     * @param repositoryLocation
     * @param header
     * @param testOperations
     * @return
     */
    protected boolean saveTest(String repositoryLocation, TestHeader header, List <TestOperation> testOperations) {
        return Utils.saveKualiTest(this, repositoryLocation, platform, header, testOperations, testDescription);
    }

    protected void setInitialButtonState() {
        saveTest.setEnabled(false);
    }

    /**
     *
     * @return
     */
    protected boolean isStartTestRequired() { return false; }

    /**
     *
     * @return
     */
    protected boolean isParameterOperationRequired() { return false; }

    protected abstract void handleStartTest();
    protected abstract void handleCancelTest();
    protected abstract void handleCreateCheckpoint();
    protected abstract void handleCreateComment();
    protected abstract List <Checkpoint> getCheckpoints();
    protected abstract List <String> getComments();

    protected List <TestExecutionParameter> getParameters() {
        return null;
    }

    protected void handleCreateParameter() {}
    protected void handleViewParameters() {
        new TestExecutionParametersDlg(getMainframe(), getParameters());
    }

    protected void handleViewCheckpoints() {
        new TestCheckpointsDlg(getMainframe(), getCheckpoints());
    };

    protected void handleViewComments() {
        new TestCommentsDlg(getMainframe(), getComments());
    };
    
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
    
    protected void addCheckpoint(List<TestOperation> testOperations, Checkpoint checkpoint, String comment) {
        if (StringUtils.isNotBlank(comment)) {
            addComment(testOperations, comment);
        }
        
        TestOperation testOp = TestOperation.Factory.newInstance();
        testOp.setOperationType(TestOperationType.CHECKPOINT);
        Operation op = testOp.addNewOperation();
        op.addNewCheckpointOperation();

        for (CheckpointProperty p : checkpoint.getCheckpointProperties().getCheckpointPropertyArray()) {
            if (StringUtils.isNotBlank(p.getPropertySection())) {
                p.setPropertySection(Utils.formatHtmlForComparisonProperty(p.getPropertySection()));
            }
        }
        
        op.setCheckpointOperation(checkpoint);
        testOperations.add(testOp);
    }
    
    protected void addComment(List<TestOperation> testOperations, String comment) {
        TestOperation testOp = TestOperation.Factory.newInstance();
        testOp.setOperationType(TestOperationType.COMMENT);
        Operation op = testOp.addNewOperation();
        CommentOperation cop = op.addNewCommentOperation();
        cop.setComment(comment);
        testOperations.add(testOp);
    }
    
    protected abstract boolean isForCheckpoint();
    
    private void setMenuState(boolean enable) {
        cancelTest.setEnabled(enable);
        saveTest.setEnabled(enable);
        createCheckpoint.setEnabled(enable);
        createComment.setEnabled(enable);
        createParameter.setEnabled(enable);
        viewCheckpoints.setEnabled(enable);
        viewComments.setEnabled(enable);
        viewParameters.setEnabled(enable);
    }
}
