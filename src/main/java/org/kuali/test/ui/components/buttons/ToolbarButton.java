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

import java.awt.Insets;
import javax.swing.ImageIcon;
import javax.swing.JButton;


public class ToolbarButton extends JButton {
    public ToolbarButton(String actionCommand, ImageIcon icon) {
        this(actionCommand, actionCommand, icon);
    }

    public ToolbarButton(ImageIcon icon, String tooltip) {
        super(icon);
        setToolTipText(tooltip);
        setMargin(new Insets(1, 1, 1, 3));
    }

    public ToolbarButton(String actionCommand, String txt, ImageIcon icon) {
        super(icon);
        setActionCommand(actionCommand);
        setText(txt);
        setMargin(new Insets(1, 1, 1, 3));
    }
}
