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

import java.awt.Component;
import java.util.ArrayList;
import java.util.EventObject ;
import java.util.List ;
import javax.swing.BorderFactory ;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author rbtucker
 */
public class TableCellIconButton extends JButton implements TableCellEditor, TableCellRenderer {
    private int currentRow = -1;
    private List <CellEditorListener> listeners = new ArrayList<CellEditorListener>();
    /**
     *
     * @param icon
     */
    public TableCellIconButton(ImageIcon icon) {
        super(icon);
        setBorder(BorderFactory.createEmptyBorder());
        setContentAreaFilled(false);
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
        fireEditingStopped();
        return true;
    }

    @Override
    public void cancelCellEditing() {
        fireEditingCancelled();
    }

    @Override
    public void addCellEditorListener(CellEditorListener l) {
        listeners.add(l);
    }

    @Override
    public void removeCellEditorListener(CellEditorListener l) {
        listeners.remove(l);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        return this;
    }
    
    protected void  fireEditingStopped() {
        ChangeEvent ce = new ChangeEvent(this);
        for (CellEditorListener listener : listeners) {
            listener.editingStopped(ce);
        }
    }

    protected void fireEditingCancelled() {
        ChangeEvent ce = new ChangeEvent(this);
        for (CellEditorListener listener : listeners) {
            listener.editingCanceled(ce);
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
