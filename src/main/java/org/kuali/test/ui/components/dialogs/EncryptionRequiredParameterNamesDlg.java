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
import java.util.Collections;
import java.util.List;
import javax.swing.JLabel;
import org.apache.commons.lang3.StringUtils;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.ui.base.BaseSetupDlg;
import org.kuali.test.ui.base.BaseTable;
import org.kuali.test.ui.base.SimpleInputDlg;
import org.kuali.test.ui.base.TableConfiguration;
import org.kuali.test.ui.components.panels.TablePanel;
import org.kuali.test.ui.utils.UIUtils;
import org.kuali.test.utils.Constants;

/**
 *
 * @author rbtucker
 */
public class EncryptionRequiredParameterNamesDlg extends BaseSetupDlg {
    private TablePanel tp;
    
    /**
     * Creates new form EncryptionRequiredParameterNamesDlg
     * @param mainFrame
     */
    public EncryptionRequiredParameterNamesDlg(TestCreator mainFrame) {
        super(mainFrame);
        setTitle("Parameters requiring encryption");
        initComponents();
    }

    private void initComponents() {
        tp = new TablePanel(buildNameTable());
        tp.addAddButton(this, Constants.ADD_NAME_ACTION, "add encryption parameter name");
        tp.addDeleteButton(this, Constants.REMOVE_NAME_ACTION, "remove selected encryption parameter name");
        
        getContentPane().add(tp, BorderLayout.CENTER);

        addStandardButtons();
        setDefaultBehavior();
    }
    
    private BaseTable buildNameTable() {
        TableConfiguration config = new TableConfiguration();
        config.setTableName("encryption-required-parameter-names");
        config.setDisplayName("Parameter Names");
        
        int[] alignment = new int[2];
        for (int i = 0; i < alignment.length; ++i) {
            if (i == 0) {
                alignment[i] = JLabel.CENTER;
            } else {
                alignment[i] = JLabel.LEFT;
            }
        }
            
        config.setColumnAlignment(alignment);
        
        config.setHeaders(new String[] {
            "Parameter Name"
        });
        
        config.setPropertyNames(new String[] {
            "name"
        });
            
        config.setColumnTypes(new Class[] {
            String.class
        });
        
        config.setColumnWidths(new int[] {
            30
        });

        if (getConfiguration().getParametersRequiringEncryption() != null) {
            config.setData(new ArrayList<String>(Arrays.asList(getConfiguration().getParametersRequiringEncryption().getNameArray())));
        }
        
        BaseTable retval = new BaseTable(config) {
            @Override
            public Object getValueAt(int row, int column) {
                return getModel().getData().get(row);
            }
        };
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
        
        List <String> names = tp.getTable().getTableData();
        Collections.sort(names);
        
        getConfiguration().getParametersRequiringEncryption().setNameArray(names.toArray(new String[names.size()]));
        
        setSaved(true);
        dispose();
        retval = true;
        
        return retval;
    }
    
    private boolean parameterNameExists(String name) {
        boolean retval = false;
        for (String nm : (List <String>)tp.getTable().getTableData()) {
            if (nm.equalsIgnoreCase(name)) {
                retval = true;
                break;
            }
        }
        
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
        return "encryption-required-parameter-names";
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

                if (UIUtils.promptForDelete(EncryptionRequiredParameterNamesDlg.this, 
                    "Remove Parameter", "Remove parameter name '" + param + "'?")) {
                    l.remove(selrow);
                    tp.getTable().getModel().fireTableRowsDeleted(selrow, selrow);
                }
            }
        } else if (Constants.ADD_NAME_ACTION.equals(actionCommand)) {
            SimpleInputDlg dlg = new SimpleInputDlg(this, "Parameter Name") {

                @Override
                protected String getErrorMessage(String inputValue) {
                    return "Parameter name '" + inputValue + "' already exists";
                }

                @Override
                protected boolean isInputError(String inputValue) {
                    return parameterNameExists(inputValue);
                }
            };
            
            String name = dlg.getEnteredValue();
            
            if (StringUtils.isNotBlank(name)) {
                List <String> l = tp.getTable().getTableData();
                int sz = l.size();
                l.add(name);
                tp.getTable().getModel().fireTableRowsInserted(sz, sz);
            }
        }
    }
}
