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

import java.awt.event.ActionEvent;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.tree.DefaultMutableTreeNode;
import org.kuali.test.Platform;
import org.kuali.test.SuiteTest;
import org.kuali.test.TestSuite;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.runner.execution.TestExecutionContext;
import org.kuali.test.ui.base.BaseTreePopupMenu;
import org.kuali.test.ui.components.splash.SplashDisplay;
import org.kuali.test.utils.Constants;


public class RepositoryPopupMenu extends BaseTreePopupMenu {
    public static final String ADD_PLATFORM_ACTION = "Add Platform";
    public static final String EDIT_PLATFORM_ACTION = "Edit Platform";
    public static final String ADD_TEST_SUITE_ACTION = "Add Test Suite";
    public static final String EDIT_TEST_SUITE_ACTION = "Edit Test Suite";
    public static final String DELETE_TEST_SUITE_ACTION = "Delete Test Suite";
    public static final String ADD_TEST_ACTION = "Add Test";
    public static final String EDIT_TEST_ACTION = "Edit Test";
    public static final String RUN_TEST_SUITE_ACTION = "Run Test Suite";
    public static final String REMOVE_TEST_ACTION = "Remove Test";
    
    public RepositoryPopupMenu(TestCreator mainframe) {
        super(mainframe);
    }

    @Override
    protected void handleAction(DefaultMutableTreeNode actionNode, ActionEvent e) {
        if (ADD_PLATFORM_ACTION.equalsIgnoreCase(e.getActionCommand())) {
            getMainframe().handleAddPlatform(null);
        } else if (EDIT_PLATFORM_ACTION.equalsIgnoreCase(e.getActionCommand())) {
            getMainframe().handleEditPlatform((Platform)actionNode.getUserObject());
        } else if (ADD_TEST_SUITE_ACTION.equalsIgnoreCase(e.getActionCommand())
            || EDIT_TEST_SUITE_ACTION.equalsIgnoreCase(e.getActionCommand())) {
            getMainframe().handleAddEditTestSuite(actionNode);
        } else if (RUN_TEST_SUITE_ACTION.equalsIgnoreCase(e.getActionCommand())) {
            final TestSuite testSuite = (TestSuite)actionNode.getUserObject();
            new SplashDisplay(getMainframe(), "Run Test Suite", "Running test suite " + testSuite.getName() + "...") {
                @Override
                protected void runProcess() {
                    new TestExecutionContext(getMainframe().getConfiguration(), testSuite).runTest();
                }
            };
        } else if (DELETE_TEST_SUITE_ACTION.equalsIgnoreCase(e.getActionCommand())) {
            getMainframe().handleDeleteTestSuite(actionNode);
        } else if (ADD_TEST_ACTION.equalsIgnoreCase(e.getActionCommand())) {
        } else if (REMOVE_TEST_ACTION.equalsIgnoreCase(e.getActionCommand())) {
            getMainframe().handleRemoveTest(actionNode);
        } else if (Constants.SHOW_TEST_INFORMATION_ACTION.equalsIgnoreCase(e.getActionCommand())) {
            getMainframe().handleShowTestInformation(actionNode);
        }
        
    }
    
    @Override
    protected void populateMenuForNode(DefaultMutableTreeNode node) {
        removeAll();
        
        if (node.isRoot()) {
            JMenuItem m = new JMenuItem(ADD_PLATFORM_ACTION);
            add(m);
            m.addActionListener(this);
        } else if (node.getUserObject() instanceof Platform) {
            JMenuItem m = new JMenuItem(ADD_PLATFORM_ACTION);
            add(m);
            m.addActionListener(this);
            
            m = new JMenuItem(EDIT_PLATFORM_ACTION);
            add(m);
            m.addActionListener(this);

            add(new JSeparator());
            m = new JMenuItem(ADD_TEST_SUITE_ACTION);
            add(m);
            m.addActionListener(this);
        } else if (node.getUserObject() instanceof TestSuite) {
            JMenuItem m = new JMenuItem(ADD_TEST_SUITE_ACTION);
            add(m);
            m.addActionListener(this);
            
            m = new JMenuItem(EDIT_TEST_SUITE_ACTION);
            add(m);
            m.addActionListener(this);

            m = new JMenuItem(RUN_TEST_SUITE_ACTION);
            add(m);
            m.addActionListener(this);
            
            add(new JSeparator());
            
            m = new JMenuItem(DELETE_TEST_SUITE_ACTION);
            add(m);
            m.addActionListener(this);

            add(new JSeparator());
            m = new JMenuItem(ADD_TEST_ACTION);
            add(m);
            m.addActionListener(this);
        } else if (node.getUserObject() instanceof SuiteTest) {
            JMenuItem m = new JMenuItem(ADD_TEST_ACTION);
            add(m);
            m.addActionListener(this);
            
            m = new JMenuItem(REMOVE_TEST_ACTION);
            add(m);
            m.addActionListener(this);

            add(new JSeparator());
            
            m = new JMenuItem(Constants.SHOW_TEST_INFORMATION_ACTION);
            add(m);
            m.addActionListener(this);
        }
    }
}
