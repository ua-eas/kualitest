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
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
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

/**
 *
 * @author rbtucker
 */
public class SqlSelectPanel extends BaseSqlPanel <SelectColumnData> {
    private static final Logger LOG = Logger.getLogger(SqlSelectPanel.class);
    private JCheckBox distinct;
    private JButton addAllButton;
    private TablePanel tp;
    
    /**
     *
     * @param mainframe
     * @param dbPanel
     */
    public SqlSelectPanel(TestCreator mainframe, DatabasePanel dbPanel) {
        super(mainframe, dbPanel, SelectColumnData.class);
        initComponents();
    }


    private void initComponents() {
        tp = new TablePanel(getSelectColumnTable());
        
        setTablePanel(tp);
        
        createTableCellEditorRenderer(0, 1);
        createColumnCellEditorRenderer(1);

        addAllButton = tp.addButton(this, Constants.ADD_ALL_COLUMNS_ACTION, Constants.ADD_ICON, "add all selected columns");
        addAllButton.setEnabled(getDbPanel().haveSelectedColumns());
        tp.addAddButton(this, Constants.ADD_COLUMN_ACTION, "add new select column");
        tp.getAddButton().setEnabled(getDbPanel().haveSelectedColumns());
        tp.addDeleteButton(this, Constants.DELETE_COLUMN_ACTION, "delete selected column");
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

    /**
     *
     * @param scd
     * @return
     */
    @Override
    protected boolean validateRequiredFields(SelectColumnData scd) {
        return ((scd.getTableData() != null) && (scd.getColumnData() != null));
    }
    
    /**
     *
     */
    @Override
    protected void handlePanelShown() {
        populateSelectedTables(0);
        addAllButton.setEnabled(getDbPanel().haveSelectedColumns());
        tp.getAddButton().setEnabled(getDbPanel().haveSelectedColumns());
        
        checkSelectedColumns();
    }
    
    private void checkSelectedColumns() {
        Iterator <SelectColumnData> it = tp.getTable().getTableData().iterator();
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
    
    /**
     *
     * @return
     */
    @Override
    protected String getAddAction() {
        return Constants.ADD_COLUMN_ACTION;
    }

    /**
     *
     * @return
     */
    @Override
    protected String getDeleteAction() {
        return Constants.DELETE_COLUMN_ACTION;
    }

    /**
     *
     * @param cd
     */
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

    /**
     *
     * @return
     */
    @Override
    protected String getRequiredColumnList() {
        return "table, column";
    }
    
    /**
     *
     * @return
     */
    public boolean isDistinct() {
        return distinct.isSelected();
    }
    
    /**
     *
     * @return
     */
    @Override
    public boolean haveEntries() {
        return !getTable().getTableData().isEmpty();
    }

    @Override
    protected void handleUnprocessedActions(ActionEvent e) {
        if (Constants.ADD_ALL_COLUMNS_ACTION.equals(e.getActionCommand())) {
            List data = getTable().getTableData();
            
            if (data == null) {
                getTable().setTableData(data = new ArrayList());
            }
            
            if (!data.isEmpty()) {
                int rows = data.size();
                data.clear();
                getTable().getModel().fireTableRowsDeleted(0, rows);
            }
            
            List <TableData> tdlist = getDbPanel().getSelectedDbObjects();
            
            for (TableData td : tdlist) {
                for (ColumnData cd : td.getColumns()) {
                    if (cd.isSelected()) {
                        SelectColumnData scd = new SelectColumnData();
                        scd.setTableData(td);
                        scd.setColumnData(cd);
                        data.add(scd);
                    }
                }
            }

            if (!data.isEmpty()) {
                getTable().getModel().fireTableRowsInserted(0, data.size()-1);
            }
        }
    }
}
