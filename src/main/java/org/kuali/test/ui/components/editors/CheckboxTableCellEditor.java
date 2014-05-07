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

import java.awt.Component;
import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;


public class CheckboxTableCellEditor extends DefaultCellEditor implements CellEditorListener {
    public CheckboxTableCellEditor() {
        super(new JCheckBox());
        addCellEditorListener(this);
    }

    @Override
    public void editingStopped(ChangeEvent e) {
        handleEditingStopped();
    }

    @Override
    public void editingCanceled(ChangeEvent e) {
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        JCheckBox retval =  (JCheckBox)super.getTableCellEditorComponent(table, value, isSelected, row, column); 
        retval.setHorizontalAlignment(SwingConstants.CENTER);
        return retval;
    }
    
    protected void handleEditingStopped() {
    }
}
