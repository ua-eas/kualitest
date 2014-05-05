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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import org.kuali.test.utils.Constants;


public class CalendarTableCellRenderer extends DefaultTableCellRenderer {
    private SimpleDateFormat df;
    public CalendarTableCellRenderer() {
        this(Constants.DEFAULT_TIMESTAMP_FORMAT_STRING);
    }
    
    public CalendarTableCellRenderer(String format) {
        df = new SimpleDateFormat(format);
    }
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        JLabel retval = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column); 
        
        if (value == null) {
            retval.setText("");
        } else {
            Calendar c = (Calendar)value;
            retval.setText(df.format(c.getTime()));
        }
        
        return retval;
    }
}
