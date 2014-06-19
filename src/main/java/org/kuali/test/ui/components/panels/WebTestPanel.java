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

import chrriis.dj.nativeswing.NativeSwing;
import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserAdapter;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserNavigationEvent;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserWindowOpeningEvent;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserWindowWillOpenEvent;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JTabbedPane;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.test.Checkpoint;
import org.kuali.test.CheckpointType;
import org.kuali.test.Operation;
import org.kuali.test.Platform;
import org.kuali.test.TestExecutionParameter;
import org.kuali.test.TestHeader;
import org.kuali.test.TestOperation;
import org.kuali.test.TestOperationType;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.proxyserver.TestProxyServer;
import org.kuali.test.ui.components.buttons.CloseTabIcon;
import org.kuali.test.ui.components.buttons.ToolbarButton;
import org.kuali.test.ui.components.dialogs.CheckPointTypeSelectDlg;
import org.kuali.test.ui.components.dialogs.FileCheckPointDlg;
import org.kuali.test.ui.components.dialogs.HtmlCheckPointDlg;
import org.kuali.test.ui.components.dialogs.MemoryCheckPointDlg;
import org.kuali.test.ui.components.dialogs.SqlCheckPointDlg;
import org.kuali.test.ui.components.dialogs.TestExecutionParameterDlg;
import org.kuali.test.ui.components.dialogs.WebServiceCheckPointDlg;
import org.kuali.test.ui.components.splash.SplashDisplay;
import org.kuali.test.utils.Constants;
import org.kuali.test.utils.Utils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 
 * @author rbtucker
 */
public class WebTestPanel extends BaseCreateTestPanel implements ContainerListener  {
    private static final Logger LOG = Logger.getLogger(WebTestPanel.class);

    private TestProxyServer testProxyServer;
    private JTabbedPane tabbedPane;
    private int nodeId = 0;
    private String lastProxyHtmlResponse;
    private ToolbarButton executionAttribute;
    
    public WebTestPanel(TestCreator mainframe, Platform platform, TestHeader testHeader) {
        super(mainframe, platform, testHeader);

        new SplashDisplay(mainframe, "Initializing Web Test", "Loading web proxy server...") {
            @Override
            protected void runProcess() {
                testProxyServer = new TestProxyServer(WebTestPanel.this);
                getStartTest().setEnabled(true);
            }
        };

        initializeNativeBrowser();
        initComponents();
    }

    @Override
    protected void initComponents() {
        super.initComponents();
        getStartTest().setEnabled(false);
        addContainerListener(this);
        tabbedPane = new JTabbedPane();
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        addNewBrowserPanel(true);
        add(tabbedPane, BorderLayout.CENTER);
    }

    public WebBrowserPanel addNewBrowserPanel(boolean initial) {
        WebBrowserPanel retval = new WebBrowserPanel(createWebBrowser(initial)); 

        if (!initial) {
            tabbedPane.addTab(Constants.NEW_BROWSER_TAB_DEFAULT_TEXT, new CloseTabIcon(), retval);
        } else {
            tabbedPane.addTab(Constants.NEW_WEB_TEST, retval);
        }
        
        tabbedPane.setSelectedComponent(retval);
        
        return retval;
    }

    private JWebBrowser createWebBrowser(boolean initial) {
        JWebBrowser retval = new JWebBrowser();

        if (initial) {
            retval.setButtonBarVisible(false);
            retval.setLocationBarVisible(false);
            retval.setMenuBarVisible(false);
            retval.setStatusBarVisible(true);
        }

        retval.addWebBrowserListener(new WebBrowserAdapter() {
            
            @Override
            public void windowWillOpen(WebBrowserWindowWillOpenEvent e) {
                e.setNewWebBrowser(addNewBrowserPanel(false).getWebBrowser());
            }

            @Override
            public void windowOpening(WebBrowserWindowOpeningEvent e) {
                JWebBrowser wb = e.getNewWebBrowser();
                wb.setBarsVisible(false);
                wb.setStatusBarVisible(true);
            }

            @Override
            public void locationChanged(WebBrowserNavigationEvent e) {
                int selindx = tabbedPane.getSelectedIndex();
                if (Constants.NEW_BROWSER_TAB_DEFAULT_TEXT.equals(tabbedPane.getTitleAt(selindx))) {
                    Object o = e.getWebBrowser().executeJavascriptWithResult("return document.title;");
                    if (o != null) {
                        tabbedPane.setTitleAt(selindx, o.toString());
                    }
                }
            }
        });

        return retval;
    }

