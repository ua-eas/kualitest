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
import java.util.Iterator;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.ui.base.BaseTable;
import org.kuali.test.ui.base.TableConfiguration;
import org.kuali.test.ui.components.editmasks.FloatTextField;
import org.kuali.test.ui.components.editmasks.IntegerTextField;
import org.kuali.test.ui.components.editors.ComboBoxCellEditor;
import org.kuali.test.ui.components.editors.DateChooserCellEditor;
import org.kuali.test.ui.components.panels.TablePanel;
import org.kuali.test.ui.components.renderers.ComboBoxTableCellRenderer;
import org.kuali.test.ui.components.sqlquerytree.ColumnData;
import org.kuali.test.ui.components.sqlquerytree.TableData;
import org.kuali.test.utils.Constants;
import org.kuali.test.utils.Utils;

/**
 *
 * @author rbtucker
 */
public class SqlWherePanel extends BaseSqlPanel <WhereColumnData> {
    private static final Logger LOG = Logger.getLogger(SqlWherePanel.class);
    private final TableCellEditor intCellEditor;
    private final TableCellEditor floatCellEditor;
    private final DateChooserCellEditor dateCellEditor;
    private final DateChooserCellEditor dateCellRenderer;
    private final TableCellEditor defaultCellEditor;
    private final DefaultTableCellRenderer defaultCellRenderer;
    private TablePanel tp;
    /**
     *
     * @param mainframe
     * @param dbPanel
     */
    public SqlWherePanel(TestCreator mainframe, DatabasePanel dbPanel) {
        super(mainframe, dbPanel, WhereColumnData.class);
        intCellEditor = new WhereValueCellEditor(dbPanel, new IntegerTextField());
        floatCellEditor = new DefaultCellEditor(new FloatTextField());
        dateCellEditor = new DateChooserCellEditor();
        dateCellRenderer = new DateChooserCellEditor();
        defaultCellEditor = new WhereValueCellEditor(dbPanel, new JTextField());
        defaultCellRenderer = new DefaultTableCellRenderer();

        initComponents();
    }

    private void initComponents() {
        tp = new TablePanel(getWhereColumnTable());
        setTablePanel(tp);
        
        createTableCellEditorRenderer(2, 3);
        createColumnCellEditorRenderer(3);
        
        tp.addAddButton(this, Constants.ADD_COMPARISON_ACTION, "add new where comparison");
        tp.getAddButton().setEnabled(false);
        tp.addDeleteButton(this, Constants.DELETE_COMPARISON_ACTION, "delete selected row");
        
        add(tp, BorderLayout.CENTER);
    }
    
    private BaseTable getWhereColumnTable() {
        TableConfiguration tc = new TableConfiguration();
        
        tc.setHeaders(new String[] {
            "and/or",
            "(",
            "table",
            "column",
            "operator",
            "value",
            ")"
        });
        
        tc.setPropertyNames(new String[] {
            "andOr", // 0
            "openParenthesis", // 1
            "tableData", // 2
            "columnData", // 3
            "operator", // 4
            "value", // 5
            "closeParenthesis" // 6
        });
        
        tc.setColumnTypes(new Class[] {
            String.class,
            String.class,
            TableData.class,
            ColumnData.class,
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
                WhereColumnData wcd = (WhereColumnData)getTableData().get(row);
                
                switch(column) {
                    case 0:
                        retval = (row > 0);
                        break;
                    case 1:
                    case 2:
                    case 6:
                        retval = true;
                        break;
                    case 3:
                        retval = (wcd.getTableData() != null);
                        break;
                    case 4:
                        retval = (wcd.getColumnData() != null);
                        break;
                    case 5:
                        retval = ((wcd.getColumnData() != null) 
                            && !Constants.NULL.equals(wcd.getOperator())
                            && !Constants.NOT_NULL.equals(wcd.getOperator()));
                        
                        if (!retval) {
                            wcd.setValue(null);
                        }
                        break;
                }
                
                return retval;
            }

            @Override
            protected String getTooltip(int row, int col) {
                String retval = null;
                if (col == 0) {
                    WhereColumnData wcd =  (WhereColumnData)getTableData().get(row);
                    
                    if (wcd != null) {
                        TableData td = wcd.getTableData();
                        
                        if (td != null) {
                            retval = getDbPanel().getTableDataTooltip(td);
                        }
                    }
                }
                
                return retval;
            }

            @Override
            public Object getValueAt(int row, int column) {
                // no and/or on first row
                if ((row == 0) && (column == 0)) {
                    return null;
                } else {
                    return super.getValueAt(row, column); 
                }
            }

            @Override
            public TableCellEditor getCellEditor(int row, int column) {
                TableCellEditor retval = null;
                if (column == 5) {
                    WhereColumnData wcd = (WhereColumnData)getTable().getTableData().get(row);
                    if (wcd != null) {
                        retval = getValueCellEditor(wcd.getColumnData());
                    } else {
                        retval = defaultCellEditor;
                    }
                } else {
                    retval = super.getCellEditor(row, column); 
                }
                
                return retval;
            }

            @Override
            public TableCellRenderer getCellRenderer(int row, int column) {
                TableCellRenderer retval = null;
                // handle value entry editor
                if (column == 5) {
                    WhereColumnData wcd = (WhereColumnData)getTable().getTableData().get(row);
                    if (wcd != null) {
                        retval = getValueCellRenderer(wcd.getColumnData());
                    } else {
                        retval = defaultCellRenderer;
                    }
                } else {
                    retval = super.getCellRenderer(row, column); 
                }
                
                return retval;
            }
        };
        
        retval.getColumnModel().getColumn(0).setCellEditor(new ComboBoxCellEditor(new JComboBox(Constants.AND_OR)));
        retval.getColumnModel().getColumn(0).setCellRenderer(new ComboBoxTableCellRenderer(Constants.AND_OR));
        
        retval.getColumnModel().getColumn(1).setCellEditor(new ComboBoxCellEditor(new JComboBox(Constants.OPEN_PARENTHESIS)));
        retval.getColumnModel().getColumn(1).setCellRenderer(new ComboBoxTableCellRenderer(Constants.OPEN_PARENTHESIS));

        retval.getColumnModel().getColumn(4).setCellEditor(new ComboBoxCellEditor(new JComboBox(Constants.OPERATORS)));
        retval.getColumnModel().getColumn(4).setCellRenderer(new ComboBoxTableCellRenderer(Constants.OPERATORS));

        retval.getColumnModel().getColumn(6).setCellEditor(new ComboBoxCellEditor(new JComboBox(Constants.CLOSE_PARENTHESIS)));
        retval.getColumnModel().getColumn(6).setCellRenderer(new ComboBoxTableCellRenderer(Constants.CLOSE_PARENTHESIS));

        return retval;
    }

