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

package org.kuali.test.ui.components.webservicetree;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import org.apache.log4j.Logger;
import org.kuali.test.KualiTestConfigurationDocument;
import org.kuali.test.WebService;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.ui.base.BaseTree;

/**
 *
 * @author rbtucker
 */
public class WebServiceTree extends BaseTree {
    private static final Logger LOG = Logger.getLogger(WebServiceTree.class);
    private final KualiTestConfigurationDocument.KualiTestConfiguration configuration;
    private final WebServicePopupMenu popupMenu;
    
    public WebServiceTree(TestCreator mainframe, KualiTestConfigurationDocument.KualiTestConfiguration configuration) {
        super(mainframe);
        setRootVisible(false);
        this.setShowsRootHandles(true);
        this.configuration = configuration;
        popupMenu = new WebServicePopupMenu(mainframe);
        init();
    }
      
    public KualiTestConfigurationDocument.KualiTestConfiguration getConfiguration() {
        return configuration;
    }
    
    @Override
    protected TreeCellRenderer getTreeCellRenderer() {
        return new WebServiceTreeCellRenderer();
    }
    
    @Override
    protected DefaultTreeModel getTreeModel() {
        return new WebServiceTreeModel(new WebServiceNode(configuration, null));
    }
    
    @Override
    protected void showPopup(DefaultMutableTreeNode node, int x, int y) {
        if (node.getUserObject() instanceof WebService) {
            popupMenu.show(this, node, x, y);
        }
    }
    
    public void addWebService(WebService webService) {
        getModel().insertNodeInto(new WebServiceNode(configuration, webService), getRootNode(), getRootNode().getChildCount());
    }
}