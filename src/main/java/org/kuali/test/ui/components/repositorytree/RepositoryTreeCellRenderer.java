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

package org.kuali.test.ui.components.repositorytree;

import javax.swing.ImageIcon;
import javax.swing.tree.DefaultMutableTreeNode;
import org.kuali.test.Platform;
import org.kuali.test.SuiteTest;
import org.kuali.test.TestSuite;
import org.kuali.test.ui.base.BaseTreeCellRenderer;
import org.kuali.test.utils.Constants;

/**
 *
 * @author rbtucker
 */
public class RepositoryTreeCellRenderer extends BaseTreeCellRenderer {

    /**
     *
     */
    public RepositoryTreeCellRenderer() {
    }

    /**
     *
     * @param node
     * @return
     */
    @Override
    protected ImageIcon getIcon(DefaultMutableTreeNode node) {
        ImageIcon retval = null;
        if (node != null) {
            if (node.isRoot()) {
                retval = Constants.REPOSITORY_ICON;
            } else if (node.getUserObject() instanceof Platform) {
                retval = Constants.PLATFORM_ICON;
            } else if (node.getUserObject() instanceof TestSuite) {
                retval = Constants.TEST_SUITE_ICON;
            } else if (node.getUserObject() instanceof SuiteTest) {
                retval = Constants.TEST_ICON;
            }
        }
        
        return retval;
    }
}
