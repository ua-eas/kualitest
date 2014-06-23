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

/**
 *
 * @author rbtucker
 */
public class IntegerTextField extends BaseEditMaskField {

    /**
     *
     */
    public IntegerTextField() {
        super(BaseEditMaskField.INTEGER_MASK);
    }

    /**
     *
     * @param in
     */
    public void setInt(int in) {
        setText("" + in);
    }
    
    /**
     *
     * @return
     */
    public int getInt() {
        int retval = 0;
        try {
            retval = Integer.parseInt(getText());
        }
        
        catch (Exception ex) {
        }
        
        return retval;
    }
}
