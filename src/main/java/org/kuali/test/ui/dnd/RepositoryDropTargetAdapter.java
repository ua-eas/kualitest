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
package org.kuali.test.ui.dnd;

import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.util.List;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.test.Platform;
import org.kuali.test.SuiteTest;
import org.kuali.test.TestSuite;
import org.kuali.test.ui.components.repositorytree.RepositoryTree;

public class RepositoryDropTargetAdapter extends DropTargetAdapter {
    private static final Logger LOG = Logger.getLogger(RepositoryDropTargetAdapter.class);
    
    private RepositoryTree repositoryTree;
    private DropTarget dropTarget;

    public RepositoryDropTargetAdapter(RepositoryTree repositoryTree) {
        this(repositoryTree, DnDConstants.ACTION_LINK);
    }

    public RepositoryDropTargetAdapter(RepositoryTree repositoryTree, int ops) {
        this.repositoryTree = repositoryTree;
        dropTarget = new DropTarget(repositoryTree, ops, this, true, null);
    }

    @Override
    public void dragOver(DropTargetDragEvent dtde) {
        if (canDrop(dtde)) {
            dtde.acceptDrag(dtde.getDropAction());
        } else {
            dtde.acceptDrag(DnDConstants.ACTION_NONE);
            dtde.rejectDrag();
        }
    }

    private boolean canDrop(DropTargetEvent dte) {
        boolean retval = false;
        try {
            if (dte.getDropTargetContext().getComponent() == repositoryTree) {
                Point pt = null;
                Transferable t = null;
                int ops = -1;
                DataFlavor dataFlavor = null;
                if (dte instanceof DropTargetDragEvent) {
                    pt = ((DropTargetDragEvent)dte).getLocation();
                    t = ((DropTargetDragEvent)dte).getTransferable();
                    ops = ((DropTargetDragEvent)dte).getSourceActions();
                    dataFlavor = ((DropTargetDragEvent)dte).getCurrentDataFlavors()[0];
                } else if (dte instanceof DropTargetDropEvent) {
                    pt = ((DropTargetDropEvent)dte).getLocation();
                    t = ((DropTargetDropEvent)dte).getTransferable();
                    ops = ((DropTargetDropEvent)dte).getSourceActions();
                    dataFlavor = ((DropTargetDropEvent)dte).getCurrentDataFlavors()[0];
                }

                if ((pt != null) && (t != null)) {
                    if (isTestLinkTransfer(t, ops, dataFlavor)) {
                        DefaultMutableTreeNode node = getNodeAtPoint(pt);
                        if (node != null) {
                            Object uo = node.getUserObject();

                            if (uo != null) {
                                if (uo instanceof TestSuite) {
                                    TestSuite testSuite = (TestSuite)uo;
                                    RepositoryTransferData<Platform, List<String>> data = (RepositoryTransferData<Platform, List<String>>)t.getTransferData(dataFlavor);

                                    retval = (StringUtils.equalsIgnoreCase(data.getTarget().getName(), testSuite.getPlatformName()));
                                }
                            }
                        }
                    } else if (isTestOrderTransfer(t, ops, dataFlavor)) {
                        DefaultMutableTreeNode node = getNodeAtPoint(pt);
                        if (node != null) {
                            Object uo = node.getUserObject();
                            if (uo != null) {
                                if (uo instanceof SuiteTest) {
                                    SuiteTest targetSuiteTest = (SuiteTest)uo;
                                    RepositoryTransferData<TestSuite, SuiteTest> data = (RepositoryTransferData<TestSuite, SuiteTest>)t.getTransferData(dataFlavor);

                                    retval = (StringUtils.equalsIgnoreCase(data.getTarget().getPlatformName(), targetSuiteTest.getTestHeader().getPlatformName())
                                        && !StringUtils.equalsIgnoreCase(targetSuiteTest.getTestHeader().getTestName(), data.getData().getTestHeader().getTestName())
                                        && (targetSuiteTest.getIndex() != data.getData().getIndex()));
                                }
                            }
                        }
                    }
                }
            }
        }
        
        catch (Exception ex) {
            LOG.warn(ex.toString(), ex);
        }
        
        return retval;
    }
    

    private DefaultMutableTreeNode getNodeAtPoint(Point pt) {
        DefaultMutableTreeNode retval = null;
        TreePath path = repositoryTree.getPathForLocation(pt.x, pt.y);
        if (path != null) {
            retval = (DefaultMutableTreeNode)path.getLastPathComponent();
        }
        return retval;
    }
    
    private boolean isTestLinkTransfer(Transferable t, int ops, DataFlavor dataFlavor) {
        boolean retval = ((t != null) && (ops == DnDConstants.ACTION_LINK) && dataFlavor.equals(DndHelper.getTestDataFlavor()));
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("isTestLinkTransfer: " + retval);
        }
        
        return retval;
    }
    
    private boolean isTestOrderTransfer(Transferable t, int ops, DataFlavor dataFlavor) {
        boolean retval = ((t != null) && (ops == DnDConstants.ACTION_LINK) && dataFlavor.equals(DndHelper.getTestOrderDataFlavor()));
        if (LOG.isDebugEnabled()) {
            LOG.debug("isTestOrderTransfer: " + retval);
        }
        
        return retval;
    }

    @Override
    public void drop(DropTargetDropEvent dtde) {
        boolean success = false;
        try {
            if (canDrop(dtde)) {
                Transferable t = dtde.getTransferable();
                Object data = t.getTransferData(t.getTransferDataFlavors()[0]);
                DefaultMutableTreeNode dropNode = getNodeAtPoint(dtde.getLocation());
                if ((dropNode != null) && (data != null) && (data instanceof RepositoryTransferData)) {
                    repositoryTree.handleDataDrop(t.getTransferDataFlavors()[0], (RepositoryTransferData)data, dropNode);
                    success = true;
                    dtde.dropComplete(true);
                } 
            }
        } 
        
        catch (Exception e) {
            LOG.error(e.toString(), e);
        }
        
        if (!success) {
            dtde.rejectDrop();
        }
    }
}
