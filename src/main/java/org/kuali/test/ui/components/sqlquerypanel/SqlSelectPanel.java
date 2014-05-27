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

package org.kuali.test.ui.components.sqlquerypanel;

import java.awt.BorderLayout;
import java.util.List;
import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.ui.base.BaseTable;
import org.kuali.test.ui.base.TableConfiguration;
import org.kuali.test.ui.components.editmasks.IntegerTextField;
import org.kuali.test.ui.components.editors.ComboBoxCellEditor;
import org.kuali.test.ui.components.panels.TablePanel;
import org.kuali.test.ui.components.renderers.ComboBoxTableCellRenderer;
import org.kuali.test.ui.components.sqlquerytree.ColumnData;
import org.kuali.test.ui.components.sqlquerytree.TableData;
import org.kuali.test.utils.Constants;
import org.kuali.test.utils.Utils;


public class SqlSelectPanel extends BaseSqlPanel <SelectColumnData> {
    private static final Logger LOG = Logger.getLogger(SqlSelectPanel.class);
    private JCheckBox distinct;
    
    public SqlSelectPanel(TestCreator mainframe, DatabasePanel dbPanel) {
        super(mainframe, dbPanel, SelectColumnData.class);
        initComponents();
    }

    private void initComponents() {
        TablePanel tp = new TablePanel(getSelectColumnTable());
        
        setTablePanel(tp);
        
        createTableCellEditorRenderer(0, 1);
        createColumnCellEditorRenderer(1);

        tp.addAddButton(this, Constants.ADD_COLUMN_ACTION, "add new select column");
        tp.getAddButton().setEnabled(false);
        tp.addDeleteButton(this, Constants.DELETE_COLUMN_ACTION, "delete selected row");
        tp.getButtonPanel().add(distinct = new JCheckBox("DISTINCT"));

        add(tp, BorderLayout.CENTER);
    }
    
    private BaseTable getSelectColumnTable() {
        TableConfiguration tc = new TableConfiguration();
        
        tc.setHeaders(new String[] {
            "table",
            "column",
            "function",
            "order",
            "asc/desc"
        });
        
        tc.setPropertyNames(new String[] {
            "tableData",
            "columnData",
            "function",
            "order",
            "ascDesc"
        });
        
        tc.setColumnTypes(new Class[] {
            TableData.class,
            ColumnData.class,
            String.class,
            String.class,
            String.class,
            String.class
        });
        
        tc.setColumnWidths(new int[] {
            30,
            40,
            15,
            10,
            15
        });

        tc.setTableName("sql-select-column-table");
        tc.setDisplayName("Select Columns");

        BaseTable retval = new BaseTable(tc) {
            @Override
            public boolean isCellEditable(int row, int column) {
                boolean retval = false;
                SelectColumnData scd = (SelectColumnData)getTableData().get(row);
                
                
                switch(column) {
                    case 0:
                        retval = true;
                        break;
                    case 1:
                        retval = (scd.getTableData() != null);
                        break;
                    case 2:
                    case 3:
                        retval = (scd.getColumnData() != null);
                        break;
                    case 4:
                        retval = ((scd.getColumnData() != null) && StringUtils.isNotBlank(scd.getOrder()));
                        break;
                }
                
                return retval;
            }

            @Override
            protected String getTooltip(int row, int col) {
                String retval = null;
                if (col == 0) {
                    SelectColumnData scd =  (SelectColumnData)getTableData().get(row);
                    
                    if (scd != null) {
                        TableData td = scd.getTableData();
                        
                        if (td != null) {
                            retval = getDbPanel().getTableDataTooltip(td);
                        }
                    }
                }
                
                return retval;
            }
        };

        retval.getColumnModel().getColumn(3).setCellEditor(new DefaultCellEditor(new IntegerTextField()));
        retval.getColumnModel().getColumn(2).setCellEditor(new ComboBoxCellEditor(new JComboBox()));
        retval.getColumnModel().getColumn(2).setCellRenderer(new ComboBoxTableCellRenderer(Constants.AGGREGATE_FUNCTIONS));
        retval.getColumnModel().getColumn(4).setCellEditor(new ComboBoxCellEditor(new JComboBox(Constants.ASC_DESC)));
        retval.getColumnModel().getColumn(4).setCellRenderer(new ComboBoxTableCellRenderer(Constants.ASC_DESC));
        
        return retval;
    }

    @Override
    protected boolean validateRequiredFields(SelectColumnData scd) {
        return ((scd.getTableData() != null) && (scd.getColumnData() != null));
    }
    
    
    @Override
    protected void handlePanelShown() {
        populateSelectedTables(0);
    }
    
    @Override
    protected String getAddAction() {
        return Constants.ADD_COLUMN_ACTION;
    }

    @Override
    protected String getDeleteAction() {
        return Constants.DELETE_COLUMN_ACTION;
    }

    @Override
    protected void handleColumnChanged(ColumnData cd) {
        List <String> functions = Utils.getAggregateFunctionsForType(cd.getDataType());
        ComboBoxCellEditor editor = (ComboBoxCellEditor)getTable().getColumnModel().getColumn(2).getCellEditor();
        editor.getComboBox().removeAllItems();

        ComboBoxTableCellRenderer renderer = (ComboBoxTableCellRenderer)getTable().getColumnModel().getColumn(2).getCellRenderer();
        renderer.removeAllItems();

        for (String f : functions) {
            renderer.addItem(f);
            editor.getComboBox().addItem(f);
        }
    }

    @Override
    protected String getRequiredColumnList() {
        return "table, column";
    }
    
    public boolean isDistinct() {
        return distinct.isSelected();
    }
    
    @Override
    public boolean haveEntries() {
        return !getTable().getTableData().isEmpty();
    }
}
