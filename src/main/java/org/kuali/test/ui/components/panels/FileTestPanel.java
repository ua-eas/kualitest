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
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.filechooser.FileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.test.Checkpoint;
import org.kuali.test.Operation;
import org.kuali.test.Platform;
import org.kuali.test.TestHeader;
import org.kuali.test.TestOperation;
import org.kuali.test.TestOperationType;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.ui.components.buttons.FileSearchButton;
import org.kuali.test.ui.components.dialogs.FileCheckPointDlg;
import org.kuali.test.ui.utils.UIUtils;
import org.kuali.test.utils.Constants;

public class FileTestPanel extends BaseCreateTestPanel {

    private static final Logger LOG = Logger.getLogger(FileTestPanel.class);
    private List<TestOperation> testOperations = new ArrayList<TestOperation>();
    private JTextField fileDirectory;
    private JTextField fileNamePattern;
    private JTextField containingText;
    private List <JCheckBox> fileComparisons;

    private boolean forCheckpoint;

    public FileTestPanel(TestCreator mainframe, Platform platform, TestHeader testHeader) {
        this(mainframe, platform, testHeader, false);
    }

    public FileTestPanel(TestCreator mainframe, Platform platform, TestHeader testHeader, boolean forCheckpoint) {
        super(mainframe, platform, testHeader);
        this.forCheckpoint = forCheckpoint;
        initComponents();
    }

    @Override
    protected JToolBar createToolbar() {
        if (forCheckpoint) {
            return null;
        } else {
            return super.createToolbar();
        }
    }

    protected void initComponents() {
        super.initComponents();
        
        List<String> labels = new ArrayList<String>();
        labels.add("File Directory");
        labels.add("File Name Pattern");
        
        for (int i = 0; i < Constants.FILE_CHECK_CONDITIONS.length; ++i) {
            labels.add("");
        }
        
        labels.add("Contains Text");

        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        p.add(fileDirectory = new JTextField(30));
        JButton b = new FileSearchButton();
        b.addActionListener(this);
        p.add(b);
        
        List <JComponent> components = new ArrayList<JComponent>();

        components.add(p);
        components.add(fileNamePattern = new JTextField(20));

        fileComparisons = new ArrayList<JCheckBox>();;
        for (int i = 0; i < Constants.FILE_CHECK_CONDITIONS.length; ++i) {
            JCheckBox cb = new JCheckBox(Constants.FILE_CHECK_CONDITIONS[i]);
            cb.addActionListener(this);
            components.add(cb);
            fileComparisons.add(cb);
        }
        
        components.add(containingText = new JTextField(40));

        p = new JPanel(new BorderLayout(3, 3));
        p.add(UIUtils.buildEntryPanel(labels.toArray(new String[labels.size()]), 
            components.toArray(new JComponent[components.size()])), BorderLayout.NORTH);
        add(p, BorderLayout.CENTER);
        
        if (!forCheckpoint) {
            getCreateCheckpoint().setEnabled(true);
        }
    }

    @Override
    protected void handleCancelTest() {
        getMainframe().getCreateTestPanel().clearPanel("test '" + getTestHeader().getTestName() + "' cancelled");
        getMainframe().getCreateTestButton().setEnabled(true);
        getMainframe().getCreateTestMenuItem().setEnabled(true);
        testOperations.clear();
    }

    @Override
    protected void handleCreateCheckpoint() {
        if (isValidFileSetup()) {
            FileCheckPointDlg dlg = new FileCheckPointDlg(getMainframe(), getTestHeader(), this);
            
            if (dlg.isSaved()) {
                addCheckpoint((Checkpoint)dlg.getNewRepositoryObject());
                getSaveTest().setEnabled(true);
            }
        }
    }

    public boolean isValidFileSetup() {
        boolean retval = false;
        File f = new File(fileDirectory.getText());
        if (f.exists() && f.isDirectory()) {
            if (StringUtils.isNotBlank(fileNamePattern.getText())) {
                retval = isFileComparisonSelected();
            } 
        }
        
        if (!retval && StringUtils.isBlank(containingText.getText())) {
            UIUtils.showError(this, "Missing Required Entries", 
                "Directory, file name pattern and at least one file comparison selection required.");
        }

        
        return retval;
    }

    private boolean isFileComparisonSelected() {
        boolean retval = false;
        for (JCheckBox cb : fileComparisons) {
            if (cb.isSelected()) {
                retval = true;
                break;
            }
        }
        
        return retval;
    }
    
