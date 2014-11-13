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
import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserAdapter;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserNavigationEvent;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserWindowOpeningEvent;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserWindowWillOpenEvent;
import java.awt.BorderLayout;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.test.Checkpoint;
import org.kuali.test.CheckpointType;
import org.kuali.test.Operation;
import org.kuali.test.Parameter;
import org.kuali.test.Platform;
import org.kuali.test.TestExecutionParameter;
import org.kuali.test.TestHeader;
import org.kuali.test.TestOperation;
import org.kuali.test.TestOperationType;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.proxyserver.TestProxyServer;
import org.kuali.test.runner.execution.WebServiceOperationExecution;
import org.kuali.test.ui.base.SimpleInputDlg2;
import org.kuali.test.ui.components.buttons.CloseTabIcon;
import org.kuali.test.ui.components.dialogs.CheckPointTypeSelectDlg;
import org.kuali.test.ui.components.dialogs.FileCheckPointDlg;
import org.kuali.test.ui.components.dialogs.HtmlCheckPointDlg;
import org.kuali.test.ui.components.dialogs.MemoryCheckPointDlg;
import org.kuali.test.ui.components.dialogs.SqlCheckPointDlg;
import org.kuali.test.ui.components.dialogs.TestExecutionParameterDlg;
import org.kuali.test.ui.components.dialogs.WebServiceCheckPointDlg;
import org.kuali.test.ui.components.splash.SplashDisplay;
import org.kuali.test.ui.utils.UIUtils;
import org.kuali.test.utils.Constants;
import org.kuali.test.utils.Utils;

/**
 * 
 * @author rbtucker
 */
public class WebTestPanel extends BaseCreateTestPanel implements ContainerListener  {
    private static final Logger LOG = Logger.getLogger(WebTestPanel.class);

    private TestProxyServer testProxyServer;
    private JTabbedPane tabbedPane;
    private String lastProxyHtmlResponse;
    /**
     *
     * @param mainframe
     * @param platform
     * @param testHeader
     */
    public WebTestPanel(TestCreator mainframe, Platform platform, TestHeader testHeader, String testDescription) {
        super(mainframe, platform, testHeader, testDescription);
        initComponents();
    }

    @Override
    protected void initComponents() {
        super.initComponents();
        addContainerListener(this);
        tabbedPane = new JTabbedPane();
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
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

    private void addTestExecutionParameter(TestExecutionParameter param) {
        TestOperation testOp = TestOperation.Factory.newInstance();
        testOp.setOperationType(TestOperationType.TEST_EXECUTION_PARAMETER);
        Operation op = testOp.addNewOperation();
        op.addNewCheckpointOperation();
        op.setTestExecutionParameter(param);
        testProxyServer.getTestOperations().add(testOp);
    }

    private void createHtmlCheckpoint() {
        JWebBrowser wb = getCurrentBrowser();
        HtmlCheckPointDlg dlg = new HtmlCheckPointDlg(getMainframe(), getTestHeader(), wb, lastProxyHtmlResponse);

        if (dlg.isSaved()) {
            addCheckpoint(testProxyServer.getTestOperations(), (Checkpoint)dlg.getNewRepositoryObject(), dlg.getComment());
        }
    }
    
    private void createMemoryCheckpoint() {
        MemoryCheckPointDlg dlg = new MemoryCheckPointDlg(getMainframe(), getTestHeader());

        if (dlg.isSaved()) {
            addCheckpoint(testProxyServer.getTestOperations(), (Checkpoint)dlg.getNewRepositoryObject(), dlg.getComment());
        }
    }
    
    private void createSqlCheckpoint() {
        SqlCheckPointDlg dlg = new SqlCheckPointDlg(getMainframe(), getTestHeader(), null, testProxyServer);

        if (dlg.isSaved()) {
            addCheckpoint(testProxyServer.getTestOperations(), (Checkpoint)dlg.getNewRepositoryObject(), dlg.getComment());
        }
    }
    
    private void createFileCheckpoint() {
        FileCheckPointDlg dlg = new FileCheckPointDlg(getMainframe(), getTestHeader(), null);

        if (dlg.isSaved()) {
            addCheckpoint(testProxyServer.getTestOperations(), (Checkpoint)dlg.getNewRepositoryObject(), dlg.getComment());
        }
    }

    private void createWebServiceCheckpoint() {
        WebServiceCheckPointDlg dlg = new WebServiceCheckPointDlg(getMainframe(), getTestHeader(), null);

        if (dlg.isSaved()) {
            final Checkpoint cp = (Checkpoint)dlg.getNewRepositoryObject();
            addCheckpoint(testProxyServer.getTestOperations(), cp, dlg.getComment());
            if (dlg.isRunWebService()) {
                String opname = "unknown";
                final boolean poll = dlg.isPoll();
                getTestHeader().setAdditionalParameters("" + poll);
                if (cp.getInputParameters() != null) {
                    for (Parameter param : cp.getInputParameters().getParameterArray()) {
                        if (Constants.WEB_SERVICE_OPERATION.equalsIgnoreCase(param.getName())) {
                            opname = param.getValue();
                            break;
                        }
                    }
                }
                
                String[] wsparts = Utils.getWebServiceOperationParts(opname);
                long start = System.currentTimeMillis();
                
                new SplashDisplay(getMainframe(), "Run Web Service", "Running web service '" + wsparts[1] + "'", true) {
                    @Override
                    protected void processCompleted() {
                    }

                    @Override
                    protected void runProcess() {
                        WebServiceOperationExecution wsop = new WebServiceOperationExecution(cp) {
                            
                            @Override
                            protected void processUpdate(long runtime) {
                                String txt = "  Elapsed Time: " + (runtime / 1000) + "sec.";
                                if (runtime >= Constants.MILLIS_PER_MINUTE) {
                                    txt = ("  Elapsed Time: " + (runtime / Constants.MILLIS_PER_MINUTE) + "min. " + ((runtime / 1000) % 60) + "sec.");
                                }
                                
                                updateElapsedTime(txt);
                            }
                        };
                        
                        try {
                            wsop.executeWebServiceCall(getMainframe().getConfiguration(), getPlatform(), cp, poll);
                        }
                        
                        catch (Exception ex) {
                            LOG.error(ex.toString(), ex);
                            UIUtils.showError(getDlg(), "Error", ex.toString());
                        }
                    }

                };
            }
        }
    }

    @Override
    protected void handleCancelTest() {
        testProxyServer.getTestOperations().clear();
        getMainframe().getCreateTestPanel().clearPanel("test '" + getTestHeader().getTestName() + "' cancelled");
        getMainframe().getCreateTestButton().setEnabled(true);
        getMainframe().getCreateTestMenuItem().setEnabled(true);
        closeProxyServer();
    }

    @Override
    protected void handleStartTest() {
        getMainframe().startSpinner("Initializing browser interface...");
        
        new SwingWorker() {
            @Override
            protected Object doInBackground() throws Exception {
                testProxyServer = new TestProxyServer(WebTestPanel.this);
                addNewBrowserPanel(true);
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        getCurrentBrowser().navigate(getPlatform().getWebUrl());
                    }
                });
                return null;
            };

