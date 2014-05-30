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

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import org.apache.log4j.Logger;
import org.apache.ws.commons.schema.XmlSchema;
import org.kuali.test.Checkpoint;
import org.kuali.test.Operation;
import org.kuali.test.Platform;
import org.kuali.test.TestHeader;
import org.kuali.test.TestOperation;
import org.kuali.test.TestOperationType;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.ui.base.BaseTable;


public class FileTestPanel extends BaseCreateTestPanel {
    private static final Logger LOG = Logger.getLogger(FileTestPanel.class);
    private List <TestOperation> testOperations = new ArrayList<TestOperation>();
    private BaseTable inputParameters;
    private JComboBox operations;
    private XmlSchema xmlSchema;
    private JLabel returnType;
    private JTextField expectedResult;
    private JComboBox failureAction;
    private boolean forCheckpoint;
    
    public FileTestPanel(TestCreator mainframe, Platform platform, TestHeader testHeader) {
        this (mainframe, platform, testHeader, false);
    }
    
    public FileTestPanel(TestCreator mainframe, Platform platform, TestHeader testHeader, boolean forCheckpoint) {
        super(mainframe, platform, testHeader);
        this.forCheckpoint = forCheckpoint;
        initComponents();
    }

    private void initComponents() {
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
    }

    public String getFailureAction() {
        return (String)failureAction.getSelectedItem();
    }
    
    public boolean isForCheckpoint() {
        return forCheckpoint;
    }

    @Override
    protected void handleStartTest() {
    }
}
