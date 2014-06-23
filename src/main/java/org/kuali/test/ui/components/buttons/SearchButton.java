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

import java.awt.Cursor;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import org.kuali.test.utils.Constants;

/**
 *
 * @author rbtucker
 */
public class SearchButton extends JButton {

    /**
     *
     */
    public SearchButton() {
        this(Constants.FILE_SEARCH_ICON, Constants.FILE_SEARCH_ACTION);
    }

    /**
     *
     * @param icon
     */
    public SearchButton(ImageIcon icon) {
        this(icon, Constants.FILE_SEARCH_ACTION);
    }

    /**
     *
     * @param actionCommand
     */
    public SearchButton(String actionCommand) {
        this(Constants.FILE_SEARCH_ICON, actionCommand);
    }
    
    /**
     *
     * @param icon
     * @param actionCommand
     */
    public SearchButton(ImageIcon icon, String actionCommand) {
        super(icon);
        setActionCommand(actionCommand);
        setBorder(BorderFactory.createEmptyBorder());
        setContentAreaFilled(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
}
