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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import javax.swing.JComboBox;
import javax.swing.JTable;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.ui.base.BaseTable;
import org.kuali.test.ui.base.TableConfiguration;
import org.kuali.test.ui.components.editors.ComboBoxCellEditor;
import org.kuali.test.ui.components.renderers.ComboBoxTableCellRenderer;
import org.kuali.test.ui.components.sqlquerytree.ColumnData;
import org.kuali.test.utils.Constants;
import org.kuali.test.utils.Utils;


public class SqlSelectPanel extends BaseSqlPanel implements ActionListener {
    private static final Logger LOG = Logger.getLogger(SqlSelectPanel.class);
    private Map<String, List<ColumnData>> availableColumns;
    private List<AvailableTable> availableTables = new ArrayList<AvailableTable>();
    
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
            "function",
            "column",
            "type",
            "order",
            "asc/desc"
        });
        
        tc.setPropertyNames(new String[] {
            "availableTable.tableDisplayName",
            "function",
            "columnDisplayName",
            "order",
            "type",
            "ascDesc"
        });
        
        tc.setColumnTypes(new Class[] {
            String.class,
            String.class,
            String.class,
            Integer.class,
            String.class,
            String.class
        });
        
        tc.setColumnWidths(new int[] {
            30,
            20,
            30,
            15,
            20,
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
                    case 3:
                        retval = StringUtils.isNotBlank(scd.getColumnName());
                        break;
                    case 2:
                        retval = (scd.getAvailableTable() != null);
                        break;
                    case 5:
                        retval = (StringUtils.isNotBlank(scd.getColumnName()) && (scd.getOrder() != null) && (scd.getOrder() > 0));
                        break;
                }
                
                return retval;
            }
        };
        
        retval.getColumnModel().getColumn(0).setCellEditor(new ComboBoxCellEditor(new JComboBox()) {
            @Override
            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
                JComboBox retval =  (JComboBox)super.getTableCellEditorComponent(table, value, isSelected, row, column);
                
                retval.removeAllItems();

                for (AvailableTable t : availableTables) {
                    retval.addItem(t);
                }
                
                return retval;
            }
            
        });
        
        retval.getColumnModel().getColumn(0).setCellRenderer(new ComboBoxTableCellRenderer(new AvailableTable[0]));

        retval.getColumnModel().getColumn(1).setCellEditor(new ComboBoxCellEditor(new JComboBox()) {
            @Override
            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
                JComboBox retval =  (JComboBox)super.getTableCellEditorComponent(table, value, isSelected, row, column);
                
                retval.removeAllItems();
                SelectColumnData scd = (SelectColumnData)value;
                
                for (String f : Utils.getAggregateFunctionsForType(scd.getJdbcType())) {
                    retval.addItem(f);
                }
                
                return retval;
            }
            
        });
        retval.getColumnModel().getColumn(1).setCellRenderer(new ComboBoxTableCellRenderer(Constants.AGGREGATE_FUNCTIONS));
        
        retval.getColumnModel().getColumn(5).setCellEditor(new ComboBoxCellEditor(new JComboBox(Constants.ASC_DESC)));
        retval.getColumnModel().getColumn(5).setCellRenderer(new ComboBoxTableCellRenderer(Constants.ASC_DESC));
        
        return retval;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (Constants.ADD_COLUMN_ACTION.equals(e.getActionCommand())) {
            List l = tp.getTable().getTableData();
            l.add(new SelectColumnData());
            tp.getTable().getModel().fireTableRowsInserted(l.size()-1, l.size()-1);
        } else if (Constants.DELETE_COLUMN_ACTION.equals(e.getActionCommand())) {
            
        }
    }
    
    @Override
    protected void handlePanelShown() {
        tp.getAddButton().setEnabled(getDbPanel().haveSelectedColumns());
        
        if (getDbPanel().haveSelectedColumns()) {
            Map <String, List<ColumnData>> selcols = getDbPanel().getAvailableColumnMap();

            availableTables.clear();
            
            for (String s : selcols.keySet()) {
                availableTables.add(new AvailableTable(s));
            }
            
            Collections.sort(availableTables);
            
            tp.getTable().getColumnModel().getColumn(0).setCellRenderer(new ComboBoxTableCellRenderer(availableTables.toArray(new AvailableTable[availableTables.size()])));
        }
    }

    private class AvailableTable implements Comparable <AvailableTable> {
        private String schema;
        private String tableDisplayName;
        private String tableName;

        public AvailableTable(String fullTableName) {
            StringTokenizer st = new StringTokenizer(fullTableName, ".");
            
            if (st.countTokens() == 3) {
                schema = st.nextToken();
                tableName = st.nextToken();
                tableDisplayName = st.nextToken();
            }
        }
        
        public String getSchema() {
            return schema;
        }

        public void setSchema(String schema) {
            this.schema = schema;
        }

        public String getTableDisplayName() {
            return tableDisplayName;
        }

        public void setTableDisplayName(String tableDisplayName) {
            this.tableDisplayName = tableDisplayName;
        }

        public String getTableName() {
            return tableName;
        }

        public void setTableName(String tableName) {
            this.tableName = tableName;
        }
        
        public String toString() {
            return tableDisplayName;
        }

        @Override
        public int compareTo(AvailableTable o) {
            return tableDisplayName.compareTo(o.getTableDisplayName());
        }
    }

    private class SelectColumnData {
        private AvailableTable availableTable;
        private String function;
        private String columnName;
        private String columnDisplayName;
        private Integer order;
        private String ascDesc;
        private int jdbcType;
        private String typeName;

        public AvailableTable getAvailableTable() {
            return availableTable;
        }

        public void setAvailableTable(AvailableTable availableTable) {
            this.availableTable = availableTable;
        }

        public String getFunction() {
            return function;
        }

        public void setFunction(String function) {
            this.function = function;
        }

        public String getColumnName() {
            return columnName;
        }

        public void setColumnName(String columnName) {
            this.columnName = columnName;
        }

        public String getColumnDisplayName() {
            return columnDisplayName;
        }

        public void setColumnDisplayName(String columnDisplayName) {
            this.columnDisplayName = columnDisplayName;
        }

        public int getJdbcType() {
            return jdbcType;
        }

        public void setJdbcType(int jdbcType) {
            this.jdbcType = jdbcType;
        }


        public Integer getOrder() {
            return order;
        }

        public void setOrder(Integer order) {
            this.order = order;
        }

        public String getAscDesc() {
            return ascDesc;
        }

        public void setAscDesc(String ascDesc) {
            this.ascDesc = ascDesc;
        }

        public String getTypeName() {
            return typeName;
        }
    }
}