    /**
     *
     * @param wcd
     * @return
     */
    @Override
    protected boolean validateRequiredFields(WhereColumnData wcd) {
        return (StringUtils.isNotBlank(wcd.getOperator())  && StringUtils.isNotBlank(wcd.getValue()));
    }

    /**
     *
     */
    @Override
    protected void handlePanelShown() {
        populateSelectedTables(2);
        checkSelectedColumns();
    }
    
    private void checkSelectedColumns() {
        Iterator <WhereColumnData> it = tp.getTable().getTableData().iterator();
        boolean itemsRemoved = false;
        while (it.hasNext()) {
            if (!it.next().getColumnData().isSelected()) {
                it.remove();
                itemsRemoved = true;
            }
        }
        
        if (itemsRemoved) {
            tp.getTable().getModel().fireTableDataChanged();
        }
    }


    private TableCellEditor getValueCellEditor(ColumnData cd) {
        TableCellEditor retval = null;
        
        if (cd != null) {
            if (Utils.isIntegerJdbcType(cd.getDataType(), cd.getDecimalDigits())) {
                retval = intCellEditor;
            } else if (Utils.isFloatJdbcType(cd.getDataType(),cd.getDecimalDigits())) {
                retval = floatCellEditor;
            } else if (Utils.isDateJdbcType(cd.getDataType()) || Utils.isTimestampJdbcType(cd.getDataType())) {
                retval = dateCellEditor;
            } else {
                retval = defaultCellEditor;
            }
        } else {
            retval = defaultCellEditor;
        }
        
        return retval;
    }
    
    private TableCellRenderer getValueCellRenderer(ColumnData cd) {
        TableCellRenderer retval = null;
        
        if (cd == null) {
            retval = defaultCellRenderer;
        } else {
            if (Utils.isDateJdbcType(cd.getDataType()) || Utils.isTimestampJdbcType(cd.getDataType())) {
                retval = dateCellRenderer;
            } else {
                retval = defaultCellRenderer;
            }
        }
        
        return retval;
    }
    
    /**
     *
     * @return
     */
    @Override
    protected String getAddAction() {
        return Constants.ADD_COMPARISON_ACTION;
    }

    /**
     *
     * @return
     */
    @Override
    protected String getDeleteAction() {
        return Constants.DELETE_COMPARISON_ACTION;
    }

    /**
     *
     * @return
     */
    @Override
    protected String getRequiredColumnList() {
        return "table, column, operator, value";
    }

    /**
     *
     * @return
     */
    @Override
    public boolean haveEntries() {
        return !getTable().getTableData().isEmpty();
    }

    /**
     *
     * @param cd
     */
    @Override
    protected void initializeColumnData(WhereColumnData cd) {
        if (!getTable().getTableData().isEmpty()) {
            cd.setAndOr(Constants.AND);
        }
        
        cd.setOperator(Constants.EQUAL_TO);
    }
}
