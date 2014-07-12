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
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.DefaultCellEditor;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.CellEditorListener;
import javax.swing.table.TableCellEditor;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.test.Column;
import org.kuali.test.DatabaseConnection;
import org.kuali.test.Platform;
import org.kuali.test.Table;
import org.kuali.test.ui.base.BaseTable;
import org.kuali.test.ui.components.buttons.SearchButton;
import org.kuali.test.ui.utils.UIUtils;
import org.kuali.test.utils.Utils;


public class WhereValueCellEditor extends JPanel implements TableCellEditor, ActionListener {
    private static final Logger LOG = Logger.getLogger(WhereValueCellEditor.class);
    
    private DefaultCellEditor cellEditor;
    private SearchButton lookup;
    private DatabasePanel dbPanel;
    private WhereColumnData currentRowData;
    private Map <String, String> globalLookups = new HashMap<String, String>();
    private BaseTable table;
    
    public WhereValueCellEditor(DatabasePanel dbPanel, JTextField tf) {
        super(new BorderLayout());
        this.dbPanel = dbPanel;
        cellEditor = new DefaultCellEditor(tf);
        add(cellEditor.getComponent(), BorderLayout.CENTER);
        add(lookup = new SearchButton(), BorderLayout.EAST);
        lookup.setEnabled(false);
        lookup.addActionListener(this);
    }
    
    @Override
    public Component getTableCellEditorComponent(JTable jtable, java.lang.Object value, boolean isSelected, int rowIndex, int columnIndex) {
        cellEditor.getTableCellEditorComponent(table, value, isSelected, rowIndex, columnIndex);

        currentRowData = null;

        table = (BaseTable)jtable;
        WhereColumnData wcd = (WhereColumnData)table.getRowData(rowIndex);
        
        Column col = findColumn(wcd);
        
        lookup.setEnabled((col != null) && (StringUtils.isNotBlank(dbPanel.getGlobalLookupSql(col.getColumnName())) 
            || StringUtils.isNotBlank(col.getLookupSqlSelect())));
        
        if (lookup.isEnabled()) {
            currentRowData = wcd;
            wcd.setRow(rowIndex);
            wcd.setColumn(columnIndex);
        }
        
        return this;
    }

    private Column findColumn(WhereColumnData wcd) {
        Column retval = null;
        if (wcd != null) {
            Table t = dbPanel.getAdditionalDbInfo().get(wcd.getTableData().getName());
            if (t != null) {
                if (t.getColumns() != null) {
                    for (Column col : t.getColumns().getColumnArray()) {
                        if (col.getColumnName().equals(wcd.getColumnData().getName())) {
                            retval = col;
                            break;
                        }
                    }
                }
            }
        }
        
        return retval;
    }
    @Override
    public Object getCellEditorValue() {
        return cellEditor.getCellEditorValue();
    }

    @Override
    public boolean isCellEditable(EventObject eo) {
        return true;
    }

    @Override
    public boolean shouldSelectCell(EventObject eo) {
        return cellEditor.shouldSelectCell(eo);
    }

    @Override
    public boolean stopCellEditing() {
        currentRowData = null;
        return cellEditor.stopCellEditing();
    }

    @Override
    public void cancelCellEditing() {
        currentRowData = null;
        cellEditor.cancelCellEditing();
    }

    @Override
    public void addCellEditorListener(CellEditorListener cl) {
        cellEditor.addCellEditorListener(cl);
    }

    @Override
    public void removeCellEditorListener(CellEditorListener cl) {
        cellEditor.removeCellEditorListener(cl);
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (currentRowData != null) {
            Column col = findColumn(currentRowData);
            String sql = col.getLookupSqlSelect();
            
            if (StringUtils.isBlank(sql)) {
                sql = dbPanel.getGlobalLookupSql(col.getColumnName());
            }
            
            if (StringUtils.isNotBlank(sql)) {
                List <LookupValue> lookupValues = getLookupValues(sql);
                
                if (!lookupValues.isEmpty()) {
                    WhereValueLookupDlg dlg = new WhereValueLookupDlg(dbPanel.getMainframe(), lookupValues);
                    
                    LookupValue value = dlg.getLookupValue();
                    if (value != null) {
                        JTextField tf = (JTextField)cellEditor.getComponent();
                        tf.setText(value.getName());
                    }
                }
            }
            currentRowData = null;
        }
    }
    
    private List <LookupValue> getLookupValues(String sql) {
        List <LookupValue> retval = new ArrayList <LookupValue>();
        Connection conn = null;
        Statement stmt = null;
        ResultSet res = null;
        
        try {
            Platform platform = dbPanel.getPlatform();
            DatabaseConnection dbconn = Utils.findDatabaseConnectionByName(dbPanel.getMainframe().getConfiguration(), platform.getDatabaseConnectionName());
            
            if (dbconn != null) {
                conn = Utils.getDatabaseConnection(dbPanel.getMainframe().getEncryptionPassword(), dbconn);
                stmt = conn.createStatement();
                res = stmt.executeQuery(sql);
                
                while (res.next()) {
                    LookupValue val = new LookupValue();
                    val.setName(res.getString(1));
                    val.setValue(res.getString(2));
                    
                    retval.add(val);
                }
            }
            
            if (retval.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No lookup values found");
            }
        }
        
        catch (Exception ex) {
            LOG.error(ex.toString(), ex);
            UIUtils.showError(dbPanel.getMainframe(), 
                "Lookup Error", "Error occurred while attempting to load lookup data from database - " + ex.toString());
        }
        
        finally {
            Utils.closeDatabaseResources(conn, stmt, res);
        }
        
        return retval;
    }
}
