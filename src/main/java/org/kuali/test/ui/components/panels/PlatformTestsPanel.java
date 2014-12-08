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
package org.kuali.test.ui.components.panels;

import java.awt.BorderLayout;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import org.apache.log4j.Logger;
import org.kuali.test.Platform;
import org.kuali.test.TestHeader;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.runner.TestRunner;
import org.kuali.test.runner.execution.TestExecutionMonitor;
import org.kuali.test.ui.base.BasePanel;
import org.kuali.test.ui.components.dialogs.LoadTestDlg;
import org.kuali.test.ui.components.splash.RunningTestDisplay;
import org.kuali.test.ui.dnd.DndHelper;
import org.kuali.test.ui.dnd.RepositoryDragSourceAdapter;
import org.kuali.test.ui.dnd.RepositoryTransferData;
import org.kuali.test.ui.dnd.RepositoryTransferable;
import org.kuali.test.utils.Constants;
import org.kuali.test.utils.Utils;

/**
 *
 * @author rbtucker
 */
public class PlatformTestsPanel extends BasePanel 
    implements TreeSelectionListener, DragGestureListener, ActionListener {
    private static final Logger LOG = Logger.getLogger(PlatformTestsPanel.class);
    private static final String DELETE_TEST = "Delete test";
    private static final String RUN_TEST = "Run test";
    private static final String RUN_TEST_LOAD_TEST = "Run test load test";
    
    private JList testList;
    private Platform currentPlatform;
    private JPopupMenu popupMenu;
    private JMenuItem deleteTestMenuItem;
    private TestHeader currentTestHeader;
    
    /**
     *
     * @param mainframe
     * @param platform
     */
    public PlatformTestsPanel(TestCreator mainframe, Platform platform) {
        super(mainframe);
        initComponents();
        if (platform != null) {
            populateList(platform);
        }
    }

    /**
     *
     * @param mainframe
     */
    public PlatformTestsPanel(TestCreator mainframe) {
        this(mainframe, null);
    }

    private void initComponents() {
        JPanel p = new JPanel(new BorderLayout());
        p.add(new JLabel("Available Platform Tests"), BorderLayout.NORTH);

        add(p, BorderLayout.NORTH);
        add(new JScrollPane(testList = new JList(new DefaultListModel())), BorderLayout.CENTER);
        
        popupMenu = new JPopupMenu();
        JMenuItem m = new JMenuItem(Constants.SHOW_TEST_INFORMATION_ACTION);
        popupMenu.add(m);
        m.addActionListener(this);
        popupMenu.add(new JSeparator());

        m = new JMenuItem(RUN_TEST);
        popupMenu.add(m);
        m.addActionListener(this);

        m = new JMenuItem(RUN_TEST_LOAD_TEST);
        popupMenu.add(m);
        m.addActionListener(this);

        popupMenu.add(new JSeparator());
        deleteTestMenuItem = new JMenuItem(DELETE_TEST);
        popupMenu.add(deleteTestMenuItem);
        deleteTestMenuItem.addActionListener(this);
        
        testList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        testList.addMouseListener(new MouseAdapter() {
            private void myPopupEvent(MouseEvent e) {
                int indx = testList.locationToIndex(e.getPoint());
                if (indx > -1) {
                    // only allow deleting 1 test at a time
                    deleteTestMenuItem.setEnabled((getSelectedTests() != null) && (getSelectedTests().size() == 1));
                    showPopup((String)testList.getModel().getElementAt(indx), e.getX(), e.getY());
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
        });
        
        new DragSource().createDefaultDragGestureRecognizer(testList, DnDConstants.ACTION_COPY, this);
    }
    
    private void showPopup(String testName, int x, int y) {
        if (currentPlatform != null) {
            this.currentTestHeader = Utils.findTestHeaderByName(currentPlatform, testName);

            if (currentTestHeader != null) {
                popupMenu.show(this, x, y);
            }
        }
    }
    
    @Override
    public void dragGestureRecognized(DragGestureEvent event) {
        List <String> testNames = this.getSelectedTests();
        
        if (!testNames.isEmpty() && (currentPlatform != null)) {
            event.startDrag(DragSource.DefaultLinkNoDrop, 
                new RepositoryTransferable<Platform, List<String>>(new RepositoryTransferData(currentPlatform, testNames), DndHelper.getTestDataFlavor()),
                new RepositoryDragSourceAdapter());
        }
    }

    @Override
    public void valueChanged(TreeSelectionEvent e) {
        DefaultMutableTreeNode oldnode = null;
        DefaultMutableTreeNode newnode = null;

        if ((e.getOldLeadSelectionPath() != null)
            && (e.getOldLeadSelectionPath().getLastPathComponent() != null)) {
            oldnode = (DefaultMutableTreeNode) e.getOldLeadSelectionPath().getLastPathComponent();
        }

        if ((e.getNewLeadSelectionPath() != null)
            && (e.getNewLeadSelectionPath().getLastPathComponent() != null)) {
            newnode = (DefaultMutableTreeNode) e.getNewLeadSelectionPath().getLastPathComponent();
        }

        Platform oldPlatform = Utils.getPlatformForNode(getMainframe().getConfiguration(), oldnode);
        Platform newPlatform = Utils.getPlatformForNode(getMainframe().getConfiguration(), newnode);

        currentPlatform = newPlatform;

        if (newPlatform == null) {
            clearList();
        } else if ((oldPlatform == null)
            || !newPlatform.getName().equalsIgnoreCase(oldPlatform.getName())) {
            populateList(newPlatform);
        }
    }

    private void clearList() {
        DefaultListModel model = (DefaultListModel) testList.getModel();
        model.clear();

    }

    /**
     *
     * @param platform
     */
    public void populateList(Platform platform) {
        if (platform != null) {
            currentPlatform = platform;
            if (LOG.isDebugEnabled()) {
                LOG.debug("populating test list for platform " + platform.getName());
            }
            clearList();
            DefaultListModel model = (DefaultListModel) testList.getModel();
            for (TestHeader th : platform.getPlatformTests().getTestHeaderArray()) {
                model.addElement(th.getTestName());
            }

        } else {
            clearList();
        }
    }

    /**
     *
     * @return
     */
    public Platform getCurrentPlatform() {
        return currentPlatform;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (Constants.SHOW_TEST_INFORMATION_ACTION.equals(e.getActionCommand())) {
            getMainframe().handleShowTestInformation(currentTestHeader);
        } else if (DELETE_TEST.equals(e.getActionCommand())) {
            getMainframe().handleDeleteTest(currentTestHeader);
        } else if (RUN_TEST.equals(e.getActionCommand())) {
            new RunningTestDisplay(getMainframe(), "Running Test") {
                private long startTime = System.currentTimeMillis();
                
                @Override
                protected void runProcess() {
                    TestExecutionMonitor monitor = new TestRunner(getMainframe().getConfiguration()).runTest(currentPlatform.getName(), currentTestHeader.getTestName(), 1);
                    if (monitor != null) {
                        monitor.setOverrideEmail(getMainframe().getLocalRunEmailAddress());
                        while (!monitor.testsCompleted()) {
                            try {
                                updateDisplay(monitor.buildDisplayMessage("Running test '" + currentTestHeader.getTestName() + "'...", startTime));
                                if (isCancelTest()) {
                                    break;
                                }
                                Thread.sleep(Constants.LOCAL_TEST_RUN_SLEEP_TIME);
                            } 

                            catch (InterruptedException ex) {};
                        }
                    }
                }
            };
        } else if (RUN_TEST_LOAD_TEST.equals(e.getActionCommand())) {
             LoadTestDlg dlg = new LoadTestDlg(getMainframe(), currentTestHeader.getTestName(), false);
             
             if (dlg.isSaved()) {
                final int testRuns = dlg.getTestRuns();
                String testName = currentTestHeader.getTestName();
                
                if (testRuns > 0) {
                    getMainframe().startSpinner2("Running load test: test - " + testName + "[" + testRuns + "]");
                    new SwingWorker() {
                        @Override
                        protected Object doInBackground() throws Exception {
                            String retval = null;
                            try {
                                TestExecutionMonitor monitor = new TestRunner(getMainframe().getConfiguration()).runTest(currentPlatform.getName(), currentTestHeader.getTestName(), testRuns);

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
        }
    }
    
    /**
     *
     * @param listener
     */
    public void addListSelectionListener(ListSelectionListener listener) {
        testList.addListSelectionListener(listener);
    }
    
    /**
     *
     * @return
     */
    public List<String> getSelectedTests() {
        List <String> retval = new ArrayList<String>();
        
        int sz = testList.getModel().getSize();
        
        for (int i = 0; i < sz; ++i) {
            if (testList.isSelectedIndex(i)) {
                retval.add((String)testList.getModel().getElementAt(i));
            }
        }
        return retval;
    }
}
