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
import java.util.Arrays;
import javax.swing.JDialog;
import javax.swing.JLabel;
import org.kuali.test.Checkpoint;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.ui.base.BaseSetupDlg;
import org.kuali.test.ui.base.BaseTable;
import org.kuali.test.ui.base.TableConfiguration;
import org.kuali.test.ui.components.panels.TablePanel;
import org.kuali.test.utils.Constants;

/**
 *
 * @author rbtucker
 */
public class CheckpointDetailsDlg extends BaseSetupDlg {
    private Checkpoint checkpoint;
    private BaseTable checkpointTable;
    
    /**
     * Creates new form CheckpointDetailsDlg
     * @param mainFrame
     * @param parentDlg
     * @param checkpoint
     */
    public CheckpointDetailsDlg(TestCreator mainFrame, JDialog parentDlg, Checkpoint checkpoint) {
        super(mainFrame, parentDlg);
        this.checkpoint = checkpoint;
        setTitle("Checkpoint Details");

        initComponents();
    }

    private void initComponents() {
        getContentPane().add(new TablePanel(buildPropertiesTable()), BorderLayout.NORTH);
        getContentPane().add(new TablePanel(buildParametersTable()), BorderLayout.CENTER);
        
        addStandardButtons();
        getSaveButton().setVisible(false);
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
        
        return new BaseTable(config);
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

        if (checkpoint.getCheckpointProperties().sizeOfCheckpointPropertyArray() > 0) {
            config.setData(Arrays.asList(checkpoint.getCheckpointProperties().getCheckpointPropertyArray()));
        }
        
        return new BaseTable(config);
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
        return false;
    }

    @Override
    public boolean isResizable() {
        return true;
    }
}
