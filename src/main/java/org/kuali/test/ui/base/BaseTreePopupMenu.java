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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultMutableTreeNode;
import org.kuali.test.KualiTestApp;


public abstract class BaseTreePopupMenu extends JPopupMenu implements ActionListener {
    private DefaultMutableTreeNode actionNode;
    private KualiTestApp mainframe;
    
    public BaseTreePopupMenu(KualiTestApp mainframe) {
        this.mainframe = mainframe;
        initMenu();
    }
    
    public void actionPerformed(ActionEvent e) {
        handleAction(actionNode, e);
        actionNode = null;
    }
    
    public KualiTestApp getMainframe() {
        return mainframe;
    }
    
    public void show(BaseTree tree, DefaultMutableTreeNode node, int x, int y) {
        actionNode = node;
        populateMenuForNode(node);
        super.show(tree, x, y); 
    }

    protected void populateMenuForNode(DefaultMutableTreeNode node) {
    }

    protected abstract void handleAction(DefaultMutableTreeNode actionNode, ActionEvent e);
    
    protected void initMenu() {
        
    }
}
