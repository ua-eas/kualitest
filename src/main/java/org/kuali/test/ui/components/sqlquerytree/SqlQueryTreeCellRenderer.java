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

package org.kuali.test.ui.components.sqlquerytree;

import java.awt.Component;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import org.kuali.test.ui.base.BaseTreeCellRenderer;
import org.kuali.test.utils.Constants;

/**
 *
 * @author rbtucker
 */
public class SqlQueryTreeCellRenderer extends BaseTreeCellRenderer {
    public SqlQueryTreeCellRenderer() {
    }

    /**
     *
     * @param node
     * @return
     */
    @Override
    protected ImageIcon getIcon(DefaultMutableTreeNode node) {
        ImageIcon retval = null;
        if (node != null) {
            if (node.isRoot()) {
                retval = Constants.DATABASE_ICON;
            } else if (node.isLeaf()) {
                ColumnData cd = (ColumnData)node.getUserObject();
                
                if (cd.getPrimaryKeyIndex() < Integer.MAX_VALUE) {
                    if (cd.isSelected()) {
                        retval = Constants.DATABASE_PKCOLUMN_SELECTED_ICON;
                    } else {
                        retval = Constants.DATABASE_PKCOLUMN_ICON;
                    }
                } else {
                    if (cd.isSelected()) {
                        retval = Constants.DATABASE_COLUMN_SELECTED_ICON;
                    } else {
                        retval = Constants.DATABASE_COLUMN_ICON;
                    }
                }
            } else if (node.getUserObject() instanceof TableData) {
                TableData td = (TableData)node.getUserObject();
                
                if (td.isOuterJoin()) {
                    retval = Constants.DATABASE_TABLE_OUTER_JOIN_ICON;
                } else {
                    retval = Constants.DATABASE_TABLE_ICON;
                }
            }
        }
        
        return retval;
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, 
        boolean expanded, boolean leaf, int row, boolean hasFocus) {
        Component retval = super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        
        SqlQueryNode node = (SqlQueryNode)value;
        
        if (node != null) {
            if (node.isRoot()) {
                tree.setRowHeight(Constants.DEFAULT_TREE_ROW_HEIGHT);
            } else {
                tree.setRowHeight(23);
            }
        }
    
        return retval;
    }
}
