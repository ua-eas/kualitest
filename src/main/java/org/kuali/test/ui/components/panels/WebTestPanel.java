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
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.test.Checkpoint;
import org.kuali.test.CheckpointType;
import org.kuali.test.HtmlRequestOperation;
import org.kuali.test.Operation;
import org.kuali.test.Parameter;
import org.kuali.test.Platform;
import org.kuali.test.RequestParameter;
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
import org.kuali.test.ui.components.dialogs.HttpResponseTimeCheckpointDlg;
import org.kuali.test.ui.components.dialogs.MemoryCheckPointDlg;
import org.kuali.test.ui.components.dialogs.SqlCheckPointDlg;
import org.kuali.test.ui.components.dialogs.TestExecutionParameterDlg;
import org.kuali.test.ui.components.dialogs.WebServiceCheckPointDlg;
import org.kuali.test.ui.components.splash.SplashDisplay;
import org.kuali.test.ui.utils.UIUtils;
import org.kuali.test.utils.Constants;
import org.kuali.test.utils.UpdateableNameValuePair;
import org.kuali.test.utils.Utils;

/**
 *
 * @author rbtucker
 */
public class WebTestPanel extends BaseCreateTestPanel {

    private static final Logger LOG = Logger.getLogger(WebTestPanel.class);
    private TestProxyServer testProxyServer;
    private JTabbedPane tabbedPane;
    
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
        CheckPointTypeSelectDlg dlg = new CheckPointTypeSelectDlg(getMainframe(), getTestHeader().getTestType(), getPlatform());

