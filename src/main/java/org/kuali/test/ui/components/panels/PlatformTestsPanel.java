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
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import org.apache.log4j.Logger;
import org.kuali.test.Platform;
import org.kuali.test.TestHeader;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.ui.base.BasePanel;
import org.kuali.test.ui.components.dialogs.TestInformationDlg;
import org.kuali.test.ui.dnd.DndHelper;
import org.kuali.test.ui.dnd.RepositoryDragSourceAdapter;
import org.kuali.test.ui.dnd.RepositoryTransferData;
import org.kuali.test.ui.dnd.RepositoryTransferable;
import org.kuali.test.utils.Utils;

public class PlatformTestsPanel extends BasePanel 
    implements TreeSelectionListener, DragGestureListener, ActionListener {
    private static final Logger LOG = Logger.getLogger(PlatformTestsPanel.class);
    private static final String SHOW_TEST_INFORMATION = "Show test information";
    
    private JList testList;
    private Platform currentPlatform;
    private JPopupMenu popupMenu;
    private TestHeader testHeader;
    
    public PlatformTestsPanel(TestCreator mainframe) {
        super(mainframe);
        
        JPanel p = new JPanel(new BorderLayout());
        p.add(new JLabel("Available Tests"), BorderLayout.NORTH);

        add(p, BorderLayout.NORTH);
        add(new JScrollPane(testList = new JList(new DefaultListModel())), BorderLayout.CENTER);
        
        popupMenu = new JPopupMenu();
        JMenuItem m = new JMenuItem(SHOW_TEST_INFORMATION);
        popupMenu.add(m);
        m.addActionListener(this);
        
        testList.addMouseListener(new MouseAdapter() {
            private void myPopupEvent(MouseEvent e) {
                int indx = testList.locationToIndex(e.getPoint());
                if (indx > -1) {
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
        
        new DragSource().createDefaultDragGestureRecognizer(testList, DnDConstants.ACTION_LINK, this);
    }

    private void showPopup(String testName, int x, int y) {
        if (currentPlatform != null) {
            this.testHeader = Utils.findTestHeaderByName(currentPlatform, testName);

            if (testHeader != null) {
                popupMenu.show(this, x, y);
            }
        }
    }
    
    @Override
    public void dragGestureRecognized(DragGestureEvent event) {
        List <String> testNames = (List<String>)testList.getSelectedValuesList();
        if ((testNames != null) && !testNames.isEmpty() && (currentPlatform != null)) {
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
        
        getMainframe().getCreateTestButton().setEnabled(newPlatform != null);
    }

    private void clearList() {
        DefaultListModel model = (DefaultListModel) testList.getModel();
        model.clear();

    }

    public void populateList(Platform platform) {
        if (platform != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("populating test for platform " + platform.getName());
            }
            clearList();
            DefaultListModel model = (DefaultListModel) testList.getModel();
            for (TestHeader testHeader : platform.getPlatformTests().getTestHeaderArray()) {
                model.addElement(testHeader.getTestName());
            }

        } else {
            clearList();
        }
    }

    public Platform getCurrentPlatform() {
        return currentPlatform;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (SHOW_TEST_INFORMATION.equals(e.getActionCommand())) {
            showTestInformation();
        }
    }
    
    private void showTestInformation() {
        new TestInformationDlg(getMainframe(), testHeader);
    }
}
