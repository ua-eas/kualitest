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
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import org.kuali.test.TestExecutionParameter;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.ui.base.BaseSetupDlg;
import org.kuali.test.ui.base.BaseTable;
import org.kuali.test.ui.base.TableConfiguration;
import org.kuali.test.ui.components.panels.TablePanel;
import org.kuali.test.ui.utils.UIUtils;
import org.kuali.test.utils.Constants;
import org.kuali.test.CheckpointProperty;

/**
 *
 * @author rbtucker
 */
public class TestExecutionParameterDetailsDlg extends BaseSetupDlg {
    /**
     * 
     * @param mainFrame
     * @param parentDlg
     * @param parameter 
     */
    public TestExecutionParameterDetailsDlg(TestCreator mainFrame, JDialog parentDlg, TestExecutionParameter parameter) {
        super(mainFrame, parentDlg);
        setTitle("Test Execution Parameter Details");

        initComponents(parameter);
    }

    private void initComponents(TestExecutionParameter parameter) {
         String[] labels = {
            "Parameter Name",
        };
        
        JComponent[] components = {new JLabel(parameter.getName())};
        
        getContentPane().add(UIUtils.buildEntryPanel(labels, components), BorderLayout.NORTH);
        getContentPane().add(new TablePanel(buildPropertiesTable(parameter.getValueProperty()), 5), BorderLayout.CENTER);
        
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
    
    private BaseTable buildPropertiesTable(CheckpointProperty checkpointProperty) {
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
            "Origial Value"
        });
        
        config.setPropertyNames(new String[] {
            "propertyGroup",
            "propertySection",
            "valueType",
            "displayName",
            "propertyValue"
        });
            
        config.setColumnTypes(new Class[] {
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
            20
        });

        config.setData(Arrays.asList(new CheckpointProperty[] {checkpointProperty}));
                
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
