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

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.test.KualiTestConfigurationDocument;
import org.kuali.test.Platform;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.ui.base.BaseTree;

/**
 *
 * @author rbtucker
 */
public class SqlQueryTree extends BaseTree {
    private static final Logger LOG = Logger.getLogger(SqlQueryTree.class);
    private KualiTestConfigurationDocument.KualiTestConfiguration configuration;
    private final SqlQueryPopupMenu popupMenu;
    private Platform platform;
    
    public SqlQueryTree(TestCreator mainframe, Platform platform) {
        super(mainframe);
        this.platform = platform;
        popupMenu = new SqlQueryPopupMenu(mainframe);
        init();
        addTreeSelectionListener(mainframe.getPlatformTestsPanel());
    }

    public KualiTestConfigurationDocument.KualiTestConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    protected TreeCellRenderer getTreeCellRenderer() {
        return new SqlQueryTreeCellRenderer();
    }

    @Override
    protected DefaultTreeModel getTreeModel() {
        return new SqlQueryTreeModel(new SqlQueryNode(configuration, platform));
    }

    @Override
    protected void showPopup(DefaultMutableTreeNode node, int x, int y) {
        popupMenu.show(this, node, x, y);
    }

    @Override
    protected String getTooltip(Object value) {
        String retval = null;
        
        if (value instanceof TableData) {
            TableData td = (TableData)value;
            
            if (StringUtils.isNotBlank(td.getForeignKeyName())) {
                StringBuilder buf = new StringBuilder(128);
                buf.append("<html>foreign key: ");
                buf.append(td.getForeignKeyName());
                buf.append("<br />");
                
                for (String[] s : td.getLinkColumns()) {
                    buf.append(s[0]);
                    buf.append("=");
                    buf.append(s[1]);
                    buf.append("<br />");
                }
                
                buf.append("</html>");
                
                retval = buf.toString();
            }
        }
        
        return retval;
    }
}
