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

import javax.swing.ImageIcon;
import javax.swing.tree.DefaultMutableTreeNode;
import org.kuali.test.DatabaseConnection;
import org.kuali.test.ui.base.BaseTreeCellRenderer;

/**
 *
 * @author rbtucker
 */
public class DatabaseTreeCellRenderer extends BaseTreeCellRenderer {
    private static final ImageIcon DATABASE_ICON = new ImageIcon(DatabaseTreeCellRenderer.class.getResource("/images/database.png"));
    private static final ImageIcon DATABASE_SETTING_ICON = new ImageIcon(DatabaseTreeCellRenderer.class.getResource("/images/database-setting.png"));

    public DatabaseTreeCellRenderer() {
    }

    @Override
    protected ImageIcon getIcon(DefaultMutableTreeNode node) {
        ImageIcon retval = null;

        if (node != null) {
            if (node.getUserObject() instanceof DatabaseConnection) {
                retval = DATABASE_ICON;
            } else if (node.getUserObject() instanceof String) {
                retval = DATABASE_SETTING_ICON;
            }
        }
        
        return retval;
    }
}
