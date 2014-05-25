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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.text.DateFormatter;
import javax.swing.text.MaskFormatter;


public class BaseEditMaskField extends JFormattedTextField {
    private static final int DEFAULT_NUMERIC_COLUMNS = 10;
    private static final int DEFAULT_DATETIME_COLUMNS = 15;
    public static final int DATE_MASK = 1;
    public static final int NUMERIC_MASK = 2;
    
    public BaseEditMaskField(String mask, int maskType) {
        switch(maskType) {
            case DATE_MASK:
                initMask(mask, maskType, DEFAULT_DATETIME_COLUMNS);
                break;
            case NUMERIC_MASK:
                initMask(mask, maskType, DEFAULT_NUMERIC_COLUMNS);
                break;
        }
    }

    public BaseEditMaskField(String mask, int maskType, int numcols) {
        initMask(mask, maskType, numcols);
    }
    
    private void initMask(String mask, int maskType, int numcols) {
        switch(maskType) {
            case DATE_MASK:
                setFormatter(new DateFormatter(new SimpleDateFormat(mask)));
                setColumns(numcols);
                break;
            case NUMERIC_MASK:
                try {
                    setFormatter(new MaskFormatter(mask));
                }
                
                catch (Exception ex) {
                     setFormatter(new MaskFormatter());
                }
                
                setColumns(numcols);
                break;
        }

        setInputVerifier(createInputVerifier());
        setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
    }
    
    private InputVerifier createInputVerifier() {
        return new InputVerifier() {
            @Override
            public boolean verify(JComponent input) {
                boolean retval = false;
                String text = getText();
                try {
                    AbstractFormatter formatter = getFormatter();
                    if (formatter != null) {
                        getFormatter().stringToValue(text);
                        retval = true;
                    }
                 } 
                
                 catch (ParseException pe) {
                     retval = false;
                 }

                return retval;
            }
            
            @Override
            public boolean shouldYieldFocus(JComponent input) {
                return verify(input);
            }
        };
    }
}
