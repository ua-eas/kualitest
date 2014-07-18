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

import chrriis.dj.nativeswing.NSComponentOptions;
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
import javax.swing.SwingUtilities;
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

/**
 * 
 * @author rbtucker
 */
public class WebTestPanel extends BaseCreateTestPanel implements ContainerListener  {
    private static final Logger LOG = Logger.getLogger(WebTestPanel.class);

    private TestProxyServer testProxyServer;
    private JTabbedPane tabbedPane;
    private String lastProxyHtmlResponse;
    private ToolbarButton executionAttribute;
    private ToolbarButton refresh;
    
    /**
     *
     * @param mainframe
     * @param platform
     * @param testHeader
     */
    public WebTestPanel(TestCreator mainframe, Platform platform, TestHeader testHeader) {
        super(mainframe, platform, testHeader);

        initComponents();

        new SplashDisplay(mainframe, "Initializing Web Test", "Loading web proxy server...") {
            @Override
            protected void runProcess() {
                testProxyServer = new TestProxyServer(WebTestPanel.this);
                getStartTest().setEnabled(true);
            }
        };

        initializeNativeBrowser();
    }

    /**
     *
     */
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

    /**
     *
     * @param initial
     * @return
     */
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
        JWebBrowser retval = new JWebBrowser(NSComponentOptions.destroyOnFinalization());

        if (initial) {
            retval.setButtonBarVisible(false);
            retval.setLocationBarVisible(false);
            retval.setMenuBarVisible(false);
            retval.setStatusBarVisible(true);
        }

        retval.addWebBrowserListener(new WebBrowserAdapter() {
            @Override
            public void windowWillOpen(WebBrowserWindowWillOpenEvent e) {
                e.getNewWebBrowser().disposeNativePeer();
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

   public String getCurrentHtmlResponse(JWebBrowser wb) {
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
        HtmlCheckPointDlg dlg = new HtmlCheckPointDlg(getMainframe(), getTestHeader(), wb, getCurrentHtmlResponse(wb));

        if (dlg.isSaved()) {
            addCheckpoint((Checkpoint)dlg.getNewRepositoryObject());
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

    @Override
    protected void handleCancelTest() {
        testProxyServer.getTestOperations().clear();
        getMainframe().getCreateTestPanel().clearPanel("test '" + getTestHeader().getTestName() + "' cancelled");
        getMainframe().getCreateTestButton().setEnabled(true);
        getMainframe().getCreateTestMenuItem().setEnabled(true);
        executionAttribute.setEnabled(false);
        refresh.setEnabled(false);

        closeProxyServer();
    }

    @Override
    protected void handleStartTest() {
        getMainframe().getCreateTestButton().setEnabled(false);
        getMainframe().getCreateTestMenuItem().setEnabled(false);
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                getCurrentBrowser().navigate(getPlatform().getWebUrl());
            }
        });
         
        executionAttribute.setEnabled((getMainframe().getConfiguration().getTestExecutionParameterNames() != null) 
            && (getMainframe().getConfiguration().getTestExecutionParameterNames().sizeOfNameArray() > 0));
        refresh.setEnabled(true);
    }

    public JWebBrowser getCurrentBrowser() {
        JWebBrowser retval = null;
        WebBrowserPanel p = (WebBrowserPanel)tabbedPane.getSelectedComponent();
        
        if (p != null) {
            retval = p.getWebBrowser();
        }
        
        return retval;
    }

    /**
     *
     * @return
     */
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
            try {
                NativeInterface.open();
            }
            
            catch (Exception ex) {
                LOG.error(ex.toString(), ex);
            }
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

    /**
     *
     * @return
     */
    @Override
    protected boolean isStartTestRequired() {
        return true;
    }

    /**
     *
     * @param lastProxyHtmlResponse
     */
    public void setLastProxyHtmlResponse(String lastProxyHtmlResponse) {
        this.lastProxyHtmlResponse = lastProxyHtmlResponse;
    }

    /**
     *
     * @return
     */
    @Override
    protected List<ToolbarButton> getCustomButtons() {
        List <ToolbarButton> retval = new ArrayList<ToolbarButton>();
        retval.add(executionAttribute = new ToolbarButton(Constants.EXECUTION_PARAMETER_ACTION, Constants.EXECUTION_PARAMETER_ICON));
        executionAttribute.setToolTipText("add test execution parameter");
        executionAttribute.setEnabled(false);
        retval.add(refresh = new ToolbarButton(Constants.REFRESH_BROWSER_ACTION, Constants.REFRESH_BROWSER_ICON));
        refresh.setToolTipText("refresh browser");
        refresh.setEnabled(false);

        return retval;
    }

    /**
     *
     * @param e
     */
    @Override
    protected void handleUnprocessedActionEvent(ActionEvent e) {
        if (Constants.EXECUTION_PARAMETER_ACTION.equalsIgnoreCase(e.getActionCommand())) {
            handleAddExecutionParameter();
        } else if (Constants.REFRESH_BROWSER_ACTION.equalsIgnoreCase(e.getActionCommand())) {
            getCurrentBrowser().reloadPage();
        }
    }
    
    private void handleAddExecutionParameter() {
        TestExecutionParameterDlg dlg = new TestExecutionParameterDlg(getMainframe(), 
            getCurrentBrowser(), getTestHeader(), getCurrentHtmlResponse(getCurrentBrowser()));
        
        if (dlg.isSaved()) {
            addTestExecutionParameter(dlg.getTestExecutionParameter());
        }
    }

    /**
     *
     * @return
     */
    public TestProxyServer getTestProxyServer() {
        return testProxyServer;
    }
}
