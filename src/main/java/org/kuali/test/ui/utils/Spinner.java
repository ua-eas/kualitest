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

package org.kuali.test.ui.utils;

import javax.swing.JLabel;
import org.kuali.test.utils.Constants;


public class Spinner extends JLabel {
    public Spinner() {
        super(Constants.LOADING_SPINNER_ICON);
        setVisible(false);
    }
    
    public Spinner(String msg) {
        this();
        startSpinner(msg);
    }

    public void startSpinner(final String msg) {
        setText(msg);
        setVisible(true);
        getIcon().paintIcon(this, getGraphics(), 0, 0);
        repaint(0);
    }
    
    public void stopSpinner() {
        setText("");
        setVisible(false);
    }
}
