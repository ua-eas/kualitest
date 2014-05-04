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

import javax.swing.JComboBox;
import org.kuali.test.ComparisonOperator;
import org.kuali.test.FailureAction;
import org.kuali.test.ValueType;
import org.kuali.test.ui.base.BaseTable;
import org.kuali.test.ui.base.TableConfiguration;
import org.kuali.test.ui.components.editors.CheckboxTableCellEditor;
import org.kuali.test.ui.components.editors.ComboBoxCellEditor;
import org.kuali.test.ui.components.renderers.CheckboxTableCellRenderer;
import org.kuali.test.ui.components.renderers.ComboBoxCellRenderer;
import org.kuali.test.utils.Utils;


public class CheckpointTable extends BaseTable {
    public CheckpointTable(TableConfiguration config) {
        super(config);
        
        getColumnModel().getColumn(0).setCellEditor(new CheckboxTableCellEditor());
        getColumnModel().getColumn(0).setCellRenderer(new CheckboxTableCellRenderer());
        
        String[] valueTypes = Utils.getXmlEnumerations(ValueType.class, true);
        JComboBox cb = new JComboBox(valueTypes);
        getColumnModel().getColumn(2).setCellEditor(new ComboBoxCellEditor(cb));
        getColumnModel().getColumn(2).setCellRenderer(new ComboBoxCellRenderer(valueTypes));

        String[] comparisonOperators = Utils.getXmlEnumerations(ComparisonOperator.class, true);
        cb = new JComboBox(comparisonOperators);
        getColumnModel().getColumn(3).setCellEditor(new ComboBoxCellEditor(cb));
        getColumnModel().getColumn(3).setCellRenderer(new ComboBoxCellRenderer(comparisonOperators));
        
        String[] failureActions = Utils.getXmlEnumerations(FailureAction.class, true);
        cb = new JComboBox(failureActions);
        getColumnModel().getColumn(5).setCellEditor(new ComboBoxCellEditor(cb));
        getColumnModel().getColumn(5).setCellRenderer(new ComboBoxCellRenderer(failureActions));

    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return ((column == 0) || isRowEditable(row));
    }
    
    private boolean isRowEditable(int row) {
        Boolean retval = (Boolean)getValueAt(row, 0);
        return retval.booleanValue();
    }
}
