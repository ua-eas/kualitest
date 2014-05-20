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

package org.kuali.test.ui.base;

import java.awt.Component;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import org.apache.commons.lang3.StringUtils;


public class BaseTreeCellRenderer extends DefaultTreeCellRenderer {
    public BaseTreeCellRenderer() {
        
    }
    
    @Override
    public Component getTreeCellRendererComponent(
        JTree tree,
        Object value,
        boolean selected,
        boolean expanded,
        boolean leaf,
        int row,
        boolean hasFocus) {

        // Find out which node we are rendering and get its text
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        
        ImageIcon icon = getIcon(node);
        if (icon != null) {
            setIcon(icon);
        }

        this.setText(node.toString());
        
        this.selected = selected;
        this.hasFocus = hasFocus;

        setSelectionDisplay();
        
        String tooltip = getTooltip(value);
        
        if (StringUtils.isNotBlank(tooltip)) {
            setToolTipText(tooltip);
        }
        
        return this;
    }

    protected void setSelectionDisplay() {
        if (this.selected) {
            super.setBackground(getBackgroundSelectionColor());
            setForeground(getTextSelectionColor());

            if (this.hasFocus) {
                setBorderSelectionColor(UIManager.getLookAndFeelDefaults().getColor("Tree.selectionBorderColor"));
            } else {
                setBorderSelectionColor(null);
            }
        } else {
            setBackground(getBackgroundNonSelectionColor());
            setForeground(getTextNonSelectionColor());
            setBorderSelectionColor(null);
        }
    }
    
    protected ImageIcon getIcon(DefaultMutableTreeNode nod) {
        return null;
    }
    
    protected String getTooltip(Object value) {
        return null;
    }
}