    private void addCheckpoint(Checkpoint checkpoint) {
        TestOperation testOp = TestOperation.Factory.newInstance();
        testOp.setOperationType(TestOperationType.CHECKPOINT);
        Operation op = testOp.addNewOperation();
        op.addNewCheckpointOperation();
        op.setCheckpointOperation(checkpoint);
        testOperations.add(testOp);
    }

    @Override
    protected boolean handleSaveTest() {
        boolean retval = saveTest(getMainframe().getConfiguration().getRepositoryLocation(),
            getTestHeader(), testOperations);

        if (retval) {
            getMainframe().getTestRepositoryTree().saveConfiguration();
            getMainframe().getCreateTestPanel().clearPanel("test '" + getTestHeader().getTestName() + "' created");
        }

        return retval;
    }

    @Override
    protected void handleUnprocessedActionEvent(ActionEvent e) {
        if (Constants.FILE_SEARCH_ACTION.equals(e.getActionCommand())) {
            Preferences proot = Preferences.userRoot();
            Preferences node = proot.node(Constants.PREFS_FILES_NODE);
      
            String lastDir = node.get(Constants.PREFS_LAST_FILE_TEST_DIR, System.getProperty("user.home") );
            
            JFileChooser chooser = new JFileChooser(lastDir);
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            
            chooser.setFileFilter(new FileFilter() {
                @Override
                public boolean accept(File f) {
                    return (f.isDirectory() && f.exists());
                }

                @Override
                public String getDescription() {
                    return "file inquiry directory";
                }
            });
            
            int returnVal = chooser.showOpenDialog(getMainframe());
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File f = chooser.getSelectedFile();
                fileDirectory.setText(f.getPath());
                node.put(Constants.PREFS_LAST_FILE_TEST_DIR, f.getPath());
            }
        } else if (Constants.FILE_EXISTS.equals(e.getActionCommand())) {
            getFileCheckCondition(Constants.FILE_DOES_NOT_EXIST).setSelected(false);
            getFileCheckCondition(Constants.FILE_SIZE_GREATER_THAN_ZERO).setEnabled(true);
            getFileCheckCondition(Constants.FILE_CREATED_TODAY).setEnabled(true);
            getFileCheckCondition(Constants.FILE_CREATED_YESTERDAY).setEnabled(true);
            containingText.setEnabled(true);
        } else if (Constants.FILE_DOES_NOT_EXIST.equals(e.getActionCommand())) {
            getFileCheckCondition(Constants.FILE_EXISTS).setSelected(false);
            getFileCheckCondition(Constants.FILE_SIZE_GREATER_THAN_ZERO).setSelected(false);
            getFileCheckCondition(Constants.FILE_SIZE_GREATER_THAN_ZERO).setEnabled(false);
            getFileCheckCondition(Constants.FILE_CREATED_TODAY).setSelected(false);
            getFileCheckCondition(Constants.FILE_CREATED_TODAY).setEnabled(false);
            getFileCheckCondition(Constants.FILE_CREATED_YESTERDAY).setSelected(false);
            getFileCheckCondition(Constants.FILE_CREATED_YESTERDAY).setEnabled(false);
            containingText.setText("");
            containingText.setEnabled(false);
        } else if (Constants.FILE_CREATED_TODAY.equals(e.getActionCommand())) {
            getFileCheckCondition(Constants.FILE_CREATED_YESTERDAY).setSelected(false);
        } else if (Constants.FILE_CREATED_YESTERDAY.equals(e.getActionCommand())) {
            getFileCheckCondition(Constants.FILE_CREATED_TODAY).setSelected(false);
        }
    }

    /*
    public String getFailureAction() {
        return (String) failureAction.getSelectedItem();
    }
    */

    private JCheckBox getFileCheckCondition(String actionCommand) {
        JCheckBox retval = null;
        
        for (JCheckBox cb : fileComparisons) {
            if (actionCommand.equals(cb.getActionCommand())) {
                retval = cb;
                break;
            }
        }
        
        return retval;
    }
    
    public boolean isForCheckpoint() {
        return forCheckpoint;
    }

    @Override
    protected void handleStartTest() {
    }
    
    public String getFileDirectory() {
        return fileDirectory.getText();
    }

    public String getFileNamePattern() {
        return fileNamePattern.getText();
    }
    
    public String getContainingText() {
        return containingText.getText();
    }
    
    public List <String> getSelectedFileComparisons() {
        List <String> retval = new ArrayList<String>();
        
        for (JCheckBox cb : fileComparisons) {
            if (cb.isSelected()) {
                retval.add(cb.getActionCommand());
            }
        }
        
        return retval;
    }
}
