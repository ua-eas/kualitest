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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.apache.commons.lang3.StringUtils;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.ui.base.BaseSetupDlg;
import org.kuali.test.ui.base.BaseTable;
import org.kuali.test.ui.base.TableConfiguration;
import org.kuali.test.ui.components.buttons.AddButton;
import org.kuali.test.ui.components.buttons.TableCellIconButton;
import org.kuali.test.ui.components.panels.TablePanel;
import org.kuali.test.ui.utils.UIUtils;
import org.kuali.test.utils.Constants;

/**
 *
 * @author rbtucker
 */
public class TestExecutionParameterNamesDlg extends BaseSetupDlg {
    private JTextField name;
    private BaseTable nameTable;
    
    /**
     * Creates new form TestSuiteDlg
     * @param mainFrame
     */
    public TestExecutionParameterNamesDlg(TestCreator mainFrame) {
        super(mainFrame);
        setTitle("Test execution parameter names");
        initComponents();
    }

    private void initComponents() {
        String[] labels = new String[] {
            "Parameter Name"
        };
        
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 1));
        p.add(name = new JTextField(20));
        AddButton b = new AddButton(Constants.ADD_NAME_ACTION, "Add parameter name");
        b.addActionListener(this);
        p.add(b);
        
        JComponent[] components = new JComponent[] {
            p
        };

        p = new JPanel(new BorderLayout(3, 3));
        p.add(UIUtils.buildEntryPanel(labels, components), BorderLayout.NORTH);

        p.add(new TablePanel(nameTable = buildNameTable()), BorderLayout.CENTER);
        
        getContentPane().add(p, BorderLayout.CENTER);

        addStandardButtons();
        setDefaultBehavior();
    }
    
    private BaseTable buildNameTable() {
        TableConfiguration config = new TableConfiguration();
        config.setTableName("test-execution-parameter-names");
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
            "Remove",
            "Parameter Name"
        });
        
        config.setPropertyNames(new String[] {
            Constants.IGNORE_TABLE_DATA_INDICATOR,
            "name"
        });
            
        config.setColumnTypes(new Class[] {
            String.class,
            String.class
        });
        
        config.setColumnWidths(new int[] {
            15,
            30
        });

        if (getConfiguration().getTestExecutionParameterNames() != null) {
            config.setData(new ArrayList<String>(Arrays.asList(getConfiguration().getTestExecutionParameterNames().getNameArray())));
        }
        
        BaseTable retval = new BaseTable(config) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return (column == 0);
            }

            @Override
            public Object getValueAt(int row, int column) {
                if (column == 1) {
                    return getModel().getData().get(row);
                } else {
                    return null;
                }
            }
            
            
        };
        
        TableCellIconButton b = new TableCellIconButton(Constants.DELETE_ICON);
        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                TableCellIconButton b = (TableCellIconButton)e.getSource();
                List <String> l = nameTable.getTableData();
                if ((b.getCurrentRow() > -1) && (l.size() > b.getCurrentRow())) {
                    String param = l.get(b.getCurrentRow());
                    
                    if (UIUtils.promptForDelete(TestExecutionParameterNamesDlg.this, 
                        "Remove Parameter", "Remove test execution parameter name '" + param + "'?")) {
                        l.remove(b.getCurrentRow());
                        nameTable.getModel().fireTableRowsDeleted(b.getCurrentRow(), b.getCurrentRow());
                    }
                }
            }
        });
        
        retval.getColumnModel().getColumn(0).setCellRenderer(b);
        retval.getColumnModel().getColumn(0).setCellEditor(b);

        return retval;
    }
    
    
    @Override
    protected boolean save() {
        boolean retval = false;
        getConfiguration().setModified(true);
        
        List <String> names = nameTable.getTableData();
        Collections.sort(names);
        
        getConfiguration().getTestExecutionParameterNames().setNameArray(names.toArray(new String[names.size()]));
        
        setSaved(true);
        dispose();
        retval = true;
        
        return retval;
    }
    
    private boolean parameterNameExists() {
        boolean retval = false;
        String newname = name.getText();
        
        for (String nm : (List <String>)nameTable.getTableData()) {
            if (nm.equalsIgnoreCase(newname)) {
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

    @Override
    protected String getDialogName() {
        return "test-execution-parameter-names";
    }

    @Override
    protected void handleOtherActions(String actionCommand) {
        if (StringUtils.isBlank(name.getText())) {
            UIUtils.showError(this, "Parameter Name required", "Please enter a parameter name");
        } else if (parameterNameExists()) {
            UIUtils.showError(this, "Parameter Name Exists", "Parameter name '" + name.getText() + "' already exists");
        } else {
            int row = nameTable.getTableData().size();
            nameTable.getTableData().add(name.getText());
            nameTable.getModel().fireTableRowsInserted(row, row);
        }
    }
}