            @Override
            protected void done() {
                getMainframe().stopSpinner();
            }
        }.execute();

        getMainframe().getCreateTestButton().setEnabled(false);
        getMainframe().getCreateTestMenuItem().setEnabled(false);
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
    @Override
    public void componentAdded(ContainerEvent e) {
    }

    @Override
    public void componentRemoved(ContainerEvent e) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("component removed: " + e.getComponent().getClass().getName());
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
     * @param lastResponse 
     */
    public void setLastProxyHtmlResponse(String lastResponse) {
        if (isHtml(lastResponse)) {
            lastProxyHtmlResponse = lastResponse;
        }
    }

    private boolean isHtml(String input) {
        return (StringUtils.isNotBlank(input) && input.contains("<html>"));
    }

    @Override
   protected void handleCreateParameter() {
        TestExecutionParameterDlg dlg = new TestExecutionParameterDlg(getMainframe(), 
            getCurrentBrowser(), getTestHeader(), lastProxyHtmlResponse);
        
        if (dlg.isSaved()) {
            addTestExecutionParameter(dlg.getTestExecutionParameter());
        }
    }
    
    public TestProxyServer getTestProxyServer() {
        return testProxyServer;
    }
    
    @Override
    protected List<Checkpoint> getCheckpoints() {
        List <Checkpoint> retval = new ArrayList<Checkpoint>();
        
        for (TestOperation op :  testProxyServer.getTestOperations()) {
            if (op.getOperation().getCheckpointOperation() != null) {
                retval.add(op.getOperation().getCheckpointOperation());
            }
        }
        
        return retval;
    }
    
    @Override
    protected List<String> getComments() {
        List <String> retval = new ArrayList<String>();
        
        for (TestOperation op :  testProxyServer.getTestOperations()) {
            if (op.getOperation().getCommentOperation() != null) {
                retval.add(op.getOperation().getCommentOperation().getComment());
            }
        }
        
        return retval;
    }

    @Override
    protected void handleCreateComment() {
        SimpleInputDlg2 dlg = new SimpleInputDlg2(getMainframe(), "Add Comment");
        
        String comment = dlg.getEnteredValue();
        if (StringUtils.isNotBlank(comment)) {
            addComment(testProxyServer.getTestOperations(), comment);
        }
    }

    @Override
    protected List<TestExecutionParameter> getParameters() {
        List <TestExecutionParameter> retval = new ArrayList<TestExecutionParameter>();
        
        for (TestOperation op :  testProxyServer.getTestOperations()) {
            if (op.getOperation().getTestExecutionParameter() != null) {
                retval.add(op.getOperation().getTestExecutionParameter());
            }
        }
        
        return retval;
    }

    @Override
    protected boolean isParameterOperationRequired() {
        return true;
    }

    @Override
    protected boolean isForCheckpoint() {
        return false;
    }
}