        if (dlg.isSaved()) {
            int cptype = dlg.getCheckpointType().intValue();

            if (LOG.isDebugEnabled()) {
                LOG.debug("checkpoint type: " + dlg.getCheckpointType() + ", intval: " + cptype);
            }

            switch (cptype) {
                case CheckpointType.INT_HTTP:
                    handleCreateHtmlCheckpoint();
                    break;
                case CheckpointType.INT_MEMORY:
                    createMemoryCheckpoint();
                    break;
                case CheckpointType.INT_SQL:
                    handleCreateSqlCheckpoint();
                    break;
                case CheckpointType.INT_WEB_SERVICE:
                    handleCreateWebServiceCheckpoint();
                    break;
                case CheckpointType.INT_FILE:
                    handleCreateFileCheckpoint();
                    break;
                case CheckpointType.INT_HTTP_RESPONSE_TIME:
                    handleCreateHttpResponseTimeCheckpoint();
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

    private void handleCreateHtmlCheckpoint() {
        HtmlCheckPointDlg dlg = new HtmlCheckPointDlg(getMainframe(), getTestHeader(), getCurrentBrowser());

        if (dlg.isSaved()) {
            addCheckpoint(testProxyServer.getTestOperations(), (Checkpoint) dlg.getNewRepositoryObject(), dlg.getComment());
        }
    }

    private void createMemoryCheckpoint() {
        MemoryCheckPointDlg dlg = new MemoryCheckPointDlg(getMainframe(), getTestHeader());

        if (dlg.isSaved()) {
            addCheckpoint(testProxyServer.getTestOperations(), (Checkpoint) dlg.getNewRepositoryObject(), dlg.getComment());
        }
    }

    private void handleCreateSqlCheckpoint() {
        SqlCheckPointDlg dlg = new SqlCheckPointDlg(getMainframe(), getTestHeader(), null, testProxyServer);

        if (dlg.isSaved()) {
            addCheckpoint(testProxyServer.getTestOperations(), (Checkpoint) dlg.getNewRepositoryObject(), dlg.getComment());
        }
    }

    private void handleCreateHttpResponseTimeCheckpoint() {
        HttpResponseTimeCheckpointDlg dlg = new HttpResponseTimeCheckpointDlg(getMainframe(), getTestHeader());

        if (dlg.isSaved()) {
            addCheckpoint(testProxyServer.getTestOperations(), (Checkpoint) dlg.getNewRepositoryObject(), dlg.getComment());
        }
    }

    private void handleCreateFileCheckpoint() {
        FileCheckPointDlg dlg = new FileCheckPointDlg(getMainframe(), getTestHeader(), null);

        if (dlg.isSaved()) {
            addCheckpoint(testProxyServer.getTestOperations(), (Checkpoint) dlg.getNewRepositoryObject(), dlg.getComment());
        }
    }

    private void handleCreateWebServiceCheckpoint() {
        WebServiceCheckPointDlg dlg = new WebServiceCheckPointDlg(getMainframe(), getTestHeader(), null);

        if (dlg.isSaved()) {
            final Checkpoint cp = (Checkpoint) dlg.getNewRepositoryObject();
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
                        } catch (Exception ex) {
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
            }

            ;

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
        WebBrowserPanel p = (WebBrowserPanel) tabbedPane.getSelectedComponent();

        if (p != null) {
            retval = p.getWebBrowser();
        }

        return retval;
    }

    private boolean moveAttachmentFiles(List <TestOperation> testOperations) throws IOException {
        for (TestOperation op : testOperations) {
            if (op.getOperation().getHtmlRequestOperation() != null) {
                HtmlRequestOperation hop = op.getOperation().getHtmlRequestOperation();
                if (Utils.isMultipart(hop)) {
                    RequestParameter contentParameter = Utils.getContentParameter(hop);

                    if (StringUtils.isNotBlank(contentParameter.getValue()) && contentParameter.getValue().contains(Constants.FILE_ATTACHMENT_MARKER)) {
                        List <NameValuePair> params = Utils.getNameValuePairsFromMultipartParams(contentParameter.getValue());

                        if (params != null) {
                            List <NameValuePair> newparams = new ArrayList<NameValuePair>();
                    
                            for (NameValuePair nvp : params) {
                                if (nvp.getName().contains(Constants.FILE_ATTACHMENT_MARKER)) {
                                    File newFile=  buildAttachmentFileName(nvp.getValue());
                                    StringTokenizer st = new StringTokenizer(nvp.getValue(), Constants.SEPARATOR_COLON);
                                    String contentType = st.nextToken();
                                    FileUtils.deleteQuietly(newFile);
                                    FileUtils.copyFile(new File(st.nextToken()), newFile);
                                    newparams.add(new UpdateableNameValuePair(nvp.getName(), contentType + Constants.SEPARATOR_COLON + newFile.getPath()));
                                } else {
                                    newparams.add(nvp);
                                }
                            }
                        
                            contentParameter.setValue(Utils.buildMultipartParameterString(newparams));
                        }
                    } 
                }
            }
        }
        
        
        return true;
    }
    
    private File buildAttachmentFileName(String tmpFileName) {
        File retval = null;
        
        File f = new File(tmpFileName);
        
        int pos = f.getName().indexOf(Constants.TMP_FILE_PREFIX_SEPARATOR);
        
        if (pos > -1) {
            StringBuilder buf = new StringBuilder(128);
            
            buf.append(Utils.buildPlatformTestsDirectoryName(getMainframe().getConfiguration().getRepositoryLocation(), getPlatform().getName()));
            buf.append(File.separator);
            buf.append(Constants.ATTACHMENTS);
            buf.append(File.separator);
            buf.append(Utils.formatForFileName(getTestHeader().getTestName()));
            buf.append(File.separator);
            buf.append(f.getName().substring(0, pos));
            
            retval = new File(buf.toString());
            
            if (!retval.getParentFile().exists()) {
                retval.getParentFile().mkdirs();
            }
        }
        
        return retval;
    }
    /**
     *
     * @return
     */
    @Override
    protected boolean handleSaveTest() {
        boolean retval = false;
        
         new SwingWorker() {
            @Override
            protected Object doInBackground() throws Exception {
                Boolean retval = false;
                getMainframe().startSpinner("Saving test...");

                if (moveAttachmentFiles(testProxyServer.getTestOperations())) {
                    retval = saveTest(getMainframe().getConfiguration().getRepositoryLocation(),
                        getTestHeader(), testProxyServer.getTestOperations());
                }

                return retval;
            }
            
            @Override
            protected void done() {
                Boolean b = false;
                try {
                    b = (Boolean)get();
                } 
                
                catch (Exception ex) {};
                
                if (b) {
                    getMainframe().getTestRepositoryTree().saveConfiguration();
                    getMainframe().getCreateTestPanel().clearPanel("test '" + getTestHeader().getTestName() + "' created");
                }
                closeProxyServer();
                getMainframe().getPlatformTestsPanel().populateList(getPlatform());
                getMainframe().stopSpinner();
            }
            
        }.execute();
        
        
        return retval;
    }

    public void closeProxyServer() {
        try {
            if (testProxyServer != null) {
                if (testProxyServer.getTestOperations() != null) {
                   testProxyServer.getTestOperations().clear();
                }
                testProxyServer.stop();
                testProxyServer = null;
            }
        } catch (Throwable t) {
            LOG.warn(t.toString(), t);
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

    private boolean isHtml(String input) {
        return (StringUtils.isNotBlank(input) && input.contains("<html>"));
    }

    @Override
    protected void handleCreateParameter() {
        TestExecutionParameterDlg dlg = new TestExecutionParameterDlg(getMainframe(), getCurrentBrowser(), testProxyServer.getTestOperations(), getTestHeader());

        if (dlg.isSaved()) {
            addTestExecutionParameter(dlg.getTestExecutionParameter());
        }
    }

    public TestProxyServer getTestProxyServer() {
        return testProxyServer;
    }

    @Override
    protected List<Checkpoint> getCheckpoints() {
        List<Checkpoint> retval = new ArrayList<Checkpoint>();

        for (TestOperation op : testProxyServer.getTestOperations()) {
            if (op.getOperation().getCheckpointOperation() != null) {
                retval.add(op.getOperation().getCheckpointOperation());
            }
        }

        return retval;
    }

    @Override
    protected List<String> getComments() {
        List<String> retval = new ArrayList<String>();

        for (TestOperation op : testProxyServer.getTestOperations()) {
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
        List<TestExecutionParameter> retval = new ArrayList<TestExecutionParameter>();

        for (TestOperation op : testProxyServer.getTestOperations()) {
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