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

import java.awt.event.KeyEvent;
import javax.swing.JTextField;


public class BaseEditMaskField extends JTextField {
    private static final int DEFAULT_NUMERIC_COLUMNS = 10;
    public static final int INTEGER_MASK = 1;
    public static final int FLOAT_MASK = 2;
    private int maskType;
    
    public BaseEditMaskField(int maskType) {
        initMask(maskType, 0);
    }

    public BaseEditMaskField(int maskType, int numcols) {
        initMask(maskType, numcols);
    }
    
    private void initMask(int maskType, int numcols) {
        this.maskType = maskType;
        
        if (numcols == 0) {
            switch(maskType) {
                case INTEGER_MASK:
                case FLOAT_MASK:
                    numcols = DEFAULT_NUMERIC_COLUMNS;
                    break;
            }
        }
        
        setColumns(numcols);
    }
    
    private boolean isValidNumericEntry(char c) {
        return (Character.isDigit(c) 
            || ((c == '-') && (getCaretPosition() == 0))
            || ((c == ',') && (getCaretPosition() > 0))
            || ((c == '.' && !getText().contains("."))));
    }
    
    private boolean isValidIntegerEntry(char c) {
        return (isValidNumericEntry(c) && (c != '.'));
    }
    
    @Override
    protected void processKeyEvent(KeyEvent e) {
        boolean process = true;
        if (isMaskInputEvent(e)) {
            char c = e.getKeyChar();
            switch(maskType) {
                case INTEGER_MASK:
                    process = isValidIntegerEntry(c);
                    break;
                case FLOAT_MASK:
                    process = isValidNumericEntry(c);
                    break;
            }
        } 
        
        
        if (process) {
            super.processKeyEvent(e);
        }
    }
    
    private boolean isMaskInputEvent(KeyEvent e) {
        boolean retval = false;
        if ((e.getModifiers()!= KeyEvent.CHAR_UNDEFINED) 
            && (e.getKeyCode() != KeyEvent.VK_BACK_SPACE)
            && (e.getKeyCode() != KeyEvent.VK_LEFT)
            && (e.getKeyCode() != KeyEvent.VK_RIGHT)
            && (e.getKeyCode() != KeyEvent.VK_BACK_SPACE)
            && (e.getKeyCode() != KeyEvent.VK_DELETE)) {
            retval = true;
        }
        
        return retval;
    }
}
