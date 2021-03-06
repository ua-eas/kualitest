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

package org.kuali.test.ui.components.labels;

import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.UIManager;


public class DataDisplayLabel extends JLabel {
    public DataDisplayLabel(String text) {
        super(text);
        Font labelFont = (Font)UIManager.get("Label.font");
        setFont(new Font(labelFont.getName(), Font.PLAIN, labelFont.getSize()+1));
        this.setVerticalAlignment(JLabel.BOTTOM);
    }
}
