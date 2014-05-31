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
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;
import org.apache.log4j.Logger;
import org.kuali.test.Checkpoint;
import org.kuali.test.Operation;
import org.kuali.test.Platform;
import org.kuali.test.TestHeader;
import org.kuali.test.TestOperation;
import org.kuali.test.TestOperationType;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.ui.components.buttons.FileSearchButton;
import org.kuali.test.ui.utils.UIUtils;
import org.kuali.test.utils.Constants;

public class FileTestPanel extends BaseCreateTestPanel {

    private static final Logger LOG = Logger.getLogger(FileTestPanel.class);
    private List<TestOperation> testOperations = new ArrayList<TestOperation>();
    private JTextField fileDirectory;
    private JTextField fileNamePattern;

    private boolean forCheckpoint;

    public FileTestPanel(TestCreator mainframe, Platform platform, TestHeader testHeader) {
        this(mainframe, platform, testHeader, false);
    }

    public FileTestPanel(TestCreator mainframe, Platform platform, TestHeader testHeader, boolean forCheckpoint) {
        super(mainframe, platform, testHeader);
        this.forCheckpoint = forCheckpoint;
        initComponents();
    }

    private void initComponents() {
        String[] labels = new String[]{
            "File Directory",
            "File Name Pattern",};

        fileDirectory = new JTextField(30);
        fileNamePattern = new JTextField(20);

        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        p.add(fileDirectory);

        JButton b = new FileSearchButton();
        b.addActionListener(this);
        p.add(b);
        
        JComponent[] components = new JComponent[]{
            p,
            fileNamePattern
        };

        p = new JPanel(new BorderLayout(3, 3));
        p.add(UIUtils.buildEntryPanel(labels, components), BorderLayout.NORTH);
        add(p, BorderLayout.CENTER);
    }

    @Override
    protected void handleCancelTest() {
        getMainframe().getCreateTestPanel().clearPanel("test '" + getTestHeader().getTestName() + "' cancelled");
        getMainframe().getCreateTestButton().setEnabled(true);
        testOperations.clear();
    }

    @Override
    protected void handleCreateCheckpoint() {
        if (isValidFileSetup()) {
        }
    }

    public boolean isValidFileSetup() {
        boolean retval = false;
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
            JFileChooser chooser = new JFileChooser();
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
                System.out.println("You chose to open this file: "
                    + chooser.getSelectedFile().getName());
            }

        }
    }

    /*
    public String getFailureAction() {
        return (String) failureAction.getSelectedItem();
    }
    */

    public boolean isForCheckpoint() {
        return forCheckpoint;
    }

    @Override
    protected void handleStartTest() {
    }
}
