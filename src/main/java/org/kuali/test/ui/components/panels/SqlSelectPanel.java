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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.ui.base.BaseTable;
import org.kuali.test.ui.base.TableConfiguration;
import org.kuali.test.ui.components.editmasks.IntegerTextField;
import org.kuali.test.ui.components.editors.ComboBoxCellEditor;
import org.kuali.test.ui.components.renderers.ComboBoxTableCellRenderer;
import org.kuali.test.ui.components.sqlquerytree.ColumnData;
import org.kuali.test.ui.components.sqlquerytree.TableData;
import org.kuali.test.utils.Constants;
import org.kuali.test.utils.Utils;


public class SqlSelectPanel extends BaseSqlPanel implements ActionListener {
    private static final Logger LOG = Logger.getLogger(SqlSelectPanel.class);
    private List<TableData> selectedDbObjects = new ArrayList<TableData>();
    
    private TablePanel tp;
    public SqlSelectPanel(TestCreator mainframe, DatabasePanel dbPanel) {
        super(mainframe, dbPanel);
        initComponents();
    }

    private void initComponents() {
        tp = new TablePanel(getSelectColumnTable());
        tp.addAddButton(this, Constants.ADD_COLUMN_ACTION, "add new select column");
        tp.getAddButton().setEnabled(false);
        tp.addDeleteButton(this, Constants.DELETE_COLUMN_ACTION, "delete selected row");
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
        };
        
        createTableCellEditorRenderer(retval);
        createColumnCellEditorRenderer(retval);
        createFunctionCellEditorRenderer(retval);

        retval.getColumnModel().getColumn(3).setCellEditor(new DefaultCellEditor(new IntegerTextField()));
        
        retval.getColumnModel().getColumn(4).setCellEditor(new ComboBoxCellEditor(new JComboBox(Constants.ASC_DESC)));
        retval.getColumnModel().getColumn(4).setCellRenderer(new ComboBoxTableCellRenderer(Constants.ASC_DESC));
        
