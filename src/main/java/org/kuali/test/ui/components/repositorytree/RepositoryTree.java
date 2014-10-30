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

import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.kuali.test.KualiTestConfigurationDocument;
import org.kuali.test.Platform;
import org.kuali.test.SuiteTest;
import org.kuali.test.SuiteTests;
import org.kuali.test.TestHeader;
import org.kuali.test.TestSuite;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.ui.base.BaseTree;
import org.kuali.test.ui.dnd.DndHelper;
import org.kuali.test.ui.dnd.RepositoryDragSourceAdapter;
import org.kuali.test.ui.dnd.RepositoryDropTargetAdapter;
import org.kuali.test.ui.dnd.RepositoryTransferData;
import org.kuali.test.ui.dnd.RepositoryTransferable;
import org.kuali.test.ui.utils.UIUtils;
import org.kuali.test.utils.Constants;
import org.kuali.test.utils.Utils;

/**
 *
 * @author rbtucker
 */
public class RepositoryTree extends BaseTree implements DragGestureListener {
    private static final Logger LOG = Logger.getLogger(RepositoryTree.class);
    private KualiTestConfigurationDocument.KualiTestConfiguration configuration;
    private RepositoryPopupMenu popupMenu;
    
    /**
     *
     * @param mainframe
     */
    public RepositoryTree(TestCreator mainframe) {
        super(mainframe);
        popupMenu = new RepositoryPopupMenu(mainframe);
        init();
        addTreeSelectionListener(mainframe.getPlatformTestsPanel());
        new RepositoryDropTargetAdapter(this);
        new DragSource().createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY, this);
    }

    @Override
    public void dragGestureRecognized(DragGestureEvent event) {
        if (getSelectionPath() != null) {
            DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode)getSelectionPath().getLastPathComponent();

            if (isSuiteTest(selectedNode)) {
                TestSuite testSuite = (TestSuite)getParentUserObject(selectedNode);

                if (testSuite != null) {
                    event.startDrag(DragSource.DefaultCopyNoDrop, 
                        new RepositoryTransferable<TestSuite, SuiteTest>(new RepositoryTransferData(testSuite, selectedNode.getUserObject()), DndHelper.getTestOrderDataFlavor()),
                        new RepositoryDragSourceAdapter());
                }
            }
        }
    }
    
    private Object getParentUserObject(DefaultMutableTreeNode childNode) {
        Object retval = null;
        if (childNode.getParent() != null) {
            DefaultMutableTreeNode pnode = (DefaultMutableTreeNode)childNode.getParent();
            retval = pnode.getUserObject();
        }
        
        return retval;
    }
    
    private boolean isSuiteTest(DefaultMutableTreeNode node) {
        return ((node != null) && (node.getUserObject() != null) && (node.getUserObject() instanceof SuiteTest));
    }
    
    /**
     *
     * @return
     */
    public KualiTestConfigurationDocument.KualiTestConfiguration getConfiguration() {
        return configuration;
    }

    /**
     *
     * @return
     */
    public File getRepositoryRoot() {
        return new File(this.getConfiguration().getRepositoryLocation());
    }

    /**
     *
     */
    public void saveConfiguration() {
        if (StringUtils.isNotBlank(getMainframe().getConfigFileName())) {
            File f = new File(getMainframe().getConfigFileName());
            if (f.exists() && f.isFile()) {
                configuration.setModified(false);
                try {
                    KualiTestConfigurationDocument doc = KualiTestConfigurationDocument.Factory.newInstance();
                    doc.setKualiTestConfiguration(configuration);
                    doc.save(f, Utils.getSaveXmlOptions());
                    getMainframe().getCreateTestButton().setEnabled(configuration.getPlatforms().sizeOfPlatformArray() > 0);
                    getMainframe().getCreateTestMenuItem().setEnabled(configuration.getPlatforms().sizeOfPlatformArray() > 0);
                } catch (IOException ex) {
                    LOG.error(ex.toString(), ex);
                    UIUtils.showError(this, "Configuration Save Error", "An error occured while trying to save configuration,");
                }
            }
        }
    }

    /**
     *
     * @return
     */
    @Override
    protected TreeCellRenderer getTreeCellRenderer() {
        return new RepositoryTreeCellRenderer();
    }

    /**
     *
     * @return
     */
    @Override
    protected DefaultTreeModel getTreeModel() {
        loadConfiguration(null);
        return new RepositoryTreeModel(new RepositoryNode(configuration, null));
    }

    /**
     *
     * @param f
     */
    public void loadConfiguration(File f) {
        File configFile = f;
        if (configFile == null) {
            if (StringUtils.isNotBlank(getMainframe().getConfigFileName())) {
                configFile = new File(getMainframe().getConfigFileName());
            }
        } else {
            getMainframe().setConfigFileName(f.getPath());
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("configFileName: " + getMainframe().getConfigFileName());
            LOG.debug("configFile: " + configFile);
        }

        if ((configFile != null) && configFile.exists() && configFile.isFile()) {
            List<XmlError> xmlValidationErrorList = new ArrayList<XmlError>();

            try {
                // Create an XmlOptions instance for load
                XmlOptions loadOptions = new XmlOptions();
                
                configuration = KualiTestConfigurationDocument.Factory.parse(configFile, loadOptions).getKualiTestConfiguration();
                
                // Create an XmlOptions instance and set the error listener.
                XmlOptions validateOptions = new XmlOptions();
                validateOptions.setErrorListener(xmlValidationErrorList);
                configuration.validate(validateOptions);

                if (LOG.isDebugEnabled()) {
                    LOG.debug(configuration.xmlText());
                    LOG.debug("repository-location: " + configuration.getRepositoryLocation());
                    LOG.debug("platform count:" + configuration.getPlatforms().getPlatformArray().length);
                }

                if (!xmlValidationErrorList.isEmpty()) {
                    throw new XmlException("invalid xml file: " + configFile.getPath());
                }
                
                if (Constants.REPOSITORY_ROOT_REPLACE.equals(configuration.getRepositoryLocation())) {
                    configuration.setRepositoryLocation(configFile.getParent());
                }
                
                configuration.setRepositoryLocation(configFile.getParent());
                configuration.setAdditionalDbInfoLocation(configuration.getAdditionalDbInfoLocation().replace(Constants.REPOSITORY_ROOT_REPLACE, configuration.getRepositoryLocation()));
                configuration.setEncryptionPasswordFile(configuration.getEncryptionPasswordFile().replace(Constants.REPOSITORY_ROOT_REPLACE, configuration.getRepositoryLocation()));
                configuration.setTagHandlersLocation(configuration.getTagHandlersLocation().replace(Constants.REPOSITORY_ROOT_REPLACE, configuration.getRepositoryLocation()));
                configuration.setTestResultLocation(configuration.getTestResultLocation().replace(Constants.REPOSITORY_ROOT_REPLACE, configuration.getRepositoryLocation()));
                
                Utils.initializeHtmlTagHandlers(configuration);
                
            } catch (XmlException ex) {
                UIUtils.showError(this, "Invalid input configuration", "Input configuration file failed validation");
                LOG.error(ex.toString());

                for (XmlError error : xmlValidationErrorList) {
                    LOG.error(error.toString());
                }
            } catch (IOException ex) {
                LOG.error(ex.toString(), ex);
                UIUtils.showError(this, "Input File Error", "An error occured while loading configuation file " + configFile.getPath());
            }
        }
    }

    /**
     *
     * @param node
     * @param x
     * @param y
     */
    @Override
    protected void showPopup(DefaultMutableTreeNode node, int x, int y) {
        popupMenu.show(this, node, x, y);
    }

    /**
     *
     * @param repositoryObject
     */
    public void addRepositoryNode(Object repositoryObject) {
        addRepositoryNode(findRepositoryObjectParentNode(repositoryObject), repositoryObject);
    }

    private void addRepositoryNode(DefaultMutableTreeNode pnode, Object repositoryObject) {
        if (pnode != null) {
            getModel().insertNodeInto(new RepositoryNode(configuration, repositoryObject),
                pnode, pnode.getChildCount());
        }
    }

    private void addRepositoryNodes(DefaultMutableTreeNode pnode, List repositoryObjects) {
        if (pnode != null) {
            DefaultTreeModel model = getModel();
            for (Object repositoryObject : repositoryObjects) {
                model.insertNodeInto(new RepositoryNode(configuration, repositoryObject), pnode, pnode.getChildCount());
            }
        }
    }

    private DefaultMutableTreeNode findPlatformNodeByName(String platformName) {
        DefaultMutableTreeNode retval = null;

        Enumeration<RepositoryNode> children = getRootNode().children();

        while (children.hasMoreElements()) {
            RepositoryNode node = children.nextElement();

            if (node.getUserObject() instanceof Platform) {
                Platform platform = (Platform) node.getUserObject();

                if (StringUtils.equalsIgnoreCase(platformName, platform.getName())) {
                    retval = node;
                    break;
                }
            }
        }

        return retval;
    }

    private DefaultMutableTreeNode findTestSuiteNodeByName(String platformName, String testSuiteName) {
        DefaultMutableTreeNode retval = null;

        DefaultMutableTreeNode platformNode = findPlatformNodeByName(platformName);

        if (platformNode != null) {
            Enumeration<RepositoryNode> children = platformNode.children();

            while (children.hasMoreElements()) {
                RepositoryNode node = children.nextElement();

                if (node.getUserObject() instanceof TestSuite) {
                    TestSuite testSuite = (TestSuite) node.getUserObject();

                    if (StringUtils.equalsIgnoreCase(testSuiteName, testSuite.getName())) {
                        retval = node;
                        break;
                    }
                }
            }
        }

        return retval;
    }

    private DefaultMutableTreeNode findSuiteTestNode(SuiteTest suiteTest) {
        DefaultMutableTreeNode retval = null;

        DefaultMutableTreeNode testSuiteNode = findTestSuiteNodeByName(suiteTest.getTestHeader().getPlatformName(), suiteTest.getTestHeader().getTestSuiteName());

        if (testSuiteNode != null) {
            Enumeration<RepositoryNode> children = testSuiteNode.children();

            while (children.hasMoreElements()) {
                RepositoryNode node = children.nextElement();

                if (node.getUserObject() instanceof SuiteTest) {
                    SuiteTest cur = (SuiteTest) node.getUserObject();

                    if (StringUtils.equalsIgnoreCase(suiteTest.getTestHeader().getTestName(), cur.getTestHeader().getTestName())
                        && (suiteTest.getIndex() == cur.getIndex())) {
                        retval = node;
                        break;
                    }
                }
            }
        }
        
        return retval;
    }

    private DefaultMutableTreeNode findRepositoryObjectParentNode(Object repositoryObject) {
        DefaultMutableTreeNode retval = null;

        if (repositoryObject instanceof Platform) {
            retval = getRootNode();
        } else if (repositoryObject instanceof TestSuite) {
            TestSuite testSuite = (TestSuite) repositoryObject;
            retval = findPlatformNodeByName(testSuite.getPlatformName());
        } else if (repositoryObject instanceof SuiteTest) {
            SuiteTest suiteTest = (SuiteTest) repositoryObject;
            retval = findTestSuiteNodeByName(suiteTest.getTestHeader().getPlatformName(), suiteTest.getTestHeader().getTestSuiteName());
        }

        return retval;
    }
    
    private String getDropTargetName(Object userObject) {
        String retval = null;
        
        if (userObject != null) {
            if (userObject instanceof Platform) {
                Platform p = (Platform)userObject;
                retval = p.getName();
            } else if (userObject instanceof TestSuite) {
                TestSuite t = (TestSuite)userObject;
                retval = t.getName();
                
            } else if (userObject instanceof SuiteTest) {
                SuiteTest t = (SuiteTest)userObject;
                retval = t.getTestHeader().getTestName();
            }
        }
        
        return retval;
    }
    
    /**
     *
     * @param dataFlavor
     * @param data
     * @param dropNode
     */
    public void handleDataDrop(DataFlavor dataFlavor, RepositoryTransferData data, DefaultMutableTreeNode dropNode) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("dataFlavor: " + dataFlavor.toString() + ", targetType: " + data.getClass().getName() + ", targetName: " + getDropTargetName(dropNode.getUserObject()));
        }

        if (DndHelper.getTestDataFlavor().equals(dataFlavor)) {
            addSuiteTests(dropNode, (List<String>)data.getData());
        } else if (DndHelper.getTestOrderDataFlavor().equals(dataFlavor)) {
            moveSuiteTest(dropNode, (SuiteTest)data.getData());
        }

        getMainframe().getSaveConfigurationButton().setEnabled(configuration.getModified());
        getMainframe().getSaveConfigurationMenuItem().setEnabled(configuration.getModified());
    }

    /**
     *
     * @param testSuite
     * @param testNames
     */
    public void addSuiteTests(TestSuite testSuite, List <String> testNames) {
        DefaultMutableTreeNode node = findTestSuiteNodeByName(testSuite.getPlatformName(), testSuite.getName());
        
        if (node != null) {
            addSuiteTests(node, testNames);
        }
    }
    
    private void addSuiteTests(DefaultMutableTreeNode testSuiteNode, List <String> testNames) {
        TestSuite inputTestSuite = (TestSuite)testSuiteNode.getUserObject();
        if (inputTestSuite != null) {
            Platform platform = Utils.findPlatform(configuration, inputTestSuite.getPlatformName());
            TestSuite testSuite = Utils.findTestSuite(configuration, inputTestSuite.getPlatformName(), inputTestSuite.getName());
            if ((platform != null) && (testSuite != null) && (testNames != null) && !testNames.isEmpty()) {
                SuiteTests suiteTests = testSuite.getSuiteTests();
                if (suiteTests == null) {
                    suiteTests = testSuite.addNewSuiteTests();
                }

                List <SuiteTest> newSuiteTests= new ArrayList<SuiteTest>();
                for (String testName : testNames) {
                    TestHeader testHeader = Utils.findTestHeaderByName(platform, testName);

                    if (testHeader != null) {
                        SuiteTest suiteTest = suiteTests.addNewSuiteTest();
                        TestHeader th = (TestHeader)testHeader.copy();
                        th.setTestSuiteName(testSuite.getName());
                        suiteTest.setTestHeader(th);
                        suiteTest.setIndex(suiteTests.sizeOfSuiteTestArray());
                        suiteTest.setActive(true);
                        newSuiteTests.add(suiteTest);
                    }
                }
                
                addRepositoryNodes(testSuiteNode, newSuiteTests);
                getMainframe().getSaveConfigurationButton().setEnabled(true);
                getMainframe().getSaveConfigurationMenuItem().setEnabled(true);
            }
        }
    }

    private void moveSuiteTest(DefaultMutableTreeNode suiteTestTargetNode, SuiteTest suiteTest) {
        if (suiteTestTargetNode != null) {
            DefaultMutableTreeNode pnode = (DefaultMutableTreeNode)suiteTestTargetNode.getParent();
            
            if (pnode != null) {
                DefaultMutableTreeNode nodeToMove = findSuiteTestNode(suiteTest);

                if ((nodeToMove != null) && (nodeToMove != suiteTestTargetNode)) {
                    TestSuite testSuite = (TestSuite)pnode.getUserObject();

                    SuiteTest[] tests = testSuite.getSuiteTests().getSuiteTestArray();
                    
                    int pos1 = Utils.getSuiteTestArrayIndex(tests, suiteTest);
                    int pos2 = Utils.getSuiteTestArrayIndex(tests, (SuiteTest)suiteTestTargetNode.getUserObject());;

                    if ((pos1 > -1) && (pos2 > -1) && (pos1 != pos2)) {
                        SuiteTest save = (SuiteTest)tests[pos1].copy();
                        
                        if (pos1 > pos2) {
                            for(int i = pos2;i < pos1; ++i) {
                                tests[i+1].set(tests[i]);
                            }
                        } else if (pos1 < pos2) {
                            for(int i = pos1; i < pos2; ++i) {
                                tests[i].set(tests[i+1]);
                            }
                        }
                        
                        tests[pos2].set(save);

                        getModel().removeNodeFromParent(nodeToMove);
                        getModel().insertNodeInto(nodeToMove, pnode, pnode.getIndex(suiteTestTargetNode));

                        int indx = 1;
                        for (SuiteTest t : testSuite.getSuiteTests().getSuiteTestArray()) {
                            t.setIndex(indx++);
                        }
                        
                        getMainframe().getSaveConfigurationButton().setEnabled(true);
                        getMainframe().getSaveConfigurationMenuItem().setEnabled(true);
                    }
                }
            }
        }
    }
    
    /**
     *
     * @param platform
     */
    public void refreshPlatformNode(Platform platform) {
        DefaultMutableTreeNode node = findPlatformNodeByName(platform.getName());
        
        if (node != null) {
            DefaultMutableTreeNode parent = (DefaultMutableTreeNode)node.getParent();
            int indx = parent.getIndex(node);
            getModel().removeNodeFromParent(node);
            getModel().insertNodeInto(new RepositoryNode(getConfiguration(), platform), parent, indx);
            getModel().reload(parent);
        }
    }
    
    /**
     *
     * @param platformName
     * @return
     */
    public Platform selectPlatformByName(String platformName) {
        Platform retval = null;
        Enumeration <DefaultMutableTreeNode> e = getRootNode().children();
        
        while (e.hasMoreElements()) {
            DefaultMutableTreeNode node = e.nextElement();
            
           Platform p = (Platform)node.getUserObject();
            
            if (p.getName().equals(platformName)) {
                getSelectionModel().setSelectionPath(new TreePath(node.getPath()));
                retval = p;
                break;
            }
        }
        
        
        return retval;
    }
}
