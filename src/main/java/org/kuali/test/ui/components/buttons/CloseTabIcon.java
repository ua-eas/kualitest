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

import java.awt.Component ;
import java.awt.Graphics ;
import java.awt.Rectangle ;
import java.awt.event.MouseAdapter ;
import java.awt.event.MouseEvent ;
import javax.swing.Icon ;
import javax.swing.JTabbedPane ;
import org.kuali.test.utils.Constants;


public class CloseTabIcon implements Icon {
    private Icon icon = Constants.CLOSE_TAB_ICON;
    private JTabbedPane tabbedPane = null;
    private transient Rectangle position = null;

    public CloseTabIcon() {
    }

    public CloseTabIcon(Icon icon) {
        this.icon = icon;
    }

    /**
     * * when painting, remember last position painted.
     */
    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        if (null == tabbedPane) {
            tabbedPane = (JTabbedPane) c;
            tabbedPane.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent e) {  
                    // asking for isConsumed is *very* important, otherwise more than one tab might get closed!  
                    if (!e.isConsumed()  && position.contains(e.getX(), e.getY()))  {  
                        final int index = tabbedPane.getSelectedIndex();  
                        tabbedPane.remove( index );  
                        e.consume();  
                    }  
                }  
            });  
        }    

        position = new Rectangle( x,y, getIconWidth(), getIconHeight() );  
        icon.paintIcon(c, g, x, y);  
    }      

    @Override
    public int getIconWidth()  {  
        return icon.getIconWidth();  
    }    

    @Override
    public int getIconHeight()  {  
        return icon.getIconHeight();  
    }    
}
