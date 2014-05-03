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
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JTabbedPane;
import org.apache.commons.lang3.StringUtils;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.HtmlNode;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.TagNodeVisitor;
import org.kuali.test.Checkpoint;
import org.kuali.test.CheckpointProperty;
import org.kuali.test.CheckpointType;
import org.kuali.test.ComparisonOperator;
import org.kuali.test.FailureAction;
import org.kuali.test.Platform;
import org.kuali.test.TestHeader;
import org.kuali.test.TestOperation;
import org.kuali.test.ValueType;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.proxyserver.TestProxyServer;
import org.kuali.test.ui.components.buttons.CloseTabIcon;
import org.kuali.test.ui.components.dialogs.CheckPointTypeSelectDlg;
import org.kuali.test.ui.components.dialogs.HtmlCheckPointDlg;
import static org.kuali.test.ui.components.panels.BaseCreateTestPanel.LOG;
import org.kuali.test.ui.components.splash.SplashDisplay;
import org.kuali.test.utils.Constants;
import org.kuali.test.utils.HtmlTagInfo;
import org.kuali.test.utils.Utils;

public class WebTestPanel extends BaseCreateTestPanel implements ContainerListener {
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
        
        CheckPointTypeSelectDlg dlg = new CheckPointTypeSelectDlg(getMainframe());

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
        List <CheckpointProperty> checkpointProperties = loadCheckpointPropertiesFromHtml();
        
        if ((checkpointProperties != null) && !checkpointProperties.isEmpty()) {
            HtmlCheckPointDlg dlg = new HtmlCheckPointDlg(getMainframe(), getTestHeader(), checkpointProperties);
            
            if (dlg.isSaved()) {
                TestOperation op = TestOperation.Factory.newInstance();
                Checkpoint checkpoint = (Checkpoint)dlg.getNewRepositoryObject();
                
                
//                testProxyServer.getTestOperations();
            }
        }
    }

    private List <CheckpointProperty> loadCheckpointPropertiesFromHtml() {
        List <CheckpointProperty> retval = new ArrayList<CheckpointProperty>();
        HtmlCleaner cleaner = new HtmlCleaner();
        List <HtmlTagInfo> availableHtmlObjects = new ArrayList<HtmlTagInfo>();
        
        JWebBrowser wb = getCurrentBrowser();
        
        loadHtmlDomObjects(wb, cleaner, wb.getHTMLContent(), availableHtmlObjects);

        // create a map of labels objects for names - will assume that label naming
        // convention is <input-name/id>.label
        Map<String, HtmlTagInfo> labels = new HashMap<String, HtmlTagInfo>();
        
        for (HtmlTagInfo tagInfo : availableHtmlObjects) {
            if (Utils.isHtmlLabel(tagInfo)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("label - " + tagInfo);
                }

                String rootName = Utils.getHtmlLabelPartnerId(tagInfo);
                if (StringUtils.isNotBlank(rootName)) {
                    labels.put(rootName, tagInfo);
                }
            }
        }

        Set <String> tagset = new HashSet<String>();
        
        // now lets loop through the non-label elements and create checkpoint properties
        for (HtmlTagInfo tagInfo : availableHtmlObjects) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(tagInfo);
            }
            if (!Utils.isHtmlLabel(tagInfo)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("non-label - " + tagInfo);
                }

                if (Utils.isValidCheckpointTag(tagInfo) 
                    && !tagset.contains(tagInfo.getNameAttribute())) {
                    tagset.add(tagInfo.getNameAttribute());
                    CheckpointProperty p = CheckpointProperty.Factory.newInstance();

                    String key = Utils.getHtmlElementKey(tagInfo);
                    HtmlTagInfo label = labels.get(key);

                    p.setPropertyName(key);
                    
                    if (label != null) {
                        p.setDisplayName(Utils.formatDisplayName(label.getText()));
                    } else {
                        p.setDisplayName(key);
                    }

                    p.setPropertyValue(tagInfo.getText());
                    p.setOperator(ComparisonOperator.EQUAL_TO);
                    p.setOnFailure(FailureAction.NONE);
                    p.setValueType(ValueType.UNKNOWN);

                    retval.add(p);
                }
            }
        }
        
        Collections.sort(retval, new Comparator<CheckpointProperty>() {
            @Override
            public int compare(CheckpointProperty cp1, CheckpointProperty cp2) {
                return cp1.getPropertyName().toLowerCase().compareTo(cp2.getPropertyName().toLowerCase());
            }
        });
        
        return retval;
    }
    
    private void createMemoryCheckpoint() {
    }
    
    private void createSqlCheckpoint() {
    }
    
    private void createWebServiceCheckpoint() {
    }
    
    private void loadHtmlDomObjects(JWebBrowser webBrowser, final HtmlCleaner htmlCleaner, String html, final List <HtmlTagInfo> availableHtmlObjects) {
        final Set <String> iframejscalls = new HashSet<String>();
        TagNode node = htmlCleaner.clean(html);
        
        if (node != null) {
            // traverse whole DOM and update images to absolute URLs
            node.traverse(new TagNodeVisitor() {
                @Override
                public boolean visit(TagNode tagNode, HtmlNode htmlNode) {
                    if (htmlNode instanceof TagNode) {
                        TagNode tag = (TagNode) htmlNode;
                        String id = tag.getAttributeByName("id");
                        String name = tag.getAttributeByName("name");

                        if (StringUtils.isNotBlank(id) || StringUtils.isNotBlank(name)) {
                            // if this tag is aniframe we will load a javascript call
                            // that we will call later
                            if ("iframe".equalsIgnoreCase(tag.getName())) {
                                StringBuilder js = new StringBuilder(256);
                                js.append("");
                                
                                if (StringUtils.isNotBlank(id)) {
                                    js.append("return document.getElementById('");
                                    js.append(id);
                                    js.append("')");
                                } else {
                                    js.append("return document.getElementsByTagName('");
                                    js.append(name);
                                    js.append("')[0]");
                                }

                                js.append(".contentDocument.body.innerHTML;");
                                
                                iframejscalls.add(js.toString());
                            } else {
                                availableHtmlObjects.add(new HtmlTagInfo(null, tag));
                            }
                        }
                    }
                    
                    return true;
                }
            });
            
            
            for (String jscall :iframejscalls) {
                Object o = webBrowser.executeJavascriptWithResult(jscall);

                if (LOG.isDebugEnabled()){
                    LOG.debug("iframe call: " + jscall);
                }

                if (o != null) {
                    if (LOG.isTraceEnabled()) {
                        LOG.trace("----------------- iframe content ----------------");
                        LOG.trace(o.toString());
                        LOG.trace("------------------------------------------------");
                    }

                    loadHtmlDomObjects(webBrowser, htmlCleaner, o.toString(), availableHtmlObjects);
                }
            }
        }
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
}
