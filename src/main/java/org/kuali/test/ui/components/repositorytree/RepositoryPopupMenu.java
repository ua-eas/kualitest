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
import javax.swing.SwingWorker;
import javax.swing.tree.DefaultMutableTreeNode;
import org.apache.log4j.Logger;
import org.kuali.test.Platform;
import org.kuali.test.SuiteTest;
import org.kuali.test.TestSuite;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.runner.TestRunner;
import org.kuali.test.runner.execution.TestExecutionMonitor;
import org.kuali.test.ui.base.BaseTreePopupMenu;
import org.kuali.test.ui.components.dialogs.LoadTestDlg;
import org.kuali.test.ui.components.splash.RunningTestDisplay;
import org.kuali.test.ui.utils.UIUtils;
import org.kuali.test.utils.Constants;

/**
 *
 * @author rbtucker
 */
public class RepositoryPopupMenu extends BaseTreePopupMenu {
    private static final Logger LOG = Logger.getLogger(RepositoryPopupMenu.class);
    
    public static final String ADD_PLATFORM_ACTION = "Add Platform";
    public static final String EDIT_PLATFORM_ACTION = "Edit Platform";
    public static final String DELETE_PLATFORM_ACTION = "Delete Platform";
    public static final String ADD_TEST_SUITE_ACTION = "Add Test Suite";
    public static final String IMPORT_PLATFORM_TESTS_ACTION = "Import Platform Tests";
    public static final String EDIT_TEST_SUITE_ACTION = "Edit Test Suite";
    public static final String DELETE_TEST_SUITE_ACTION = "Delete Test Suite";
    public static final String ADD_TESTS_ACTION = "Add Test(s)";
    public static final String EDIT_TEST_ACTION = "Edit Test";
    public static final String RUN_TEST_SUITE_ACTION = "Run Test Suite";
    public static final String RUN_TEST_SUITE_LOAD_TEST_ACTION = "Run Test Suite Load Test";
    public static final String REMOVE_TEST_ACTION = "Remove Test";
    
    /**
     *
     * @param mainframe
     */
    public RepositoryPopupMenu(TestCreator mainframe) {
        super(mainframe);
   }

