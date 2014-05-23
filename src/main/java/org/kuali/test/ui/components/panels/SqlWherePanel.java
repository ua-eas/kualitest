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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;
import javax.swing.JComboBox;
import org.apache.log4j.Logger;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.ui.base.BaseTable;
import org.kuali.test.ui.base.TableConfiguration;
import org.kuali.test.ui.components.editors.ComboBoxCellEditor;
import org.kuali.test.ui.components.renderers.ComboBoxTableCellRenderer;
import org.kuali.test.ui.components.sqlquerytree.ColumnData;
import org.kuali.test.ui.components.sqlquerytree.TableData;
import org.kuali.test.utils.Constants;


public class SqlWherePanel extends BaseSqlPanel implements ActionListener {
    private static final Logger LOG = Logger.getLogger(SqlWherePanel.class);
    private TablePanel tp;

    public SqlWherePanel(TestCreator mainframe, DatabasePanel dbPanel) {
        super(mainframe, dbPanel);
        initComponents();
    }

    private void initComponents() {
        tp = new TablePanel(getSelectColumnTable());
        
        createTableCellEditorRenderer(tp, 2);
        createColumnCellEditorRenderer(tp, 3);

        
        tp.addAddButton(this, Constants.ADD_COMPARISON_ACTION, "add new where comparison");
        tp.getAddButton().setEnabled(false);
        tp.addDeleteButton(this, Constants.DELETE_COMPARISON_ACTION, "delete selected row");
        add(tp, BorderLayout.CENTER);
    }
    
    private BaseTable getSelectColumnTable() {
        TableConfiguration tc = new TableConfiguration();
        
        tc.setHeaders(new String[] {
            "and/or",
            "(",
            "table",
            "column",
            "oerator",
            "value",
            ")"
        });
        
        tc.setPropertyNames(new String[] {
            "andOr", // 0
            "openParenthesis", // 1
            "tableData", // 2
            "columnData", // 3
            "oerator", // 4
            "value", // 5
            "closeParenthesis" // 6
        });
        
        tc.setColumnTypes(new Class[] {
            String.class,
            String.class,
            String.class,
            String.class,
            String.class,
            String.class,
            String.class
        });
        
        tc.setColumnWidths(new int[] {
            15,
            15,
            30,
            30,
            20,
            20,
            15
        });

        tc.setTableName("sql-where-column-table");
        tc.setDisplayName("Where Comparisons");

        BaseTable retval = new BaseTable(tc) {
            @Override
            public boolean isCellEditable(int row, int column) {
                boolean retval = false;
                SqlWherePanel.WhereColumnData wcd = (SqlWherePanel.WhereColumnData)getTableData().get(row);
                
                
                switch(column) {
                    case 0:
                    case 1:
                    case 2:
                    case 6:
                        retval = true;
                        break;
                    case 3:
                        retval = (wcd.getTableData() != null);
                        break;
                    case 4:
                    case 5:
                        retval = (wcd.getColumnData() != null);
                        break;
                }
                
                return retval;
            }

            @Override
            protected String getTooltip(int row, int col) {
                String retval = null;
                if (col == 0) {
                    SqlWherePanel.WhereColumnData wcd =  (SqlWherePanel.WhereColumnData)getTableData().get(row);
                    
                    if (wcd != null) {
                        TableData td = wcd.getTableData();
                        
                        if (td != null) {
                            retval = getDbPanel().getTableDataTooltip(td);
                        }
                    }
                }
                
                return retval;
            }
        };
        
        retval.getColumnModel().getColumn(0).setCellEditor(new ComboBoxCellEditor(new JComboBox()));
        retval.getColumnModel().getColumn(0).setCellRenderer(new ComboBoxTableCellRenderer(Constants.AND_OR));
        retval.getColumnModel().getColumn(1).setCellEditor(new ComboBoxCellEditor(new JComboBox()));
        retval.getColumnModel().getColumn(1).setCellRenderer(new ComboBoxTableCellRenderer(Constants.OPEN_PARENTHESIS));

        retval.getColumnModel().getColumn(4).setCellEditor(new ComboBoxCellEditor(new JComboBox()));
        retval.getColumnModel().getColumn(4).setCellRenderer(new ComboBoxTableCellRenderer(Constants.OPERATORS));


        retval.getColumnModel().getColumn(6).setCellEditor(new ComboBoxCellEditor(new JComboBox()));
        retval.getColumnModel().getColumn(6).setCellRenderer(new ComboBoxTableCellRenderer(Constants.CLOSE_PARENTHESIS));

        return retval;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (Constants.ADD_COMPARISON_ACTION.equals(e.getActionCommand())) {
            
        } else if (Constants.DELETE_COMPARISON_ACTION.equals(e.getActionCommand())) {
            
        }
    }

    @Override
    protected void handlePanelShown() {
        populateSelectedTables(tp, 2);
    }

    public class WhereColumnData {
        private TableData tableData;
        private ColumnData columnData;
        private String openParenthesis;
        private String closeParenthesis;
        private String andOr;
        private String value;
        private String operator;

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

        public String getOpenParenthesis() {
            return openParenthesis;
        }

        public void setOpenParenthesis(String openParenthesis) {
            this.openParenthesis = openParenthesis;
        }

        public String getCloseParenthesis() {
            return closeParenthesis;
        }

        public void setCloseParenthesis(String closeParenthesis) {
            this.closeParenthesis = closeParenthesis;
        }

        public String getAndOr() {
            return andOr;
        }

        public void setAndOr(String andOr) {
            this.andOr = andOr;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getOperator() {
            return operator;
        }

        public void setOperator(String operator) {
            this.operator = operator;
        }

        
    }
    
    public List <WhereColumnData> getWhereColumnData() {
        List <WhereColumnData> retval =  (List<WhereColumnData>)tp.getTable().getTableData();
        
        Iterator <WhereColumnData> it = retval.iterator();
        
        while (it.hasNext()) {
            WhereColumnData wcd = it.next();
            
            // remove any rows that are incomplete
            if ((wcd.getColumnData() == null) || (wcd.getTableData() == null)) {
                it.remove();;
            }
        }
        
        return retval;
    }

    @Override
    protected void handleColumnChanged(ColumnData cd) {
/*
        List <String> functions = Utils.getAggregateFunctionsForType(cd.getDataType());
        ComboBoxCellEditor editor = (ComboBoxCellEditor)tp.getTable().getColumnModel().getColumn(2).getCellEditor();
        editor.getComboBox().removeAllItems();

        ComboBoxTableCellRenderer renderer = (ComboBoxTableCellRenderer)tp.getTable().getColumnModel().getColumn(2).getCellRenderer();
        renderer.removeAllItems();

        for (String f : functions) {
            renderer.addItem(f);
            editor.getComboBox().addItem(f);
        }
    */
    }
}
