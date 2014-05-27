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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.ui.base.BasePanel;
import org.kuali.test.ui.base.BaseTable;
import org.kuali.test.ui.components.editors.ComboBoxCellEditor;
import org.kuali.test.ui.components.panels.TablePanel;
import org.kuali.test.ui.components.renderers.ComboBoxTableCellRenderer;
import org.kuali.test.ui.components.sqlquerytree.ColumnData;
import org.kuali.test.ui.components.sqlquerytree.TableData;
import org.kuali.test.ui.utils.UIUtils;


public class BaseSqlPanel <T extends BaseColumnData> extends BasePanel implements ComponentListener, ActionListener {
    private final DatabasePanel dbPanel;
    private JComboBox selectedTables;
    private TablePanel tablePanel;
    private Class columnTypeClass;
    
    public BaseSqlPanel(TestCreator mainframe, DatabasePanel dbPanel, Class columnTypeClass) {
        super(mainframe);
        this.dbPanel = dbPanel;
        this.columnTypeClass = columnTypeClass;
        addComponentListener(this);
    }

    public DatabasePanel getDbPanel() {
        return dbPanel;
    }

    @Override
    public void componentResized(ComponentEvent e) {
    }

    @Override
    public void componentMoved(ComponentEvent e) {
    }

    @Override
    public void componentShown(ComponentEvent e) {
        handlePanelShown();
    }

    @Override
    public void componentHidden(ComponentEvent e) {
    }
    
    protected void handlePanelShown() {
    }

    protected void populateSelectedTables(int tableIndex) {
        tablePanel.getAddButton().setEnabled(getDbPanel().haveSelectedColumns());
        
        selectedTables.removeAllItems();
        
        // this is the table renderer for "tables" column
        JComboBox cb = (JComboBox)getTable().getColumnModel().getColumn(tableIndex).getCellRenderer();
        cb.removeAllItems();
        
        if (getDbPanel().haveSelectedColumns()) {
            List <TableData> selectedDbObjects = getDbPanel().getSelectedDbObjects();
            
            for (TableData td : selectedDbObjects) {
                selectedTables.addItem(td);
                cb.addItem(td);
            }
        }
    }
    
   protected void createTableCellEditorRenderer(final int tableIndex, final int colIndex) {
        selectedTables = new JComboBox();
        
        getTable().getColumnModel().getColumn(tableIndex).setCellEditor(new ComboBoxCellEditor(selectedTables));
        
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

        
        getTable().getColumnModel().getColumn(tableIndex).setCellRenderer(new ComboBoxTableCellRenderer(new TableData[0]));
    }
    
    protected void createColumnCellEditorRenderer(final int colIndex) {
        JComboBox cb = new JComboBox();
        getTable().getColumnModel().getColumn(colIndex).setCellEditor(new ComboBoxCellEditor(cb) {

            @Override
            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
                JComboBox retval = (JComboBox)super.getTableCellEditorComponent(table, value, isSelected, row, column);
                retval.removeAllItems();
                
                BaseColumnData bcd = (BaseColumnData)getTable().getTableData().get(row);
                
                for (ColumnData cd : getSelectedColumnData(bcd.getTableData())) {
                    retval.addItem(cd);
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
                    handleColumnChanged(cd);
                }
            }
        });

        getTable().getColumnModel().getColumn(colIndex).setCellRenderer(new ComboBoxTableCellRenderer(new ColumnData[0]) {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JComboBox retval =  (JComboBox)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column); //To change body of generated methods, choose Tools | Templates.
                BaseColumnData cd = (BaseColumnData)getTable().getTableData().get(row);
                retval.removeAllItems();
                retval.addItem(cd.getColumnData());
                return retval;
            }
        });
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
    
    protected void handleColumnChanged(ColumnData cd) {
    }

    protected BaseTable getTable() {
        BaseTable retval = null;
        
        if (tablePanel != null) {
            retval = tablePanel.getTable();
        }
        
        return retval;
    }
    
    public void clear() {
        BaseTable t = getTable();
        
        if (t != null) {
            int lastRow = t.getTableData().size();
            if (lastRow > 0) {
                t.getTableData().clear();
                t.getModel().fireTableRowsDeleted(0, lastRow-1);
            }
        }
    }
    
    
    public List <T> getColumnData() {
        List <T> retval =  (List<T>)getTable().getTableData();
        
        BaseTable t = getTable();
        
        if (t != null) {
            if (!retval.isEmpty()) {
                int lastRow = retval.size() - 1;

                T cd = retval.get(retval.size() -1);

                // if last row is not complete remove it
                if (!isLastRowComplete(retval)) {
                    retval.remove(lastRow);
                    t.getModel().fireTableRowsDeleted(lastRow, lastRow);
                }
            }
        }
        
        return retval;
    }

    private boolean isLastRowComplete(List <T> columns) {
        boolean retval = false;
        
        T cd = columns.get(columns.size() - 1);
        if ((cd.getTableData() != null) && (cd.getColumnData() != null)) {
            retval = validateRequiredFields(cd);
        }
        
        return retval;
    }
    
    protected boolean validateRequiredFields(T cd) {
        return true;
    }
    
    
    @Override
    public void actionPerformed(ActionEvent e) {
        BaseTable t = getTable();
        
        if (t != null) {
            List <T> l = t.getTableData();
            if (e.getActionCommand().equals(getAddAction())) {
                boolean addRow = (l.isEmpty() || isLastRowComplete(l));
                if (addRow) {
                    try {
                        l.add((T)columnTypeClass.newInstance());
                        t.getModel().fireTableRowsInserted(l.size()-1, l.size()-1);
                    }
                    
                    catch (Exception ex) {
                        UIUtils.showError(this, "Error", ex.toString());
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Please complete required entries (" + getRequiredColumnList() + ")");
                }
            } else if (e.getActionCommand().equals(getDeleteAction())) {
                int row = t.getSelectedRow();

                if ((row > -1) && (l.size() > row)) {
                    l.remove(row);
                    t.getModel().fireTableRowsDeleted(row, row);
                }
            }
        }
    }

    protected String getAddAction() {
        return null;
    }

    protected String getDeleteAction() {
        return null;
    }
    
    protected String getRequiredColumnList() {
        return null;
    }

    public TablePanel getTablePanel() {
        return tablePanel;
    }

    public void setTablePanel(TablePanel tablePanel) {
        this.tablePanel = tablePanel;
    }
}
