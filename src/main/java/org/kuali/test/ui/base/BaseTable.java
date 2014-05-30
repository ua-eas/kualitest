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

package org.kuali.test.ui.base;

import java.awt.Component;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.DefaultCellEditor;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import org.apache.commons.lang3.StringUtils;
import org.kuali.test.utils.Constants;


public class BaseTable extends JTable {
    private boolean initializing = true;
    public BaseTable(TableConfiguration config) {
        super(new BaseTableModel(config));

        for (int i = 0; i < config.getHeaders().length; ++i) {
            int cx = getColumnWidth(i);
            getColumnModel().getColumn(i).setWidth(cx);
            getColumnModel().getColumn(i).setPreferredWidth(cx);
        }
        
        getTableHeader().setReorderingAllowed(false);
        setShowHorizontalLines(true);
        setShowVerticalLines(true);
        setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
        initializing = false;
    }
    
    private String getColumnPreferenceKey(int col, String name) {
        StringBuilder retval = new StringBuilder(64);
        
        retval.append(getConfig().getTableName());
        retval.append(".column.");
        retval.append(getConfig().getPropertyNames()[col]);
        retval.append(".");
        retval.append(name);

        return retval.toString();
    }

    public TableConfiguration getConfig() {
        BaseTableModel tm = (BaseTableModel)getModel();
        return tm.getConfig();
    }
    
    public void saveTablePreferences() {
        TableConfiguration config = (TableConfiguration)getConfig();
        Preferences proot = Preferences.userRoot();
        Preferences node = proot.node(Constants.PREFS_TABLE_NODE);
        
        for (int i = 0; i < config.getPropertyNames().length; ++i) {
            String key = getColumnPreferenceKey(i, "width");
            node.putInt(key, getColumnModel().getColumn(i).getPreferredWidth());
        }
    }
    
    private int getColumnWidth(int col) {
        int retval = getConfig().getColumnWidths()[col];
        Preferences proot = Preferences.userRoot();
        Preferences node = proot.node(Constants.PREFS_TABLE_NODE);
        retval = node.getInt(getColumnPreferenceKey(col, "width"), retval);
        return retval;
    }

    protected TableColumn getTableColumn(int col) {
        int colwidth =  getColumnWidth(col);

        return new TableColumn(col, 
            colwidth, 
            getTableCellRenderer(getConfig(), col),
            getTableCellEditor(getConfig(), col));
    }

    protected TableCellRenderer getTableCellRenderer(final TableConfiguration config, final int col) {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel retval =  (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                retval.setHorizontalAlignment(config.getColumnAlignment()[col]);
                return retval;
            }
        };
    }

    protected TableCellEditor getTableCellEditor(TableConfiguration config, int col) {
        return new DefaultCellEditor(new JTextField());
    }
 
    @Override
    public void columnMarginChanged(ChangeEvent e) {
        if (!initializing) {
            super.columnMarginChanged(e);
            saveTablePreferences();
        }
    }

    public List getTableData() {
        BaseTableModel tm = getModel();
        return tm.getData();
    }

    public Object getTableDataAt(int row) {
        Object retval = null;
        List l = getTableData();
        
        if ((l != null) && (row < l.size())) {
            retval = l.get(row);
        }
        
        return retval;
    }

    @Override
    public BaseTableModel getModel() {
        return (BaseTableModel)super.getModel();
    }
    
    @Override
   public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
        Component retval = super.prepareRenderer(renderer, row, column);
        if (retval instanceof JComponent) {
            JComponent jc = (JComponent)retval;
            String tooltip = getTooltip(row, column);
            
            if (StringUtils.isNotBlank(tooltip)) {
                jc.setToolTipText(tooltip);
            }
            
        }
        
        return retval;

    }
   
    protected String getTooltip(int row, int col) {
        return null;
    }

    public void clear() {
        BaseTableModel tm = this.getModel();
        List l = tm.getData();
        int size = l.size();
        if (size > 0) {
            l.clear();
            tm.fireTableRowsDeleted(0, size-1);
        }
    }
    
    public void setTableData(List data) {
        clear();
        getModel().setData(data);
    }
}