    /**
     *
     * @param actionNode
     * @param e
     */
    @Override
    protected void handleAction(final DefaultMutableTreeNode actionNode, ActionEvent e) {
        if (ADD_PLATFORM_ACTION.equalsIgnoreCase(e.getActionCommand())) {
            getMainframe().handleAddPlatform(null);
        } else if (EDIT_PLATFORM_ACTION.equalsIgnoreCase(e.getActionCommand())) {
            getMainframe().handleEditPlatform((Platform)actionNode.getUserObject());
        } else if (DELETE_PLATFORM_ACTION.equalsIgnoreCase(e.getActionCommand())) {
            getMainframe().handleDeletePlatform(actionNode);
        } else if (ADD_TEST_SUITE_ACTION.equalsIgnoreCase(e.getActionCommand())
            || EDIT_TEST_SUITE_ACTION.equalsIgnoreCase(e.getActionCommand())) {
            getMainframe().handleAddEditTestSuite(actionNode);
        } else if (RUN_TEST_SUITE_ACTION.equalsIgnoreCase(e.getActionCommand())) {
            final TestSuite testSuite = (TestSuite)actionNode.getUserObject();
            new RunningTestDisplay(getMainframe(), "Running Test Suite") {
                long startTime = System.currentTimeMillis();
                
                @Override
                protected void runProcess() {
                    try {
                        TestExecutionMonitor monitor = new TestRunner(getMainframe().getConfiguration()).runTestSuite(testSuite.getPlatformName(), testSuite.getName(), 1);

                        if (monitor != null) {
                            monitor.setOverrideEmail(getMainframe().getLocalRunEmailAddress());

                            while (!monitor.testsCompleted()) {
                                try {
                                    updateDisplay(monitor.buildDisplayMessage("Running test suite '" + testSuite.getName() + "'..." , startTime));
                                    if (isCancelTest()) {
                                        break;
                                    }
                                    Thread.sleep(Constants.LOCAL_TEST_RUN_SLEEP_TIME);
                                } 

                                catch (InterruptedException ex) {};
                            }
                        }
                    } 
                    
                    catch (Exception ex) {
                        LOG.error(ex.toString(), ex);
                        if ((getDlg() != null) && getDlg().isVisible()) {
                            getDlg().dispose();
                        }
                        UIUtils.showError(getMainframe(), "Error", "Error occured while attempting to run test suite " + testSuite.getName());
                    }
                }
            };
        } else if (RUN_TEST_SUITE_LOAD_TEST_ACTION.equalsIgnoreCase(e.getActionCommand())) {
            final TestSuite testSuite = (TestSuite)actionNode.getUserObject();
            LoadTestDlg dlg = new LoadTestDlg(getMainframe(),testSuite.getName(), true);
            if (dlg.isSaved()) {
                final int testRuns = dlg.getTestRuns();
                
                if (testRuns > 0) {
                    getMainframe().startSpinner2("Running load test: test suite - " + testSuite.getName() + "[" + testRuns + "]");
                    new SwingWorker() {
                        @Override
                        protected Object doInBackground() throws Exception {
                            String retval = null;
                            try {
                                TestExecutionMonitor monitor = new TestRunner(getMainframe().getConfiguration()).runTestSuite(testSuite.getPlatformName(), testSuite.getName(), testRuns);

                                if (monitor != null) {
                                    while(!monitor.testsCompleted()) {
                                        Thread.sleep(Constants.LOCAL_TEST_RUN_SLEEP_TIME);
                                    }
                                }
                            } 

                            catch (Exception ex) {
                                LOG.error(ex.toString(), ex);
                            }

                            return retval;
                        };

                        @Override
                        protected void done() {
                            getMainframe().stopSpinner2();
                        }
                    }.execute();
                }
            }
        } else if (DELETE_TEST_SUITE_ACTION.equalsIgnoreCase(e.getActionCommand())) {
            getMainframe().handleDeleteTestSuite(actionNode);
        } else if (Constants.CREATE_TEST.equalsIgnoreCase(e.getActionCommand())) {
            getMainframe().handleCreateTest((Platform)actionNode.getUserObject());
        } else if (ADD_TESTS_ACTION.equalsIgnoreCase(e.getActionCommand())) {
            getMainframe().handleAddTests((TestSuite)actionNode.getUserObject());
        } else if (REMOVE_TEST_ACTION.equalsIgnoreCase(e.getActionCommand())) {
            getMainframe().handleRemoveTest(actionNode);
        } else if (Constants.SHOW_TEST_INFORMATION_ACTION.equalsIgnoreCase(e.getActionCommand())) {
            getMainframe().handleShowTestInformation(actionNode);
        } else if (IMPORT_PLATFORM_TESTS_ACTION.equalsIgnoreCase(e.getActionCommand())) {
            getMainframe().handleImportPlatformTests(actionNode);
        }
        
    }
    
    /**
     *
     * @param node
     */
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

            m = new JMenuItem(DELETE_PLATFORM_ACTION);
            add(m);
            m.addActionListener(this);

            add(new JSeparator());
            m = new JMenuItem(ADD_TEST_SUITE_ACTION);
            add(m);
            m.addActionListener(this);
            
            add(new JSeparator());
            m = new JMenuItem(Constants.CREATE_TEST);
            add(m);
            m.addActionListener(this);
        
            add(new JSeparator());
            m = new JMenuItem(IMPORT_PLATFORM_TESTS_ACTION);
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
            m = new JMenuItem(ADD_TESTS_ACTION);
            add(m);
            m.addActionListener(this);
        } else if (node.getUserObject() instanceof SuiteTest) {
            JMenuItem m = new JMenuItem(ADD_TESTS_ACTION);
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
