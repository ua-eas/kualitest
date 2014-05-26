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

package org.kuali.test.ui.components.databasestree;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import org.apache.log4j.Logger;
import org.kuali.test.DatabaseConnection;
import org.kuali.test.KualiTestConfigurationDocument;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.ui.base.BaseTree;

/**
 *
 * @author rbtucker
 */
public class DatabaseTree extends BaseTree {
    private static final Logger LOG = Logger.getLogger(DatabaseTree.class);
    private final KualiTestConfigurationDocument.KualiTestConfiguration configuration;
    private final DatabasePopupMenu popupMenu;
    
    public DatabaseTree(TestCreator mainframe, KualiTestConfigurationDocument.KualiTestConfiguration configuration) {
        super(mainframe);
        setRootVisible(false);
        this.setShowsRootHandles(true);
        this.configuration = configuration;
        popupMenu = new DatabasePopupMenu(mainframe);
        init();
    }
      
    public KualiTestConfigurationDocument.KualiTestConfiguration getConfiguration() {
        return configuration;
    }
    
    @Override
    protected TreeCellRenderer getTreeCellRenderer() {
        return new DatabaseTreeCellRenderer();
    }
    
    @Override
    protected DefaultTreeModel getTreeModel() {
        return new DatabaseTreeModel(new DatabaseNode(configuration, null));
    }
    
    @Override
    protected void showPopup(DefaultMutableTreeNode node, int x, int y) {
        if (node.getUserObject() instanceof DatabaseConnection) {
            popupMenu.show(this, node, x, y);
        }
    }
    
    public void addDatabaseConnection(DatabaseConnection dbconn) {
        getModel().insertNodeInto(new DatabaseNode(configuration, dbconn), getRootNode(), getRootNode().getChildCount());
    }
}