    @Override
    protected void handleCreateCheckpoint() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("in handleCreateCheckpoint()");
        }
        
        CheckPointTypeSelectDlg dlg = new CheckPointTypeSelectDlg(getMainframe(), getTestHeader().getTestType(), getPlatform());

        if (dlg.isSaved()) {
            int cptype = dlg.getCheckpointType().intValue();

            if (LOG.isDebugEnabled()) {
                LOG.debug("checkpoint type: " + dlg.getCheckpointType()  + ", intval: " + cptype);
            }

            switch(cptype) {
                case CheckpointType.INT_HTTP:
                    createHtmlCheckpoint();
                    break;
                case CheckpointType.INT_MEMORY:
                    createMemoryCheckpoint();
                    break;
                case CheckpointType.INT_SQL:
                    createSqlCheckpoint();
                    break;
                case CheckpointType.INT_WEB_SERVICE:
                    createWebServiceCheckpoint();
                    break;
                case CheckpointType.INT_FILE:
                    createFileCheckpoint();
                    break;
            }
        }
    }

    private void addCheckpoint(Checkpoint checkpoint) {
        TestOperation testOp = TestOperation.Factory.newInstance();
        testOp.setOperationType(TestOperationType.CHECKPOINT);
        Operation op = testOp.addNewOperation();
        op.addNewCheckpointOperation();
        op.setCheckpointOperation(checkpoint);
        testProxyServer.getTestOperations().add(testOp);
    }
    
    private void addTestExecutionParameter(TestExecutionParameter param) {
        TestOperation testOp = TestOperation.Factory.newInstance();
        testOp.setOperationType(TestOperationType.TEST_EXECUTION_PARAMETER);
        Operation op = testOp.addNewOperation();
        op.addNewCheckpointOperation();
        op.setTestExecutionParameter(param);
        testProxyServer.getTestOperations().add(testOp);
    }

    private String getCurrentHtmlResponse(JWebBrowser wb) {
        int proxyLength = 0;
        String retval = wb.getHTMLContent();
        
        // Look at the last response from the proxy if this is bigger than
        // the WebBrowser content we will use it - this is to handle some
        // ajax behavior that does not update the web browser content
        if (StringUtils.isNotBlank(lastProxyHtmlResponse)) {
            proxyLength = lastProxyHtmlResponse.length();
        }
        
        if (proxyLength > retval.length()) {
            retval = lastProxyHtmlResponse;
        }
        
        if (LOG.isDebugEnabled()) {
            LOG.debug(retval);
        }
        
        return retval;
    }
    
    private void createHtmlCheckpoint() {
        JWebBrowser wb = getCurrentBrowser();
        List <Element> labelNodes = new ArrayList<Element>();
        nodeId = 1;
        

        Element rootNode = getHtmlRootNode(labelNodes);

        if (rootNode != null) {
            HtmlCheckPointDlg dlg = new HtmlCheckPointDlg(getMainframe(), getTestHeader(), rootNode, labelNodes);

            if (dlg.isSaved()) {
                addCheckpoint((Checkpoint)dlg.getNewRepositoryObject());
            }
        }
    }

    private void createMemoryCheckpoint() {
        MemoryCheckPointDlg dlg = new MemoryCheckPointDlg(getMainframe(), getTestHeader());

        if (dlg.isSaved()) {
            addCheckpoint((Checkpoint)dlg.getNewRepositoryObject());
        }
    }
    
    private void createSqlCheckpoint() {
        SqlCheckPointDlg dlg = new SqlCheckPointDlg(getMainframe(), getTestHeader(), null);

        if (dlg.isSaved()) {
            addCheckpoint((Checkpoint)dlg.getNewRepositoryObject());
        }
    }
    
    private void createFileCheckpoint() {
        FileCheckPointDlg dlg = new FileCheckPointDlg(getMainframe(), getTestHeader(), null);

        if (dlg.isSaved()) {
            addCheckpoint((Checkpoint)dlg.getNewRepositoryObject());
        }
    }

    private void createWebServiceCheckpoint() {
        WebServiceCheckPointDlg dlg = new WebServiceCheckPointDlg(getMainframe(), getTestHeader(), null);

        if (dlg.isSaved()) {
            addCheckpoint((Checkpoint)dlg.getNewRepositoryObject());
        }
    }
    
    private Element getIframeBody(JWebBrowser webBrowser, Element iframeNode) {
        Element retval = null;
            
        String js = getJsIframeDataCall(iframeNode);

        Object o = webBrowser.executeJavascriptWithResult(js);

        // if we get html back then clean and get the iframe body node
        if (o != null) {
            retval = Utils.tidify(o.toString()).getDocumentElement();
        }

        return retval;
    }
    
    private Element getRootNodeFromHtml(final JWebBrowser webBrowser, List <Element> labelNodes, String html) {
        Document doc = Utils.tidify(html);
        traverseNode(doc.getDocumentElement(), webBrowser, labelNodes);
        return doc.getDocumentElement();
    }

    private void traverseNode(Element parentNode, JWebBrowser webBrowser, List <Element> labelNodes) {
        for (Element childNode : Utils.getChildElements(parentNode)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("child node: " + childNode.getNodeName());
            }
            childNode.setAttribute(Constants.NODE_ID, Constants.NODE_ID + (nodeId++));
            
            // if this tag is an iframe we will load by javascript call
            if (Constants.HTML_TAG_TYPE_IFRAME.equalsIgnoreCase(childNode.getTagName())) {
                Element iframeBody = getIframeBody(webBrowser, childNode);

                if (iframeBody != null) {
                    childNode.appendChild(iframeBody);
                    traverseNode(iframeBody, webBrowser, labelNodes);
                }
            } else if (Constants.HTML_TAG_TYPE_LABEL.equalsIgnoreCase(childNode.getTagName())) {
                String att = childNode.getAttribute(Constants.HTML_TAG_ATTRIBUTE_FOR);

                if (StringUtils.isNotBlank(att)) {
                    labelNodes.add(childNode);
                }
            }

            traverseNode(childNode, webBrowser, labelNodes);
        }
     }

    
    @Override
    protected void handleCancelTest() {
        testProxyServer.getTestOperations().clear();
        getMainframe().getCreateTestPanel().clearPanel("test '" + getTestHeader().getTestName() + "' cancelled");
        getMainframe().getCreateTestButton().setEnabled(true);
        getMainframe().getCreateTestMenuItem().setEnabled(true);
        executionAttribute.setEnabled(false);

        closeProxyServer();
    }

    @Override
    protected void handleStartTest() {
        getMainframe().getCreateTestButton().setEnabled(false);
        getMainframe().getCreateTestMenuItem().setEnabled(false);
        getCurrentBrowser().navigate(getPlatform().getWebUrl());
        executionAttribute.setEnabled(true);
    }

    private JWebBrowser getCurrentBrowser() {
        JWebBrowser retval = null;
        WebBrowserPanel p = (WebBrowserPanel)tabbedPane.getSelectedComponent();
        
        if (p != null) {
            retval = p.getWebBrowser();
        }
        
        return retval;
    }
    @Override
    protected boolean handleSaveTest() {
        boolean retval = saveTest(getMainframe().getConfiguration().getRepositoryLocation(),
            getTestHeader(), testProxyServer.getTestOperations());

        if (retval) {
            getMainframe().getTestRepositoryTree().saveConfiguration();
            getMainframe().getCreateTestPanel().clearPanel("test '" + getTestHeader().getTestName() + "' created");
        }

        closeProxyServer();
        
        return retval;
    }
    
    private void closeProxyServer() {
        try {
            if (testProxyServer != null) {
                testProxyServer.stop();
            }
        }
        
        catch (Throwable t) {
            LOG.warn(t.toString(), t);
        }
    }

    private void initializeNativeBrowser() {
        if (!NativeInterface.isInitialized()) {
            NativeSwing.initialize();
        }

        if (!NativeInterface.isOpen()) {
            NativeInterface.open();
        }
    }

    public void browserRemoved() {
        try {
            if (NativeInterface.isOpen()) {
                NativeInterface.close();
            }
        } catch (Exception ex) {
            LOG.warn(ex.toString());
        }
    }

    @Override
    public void componentAdded(ContainerEvent e) {
    }

    @Override
    public void componentRemoved(ContainerEvent e) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("component removed: " + e.getComponent().getClass().getName());
        }

        if (e.getComponent() instanceof JWebBrowser) {
            browserRemoved();
        }
    }

    private String getJsIframeDataCall(Element iframeNode) {
        StringBuilder retval = new StringBuilder(512);
        String src = iframeNode.getAttribute("src");
        
        if (!src.startsWith("http")) {
            String id = iframeNode.getAttribute("id");
            String name = iframeNode.getAttribute("name");
            if (StringUtils.isNotBlank(id)) {
                retval.append("return document.getElementById('");
                retval.append(id);
                retval.append("')");
            } else {
                retval.append("return document.getElementsByTagName('");
                retval.append(name);
                retval.append("')[0]");
            }

            retval.append(".contentDocument.body.innerHTML;");
        } else {
            retval.append("var xmlhttp=new XMLHttpRequest();");
            retval.append("xmlhttp.open('GET','");
            retval.append(src);
            retval.append("',false);");
            retval.append("xmlhttp.send();");
            retval.append("if (xmlhttp.status==200) { return xmlhttp.responseText; } else { return null; };");
        }
        
        return retval.toString();
    }

    @Override
    protected boolean isStartTestRequired() {
        return true;
    }

    public void setLastProxyHtmlResponse(String lastProxyHtmlResponse) {
        this.lastProxyHtmlResponse = lastProxyHtmlResponse;
    }

    @Override
    protected List<ToolbarButton> getCustomButtons() {
        List <ToolbarButton> retval = new ArrayList<ToolbarButton>();
        retval.add(executionAttribute = new ToolbarButton(Constants.EXECUTION_PARAMETER_ACTION, Constants.EXECUTION_PARAMETER_ICON));
        executionAttribute.setToolTipText("add test execution parameter");
        executionAttribute.setEnabled(false);
        return retval;
    }

    @Override
    protected void handleUnprocessedActionEvent(ActionEvent e) {
        if (Constants.EXECUTION_PARAMETER_ACTION.equalsIgnoreCase(e.getActionCommand())) {
            handleAddExecutionParameter();
        }
    }

    private List <TestExecutionParameter> getTestExecutionParameters() {
        List <TestExecutionParameter> retval = new ArrayList <TestExecutionParameter>();
        
        for (TestOperation op : testProxyServer.getTestOperations()) {
            if (op.getOperationType().equals(TestOperationType.TEST_EXECUTION_PARAMETER)) {
                retval.add(op.getOperation().getTestExecutionParameter());
            }
        }
        
        return retval;
    }
    
   public Element getHtmlRootNode(List <Element> labelNodes) {
        nodeId = 1;
        JWebBrowser wb = getCurrentBrowser();
        return getRootNodeFromHtml(wb, labelNodes, getCurrentHtmlResponse(wb));
    }
    
    private void handleAddExecutionParameter() {
        TestExecutionParameterDlg dlg = new TestExecutionParameterDlg(getMainframe(), this, null);
        
        if (dlg.isSaved()) {
            if (dlg.getRemovedParameters() != null) {
                for (TestExecutionParameter curatt : dlg.getRemovedParameters()) {
                    TestExecutionParameter rematt = (TestExecutionParameter)curatt.copy();
                    rematt.setRemove(true);
                    addTestExecutionParameter(rematt);
                }
            }
            
            TestExecutionParameter tec = dlg.getTestExecutionParameter();
            
            if (StringUtils.isNotBlank(tec.getName()) && StringUtils.isNotBlank(tec.getValue())) {
                addTestExecutionParameter(dlg.getTestExecutionParameter());
            }
        }
    }

    public TestProxyServer getTestProxyServer() {
        return testProxyServer;
    }
}
