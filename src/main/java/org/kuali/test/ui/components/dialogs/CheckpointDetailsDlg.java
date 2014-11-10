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
import java.awt.FlowLayout;
import java.util.Arrays;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.kuali.test.Checkpoint;
import org.kuali.test.ComparisonOperator;
import org.kuali.test.FailureAction;
import org.kuali.test.ValueType;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.ui.base.BaseSetupDlg;
import org.kuali.test.ui.base.BaseTable;
import org.kuali.test.ui.base.TableConfiguration;
import org.kuali.test.ui.components.editors.ComboBoxCellEditor;
import org.kuali.test.ui.components.panels.TablePanel;
import org.kuali.test.ui.components.renderers.ComboBoxTableCellRenderer;
import org.kuali.test.utils.Constants;
import org.kuali.test.utils.Utils;

/**
 *
 * @author rbtucker
 */
public class CheckpointDetailsDlg extends BaseSetupDlg {
    private Checkpoint checkpoint;
    private BaseTable checkpointTable;
    private JTextField checkpointName;
    
    /**
     * Creates new form CheckpointDetailsDlg
     * @param mainFrame
     * @param parentDlg
     * @param checkpoint
     */
    public CheckpointDetailsDlg(TestCreator mainFrame, JDialog parentDlg, Checkpoint checkpoint) {
        super(mainFrame, parentDlg);
        this.checkpoint = (Checkpoint)checkpoint.copy();
        setTitle("Checkpoint Details");

        initComponents();
    }

    private void initComponents() {
        JPanel p = new JPanel(new BorderLayout());
        JPanel p2 = new JPanel(new FlowLayout(FlowLayout.CENTER));
        
        p2.add(new JLabel("Checkpoint Name:", JLabel.RIGHT));
        p2.add(checkpointName = new JTextField(30));
        checkpointName.setText(checkpoint.getName());
        
        checkpointName.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                getSaveButton().setEnabled(true);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                getSaveButton().setEnabled(true);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                getSaveButton().setEnabled(true);
            }
            
        });
        
        p.add(p2, BorderLayout.NORTH);
        p.add(new TablePanel(buildPropertiesTable()), BorderLayout.CENTER);
        p.add(new TablePanel(buildParametersTable(), 4), BorderLayout.SOUTH);
        getContentPane().add(p, BorderLayout.CENTER);
        
        addStandardButtons();
        setDefaultBehavior();
    }


    /**
     *
     * @return
     */
    @Override
    protected String getCancelText() {
        return Constants.CLOSE_ACTION;
    }

    @Override
    protected String getSaveText() {
        return Constants.UPDATE_ACTION;
    }
    
    private BaseTable buildParametersTable() {
        TableConfiguration config = new TableConfiguration();
        config.setTableName("checkpoint-input-parameters-table");
        config.setDisplayName("Input Parameters");
        
        int[] alignment = new int[2];
        for (int i = 0; i < alignment.length; ++i) {
            alignment[i] = JLabel.LEFT;
        }
            
        config.setColumnAlignment(alignment);
        
        config.setHeaders(new String[] {
            "Name",
            "Value"
        });
        
        config.setPropertyNames(new String[] {
            "name",
            "value"
        });
            
        config.setColumnTypes(new Class[] {
            String.class,
            String.class
        });
        
        config.setColumnWidths(new int[] {
            30,
            30
        });

        if ((checkpoint.getInputParameters() != null)
            && (checkpoint.getInputParameters().sizeOfParameterArray() > 0)) {
            config.setData(Arrays.asList(checkpoint.getInputParameters().getParameterArray()));
        }
        
        return new BaseTable(config) {
            
            @Override
            public boolean isCellEditable(int row, int column) {
                return (column == 1);
            }

            @Override
            public void setValueAt(Object aValue, int row, int column) {
                super.setValueAt(aValue, row, column);
                getSaveButton().setEnabled(true);
            }
            
        };
    }
    

    private BaseTable buildPropertiesTable() {
        TableConfiguration config = new TableConfiguration();
        config.setTableName("checkpoint-properties-table");
        config.setDisplayName("Properties");
        
        int[] alignment = new int[7];
        for (int i = 0; i < alignment.length; ++i) {
            alignment[i] = JLabel.LEFT;
        }
            
        config.setColumnAlignment(alignment);
        
        config.setHeaders(new String[] {
            "Group",
            "Section",
            "Type",
            "Name",
            "Operator",
            "Value",
            "On Failure"
        });
        
        config.setPropertyNames(new String[] {
            "propertyGroup",
            "propertySection",
            "valueType",
            "displayName",
            "operator",
            "propertyValue",
            "onFailure"
        });
            
        config.setColumnTypes(new Class[] {
            String.class,
            String.class,
            String.class,
            String.class,
            String.class,
            String.class,
            String.class
        });
        
        config.setColumnWidths(new int[] {
            20,
            20,
            10,
            30,
            15,
            20,
            20
        });

        if ((checkpoint.getCheckpointProperties() != null) 
            && (checkpoint.getCheckpointProperties().sizeOfCheckpointPropertyArray() > 0)) {
            config.setData(Arrays.asList(checkpoint.getCheckpointProperties().getCheckpointPropertyArray()));
        }
        
        BaseTable retval = new BaseTable(config) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return ((column == 2) || (column > 3));
            }

            @Override
            public void setValueAt(Object aValue, int row, int column) {
                super.setValueAt(aValue, row, column); 
                getSaveButton().setEnabled(true);
            }
        };

        String[] valueTypes = Utils.getXmlEnumerations(ValueType.class, true);
        JComboBox cb = new JComboBox(valueTypes);
        retval.getColumnModel().getColumn(2).setCellEditor(new ComboBoxCellEditor(cb));
        retval.getColumnModel().getColumn(2).setCellRenderer(new ComboBoxTableCellRenderer(valueTypes));

        String[] comparisonOperators = Utils.getXmlEnumerations(ComparisonOperator.class, true);
        cb = new JComboBox(comparisonOperators);
        retval.getColumnModel().getColumn(4).setCellEditor(new ComboBoxCellEditor(cb));
        retval.getColumnModel().getColumn(4).setCellRenderer(new ComboBoxTableCellRenderer(comparisonOperators));

        String[] failureActions = Utils.getXmlEnumerations(FailureAction.class, true);
        cb = new JComboBox(failureActions);
        retval.getColumnModel().getColumn(6).setCellEditor(new ComboBoxCellEditor(cb));
        retval.getColumnModel().getColumn(6).setCellRenderer(new ComboBoxTableCellRenderer(failureActions));
        
        return retval;
    }
    
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(700, 400);
    }

    /**
     *
     * @return
     */
    @Override
    protected String getDialogName() {
        return "checkpoint-details";
    }

    /**
     *
     * @return
     */
    @Override
    protected boolean save() {
        checkpoint.setName(checkpointName.getText());
        setSaved(true);
        dispose();
        return true;
    }

    @Override
    public boolean isResizable() {
        return true;
    }

    @Override
    protected boolean getInitialSavedState() {
        return false;
    }

    public Checkpoint getCheckpoint() {
        return checkpoint;
    }

    @Override
    protected void handleOtherActions(String actionCommand) {
        getSaveButton().setEnabled(true);
    }
}
