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

import java.awt.event.ActionEvent;
import javax.swing.JMenuItem;
import javax.swing.tree.DefaultMutableTreeNode;
import org.kuali.test.WebService;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.ui.base.BaseTreePopupMenu;


public class WebServicePopupMenu extends BaseTreePopupMenu {
    public static final String ADD_WEB_SERVICE_ACTION = "Add Web Service";
    public static final String EDIT_WEB_SERVICE_ACTION = "Edit Web Service";
    public static final String REMOVE_WEB_SERVICE_ACTION = "Remove Web Service";

    public WebServicePopupMenu(TestCreator mainframe) {
        super(mainframe);
    }

    @Override
    protected void initMenu() {
        JMenuItem m = new JMenuItem(ADD_WEB_SERVICE_ACTION);
        add(m);
        m.addActionListener(this);

        m = new JMenuItem(EDIT_WEB_SERVICE_ACTION);
        add(m);   
        m.addActionListener(this);

        m = new JMenuItem(REMOVE_WEB_SERVICE_ACTION);
        add(m);   
        m.addActionListener(this);
    }
    
    @Override
    protected void handleAction(DefaultMutableTreeNode actionNode, ActionEvent e) {
        if (ADD_WEB_SERVICE_ACTION.equalsIgnoreCase(e.getActionCommand())) {
            getMainframe().handleAddWebService(null);
        } else if (EDIT_WEB_SERVICE_ACTION.equalsIgnoreCase(e.getActionCommand())) {
            getMainframe().handleEditWebService((WebService)actionNode.getUserObject());
        } else if (actionNode != null) {
            getMainframe().handleRemoveWebService(actionNode);
        }
    }
}
