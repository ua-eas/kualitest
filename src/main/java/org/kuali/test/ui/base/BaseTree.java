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

import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.kuali.test.creator.TestCreator;

/**
 *
 * @author rbtucker
 */
public abstract class BaseTree extends JTree implements TreeModelListener {

    private final TestCreator mainframe;

    /**
     *
     * @param mainframe
     */
    public BaseTree(TestCreator mainframe) {
        this.mainframe = mainframe;
    }

    /**
     *
     */
    protected void init() {
        ToolTipManager.sharedInstance().registerComponent(this);

        getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        setCellRenderer(getTreeCellRenderer());
        TreeModel myTreeModel = getTreeModel();
        
        setModel(myTreeModel);
        myTreeModel.addTreeModelListener(this);
        
        MouseAdapter ma = new MouseAdapter() {
            private void myPopupEvent(MouseEvent e) {
                TreePath path = BaseTree.this.getPathForLocation(e.getX(), e.getY());
                if (path != null) {
                    setSelectionPath(path);
                    showPopup((DefaultMutableTreeNode) path.getLastPathComponent(), e.getX(), e.getY());
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    myPopupEvent(e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    myPopupEvent(e);
                }
            }
        };

        addMouseListener(ma);
    }

    @Override
    public Insets getInsets() {
        return new Insets(10, 5, 0, 0);
    }

    /**
     *
     * @return
     */
    protected TreeCellRenderer getTreeCellRenderer() {
        return new DefaultTreeCellRenderer();
    }

    /**
     *
     * @param node
     * @param x
     * @param y
     */
    protected void showPopup(DefaultMutableTreeNode node, int x, int y) {
    }

    /**
     *
     * @return
     */
    public TestCreator getMainframe() {
        return mainframe;
    }

    @Override
    public DefaultTreeModel getModel() {
        return (DefaultTreeModel) super.getModel();
    }

    /**
     *
     * @param node
     */
    public void removeNode(DefaultMutableTreeNode node) {
        if ((node != null) && (node.getParent() != null)) {
            getModel().removeNodeFromParent(node);
        }
    }

    /**
     *
     * @return
     */
    protected abstract DefaultTreeModel getTreeModel();

    /**
     *
     * @return
     */
    public DefaultMutableTreeNode getRootNode() {
        return (DefaultMutableTreeNode) getModel().getRoot();
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        String retval = null;
        TreePath treePath = getPathForLocation(event.getX(), event.getY());

        if (treePath != null) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) treePath.getLastPathComponent();

            if (node != null) {
                retval = getTooltip(node);
            }
        }

        return retval;
    }

    /**
     *
     * @param node
     * @return
     */
    protected String getTooltip(DefaultMutableTreeNode node) {
        return null;
    }

    @Override
    public void treeNodesChanged(TreeModelEvent e) {
        handleTreeNodesChanged(e);
    }

    @Override
    public void treeNodesInserted(TreeModelEvent e) {
        handleTreeNodesInserted(e);
    }

    @Override
    public void treeNodesRemoved(TreeModelEvent e) {
        handleTreeNodesRemoved(e);
    }

    @Override
    public void treeStructureChanged(TreeModelEvent e) {
        handleTreeStuctureChanged(e);
    }

    /**
     *
     * @param e
     */
    protected void handleTreeNodesChanged(TreeModelEvent e) {
    }

    /**
     *
     * @param e
     */
    protected void handleTreeNodesInserted(TreeModelEvent e) {
    }

    /**
     *
     * @param e
     */
    protected void handleTreeNodesRemoved(TreeModelEvent e) {
    }

    /**
     *
     * @param e
     */
    protected void handleTreeStuctureChanged(TreeModelEvent e) {
    }

    /**
     *
     * @param node
     * @param childLevels
     */
    public void expandNode(DefaultMutableTreeNode node, int childLevels) {
        expandNode(node);

        if (childLevels == 0) {
            return;
        }

        if (childLevels != -1) {
            childLevels--;
        }

        DefaultTreeModel model = getModel();
        if (node == null) {
            node = getRootNode();
        }

        for (int i = 0; i < model.getChildCount(node); i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode)model.getChild(node, i);
            expandNode(child, childLevels);
        }
    }

    /**
     *
     * @param node
     */
    public void expandNode(DefaultMutableTreeNode node) {
        List <DefaultMutableTreeNode> pathList = new ArrayList<DefaultMutableTreeNode>();

        DefaultTreeModel model = getModel();

        if (node == null) {
            node = getRootNode();
        }

        if (createPath(model, pathList, getRootNode(), node)) {
            expandPath(new TreePath(pathList.toArray()));
        }
    }

    private boolean createPath(TreeModel model, List <DefaultMutableTreeNode> pathList, DefaultMutableTreeNode node, DefaultMutableTreeNode findNode) {
        boolean retval = false;
        if (node != null) {

            if (node == findNode) {
                pathList.add(0, node);
                retval = true;
            } else {
                for (int i = 0; !retval && (i < model.getChildCount(node)); i++) {
                    DefaultMutableTreeNode child = (DefaultMutableTreeNode)model.getChild(node, i);

                    if (createPath(model, pathList, child, findNode)) {
                        // if found in children ... prepend parent node
                        pathList.add(0, node);
                        retval = true;
                    }
                }
            }
        }
        
        return retval;
    }
}
