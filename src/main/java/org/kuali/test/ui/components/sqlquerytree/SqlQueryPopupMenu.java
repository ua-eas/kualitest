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

import java.awt.event.ActionEvent;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.tree.DefaultMutableTreeNode;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.ui.base.BaseTreePopupMenu;


public class SqlQueryPopupMenu extends BaseTreePopupMenu {
    public static final String INNER_JOIN_ACTION = "Inner Join";
    public static final String OUTER_JOIN_ACTION = "Outer Join";

    public SqlQueryPopupMenu(TestCreator mainframe) {
        super(mainframe);
    }

    @Override
    protected void handleAction(DefaultMutableTreeNode actionNode, ActionEvent e) {
        boolean outerJoin = false;
        if (e.getActionCommand().equals(OUTER_JOIN_ACTION)) {
            outerJoin = true;
        }
        
        TableData td = (TableData)actionNode.getUserObject();
        td.setOuterJoin(outerJoin);
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
            }
        }
    }
}
