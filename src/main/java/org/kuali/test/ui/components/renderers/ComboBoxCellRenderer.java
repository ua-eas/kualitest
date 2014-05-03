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
package org.kuali.test.ui.components.renderers;

import java.awt.Component;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.table.TableCellRenderer;

public class ComboBoxCellRenderer extends JComboBox implements TableCellRenderer {
    public ComboBoxCellRenderer(Object[] items) {
        super(items);
        setUI(new BasicComboBoxUI());
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
        boolean isSelected, boolean hasFocus, int row, int column) {
        if (isSelected) {
            setForeground(table.getSelectionForeground());
            setBackground(table.getSelectionBackground());
        } else {
            setForeground(table.getForeground());
            setBackground(table.getBackground());
        }
        // Select the current value
        if (value != null) {
            if (!table.getModel().getColumnClass(column).equals(value.getClass())) {
                value = value.toString();
            }
        }
        
        setSelectedItem(value);
        
        return this;
    }
}
