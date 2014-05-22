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

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.test.KualiTestConfigurationDocument;
import org.kuali.test.Platform;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.ui.base.BaseTree;
import org.kuali.test.ui.components.panels.DatabasePanel;

/**
 *
 * @author rbtucker
 */
public class SqlQueryTree extends BaseTree implements MouseListener {
    private static final Logger LOG = Logger.getLogger(SqlQueryTree.class);
    private KualiTestConfigurationDocument.KualiTestConfiguration configuration;
    private final SqlQueryPopupMenu popupMenu;
    private Platform platform;
    private DatabasePanel dbPanel;
    private TreeCellRenderer treeCellRenderer;
    private int columnsSelected =0;
    
    public SqlQueryTree(TestCreator mainframe, DatabasePanel dbPanel, Platform platform) {
        super(mainframe);
        this.dbPanel = dbPanel;
        this.platform = platform;
        addMouseListener(this);
        popupMenu = new SqlQueryPopupMenu(mainframe);
        init();
    }

    public KualiTestConfigurationDocument.KualiTestConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    protected TreeCellRenderer getTreeCellRenderer() {
        if (treeCellRenderer == null) {
            treeCellRenderer = new SqlQueryTreeCellRenderer();
        }
        
        return treeCellRenderer;
    }

    @Override
    protected DefaultTreeModel getTreeModel() {
        return new SqlQueryTreeModel(new SqlQueryNode(configuration, platform));
    }

    @Override
    protected void showPopup(DefaultMutableTreeNode node, int x, int y) {
        popupMenu.show(this, node, x, y);
    }

    public String getTooltip(TableData td) {
        String retval = null;
        
        DefaultMutableTreeNode node = findTableDataTreeNode(td, getRootNode());
        
        if (node != null) {
            retval = getTooltip(node);
        }
        
        return retval;
    }

    private DefaultMutableTreeNode findTableDataTreeNode(TableData td, DefaultMutableTreeNode node) {
        DefaultMutableTreeNode retval = null;
        
        if (node.getChildCount() > 0) {
            for (int i = 0; i < node.getChildCount(); ++i) {
                DefaultMutableTreeNode curnode = (DefaultMutableTreeNode)node.getChildAt(i);
                
                Object uo = curnode.getUserObject();
                if ((uo != null) && (uo == td)) {
                    retval = curnode;
                    break;
                } else {
                    retval = findTableDataTreeNode(td, curnode);
                }
            }
        }
        
        return retval;
    }
    
    @Override
    protected String getTooltip(DefaultMutableTreeNode node) {
        String retval = null;
        
        if (node.getUserObject() instanceof TableData) {
            TableData td = (TableData)node.getUserObject();
            DefaultMutableTreeNode pnode = (DefaultMutableTreeNode)node.getParent();
            
            if (pnode.getUserObject() instanceof TableData) {
                TableData pdata = (TableData)pnode.getUserObject();

                if (StringUtils.isNotBlank(td.getForeignKeyName())) {
                    StringBuilder buf = new StringBuilder(128);
                    buf.append("<html><span style='font-weight: 700; text-decoration:underline;'>foreign key: ");
                    buf.append(dbPanel.getTableDisplayName(pdata.getName()));
                    buf.append(" -> ");
                    buf.append(dbPanel.getTableDisplayName(td.getName()));
                    buf.append("</span><br />");

                    for (String[] s : td.getLinkColumns()) {
                        buf.append(dbPanel.getColumnDisplayName(pdata.getName(), s[0], false));
                        buf.append("=");
                        buf.append(dbPanel.getColumnDisplayName(td.getName(), s[1], false));
                        buf.append("<br />");
                    }

                    buf.append("</html>");

                    retval = buf.toString();
                }
            }
        }
        
        return retval;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        TreePath path = getPathForLocation(e.getX(), e.getY());
        if (path != null) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
            if (node != null) {
                if (node.getUserObject() instanceof ColumnData) {
                    ColumnData cd = (ColumnData)node.getUserObject();
                    
                    if (cd.isSelected()) {
                        columnsSelected--;
                    } else {
                        columnsSelected++;
                    }
                    
                    cd.setSelected(!cd.isSelected());
                    getComponentAt(e.getPoint()).repaint();
                    e.consume();
                }
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
    
    public int getSelectedColumnsCount() {
        return columnsSelected;
    }
    
    
}