        return retval;
    }

    private void createTableCellEditorRenderer(BaseTable table) {
        JComboBox cb = new JComboBox();
        table.getColumnModel().getColumn(0).setCellEditor(new ComboBoxCellEditor(cb) {
            @Override
            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
                JComboBox retval =  (JComboBox)super.getTableCellEditorComponent(table, value, isSelected, row, column);
                
                retval.removeAllItems();

                for (TableData t : selectedDbObjects) {
                    retval.addItem(t);
                }
                
                return retval;
            }
            
        });
        
        cb.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JComboBox cbs = (JComboBox)e.getSource();
                TableData td = (TableData)cbs.getSelectedItem();
                
                if (td != null) {
                    tp.getTable().getColumnModel().getColumn(1).setCellRenderer(new ComboBoxTableCellRenderer(getSelectedColumnData(td)));
                }
            }
        });
        
        table.getColumnModel().getColumn(0).setCellRenderer(new ComboBoxTableCellRenderer(new TableData[0]));
    }
    
    private ColumnData[] getSelectedColumnData(TableData td) {
        List <ColumnData> retval = new ArrayList<ColumnData>();
        
        for (ColumnData c: td.getColumns()) {
            if (c.isSelected()) {
                retval.add(c);
            }
        }
        
        return retval.toArray(new ColumnData[retval.size()]);
    }

    private void createColumnCellEditorRenderer(BaseTable table) {
        JComboBox cb = new JComboBox();
        table.getColumnModel().getColumn(1).setCellEditor(new ComboBoxCellEditor(cb) {
            @Override
            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
                JComboBox retval =  (JComboBox)super.getTableCellEditorComponent(table, value, isSelected, row, column);
                
                retval.removeAllItems();
                
                if (selectedDbObjects != null) {
                    List l = ((BaseTable)table).getTableData();
                    
                    if ((l != null) && (l.size() > row)) {
                        SelectColumnData scd = (SelectColumnData)l.get(row);
                        ColumnData[] selcols = getSelectedColumnData(scd.getTableData());

                        for (ColumnData c : selcols) {
                            retval.addItem(c);
                        }
                    }
                }
                
                return retval;
            }
            
        });
        
        cb.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JComboBox cbs = (JComboBox)e.getSource();
                ColumnData cd = (ColumnData)cbs.getSelectedItem();
                
                if (cd != null) {
                    List <String> functions = Utils.getAggregateFunctionsForType(cd.getDataType());
                    
                    tp.getTable().getColumnModel().getColumn(2).setCellRenderer(
                        new ComboBoxTableCellRenderer(functions.toArray(new String[functions.size()])));
                }
            }
        });

        table.getColumnModel().getColumn(2).setCellRenderer(new ComboBoxTableCellRenderer(new ColumnData[0]));
    }

    private void createFunctionCellEditorRenderer(BaseTable table) {
        table.getColumnModel().getColumn(2).setCellEditor(new ComboBoxCellEditor(new JComboBox()) {
            @Override
            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
                JComboBox retval =  (JComboBox)super.getTableCellEditorComponent(table, value, isSelected, row, column);
                
                retval.removeAllItems();
                
                if (selectedDbObjects != null) {
                    List l = ((BaseTable)table).getTableData();
                    
                    if ((l != null) && (l.size() > row)) {
                        SelectColumnData scd = (SelectColumnData)l.get(row);

                        if (scd.getColumnData() != null) {
                            for (String f : Utils.getAggregateFunctionsForType(scd.getColumnData().getDataType())) {
                                retval.addItem(f);
                            }
                        }
                    }
                }
            
                return retval;
            }
        });
        
        table.getColumnModel().getColumn(1).setCellRenderer(new ComboBoxTableCellRenderer(Constants.AGGREGATE_FUNCTIONS));
    }
    
    
    @Override
    public void actionPerformed(ActionEvent e) {
        List l = tp.getTable().getTableData();
        if (Constants.ADD_COLUMN_ACTION.equals(e.getActionCommand())) {
            l.add(new SelectColumnData());
            tp.getTable().getModel().fireTableRowsInserted(l.size()-1, l.size()-1);
        } else if (Constants.DELETE_COLUMN_ACTION.equals(e.getActionCommand())) {
            int row = tp.getTable().getSelectedRow();
            
            if ((row > -1) && (l.size() > row)) {
                l.remove(row);
                tp.getTable().getModel().fireTableRowsDeleted(row, row);
            }
        }
    }
    
    @Override
    protected void handlePanelShown() {
        tp.getAddButton().setEnabled(getDbPanel().haveSelectedColumns());
        
        if (getDbPanel().haveSelectedColumns()) {
            selectedDbObjects = getDbPanel().getSelectedDbObjects();

            tp.getTable().getColumnModel().getColumn(0).setCellRenderer(
                new ComboBoxTableCellRenderer(selectedDbObjects.toArray(
                    new TableData[selectedDbObjects.size()])));
        }
    }

    public class SelectColumnData {
        private TableData tableData;
        private ColumnData columnData;
        private String function;
        private String order;
        private String ascDesc;

        public TableData getTableData() {
            return tableData;
        }

        public void setTableData(TableData tableData) {
            this.tableData = tableData;
        }

        public ColumnData getColumnData() {
            return columnData;
        }

        public void setColumnData(ColumnData columnData) {
            this.columnData = columnData;
        }

        public String getFunction() {
            return function;
        }

        public void setFunction(String function) {
            this.function = function;
        }

        public String getOrder() {
            return order;
        }

        public void setOrder(String order) {
            this.order = order;
        }

        public String getAscDesc() {
            return ascDesc;
        }

        public void setAscDesc(String ascDesc) {
            this.ascDesc = ascDesc;
        }
    }
}
