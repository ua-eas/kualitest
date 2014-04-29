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

package org.kuali.test.ui.components.editmasks;

import javax.swing.JFormattedTextField;
import javax.swing.text.MaskFormatter;


public class BaseEditMaskField extends JFormattedTextField {
    private static final int DEFAULT_NUM_COLUMNS = 10;
    private MaskFormatter formatter;
    
    public BaseEditMaskField(String mask, int columns) {
        setColumns(columns);
        createFormatter(mask);
    }
    
    public BaseEditMaskField(String mask) {
        this(mask, DEFAULT_NUM_COLUMNS);
    }
    
    protected void createFormatter(String s) {
        try {
            formatter = new MaskFormatter(s);
        } 
        
        catch (Exception ex) {
            formatter = new MaskFormatter();
        }
    }

    @Override
    public AbstractFormatter getFormatter() {
        return formatter;
    }
}
