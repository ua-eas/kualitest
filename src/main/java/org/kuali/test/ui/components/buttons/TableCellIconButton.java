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
package org.kuali.test.ui.components.buttons;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionListener;
import java.util.EventObject;
import javax.swing.BorderFactory ;
import javax.swing.ImageIcon ;
import javax.swing.JButton ;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.EventListenerList;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author rbtucker
 */
public class TableCellIconButton extends JPanel implements TableCellEditor, TableCellRenderer {
    private int currentRow = -1;
    private JButton button;
    private EventListenerList listeners = new EventListenerList();
    /**
     *
     * @param icon
     */
    public TableCellIconButton(ImageIcon icon) {
        super(new BorderLayout(0, 0));
        add(button = new JButton(icon), BorderLayout.CENTER);
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setContentAreaFilled(false);
        setBorder(BorderFactory.createEmptyBorder());
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        currentRow = row;
        return this;
    }

    @Override
    public Object getCellEditorValue() {
        return this;
    }

    @Override
    public boolean isCellEditable(EventObject anEvent) {
        return true;
    }

    @Override
    public boolean shouldSelectCell(EventObject anEvent) {
        return false;
    }

    @Override
    public boolean stopCellEditing() {
        fireEditingCancelled();
        return true;
    }

    @Override
    public void cancelCellEditing() {
        fireEditingCancelled();
    }

    public void addActionListener(ActionListener listener) {
        button.addActionListener(listener);
    }
    
    @Override
    public void addCellEditorListener(CellEditorListener l) {
        listeners.add(CellEditorListener.class, l);
    }

    @Override
    public void removeCellEditorListener(CellEditorListener l) {
        listeners.remove(CellEditorListener.class, l);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (isSelected) {
            setForeground(UIManager.getColor("Table.selectionForeground"));
            setBackground(UIManager.getColor("Table.selectionBackground"));
        } else {
            setForeground(UIManager.getColor("Table.foreground"));
            setBackground(UIManager.getColor("Table.background"));
        }
        
        return this;
    }
    
    protected void  fireEditingStopped() {
        ChangeEvent ce = new ChangeEvent(this);
        for (Object listener : listeners.getListenerList()) {
            if (listener instanceof CellEditorListener) {
                ((CellEditorListener)listener).editingStopped(ce);
            }
        }
    }

    protected void fireEditingCancelled() {
        ChangeEvent ce = new ChangeEvent(this);
        for (Object listener : listeners.getListenerList()) {
            if (listener instanceof CellEditorListener) {
                ((CellEditorListener)listener).editingCanceled(ce);
            }
        }
    }

    /**
     *
     * @return
     */
    public int getCurrentRow() {
        return currentRow;
    }
}
