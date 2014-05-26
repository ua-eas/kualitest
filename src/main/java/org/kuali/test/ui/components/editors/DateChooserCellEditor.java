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

package org.kuali.test.ui.components.editors;

import com.toedter.calendar.JDateChooser;
import com.toedter.calendar.JDateChooserCellEditor;
import java.awt.Component;
import java.text.ParseException;
import java.util.Date;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;
import org.kuali.test.utils.Constants;


public class DateChooserCellEditor extends JDateChooserCellEditor implements TableCellRenderer {
    private final JDateChooser renderer = new JDateChooser();
    
    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        JDateChooser retval = (JDateChooser)super.getTableCellEditorComponent(table, value, isSelected, row, column);
        retval.getDateEditor().getUiComponent().setBorder(null);
        
        if (value != null) {
            try {
                retval.getDateEditor().setDate(Constants.DEFAULT_DATE_FORMAT.parse(value.toString()));
            }

            catch (ParseException ex) {
                retval.getDateEditor().setDate(null);
            }
        } else {
            retval.getDateEditor().setDate(null);
        }
        
        
        return retval;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        renderer.getDateEditor().getUiComponent().setBorder(null);
        
        Object dt = table.getValueAt(row, column);
        
        if (dt != null) {
            try {
                renderer.setDate(Constants.DEFAULT_DATE_FORMAT.parse(dt.toString()));
            }
            
            catch (ParseException ex) {
                renderer.setDate(null);
            }
        } else {
            renderer.setDate(null);
        }
        
                
        renderer.getDateEditor().getUiComponent().setOpaque(true);
        if (isSelected) {
            renderer.getDateEditor().getUiComponent().setForeground(UIManager.getColor("Table.selectionForeground"));
            renderer.getDateEditor().getUiComponent().setBackground(UIManager.getColor("Table.selectionBackground"));
        } else {
            renderer.getDateEditor().getUiComponent().setForeground(UIManager.getColor("Table.foreground"));
            renderer.getDateEditor().getUiComponent().setBackground(UIManager.getColor("Table.background"));
        }

        return renderer;
    }

    @Override
    public Object getCellEditorValue() {
        String retval = null;
        
        Date dt = (Date)super.getCellEditorValue();
        
        if (dt != null) {
            retval = Constants.DEFAULT_DATE_FORMAT.format(dt);
        }
        
        return retval;
    }
}
