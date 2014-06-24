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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.JLabel;
import org.kuali.test.AutoReplaceParameter;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.ui.base.BaseSetupDlg;
import org.kuali.test.ui.base.BaseTable;
import org.kuali.test.ui.base.TableConfiguration;
import org.kuali.test.ui.components.panels.TablePanel;
import org.kuali.test.ui.utils.UIUtils;
import org.kuali.test.utils.Constants;

/**
 *
 * @author rbtucker
 */
public class AutoReplaceParametersDlg extends BaseSetupDlg {
    private TablePanel tp;
    
    /**
     * Creates new form RemoveParameterNamesDlg
     * @param mainFrame
     */
    public AutoReplaceParametersDlg(TestCreator mainFrame) {
        super(mainFrame);
        setTitle("Auto replace parameters");
        initComponents();
    }

    private void initComponents() {
        tp = new TablePanel(buildNameTable());
        tp.addAddButton(this, Constants.ADD_NAME_ACTION, "add auto-replace parameter");
        tp.addDeleteButton(this, Constants.REMOVE_NAME_ACTION, "remove selected auto-replace parameter");
        
        getContentPane().add(tp, BorderLayout.CENTER);

        addStandardButtons();
        setDefaultBehavior();
    }
    
    private BaseTable buildNameTable() {
        TableConfiguration config = new TableConfiguration();
        config.setTableName("auto-replace-parameters");
        config.setDisplayName("Auto Replace Parameters");
        
        int[] alignment = new int[4];
        for (int i = 0; i < alignment.length; ++i) {
            if (i == 3) {
                alignment[i] = JLabel.CENTER;
            } else {
                alignment[i] = JLabel.LEFT;
            }
        }
            
        config.setColumnAlignment(alignment);
        
        config.setHeaders(new String[] {
            "Parameter Name",
            "Tag Name",
            "Retain", 
        });
        
        config.setPropertyNames(new String[] {
            "parameterName",
            "tagName",
            "retain"
        });
            
        config.setColumnTypes(new Class[] {
            String.class,
            String.class,
            String.class
        });
        
        config.setColumnWidths(new int[] {
            30,
            20,
            20
        });

        if (getConfiguration().getAutoReplaceParameters() != null) {
            config.setData(new ArrayList<AutoReplaceParameter>(Arrays.asList(getConfiguration().getAutoReplaceParameters().getAutoReplaceParameterArray())));
        }
        
        BaseTable retval = new BaseTable(config);
        
        return retval;
    }
    
    /**
     *
     * @return
     */
    @Override
    protected boolean save() {
        boolean retval = false;
        getConfiguration().setModified(true);
        
        List <AutoReplaceParameter> parameters = tp.getTable().getTableData();
  //      Collections.sort(names);
        
        if (getConfiguration().getAutoReplaceParameters() == null) {
            getConfiguration().addNewAutoReplaceParameters();
        }

        getConfiguration().getAutoReplaceParameters().setAutoReplaceParameterArray(parameters.toArray(new AutoReplaceParameter[parameters.size()]));
        
        setSaved(true);
        dispose();
        retval = true;
        
        return retval;
    }
    
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(400, 400);
    }

    /**
     *
     * @return
     */
    @Override
    protected String getDialogName() {
        return "auto-replace-parameters";
    }

    /**
     *
     * @param actionCommand
     */
    @Override
    protected void handleOtherActions(String actionCommand) {
        if (Constants.REMOVE_NAME_ACTION.equals(actionCommand)) {
            List <String> l = tp.getTable().getTableData();
            if ((l != null) && !l.isEmpty()) {
                int selrow = tp.getTable().getSelectedRow();
                String param = l.get(selrow);

                if (UIUtils.promptForDelete(AutoReplaceParametersDlg.this, 
                    "Remove Parameter", "Remove parameter '" + param + "'?")) {
                    l.remove(selrow);
                    tp.getTable().getModel().fireTableRowsDeleted(selrow, selrow);
                }
            }
        } else if (Constants.ADD_NAME_ACTION.equals(actionCommand)) {
            AutoReplaceParameterDlg dlg = new AutoReplaceParameterDlg(getMainframe(), this);
            
            if (dlg.isSaved()) {
                List <AutoReplaceParameter> l = tp.getTable().getTableData();
                int newrow = l.size();
                
                l.add((AutoReplaceParameter)dlg.getNewRepositoryObject());
                tp.getTable().getModel().fireTableRowsInserted(newrow, newrow);
            }
        }
    }
}
