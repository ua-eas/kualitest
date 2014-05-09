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

package org.kuali.test.ui.components.dialogs;

import java.awt.Component;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import org.kuali.test.ComparisonOperator;
import org.kuali.test.FailureAction;
import org.kuali.test.ValueType;
import org.kuali.test.ui.base.BaseTable;
import org.kuali.test.ui.base.BaseTableModel;
import org.kuali.test.ui.base.TableConfiguration;
import org.kuali.test.ui.components.editors.CheckboxTableCellEditor;
import org.kuali.test.ui.components.editors.ComboBoxCellEditor;
import org.kuali.test.ui.components.renderers.CheckboxTableCellRenderer;
import org.kuali.test.ui.components.renderers.ComboBoxTableCellRenderer;
import org.kuali.test.utils.Constants;
import org.kuali.test.utils.Utils;


public class CheckpointTable extends BaseTable {
    public CheckpointTable(TableConfiguration config) {
        super(config);
        
        getColumnModel().getColumn(0).setCellEditor(new CheckboxTableCellEditor() {
            @Override
            protected void handleEditingStopped() {
                int row = getSelectedRow();
                if (row > -1) {
                    BaseTableModel tm = (BaseTableModel)getModel();
                    
                    // if checkbox unchecked then clear selections
                    Boolean b = (Boolean)tm.getValueAt(row, 0);
                    if (!b) {
                        tm.setValueAt(null, row, 3);
                        tm.setValueAt(null, row, 4);
                        tm.setValueAt(null, row, 6);
                    }
                }
            }
        });
        
        getColumnModel().getColumn(0).setCellRenderer(new CheckboxTableCellRenderer());
        getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel retval = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (Constants.DEFAULT_HTML_PROPERTY_SECTION.equals(value)) {
                    retval.setText("");
                }
                
                return retval;
            }
        });

        String[] valueTypes = Utils.getXmlEnumerations(ValueType.class, true);
        JComboBox cb = new JComboBox(valueTypes);
        getColumnModel().getColumn(3).setCellEditor(new ComboBoxCellEditor(cb));
        getColumnModel().getColumn(3).setCellRenderer(new ComboBoxTableCellRenderer(valueTypes));

        String[] comparisonOperators = Utils.getXmlEnumerations(ComparisonOperator.class, true);
        cb = new JComboBox(comparisonOperators);
        getColumnModel().getColumn(4).setCellEditor(new ComboBoxCellEditor(cb));
        getColumnModel().getColumn(4).setCellRenderer(new ComboBoxTableCellRenderer(comparisonOperators));
        
        String[] failureActions = Utils.getXmlEnumerations(FailureAction.class, true);
        cb = new JComboBox(failureActions);
        getColumnModel().getColumn(6).setCellEditor(new ComboBoxCellEditor(cb));
        getColumnModel().getColumn(6).setCellRenderer(new ComboBoxTableCellRenderer(failureActions));

    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return ((column == 0) || ((column > 1) && isRowEditable(row)));
    }
    
    private boolean isRowEditable(int row) {
        return (Boolean)getValueAt(row, 0);
    }
}
