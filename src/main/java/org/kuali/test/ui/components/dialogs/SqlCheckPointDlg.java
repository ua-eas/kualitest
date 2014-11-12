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
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.test.Checkpoint;
import org.kuali.test.CheckpointProperty;
import org.kuali.test.CheckpointType;
import org.kuali.test.ComparisonOperator;
import org.kuali.test.FailureAction;
import org.kuali.test.Parameter;
import org.kuali.test.TestHeader;
import org.kuali.test.ValueType;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.proxyserver.TestProxyServer;
import org.kuali.test.ui.base.BasePanel;
import org.kuali.test.ui.components.editmasks.IntegerTextField;
import org.kuali.test.ui.components.sqlquerypanel.DatabasePanel;
import org.kuali.test.ui.utils.UIUtils;
import org.kuali.test.utils.Constants;
import org.kuali.test.utils.Utils;

/**
 *
 * @author rbtucker
 */
public class SqlCheckPointDlg extends BaseCheckpointDlg {
    private static final Logger LOG = Logger.getLogger(SqlCheckPointDlg.class);
    private final TestHeader testHeader;
    private Checkpoint checkpoint;
    private JTextField name;
    private JComboBox checkpointProperty;
    private JComboBox sqlFailure;
    private JCheckBox saveQueryResults;
    private IntegerTextField maxRunTime;
    private DatabasePanel dbPanel;
    private TestProxyServer testProxyServer;

    /**
     * 
     * @param mainFrame
     * @param testHeader
     * @param dbPanel
     * @param testProxyServer 
     */
    public SqlCheckPointDlg(TestCreator mainFrame, TestHeader testHeader, 
        DatabasePanel dbPanel, TestProxyServer testProxyServer) {
        super(mainFrame);
        this.testHeader = testHeader;
        this.dbPanel = dbPanel;
        this.testProxyServer = testProxyServer;

        if (checkpoint != null) {
            setTitle("Edit checkpoint " + checkpoint.getName());
            setEditmode(true);
        } else {
            setTitle("Add new checkpoint");
            this.checkpoint = Checkpoint.Factory.newInstance();
            this.checkpoint.setName("sql checkpoint");
            this.checkpoint.setTestName(testHeader.getTestName());
            this.checkpoint.setType(CheckpointType.SQL);
        }

        initComponents();
    }

    private void initComponents() {
        if (dbPanel == null) {
            dbPanel = new DatabasePanel(getMainframe(), Utils.findPlatform(getMainframe().getConfiguration(), 
                testHeader.getPlatformName()), testHeader, null, true, testProxyServer);
            dbPanel.addTab("Checkpoint Configuration", getCheckpointPanel());
            getContentPane().add(dbPanel, BorderLayout.CENTER);
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
            "Comment",
            "Checkpoint Property",
            "On Failure",
            "Max Run Time (sec)",
            ""
        };

        name = new JTextField(checkpoint.getName(), 20);
        name.setEditable(!isEditmode());
        checkpointProperty = new JComboBox(Constants.SQL_CHECKPOINT_PROPERTIES);
        sqlFailure = new JComboBox(Utils.getXmlEnumerations(FailureAction.class));
        saveQueryResults = new JCheckBox("Save SQL Query Results");
        maxRunTime = new IntegerTextField();
        
        JComponent[] components = new JComponent[]{
            name,
            createCommentField(),
            checkpointProperty,
            sqlFailure,
            maxRunTime,
            saveQueryResults
        };

        retval.add(UIUtils.buildEntryPanel(labels, components), BorderLayout.CENTER);

        return retval;
    }
    
    /**
     *
     * @return
     */
    @Override
    protected boolean save() {
        boolean retval = false;
        boolean oktosave = true;
        if (StringUtils.isNotBlank(name.getText())
            && !dbPanel.getSelectedDbObjects().isEmpty()
            && dbPanel.getSqlSelectPanel().haveEntries()
            && dbPanel.getSqlWherePanel().haveEntries()) {
            if (!isEditmode()) {
                if (checkpointNameExists()) {
                    oktosave = false;
                    displayExistingNameAlert("Checkpoint", name.getText());
                }
            }
        } else {
            displayRequiredFieldsMissingAlert("SQL Checkpoint", "chackpoint name, sql select columns, sql where columns");
            oktosave = false;
        }

        if (oktosave) {
            checkpoint.setName(name.getText());
            checkpoint.addNewInputParameters();

            Parameter param = checkpoint.getInputParameters().addNewParameter();
            param.setName(Constants.SQL_QUERY);
            param.setValue(dbPanel.getSqlQueryString(DatabasePanel.SQL_FORMAT_EXECUTE));
            
            param = checkpoint.getInputParameters().addNewParameter();
            param.setName(Constants.SAVE_QUERY_RESULTS_DIR);
            param.setValue("" + saveQueryResults.isSelected());

            CheckpointProperty cp = checkpoint.addNewCheckpointProperties().addNewCheckpointProperty();
            cp.setValueType(ValueType.INT);
            cp.setPropertyGroup(Constants.DATABASE_PROPERTY_GROUP);
            cp.setOnFailure(FailureAction.Enum.forString(sqlFailure.getSelectedItem().toString()));
            String s = checkpointProperty.getSelectedItem().toString();
            
            cp.setDisplayName("Row Count");
            cp.setPropertyName(Constants.ROW_COUNT_PROPERTY);
            
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
            cp.setOnFailure(FailureAction.Enum.forString(sqlFailure.getSelectedItem().toString()));
            
            cp.setDisplayName("Max Run Time (sec)");
            cp.setPropertyName("max-run-time");
            cp.setOperator(ComparisonOperator.LESS_THAN_OR_EQUAL);
            cp.setPropertyValue("" + maxRunTime.getInt());

            getConfiguration().setModified(true);
            setSaved(true);
            dispose();
            retval = true;
        }

        return retval;
    }

    private boolean checkpointNameExists() {
        boolean retval = false;
        return retval;
    }

    /**
     *
     * @return
     */
    @Override
    public Object getNewRepositoryObject() {
        return checkpoint;
    }

    @Override
    public Dimension getPreferredSize() {
        if (!dbPanel.isForCheckpoint()) {
            return new Dimension(500, 300);
        } else {
            return new Dimension(800, 600);
        }
    }

    /**
     *
     * @return
     */
    @Override
    protected String getDialogName() {
        if (!dbPanel.isForCheckpoint()) {
            return "sql-checkpoint-entry";
        } else {
            return "sql-checkpoint-entry2";
        }
    }

    @Override
    public boolean isResizable() {
        return dbPanel.isForCheckpoint();
    }
}
