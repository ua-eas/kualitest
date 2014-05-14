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
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserCommandEvent;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserEvent;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserListener;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserNavigationEvent;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserWindowOpeningEvent;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserWindowWillOpenEvent;
import java.awt.BorderLayout;
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
import org.kuali.test.Platform;
import org.kuali.test.TestHeader;
import org.kuali.test.TestOperation;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.proxyserver.TestProxyServer;
import org.kuali.test.ui.components.buttons.CloseTabIcon;
import org.kuali.test.ui.components.dialogs.CheckPointTypeSelectDlg;
import org.kuali.test.ui.components.dialogs.HtmlCheckPointDlg;
import org.kuali.test.ui.components.splash.SplashDisplay;
import org.kuali.test.utils.Constants;

public class WebTestPanel extends BaseCreateTestPanel implements ContainerListener, WebBrowserListener  {
    private static final Logger LOG = Logger.getLogger(WebTestPanel.class);

    private TestProxyServer testProxyServer;
    private JTabbedPane tabbedPane;

    public WebTestPanel(TestCreator mainframe, Platform platform, TestHeader testHeader) {
        super(mainframe, platform, testHeader);
        getStartTest().setEnabled(false);

        new SplashDisplay(mainframe, "Initializing Web Test", "Loading web proxy server...") {
            @Override
            protected void runProcess() {
                testProxyServer = new TestProxyServer();
                getStartTest().setEnabled(true);
            }
        };

        initializeNativeBrowser();
        initComponents();
    }

    private void initComponents() {
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
                if (LOG.isDebugEnabled()) {
                    LOG.debug("in windowWillOpen()");
                }

                WebBrowserPanel wbp = addNewBrowserPanel(false);
                e.setNewWebBrowser(wbp.getWebBrowser());
            }

            @Override
            public void windowOpening(WebBrowserWindowOpeningEvent e) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("in windowOpening()");
                }

                JWebBrowser wb = e.getNewWebBrowser();
                wb.setBarsVisible(false);
                wb.setStatusBarVisible(true);
            }

            @Override
            public void locationChanged(WebBrowserNavigationEvent e) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("in locationChanged()");
                }

                int selindx = tabbedPane.getSelectedIndex();
                if (Constants.NEW_BROWSER_TAB_DEFAULT_TEXT.equals(tabbedPane.getTitleAt(selindx))) {
                    Object o = e.getWebBrowser().executeJavascriptWithResult("return document.title;");
                    if (o != null) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("documentTitle: " + o.toString());
                        }

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
        
        CheckPointTypeSelectDlg dlg = new CheckPointTypeSelectDlg(getMainframe(), getPlatform());

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
            }
        }
    }

    private void createHtmlCheckpoint() {
        JWebBrowser wb = getCurrentBrowser();
        List <Node> labelNodes = new ArrayList<Node>();
        Node rootNode = getRootNodeFromHtml(wb, labelNodes, wb.getHTMLContent());
        
        if (rootNode != null) {
            HtmlCheckPointDlg dlg = new HtmlCheckPointDlg(getMainframe(), getTestHeader(), rootNode, labelNodes);

            if (dlg.isSaved()) {
                TestOperation op = TestOperation.Factory.newInstance();
                Checkpoint checkpoint = (Checkpoint)dlg.getNewRepositoryObject();
//                testProxyServer.getTestOperations();
            }
        }
    }

    private void createMemoryCheckpoint() {
    }
    
    private void createSqlCheckpoint() {
    }
    
    private void createWebServiceCheckpoint() {
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
            LOG.debug("html: " + o);
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
                node.attributes().put("test-id", node.nodeName() + "[" + depth + "][" + node.siblingIndex() + "]");
                // if this tag is an iframe we will load by javascript call
                if (Constants.HTML_TAG_TYPE_IFRAME.equalsIgnoreCase(node.nodeName())) {
                    Node iframeBody = getIframeBody(webBrowser, whitelist, node);
                    
                    if (iframeBody != null) {
                        // set the iframe node we loaded
                        ((Element)node).prependChild(iframeBody);
                        
                        traverseNode(webBrowser, whitelist, labelNodes, iframeBody);
                        
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("--------------------- iframe -----------------------");
                            LOG.debug(node.toString());
                            LOG.debug("----------------------------------------------------");
                        }
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
            if ("input".equals(tag)) {
                atts.add("value");
                atts.add("type");
                atts.add("checked");
            } else if ("label".equals(tag)) {
                atts.add("for");
            } else if ("iframe".equals(tag)) {
                atts.add("src");
            } else if ("th".equals(tag)) {
                atts.add("colspan");
            } else if ("table".equals(tag)) {
                atts.add("summary");
            } 
            
            retval.addAttributes(tag, atts.toArray(new String[atts.size()]));
        }

        
        return retval;
    }
    
    private Node getRootNodeFromHtml(final JWebBrowser webBrowser, List <Node> labelNodes, String html) {
        Whitelist whitelist = getHtmlWhitelist();
        String cleanHtml = Jsoup.clean(html, whitelist);
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("clean html: " + cleanHtml);
        }
        
        Node retval = Jsoup.parse(cleanHtml).body();

        traverseNode(webBrowser, whitelist, labelNodes, retval);

        return retval;
    }
    
    @Override
    protected void handleCancelTest() {
        testProxyServer.getTestOperations().clear();
        getMainframe().getCreateTestPanel().clearPanel("test '" + getTestHeader().getTestName() + "' cancelled");
        getMainframe().getCreateTestButton().setEnabled(true);
    }

    @Override
    protected void handleStartTest() {
        getMainframe().getCreateTestButton().setEnabled(false);
        getCurrentBrowser().navigate(getPlatform().getWebUrl());
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
            getMainframe().getSaveConfigurationButton().setEnabled(true);
            getMainframe().getCreateTestPanel().clearPanel("test '" + getTestHeader().getTestName() + "' created");
        }

        return retval;
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

            if (testProxyServer != null) {
                testProxyServer.stop();
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

    @Override
    public void windowWillOpen(WebBrowserWindowWillOpenEvent wbwwoe) {
    }

    @Override
    public void windowOpening(WebBrowserWindowOpeningEvent wbwoe) {
    }

    @Override
    public void windowClosing(WebBrowserEvent wbe) {
    }

    @Override
    public void locationChanging(WebBrowserNavigationEvent wbne) {
    }

    @Override
    public void locationChanged(WebBrowserNavigationEvent wbne) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void locationChangeCanceled(WebBrowserNavigationEvent wbne) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void loadingProgressChanged(WebBrowserEvent wbe) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void titleChanged(WebBrowserEvent wbe) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void statusChanged(WebBrowserEvent wbe) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void commandReceived(WebBrowserCommandEvent wbce) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
}
