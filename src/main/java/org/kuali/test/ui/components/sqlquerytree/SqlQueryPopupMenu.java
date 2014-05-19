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
import javax.swing.tree.DefaultMutableTreeNode;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.ui.base.BaseTreePopupMenu;


public class SqlQueryPopupMenu extends BaseTreePopupMenu {
    public SqlQueryPopupMenu(TestCreator mainframe) {
        super(mainframe);
    }

    @Override
    protected void handleAction(DefaultMutableTreeNode actionNode, ActionEvent e) {
    }
    
    @Override
    protected void populateMenuForNode(DefaultMutableTreeNode node) {
        removeAll();
    }
}
