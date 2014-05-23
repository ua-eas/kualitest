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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.ui.base.BasePanel;
import org.kuali.test.ui.components.editors.ComboBoxCellEditor;
import org.kuali.test.ui.components.renderers.ComboBoxTableCellRenderer;
import org.kuali.test.ui.components.sqlquerytree.ColumnData;
import org.kuali.test.ui.components.sqlquerytree.TableData;


public class BaseSqlPanel extends BasePanel implements ComponentListener {
    private DatabasePanel dbPanel;
    private JComboBox selectedTables;

    public BaseSqlPanel(TestCreator mainframe, DatabasePanel dbPanel) {
        super(mainframe);
        this.dbPanel = dbPanel;
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

    protected void populateSelectedTables(TablePanel tp, int col) {
        tp.getAddButton().setEnabled(getDbPanel().haveSelectedColumns());
        
        selectedTables.removeAllItems();
        
        // this is the table renderer for "tables" column
        JComboBox cb = (JComboBox)tp.getTable().getColumnModel().getColumn(col).getCellRenderer();
        cb.removeAllItems();
        
        if (getDbPanel().haveSelectedColumns()) {
            List <TableData> selectedDbObjects = getDbPanel().getSelectedDbObjects();
            
            for (TableData td : selectedDbObjects) {
                selectedTables.addItem(td);
                cb.addItem(td);
            }
        }
    }
    
   protected void createTableCellEditorRenderer(final TablePanel tp, final int col) {
        selectedTables = new JComboBox();
        
        tp.getTable().getColumnModel().getColumn(0).setCellEditor(new ComboBoxCellEditor(selectedTables));
        
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
                    ComboBoxCellEditor editor = (ComboBoxCellEditor)tp.getTable().getColumnModel().getColumn(col).getCellEditor();
                    editor.getComboBox().removeAllItems();
                    JComboBox renderer = (JComboBox)tp.getTable().getColumnModel().getColumn(col).getCellRenderer();
                    renderer.removeAllItems();

                    for (ColumnData cd : getSelectedColumnData(td)) {
                        renderer.addItem(cd);
                        editor.getComboBox().addItem(cd);
                    }
                }
            }
        });
        
        tp.getTable().getColumnModel().getColumn(0).setCellRenderer(new ComboBoxTableCellRenderer(new TableData[0]));
    }
    
    protected void createColumnCellEditorRenderer(final TablePanel tp, final int col) {
        JComboBox cb = new JComboBox();
        tp.getTable().getColumnModel().getColumn(col).setCellEditor(new ComboBoxCellEditor(cb));
        
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

        tp.getTable().getColumnModel().getColumn(col).setCellRenderer(new ComboBoxTableCellRenderer(new ColumnData[0]));
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
}
