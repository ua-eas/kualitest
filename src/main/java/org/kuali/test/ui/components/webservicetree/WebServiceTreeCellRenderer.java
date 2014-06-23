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

import javax.swing.ImageIcon;
import javax.swing.tree.DefaultMutableTreeNode;
import org.kuali.test.WebService;
import org.kuali.test.ui.base.BaseTreeCellRenderer;
import org.kuali.test.utils.Constants;

/**
 *
 * @author rbtucker
 */
public class WebServiceTreeCellRenderer extends BaseTreeCellRenderer {

    /**
     *
     */
    public WebServiceTreeCellRenderer() {
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
            if (node.getUserObject() instanceof WebService) {
                retval = Constants.WEB_SERVICE_ICON;
            } else if (node.getUserObject() instanceof String) {
                retval = Constants.WEB_SERVICE_SETTING_ICON;
            }
        }
        
        return retval;
    }
}
