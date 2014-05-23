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
import java.util.Iterator;
import java.util.List;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
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
    private TablePanel tp;
    private JComboBox selectedTables;
    
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
        
        createTableCellEditorRenderer(retval);
        createColumnCellEditorRenderer(retval);

        retval.getColumnModel().getColumn(3).setCellEditor(new DefaultCellEditor(new IntegerTextField()));
        retval.getColumnModel().getColumn(2).setCellEditor(new ComboBoxCellEditor(new JComboBox()));
        retval.getColumnModel().getColumn(2).setCellRenderer(new ComboBoxTableCellRenderer(Constants.AGGREGATE_FUNCTIONS));
        retval.getColumnModel().getColumn(4).setCellEditor(new ComboBoxCellEditor(new JComboBox(Constants.ASC_DESC)));
        retval.getColumnModel().getColumn(4).setCellRenderer(new ComboBoxTableCellRenderer(Constants.ASC_DESC));
        
        return retval;
    }

    private void createTableCellEditorRenderer(BaseTable table) {
        selectedTables = new JComboBox();
        
        table.getColumnModel().getColumn(0).setCellEditor(new ComboBoxCellEditor(selectedTables));
        
        selectedTables.setRenderer(new BasicComboBoxRenderer () {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (isSelected) {
                    setBackground(list.getSelectionBackground());
                    setForeground(list.getSelectionForeground());
                    if (value instanceof TableData) {
                        list.setToolTipText(getDbPanel().getTableDataTooltip((TableData)value));
                    }
                } else {
                   setBackground(list.getBackground());
                   setForeground(list.getForeground());
                 }
                 setFont(list.getFont());
                 setText((value == null) ? "" : value.toString());
                 
                 return this;
             }
        });

        selectedTables.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                TableData td = (TableData)selectedTables.getSelectedItem();
                
                if (td != null) {
                    ComboBoxCellEditor editor = (ComboBoxCellEditor)tp.getTable().getColumnModel().getColumn(1).getCellEditor();
                    editor.getComboBox().removeAllItems();
                    JComboBox renderer = (JComboBox)tp.getTable().getColumnModel().getColumn(1).getCellRenderer();
                    renderer.removeAllItems();

                    for (ColumnData cd : getSelectedColumnData(td)) {
                        renderer.addItem(cd);
                        editor.getComboBox().addItem(cd);
                    }
                }
            }
        });
        
        table.getColumnModel().getColumn(0).setCellRenderer(new ComboBoxTableCellRenderer(new TableData[0]));
    }
    
    private void createColumnCellEditorRenderer(BaseTable table) {
        JComboBox cb = new JComboBox();
        table.getColumnModel().getColumn(1).setCellEditor(new ComboBoxCellEditor(cb));
        
        cb.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JComboBox cbs = (JComboBox)e.getSource();
                ColumnData cd = (ColumnData)cbs.getSelectedItem();
                
                if (cd != null) {
                    List <String> functions = Utils.getAggregateFunctionsForType(cd.getDataType());
                    ComboBoxCellEditor editor = (ComboBoxCellEditor)tp.getTable().getColumnModel().getColumn(2).getCellEditor();
                    editor.getComboBox().removeAllItems();

                    ComboBoxTableCellRenderer renderer = (ComboBoxTableCellRenderer)tp.getTable().getColumnModel().getColumn(2).getCellRenderer();
                    renderer.removeAllItems();
                    
                    for (String f : functions) {
                        renderer.addItem(f);
                        editor.getComboBox().addItem(f);
                    }
                }
            }
        });

        table.getColumnModel().getColumn(1).setCellRenderer(new ComboBoxTableCellRenderer(new ColumnData[0]));
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
        
        selectedTables.removeAllItems();
        
        // this is the table renderer for "tables" column
        JComboBox cb = (JComboBox)tp.getTable().getColumnModel().getColumn(0).getCellRenderer();
        cb.removeAllItems();
        
        if (getDbPanel().haveSelectedColumns()) {
            List <TableData> selectedDbObjects = getDbPanel().getSelectedDbObjects();
            
            for (TableData td : selectedDbObjects) {
                selectedTables.addItem(td);
                cb.addItem(td);
            }
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
    
    public List <SelectColumnData> getSelectColumnData() {
        List <SelectColumnData> retval =  (List<SelectColumnData>)tp.getTable().getTableData();
        
        Iterator <SelectColumnData> it = retval.iterator();
        
        while (it.hasNext()) {
            SelectColumnData scd = it.next();
            
            // remove any rows that are incomplete
            if ((scd.getColumnData() == null) || (scd.getTableData() == null)) {
                it.remove();;
            }
        }
        
        return retval;
    }
}
