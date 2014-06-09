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
import java.util.Arrays;
import java.util.List;
import javax.swing.JTabbedPane;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.NodeVisitor;
import org.kuali.test.Checkpoint;
import org.kuali.test.CheckpointType;
import org.kuali.test.Operation;
import org.kuali.test.Platform;
import org.kuali.test.TestExecutionAttribute;
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
import org.kuali.test.ui.components.dialogs.TestExecutionAttributeDlg;
import org.kuali.test.ui.components.dialogs.WebServiceCheckPointDlg;
import org.kuali.test.ui.components.splash.SplashDisplay;
import org.kuali.test.utils.Constants;

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
    
    private void addTestExecutionAttribute(TestExecutionAttribute att) {
        TestOperation testOp = TestOperation.Factory.newInstance();
        testOp.setOperationType(TestOperationType.TEST_EXECUTION_ATTRIBUTE);
        Operation op = testOp.addNewOperation();
        op.addNewCheckpointOperation();
        op.setTestExecutionAttribute(att);
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
        
        return retval;
    }
    
    private void createHtmlCheckpoint() {
        JWebBrowser wb = getCurrentBrowser();
        List <Node> labelNodes = new ArrayList<Node>();
        nodeId = 1;
        

        Node rootNode = getRootNodeFromHtml(wb, labelNodes, getCurrentHtmlResponse(wb));

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
    
    private Node getIframeBody(JWebBrowser webBrowser, Whitelist whitelist, Node iframeNode) {
        Node retval = null;
            
        String js = getJsIframeDataCall(iframeNode);

        Object o = webBrowser.executeJavascriptWithResult(js);

        // if we get html back then clean and get the iframe body node
        if (o != null) {
            retval = Jsoup.parse(Jsoup.clean(o.toString(), whitelist)).body();
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("*********************** iframe ****************************");
            LOG.debug("id: " + iframeNode.attr("id"));
            LOG.debug("name: " + iframeNode.attr("name"));
            LOG.debug("jscall: " + js);
            LOG.debug("html: " + retval);
            LOG.debug("**********************************************************");
        }
        
        return retval;
    }
    
    private void traverseNode(final JWebBrowser webBrowser, 
        final Whitelist whitelist, 
        final List <Node> labelNodes,
        Node node) {
        node.traverse(new NodeVisitor() {
            @Override
            public void head(Node node, int depth) {
                node.attributes().put(Constants.NODE_ID, Constants.NODE_ID + (nodeId++));
                // if this tag is an iframe we will load by javascript call
                if (Constants.HTML_TAG_TYPE_IFRAME.equalsIgnoreCase(node.nodeName())) {
                    Node iframeBody = getIframeBody(webBrowser, whitelist, node);
                    
                    if (iframeBody != null) {
                        ((Element)node).prependChild(iframeBody);
                        traverseNode(webBrowser, whitelist, labelNodes, iframeBody);
                    }
                } else if (Constants.HTML_TAG_TYPE_LABEL.equalsIgnoreCase(node.nodeName())) {
                    String att = node.attr(Constants.HTML_TAG_ATTRIBUTE_FOR);

                    if (StringUtils.isNotBlank(att)) {
                        labelNodes.add(node);
                    }
                }
            }

            @Override
            public void tail(Node node, int i) {
            }
        });
    }

    private Whitelist getHtmlWhitelist() {
        Whitelist retval = Whitelist.none();
        
        retval.addTags(Constants.DEFAULT_HTML_WHITELIST_TAGS);

        for (String tag : Constants.DEFAULT_HTML_WHITELIST_TAGS) {
            List <String> atts = new ArrayList(Arrays.asList(Constants.DEFAULT_HTML_WHITELIST_TAG_ATTRIBUTES));
            if (Constants.HTML_TAG_TYPE_INPUT.equals(tag)) {
                atts.add("value");
                atts.add("type");
                atts.add("checked");
            } else if (Constants.HTML_TAG_TYPE_LABEL.equals(tag)) {
                atts.add("for");
            } else if (Constants.HTML_TAG_TYPE_IFRAME.equals(tag)) {
                atts.add("src");
            } else if (Constants.HTML_TAG_TYPE_TH.equals(tag) || Constants.HTML_TAG_TYPE_TD.equals(tag)) {
                atts.add("colspan");
            } else if (Constants.HTML_TAG_TYPE_TABLE.equals(tag)) {
                atts.add("summary");
            } 
            
            retval.addAttributes(tag, atts.toArray(new String[atts.size()]));
        }

        
        return retval;
    }
    
    private Node getRootNodeFromHtml(final JWebBrowser webBrowser, List <Node> labelNodes, String html) {
        Whitelist whitelist = getHtmlWhitelist();
        Node retval = Jsoup.parse(Jsoup.clean(html, whitelist)).body();
        traverseNode(webBrowser, whitelist, labelNodes, retval);
        return retval;
    }
    
    @Override
    protected void handleCancelTest() {
        testProxyServer.getTestOperations().clear();
        getMainframe().getCreateTestPanel().clearPanel("test '" + getTestHeader().getTestName() + "' cancelled");
        getMainframe().getCreateTestButton().setEnabled(true);
        executionAttribute.setEnabled(false);

        closeProxyServer();
    }

    @Override
    protected void handleStartTest() {
        getMainframe().getCreateTestButton().setEnabled(false);
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

    private String getJsIframeDataCall(Node iframeNode) {
        StringBuilder retval = new StringBuilder(512);
        String src = iframeNode.attr("src");
        
        if (!src.startsWith("http")) {
            String id = iframeNode.attr("id");
            String name = iframeNode.attr("name");
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
        retval.add(executionAttribute = new ToolbarButton(Constants.EXECUTION_ATTRIBUTE_ACTION, Constants.EXECUTION_CONTEXT_ATTRIBUTE_ICON));
        executionAttribute.setToolTipText("add execution context attribute");
        executionAttribute.setEnabled(false);
        return retval;
    }

    @Override
    protected void handleUnprocessedActionEvent(ActionEvent e) {
        if (Constants.EXECUTION_ATTRIBUTE_ACTION.equalsIgnoreCase(e.getActionCommand())) {
            handleAddExecutionContextAttribute();
        }
    }

    private void handleAddExecutionContextAttribute() {
        TestExecutionAttributeDlg dlg = new TestExecutionAttributeDlg(getMainframe(), null);
        
        if (dlg.isSaved()) {
            TestExecutionAttribute att = (TestExecutionAttribute)dlg.getNewRepositoryObject();
        }
    }
}
