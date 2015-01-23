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
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EventObject;
import java.util.List;
import javax.swing.DefaultCellEditor;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.CellEditorListener;
import javax.swing.table.TableCellEditor;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.test.Column;
import org.kuali.test.Table;
import org.kuali.test.TestOperation;
import org.kuali.test.ui.base.BaseTable;
import org.kuali.test.ui.base.ItemSelectDlg;
import org.kuali.test.ui.components.buttons.SearchButton;
import org.kuali.test.utils.Constants;
import org.kuali.test.utils.Utils;


public class WhereValueCellEditor extends JPanel implements TableCellEditor, ActionListener {
    private static final Logger LOG = Logger.getLogger(WhereValueCellEditor.class);
    
    private DefaultCellEditor cellEditor;
    private SearchButton lookup;
    private SearchButton executionParameter;
    private DatabasePanel dbPanel;
    private WhereColumnData currentRowData;
    private BaseTable table;
    
    public WhereValueCellEditor(DatabasePanel dbPanel, JTextField tf) {
        super(new BorderLayout());
        this.dbPanel = dbPanel;
        cellEditor = new DefaultCellEditor(tf);
        add(cellEditor.getComponent(), BorderLayout.CENTER);
        
        JPanel  p = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 2));
        
        p.add(lookup = new SearchButton());
        p.add(new JSeparator(JSeparator.VERTICAL));
        
        if (dbPanel.isForCheckpoint()) {
            p.add(executionParameter = new SearchButton(Constants.ADD_PARAM_ICON));
            executionParameter.addActionListener(this);
        }
        
        add(p, BorderLayout.EAST);
        
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
        
        if (dbPanel.isForCheckpoint()) {
            List <String> l = this.getAvailableTestExecutionParameters();
            executionParameter.setEnabled(!l.isEmpty());
        }
        
        if (lookup.isEnabled() || ((executionParameter != null) && executionParameter.isEnabled())) {
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
    public void actionPerformed(ActionEvent e) {
        if (currentRowData != null) {
            if (e.getSource() == lookup) {
                Column col = findColumn(currentRowData);
                String sql = col.getLookupSqlSelect();

                if (StringUtils.isBlank(sql)) {
                    sql = dbPanel.getGlobalLookupSql(col.getColumnName());
                }

                if (StringUtils.isNotBlank(sql)) {
                    WhereValueLookupDlg dlg = null;
                    if (dbPanel.isForCheckpoint()) {
                        dlg = new WhereValueLookupDlg(dbPanel.getMainframe(), Utils.getParentDialog(this), dbPanel.getPlatform(), sql);
                    } else {
                        dlg = new WhereValueLookupDlg(dbPanel.getMainframe(), dbPanel.getPlatform(), sql);
                    }

                    LookupValue value = dlg.getLookupValue();
                    if (value != null) {
                        JTextField tf = (JTextField)cellEditor.getComponent();
                        tf.setText(value.getName());
                    }
                }
            } else if (e.getSource() == executionParameter) {
                handleTestExecutionParameterSelect();
            }

            currentRowData = null;

            stopCellEditing();
        }
    }

    private List<String> getAvailableTestExecutionParameters() {
        List <String> retval = new ArrayList<String>();
        
        for (TestOperation op : dbPanel.getTestProxyServer().getTestOperations()) {
            if (op.getOperation().getTestExecutionParameter() != null) {
                retval.add(op.getOperation().getTestExecutionParameter().getName());
            }
        }
        
        Collections.sort(retval);
        
        return retval;
    }

    private void handleTestExecutionParameterSelect() {
        ItemSelectDlg dlg = new ItemSelectDlg(dbPanel.getMainframe(), 
            "Available Parameters", this.getAvailableTestExecutionParameters());

        String param = dlg.getSelectedValue();
        
        if (StringUtils.isNotBlank(param)) {
            JTextField tf = (JTextField)cellEditor.getComponent();
            tf.setText(Constants.TEST_EXECUTION_PARAMETER_PREFIX + param + Constants.TEST_EXECUTION_PARAMETER_SUFFIX);
        }
    }
}
