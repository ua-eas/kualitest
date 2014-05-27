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

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.ui.base.BaseTreePopupMenu;
import org.kuali.test.ui.components.sqlquerypanel.DatabasePanel;


public class SqlQueryPopupMenu extends BaseTreePopupMenu {
    public static final String INNER_JOIN_ACTION = "Inner Join";
    public static final String OUTER_JOIN_ACTION = "Outer Join";
    public static final String SELECT_ALL_COLUMNS_ACTION = "Select All Columns";
    public static final String DESELECT_ALL_COLUMNS_ACTION = "Deselect All Columns";
    
    private final SqlQueryTree tree;
    private DatabasePanel dbPanel;
    
    public SqlQueryPopupMenu(TestCreator mainframe, DatabasePanel dbPanel, SqlQueryTree tree) {
        super(mainframe);
        this.dbPanel = dbPanel;
        this.tree = tree;
    }

    @Override
    protected void handleAction(DefaultMutableTreeNode actionNode, ActionEvent e) {
        if (e.getActionCommand().equals(OUTER_JOIN_ACTION) 
            || e.getActionCommand().equals(INNER_JOIN_ACTION)) {
            boolean outerJoin = false;
            if (e.getActionCommand().equals(OUTER_JOIN_ACTION)) {
                outerJoin = true;
            }

            TableData td = (TableData)actionNode.getUserObject();
            td.setOuterJoin(outerJoin);
        } else if (e.getActionCommand().equalsIgnoreCase(SELECT_ALL_COLUMNS_ACTION)
            || e.getActionCommand().equalsIgnoreCase(DESELECT_ALL_COLUMNS_ACTION)) {
            TableData td = (TableData)actionNode.getUserObject();
            
            for (ColumnData cd : td.getColumns()) {
                cd.setSelected(e.getActionCommand().equalsIgnoreCase(SELECT_ALL_COLUMNS_ACTION));
                if (cd.isSelected()) {
                    tree.incrementColumnSelectedCount();
                } else {
                    tree.decrementColumnSelectedCount();
                }
            }

            try {
                // figure out area we need to repaint to selections
                Rectangle rc1 = tree.getRowBounds(tree.getRowForPath(new TreePath(actionNode.getPath())));
                Rectangle rc2 = tree.getRowBounds(tree.getRowForPath(new TreePath(actionNode.getLastLeaf().getPath())));

                if ((rc1 != null) && (rc2 != null)) {
                    tree.repaint(rc1.union(rc2));
                } else if (rc1 != null) {
                    tree.repaint(rc1);
                } else if (rc2 != null) {
                    tree.repaint(rc1);
                }
            }
            
            // if we have any problems just repaint everything
            catch (Exception ex) {
                tree.repaint();
            }
        } 
    }
    
    @Override
    protected void populateMenuForNode(DefaultMutableTreeNode node) {
        removeAll();
        
        if (node.getUserObject() instanceof TableData) {
            DefaultMutableTreeNode pnode = (DefaultMutableTreeNode)node.getParent();
            
            if (pnode.getUserObject() instanceof TableData) {
                TableData td = (TableData)node.getUserObject();
                
                JCheckBoxMenuItem m = new JCheckBoxMenuItem(INNER_JOIN_ACTION, !td.isOuterJoin());
                add(m);
                m.addActionListener(this);
                
                m = new JCheckBoxMenuItem(OUTER_JOIN_ACTION, td.isOuterJoin());
                add(m);
                m.addActionListener(this);

                add(new JSeparator());
                
            }

        
            JMenuItem m2 = new JMenuItem(SELECT_ALL_COLUMNS_ACTION);
            add(m2);
            m2.addActionListener(this);

            m2 = new JMenuItem(DESELECT_ALL_COLUMNS_ACTION);
            add(m2);
            m2.addActionListener(this);
        }
    }
}
