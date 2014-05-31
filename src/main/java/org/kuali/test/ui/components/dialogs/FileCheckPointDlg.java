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
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.test.Checkpoint;
import org.kuali.test.CheckpointType;
import org.kuali.test.FailureAction;
import org.kuali.test.TestHeader;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.ui.base.BasePanel;
import org.kuali.test.ui.base.BaseSetupDlg;
import org.kuali.test.ui.components.panels.FileTestPanel;
import org.kuali.test.ui.utils.UIUtils;
import org.kuali.test.utils.Utils;

/**
 *
 * @author rbtucker
 */
public class FileCheckPointDlg extends BaseSetupDlg {

    private static final Logger LOG = Logger.getLogger(FileCheckPointDlg.class);
    private final TestHeader testHeader;
    private Checkpoint checkpoint;
    private JTextField name;
    private JComboBox fileFailure;
    private FileTestPanel filePanel;

    public FileCheckPointDlg(TestCreator mainFrame, TestHeader testHeader, FileTestPanel filePanel) {
        super(mainFrame);
        this.testHeader = testHeader;
        this.filePanel = filePanel;

        if (checkpoint != null) {
            setTitle("Edit checkpoint " + checkpoint.getName());
            setEditmode(true);
        } else {
            setTitle("Add new checkpoint");
            this.checkpoint = Checkpoint.Factory.newInstance();
            this.checkpoint.setName("new checkpoint");
            this.checkpoint.setTestName(testHeader.getTestName());
            this.checkpoint.setType(CheckpointType.FILE);
        }

        initComponents();
    }

    private void initComponents() {
        if (filePanel == null) {
            filePanel = new FileTestPanel(getMainframe(), Utils.findPlatform(getMainframe().getConfiguration(), 
                testHeader.getPlatformName()), testHeader, true);
            getContentPane().add(filePanel, BorderLayout.CENTER);
        } else {
            getContentPane().add(getCheckpointPanel(), BorderLayout.CENTER);
        }

        addStandardButtons();
        setDefaultBehavior();
    }

    private JPanel getCheckpointPanel() {
        BasePanel retval = new BasePanel(getMainframe());

        String[] labels = new String[]{
            "Checkpoint Name",
            "On Failure"
        };

        name = new JTextField(checkpoint.getName(), 20);
        name.setEditable(!isEditmode());
        fileFailure = new JComboBox(Utils.getXmlEnumerations(FailureAction.class));
        
        JComponent[] components = new JComponent[]{
            name,
            fileFailure,
        };

        retval.add(UIUtils.buildEntryPanel(labels, components), BorderLayout.CENTER);

        return retval;
    }
    
    @Override
    protected boolean save() {
        boolean retval = false;
        boolean oktosave = true;
        if (StringUtils.isNotBlank(name.getText())) {
            if (!isEditmode()) {
                if (checkpointNameExists()) {
                    oktosave = false;
                    displayExistingNameAlert("Checkpoint", name.getText());
                }
            }
        } else {
            displayRequiredFieldsMissingAlert("Checkpoint", "name");
            oktosave = false;
        }

        if (oktosave) {
            if (!isEditmode()) {
            }

            checkpoint.setName(name.getText());
            checkpoint.addNewInputParameters();
/*
            Parameter param = checkpoint.getInputParameters().addNewParameter();
            param.setName("sql-query");
            param.setValue(dbPanel.getSqlQueryString(DatabasePanel.SQL_FORMAT_EXECUTE));
            
            param = checkpoint.getInputParameters().addNewParameter();
            param.setName("save-query-results");
            param.setValue("" + saveQueryResults.isSelected());

            CheckpointProperty cp = checkpoint.addNewCheckpointProperties().addNewCheckpointProperty();
            cp.setValueType(ValueType.INT);
            cp.setPropertyGroup(Constants.DATABASE_PROPERTY_GROUP);
            cp.setOnFailure(FailureAction.Enum.forString(sqlFailure.getSelectedItem().toString()));
            String s = checkpointProperty.getSelectedItem().toString();
            
            cp.setDisplayName("Row Count");
            cp.setPropertyName("row-count");
            
            if (Constants.SINGLE_ROW_EXISTS.equals(s)) {
                cp.setOperator(ComparisonOperator.EQUAL_TO);
                cp.setPropertyValue("1");
            } else {
                if (Constants.MULTIPLE_ROWS_EXIST.equals(s)) {
                    cp.setOperator(ComparisonOperator.GREATER_THAN);
                } else {
                    cp.setOperator(ComparisonOperator.EQUAL_TO);
                }
                cp.setPropertyValue("0");
            } 
                
            cp = checkpoint.addNewCheckpointProperties().addNewCheckpointProperty();
            cp.setValueType(ValueType.INT);
            cp.setPropertyGroup(Constants.DATABASE_PROPERTY_GROUP);
            cp.setOnFailure(FailureAction.Enum.forString(fileFailure.getSelectedItem().toString()));
  */          
            getConfiguration().setModified(true);
            setSaved(true);
            dispose();
            retval = true;
        }

        return retval;
    }

    private boolean checkpointNameExists() {
        boolean retval = false;
        String newname = name.getText();
        return retval;
    }

    @Override
    public Object getNewRepositoryObject() {
        return checkpoint;
    }

    @Override
    public Dimension getPreferredSize() {
        if (!filePanel.isForCheckpoint()) {
            return new Dimension(500, 300);
        } else {
            return new Dimension(800, 600);
        }
    }

    @Override
    protected String getDialogName() {
        if (!filePanel.isForCheckpoint()) {
            return "file-checkpoint-entry";
        } else {
            return "file-checkpoint-entry2";
        }
    }
}
