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

package org.kuali.test.ui.components.jmxtree;

import java.awt.event.ActionEvent;
import javax.swing.JMenuItem;
import javax.swing.tree.DefaultMutableTreeNode;
import org.kuali.test.JmxConnection;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.ui.base.BaseTreePopupMenu;

/**
 *
 * @author rbtucker
 */
public class JmxPopupMenu extends BaseTreePopupMenu {

    /**
     *
     */
    public static final String ADD_JMX_CONNECTION_ACTION = "Add JMX Connection";

    /**
     *
     */
    public static final String EDIT_JMX_CONNECTION_ACTION = "Edit JMX Connection";

    /**
     *
     */
    public static final String REMOVE_JMX_CONNECTION_ACTION = "Remove JMX Connection";

    /**
     *
     * @param mainframe
     */
    public JmxPopupMenu(TestCreator mainframe) {
        super(mainframe);
    }

    /**
     *
     */
    @Override
    protected void initMenu() {
        JMenuItem m = new JMenuItem(ADD_JMX_CONNECTION_ACTION);
        add(m);
        m.addActionListener(this);

        m = new JMenuItem(EDIT_JMX_CONNECTION_ACTION);
        add(m);   
        m.addActionListener(this);

        m = new JMenuItem(REMOVE_JMX_CONNECTION_ACTION);
        add(m);   
        m.addActionListener(this);
    }
    
    /**
     *
     * @param actionNode
     * @param e
     */
    @Override
    protected void handleAction(DefaultMutableTreeNode actionNode, ActionEvent e) {
        if (ADD_JMX_CONNECTION_ACTION.equalsIgnoreCase(e.getActionCommand())) {
            getMainframe().handleAddJmxConnection(null);
        } else if (EDIT_JMX_CONNECTION_ACTION.equalsIgnoreCase(e.getActionCommand())) {
            getMainframe().handleEditJmxConnection((JmxConnection)actionNode.getUserObject());
        } else if (actionNode != null) {
            getMainframe().handleRemoveJmxConnection(actionNode);
        }
    }
}
