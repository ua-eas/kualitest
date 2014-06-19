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
package org.kuali.test.creator;

import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.WindowStateListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.kuali.test.DatabaseConnection;
import org.kuali.test.JmxConnection;
import org.kuali.test.KualiTestConfigurationDocument;
import org.kuali.test.Platform;
import org.kuali.test.SuiteTest;
import org.kuali.test.TestHeader;
import org.kuali.test.TestSuite;
import org.kuali.test.TestType;
import org.kuali.test.WebService;
import org.kuali.test.ui.components.buttons.ToolbarButton;
import org.kuali.test.ui.components.databasestree.DatabaseTree;
import org.kuali.test.ui.components.dialogs.CreateTestDlg;
import org.kuali.test.ui.components.dialogs.DatabaseDlg;
import org.kuali.test.ui.components.dialogs.EmailDlg;
import org.kuali.test.ui.components.dialogs.JmxDlg;
import org.kuali.test.ui.components.dialogs.PlatformDlg;
import org.kuali.test.ui.components.dialogs.ScheduleTestsDlg;
import org.kuali.test.ui.components.dialogs.TestExecutionParameterNamesDlg;
import org.kuali.test.ui.components.dialogs.TestInformationDlg;
import org.kuali.test.ui.components.dialogs.TestSuiteDlg;
import org.kuali.test.ui.components.dialogs.WebServiceDlg;
import org.kuali.test.ui.components.jmxtree.JmxTree;
import org.kuali.test.ui.components.panels.CreateTestPanel;
import org.kuali.test.ui.components.panels.FileTestPanel;
import org.kuali.test.ui.components.panels.PlatformTestsPanel;
import org.kuali.test.ui.components.panels.WebServicePanel;
import org.kuali.test.ui.components.panels.WebTestPanel;
import org.kuali.test.ui.components.repositorytree.RepositoryTree;
import org.kuali.test.ui.components.splash.SplashDisplay;
import org.kuali.test.ui.components.sqlquerypanel.DatabasePanel;
import org.kuali.test.ui.components.webservicetree.WebServiceTree;
import org.kuali.test.ui.utils.UIUtils;
import org.kuali.test.utils.ApplicationInstanceListener;
import org.kuali.test.utils.ApplicationInstanceManager;
import org.kuali.test.utils.Constants;
import org.kuali.test.utils.Utils;

/**
 *
 * @author rbtucker
 */
public class TestCreator extends JFrame implements WindowListener, ClipboardOwner {

    private static final Logger LOG = Logger.getLogger(TestCreator.class);

    private String configFileName;
    private JDesktopPane desktopPane;
    private JSplitPane hsplitPane;
    private JSplitPane vsplitPane;
    private CreateTestPanel createTestPanel;
    private ToolbarButton saveConfigurationButton;
    private ToolbarButton createTestButton;
    private ToolbarButton exitApplication;
    private JMenuItem saveConfigurationMenuItem;
    private JMenuItem createTestMenuItem;
    private RepositoryTree testRepositoryTree;
    private DatabaseTree databaseTree;
    private WebServiceTree webServiceTree;
    private JmxTree jmxTree;
    private PlatformTestsPanel platformTestsPanel;

    public TestCreator(String configFileName) {
        this.configFileName = configFileName;
        if (LOG.isDebugEnabled()) {
            LOG.debug("input configuration file name: " + configFileName);
        }
        
        setIconImage(Constants.KUALI_TEST_ICON.getImage());
        setTitle(Constants.KUALI_TEST_TITLE);
        initComponents();
        loadPreferences();
        installShutdownHook();
    }

    private void installShutdownHook() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("installing shutdown hook");
        }

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("running shutdown hook");
                }

                try {
                    if (NativeInterface.isOpen()) {
                        NativeInterface.close();
                    }
                } catch (Exception ex) {
                    LOG.warn(ex);
                }
            }
        });
    }

    private void loadPreferences() {
        Preferences proot = Preferences.userRoot();
        Preferences node = proot.node(Constants.PREFS_ROOT_NODE);
        int left = node.getInt(Constants.PREFS_MAINFRAME_LEFT, Constants.MAINFRAME_DEFAULT_LEFT);
        int top = node.getInt(Constants.PREFS_MAINFRAME_TOP, Constants.MAINFRAME_DEFAULT_TOP);
        int width = node.getInt(Constants.PREFS_MAINFRAME_WIDTH, Constants.MAINFRAME_DEFAULT_WIDTH);
        int height = node.getInt(Constants.PREFS_MAINFRAME_HEIGHT, Constants.MAINFRAME_DEFAULT_HEIGHT);
        setState(node.getInt(Constants.PREFS_MAINFRAME_WINDOW_STATE, Frame.NORMAL));

        setBounds(left, top, width, height);
        hsplitPane.setDividerLocation(node.getInt(Constants.PREFS_HORIZONTAL_DIVIDER_LOCATION, Constants.DEFAULT_HORIZONTAL_DIVIDER_LOCATION));
        vsplitPane.setDividerLocation(node.getInt(Constants.PREFS_VERTICAL_DIVIDER_LOCATION, Constants.DEFAULT_VERTICAL_DIVIDER_LOCATION));
    }

    private void savePreferences() {
        Preferences proot = Preferences.userRoot();
        Preferences node = proot.node(Constants.PREFS_ROOT_NODE);

        Rectangle rect = getBounds();

        node.putInt(Constants.PREFS_MAINFRAME_LEFT, rect.x);
        node.putInt(Constants.PREFS_MAINFRAME_TOP, rect.y);
        node.putInt(Constants.PREFS_MAINFRAME_WIDTH, rect.width);
        node.putInt(Constants.PREFS_MAINFRAME_HEIGHT, rect.height);
        node.putInt(Constants.PREFS_HORIZONTAL_DIVIDER_LOCATION, hsplitPane.getDividerLocation());
        node.putInt(Constants.PREFS_VERTICAL_DIVIDER_LOCATION, vsplitPane.getDividerLocation());
        node.putInt(Constants.PREFS_MAINFRAME_WINDOW_STATE, getState());
    }

    private void createMenuBar() {
        JMenuBar mainMenu = new JMenuBar();
        JMenu fileMenu = new JMenu();
        JMenuItem loadConfiguationMenuItem = new JMenuItem();
        JMenuItem setup = new JMenu();
        JMenuItem addPlatformMenuItem = new JMenuItem();
        JMenuItem addDatabaseConnectionMenuItem = new JMenuItem();
        JMenuItem addWebServiceMenuItem = new JMenuItem();
        JMenuItem addJmxConnectionMenuItem = new JMenuItem();
        JMenuItem emailSetupMenuItem = new JMenuItem();
        JMenuItem testExecutionParameterNamesMenuItem = new JMenuItem();
        JMenuItem scheduleTestsMenuItem = new JMenuItem();
        JMenuItem exitMenuItem = new JMenuItem();
        JMenuItem helpMenu = new JMenu();
        JMenuItem contentMenuItem = new JMenuItem();
        JMenuItem aboutMenuItem = new JMenuItem();

        fileMenu.setMnemonic('f');
        fileMenu.setText("File");

        loadConfiguationMenuItem.setText("Load Configuration...");
        loadConfiguationMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                handleLoadConfiguation(evt);
            }
        });

        fileMenu.add(loadConfiguationMenuItem);

        scheduleTestsMenuItem.setText("Schedule Tests...");
        scheduleTestsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                handleScheduleTests(evt);
            }
        });

        fileMenu.add(scheduleTestsMenuItem);

        fileMenu.add(new JSeparator());
        
        saveConfigurationMenuItem = new JMenuItem("Save Repository Configuration");
        saveConfigurationMenuItem.setEnabled(false);
        
        saveConfigurationMenuItem.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                testRepositoryTree.saveConfiguration();
                saveConfigurationButton.setEnabled(false);
                saveConfigurationMenuItem.setEnabled(false);
                setCreateTestState();
            }
        });
        
        fileMenu.add(saveConfigurationMenuItem);
        
        createTestMenuItem = new JMenuItem("Create Test");
        createTestMenuItem.setEnabled(false);
        
        createTestMenuItem.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                handleCreateTest(null);
            }
        });
        
        fileMenu.add(createTestMenuItem);
        
        fileMenu.add(new JSeparator());
        
        setup.setText("Setup");

        addPlatformMenuItem.setText("Add Platform");
        addPlatformMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                handleAddPlatform(evt);
            }
        });
        setup.add(addPlatformMenuItem);

        addDatabaseConnectionMenuItem.setText("Add Database Connection");
        addDatabaseConnectionMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                handleAddDatabaseConnection(evt);
            }
        });
        setup.add(addDatabaseConnectionMenuItem);

        addWebServiceMenuItem.setText("Add Web Service");

        addWebServiceMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                handleAddWebService(evt);
            }
        });

        setup.add(addWebServiceMenuItem);
        
        addJmxConnectionMenuItem.setText("Add JMX Connection");

        addJmxConnectionMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                handleAddJmxConnection(evt);
            }
        });

        setup.add(addJmxConnectionMenuItem);
        
        testExecutionParameterNamesMenuItem.setText("Test Execution Parameter Names");
        testExecutionParameterNamesMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                handleTestExecutionParameterNamesSetup();
            }
        });
        
        setup.add(testExecutionParameterNamesMenuItem);

        setup.add(new JSeparator());
        
        emailSetupMenuItem.setText("Email Setup");
        emailSetupMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                handleEmailSetup(evt);
            }
        });
        setup.add(emailSetupMenuItem);

        fileMenu.add(setup);

        fileMenu.add(new JSeparator());

        exitMenuItem.setMnemonic('x');
        exitMenuItem.setText("Exit");
        exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitApplication.doClick();
            }
        });
        fileMenu.add(exitMenuItem);

        mainMenu.add(fileMenu);

        helpMenu.setMnemonic('h');
        helpMenu.setText("Help");

        contentMenuItem.setMnemonic('c');
        contentMenuItem.setText("Contents");
        helpMenu.add(contentMenuItem);

        aboutMenuItem.setMnemonic('a');
        aboutMenuItem.setText("About");
        helpMenu.add(aboutMenuItem);

        mainMenu.add(helpMenu);

        setJMenuBar(mainMenu);
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this
     * method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    private void initComponents() {
        desktopPane = new JDesktopPane();
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        createMenuBar();
        addWindowListener(this);

        desktopPane.setLayout(new java.awt.BorderLayout());

        hsplitPane = new JSplitPane();
        hsplitPane.setDividerLocation(150);
        hsplitPane.setOneTouchExpandable(true);

        vsplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        vsplitPane.setDividerLocation(250);

        vsplitPane.setBottomComponent(platformTestsPanel = new PlatformTestsPanel(this));

        JPanel p = new JPanel(new BorderLayout());
        p.add(new JScrollPane(testRepositoryTree = new RepositoryTree(this)), BorderLayout.CENTER);
        vsplitPane.setTopComponent(p);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab(Constants.REPOSITORY, vsplitPane);
        tabbedPane.addTab(Constants.DATABASES, new JScrollPane(databaseTree = new DatabaseTree(this, getConfiguration())));
        tabbedPane.addTab(Constants.WEBSERVICES, new JScrollPane(webServiceTree = new WebServiceTree(this, getConfiguration())));
        tabbedPane.addTab(Constants.JMX, new JScrollPane(jmxTree = new JmxTree(this, getConfiguration())));

        hsplitPane.setLeftComponent(tabbedPane);
        hsplitPane.setRightComponent(createTestPanel = new CreateTestPanel(this));

        desktopPane.add(createToolBar(), BorderLayout.NORTH);

        desktopPane.add(hsplitPane, BorderLayout.CENTER);

        getContentPane().add(desktopPane);
        
        setCreateTestState();

        pack();
    }

    private void setCreateTestState() {
        setCreateTestEnabled(getConfiguration().getPlatforms().sizeOfPlatformArray() > 0);
    }

    private void setCreateTestEnabled(boolean enabled) {
        createTestButton.setEnabled(enabled);
        createTestMenuItem.setEnabled(enabled);
    }

    public void handleDeleteTest(TestHeader testHeader) {
        if (UIUtils.promptForDelete(this, "Delete Test", "Delete test '" + testHeader.getTestName() + "'?")) {
            String platformName = testHeader.getPlatformName();
            String testFileName = testHeader.getTestFileName();
            
            Platform p = Utils.findPlatform(getConfiguration(), platformName);
            
            if (p != null) {
                if (p.getTestSuites() != null) {
                    TestSuite[] testSuites = p.getTestSuites().getTestSuiteArray();

                    for (TestSuite testSuite: testSuites) {
                        if (testSuite.getSuiteTests() != null) {
                            List <SuiteTest> l = new ArrayList<SuiteTest>();
                            SuiteTest[] suiteTests = testSuite.getSuiteTests().getSuiteTestArray();
                            for (int i = 0; i < suiteTests.length; ++i) {
                                if (!suiteTests[i].getTestHeader().getTestName().equalsIgnoreCase(testHeader.getTestName())) {
                                    l.add(suiteTests[i]);
                                }
                            }
                            
                            testSuite.getSuiteTests().setSuiteTestArray(l.toArray(new SuiteTest[l.size()]));
                        }
                    }
                }
                
                if (p.getPlatformTests() != null) {
                    TestHeader[] platformTests = p.getPlatformTests().getTestHeaderArray();
                    
                    for (int i = 0; i < platformTests.length; ++i) {
                        if (platformTests[i].getTestName().equalsIgnoreCase(testHeader.getTestName())) {
                            p.getPlatformTests().removeTestHeader(i);
                            break;
                        }
                    }
                }
                
                
                try {
                    File f = new File(testFileName);
                    
                    if (f.exists() && f.isFile()) {
                        FileUtils.forceDelete(f);
                    }
                    
                    testRepositoryTree.saveConfiguration();

                    p = Utils.findPlatform(getConfiguration(), platformName);

                    
                    getPlatformTestsPanel().populateList(p);
                    
                    testRepositoryTree.refreshPlatformNode(p);
                } 
                
                catch (IOException ex) {
                    UIUtils.showError(this, "File Delete Error", "Error occurred while deleting test file - " + testFileName);
                }
            }
        }
    }

    public void handleCreateTest(Platform platform) {
        setCreateTestEnabled(false);
        CreateTestDlg dlg = new CreateTestDlg(this, platform);

        if (dlg.isSaved()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("init new " + dlg.getTestHeader().getTestType() + " test for platform " + dlg.getTestHeader().getPlatformName());
            }

            TestHeader testHeader = dlg.getTestHeader();
            Platform testPlatform = Utils.findPlatform(getConfiguration(), testHeader.getPlatformName());

            switch (testHeader.getTestType().intValue()) {
                case TestType.INT_WEB:
                    createTestPanel.replaceCenterComponent(new WebTestPanel(this, testPlatform, testHeader));
                    break;
                case TestType.INT_WEB_SERVICE:
                    createTestPanel.replaceCenterComponent(new WebServicePanel(this, testPlatform, testHeader));
                    break;
                case TestType.INT_DATABASE:
                    createTestPanel.replaceCenterComponent(new DatabasePanel(this, testPlatform, testHeader));
                    break;
                case TestType.INT_FILE:
                    createTestPanel.replaceCenterComponent(new FileTestPanel(this, testPlatform, testHeader));
                    break;
            }
        }
        
        setCreateTestEnabled(true);
    }

    public CreateTestPanel getCreateTestPanel() {
        return createTestPanel;
    }


    public void handleExit() {
        savePreferences();
        System.exit(0);
    }
    
    public void handleAddDatabaseConnection(ActionEvent evt) {
        DatabaseDlg dlg = new DatabaseDlg(this);

        if (dlg.isSaved()) {
            saveConfigurationButton.setEnabled(true);
            saveConfigurationMenuItem.setEnabled(true);
            DatabaseConnection dbconn = (DatabaseConnection) dlg.getNewRepositoryObject();
            databaseTree.addDatabaseConnection(dbconn);
        }
    }

    public void handleEditDatabaseConnection(DatabaseConnection databaseConnection) {
        DatabaseDlg dlg = new DatabaseDlg(this, databaseConnection);

        if (dlg.isSaved()) {
            saveConfigurationButton.setEnabled(true);
            saveConfigurationMenuItem.setEnabled(true);
        }
    }

    public void handleAddWebService(ActionEvent evt) {
        WebServiceDlg dlg = new WebServiceDlg(this);

        if (dlg.isSaved()) {
            saveConfigurationButton.setEnabled(true);
            saveConfigurationMenuItem.setEnabled(true);
            WebService ws = (WebService) dlg.getNewRepositoryObject();
            webServiceTree.addWebService(ws);
        }
    }

    public void handleEditWebService(WebService ws) {
        WebServiceDlg dlg = new WebServiceDlg(this, ws);

        if (dlg.isSaved()) {
            saveConfigurationButton.setEnabled(true);
            saveConfigurationMenuItem.setEnabled(true);
        }
    }

    public void handleRemoveWebService(DefaultMutableTreeNode actionNode) {
        WebService ws = (WebService) actionNode.getUserObject();
        if (UIUtils.promptForDelete(this, "Delete Web Service",
            "Delete web service '" + ws.getName() + "'?")) {
            webServiceTree.removeNode(actionNode);
            if (Utils.removeRepositoryNode(getConfiguration(), actionNode)) {
                saveConfigurationButton.setEnabled(true);
                saveConfigurationMenuItem.setEnabled(true);
            }
        }
    }

    public void handleAddJmxConnection(ActionEvent evt) {
        JmxDlg dlg = new JmxDlg(this);

        if (dlg.isSaved()) {
            saveConfigurationButton.setEnabled(true);
            saveConfigurationMenuItem.setEnabled(true);
            JmxConnection jmx = (JmxConnection) dlg.getNewRepositoryObject();
            jmxTree.addJmxConnection(jmx);
        }
    }

    public void handleEditJmxConnection(JmxConnection jmx) {
        JmxDlg dlg = new JmxDlg(this, jmx);

        if (dlg.isSaved()) {
            saveConfigurationButton.setEnabled(true);
            saveConfigurationMenuItem.setEnabled(true);
        }
    }

    public void handleRemoveJmxConnection(DefaultMutableTreeNode actionNode) {
        JmxConnection jmx = (JmxConnection) actionNode.getUserObject();
        if (UIUtils.promptForDelete(this, "Delete JMX Connection",
            "Delete JMX connection '" + jmx.getName() + "'?")) {
           jmxTree.removeNode(actionNode);
            if (Utils.removeRepositoryNode(getConfiguration(), actionNode)) {
                saveConfigurationButton.setEnabled(true);
                saveConfigurationMenuItem.setEnabled(true);
            }
        }
    }

    public void handleAddPlatform(ActionEvent evt) {
        PlatformDlg dlg = new PlatformDlg(this);

        if (dlg.isSaved()) {
            saveConfigurationButton.setEnabled(true);
            saveConfigurationMenuItem.setEnabled(true);
            if (!dlg.isEditmode()) {
                testRepositoryTree.addRepositoryNode(dlg.getNewRepositoryObject());
            }

        }
    }

    public void handleEditPlatform(Platform platform) {
        PlatformDlg dlg = new PlatformDlg(this, platform);

        if (dlg.isSaved()) {
            saveConfigurationButton.setEnabled(true);
            saveConfigurationMenuItem.setEnabled(true);
        }
    }

    public void handleAddEditTestSuite(DefaultMutableTreeNode actionNode) {
        Platform platform = null;
        TestSuite testSuite = null;
        if (actionNode.getUserObject() instanceof Platform) {
            platform = (Platform) actionNode.getUserObject();
        } else {
            testSuite = (TestSuite) actionNode.getUserObject();
            DefaultMutableTreeNode pnode = (DefaultMutableTreeNode) actionNode.getParent();
            platform = (Platform) pnode.getUserObject();
        }

        TestSuiteDlg dlg = new TestSuiteDlg(this, platform, testSuite);

        if (dlg.isSaved()) {
            saveConfigurationButton.setEnabled(true);
            saveConfigurationMenuItem.setEnabled(true);
            if (!dlg.isEditmode()) {
                testRepositoryTree.addRepositoryNode(dlg.getNewRepositoryObject());
            }
        }
    }

    public void handleShowTestInformation(DefaultMutableTreeNode actionNode) {
        SuiteTest suiteTest = (SuiteTest) actionNode.getUserObject();
        handleShowTestInformation(suiteTest.getTestHeader());
    }

    public void handleShowTestInformation(TestHeader testHeader) {
        new TestInformationDlg(this, testHeader);
    }

    public void handleRemoveTest(DefaultMutableTreeNode actionNode) {
        SuiteTest suiteTest = (SuiteTest) actionNode.getUserObject();
        if (UIUtils.promptForDelete(this, "Remove Test",
            "Remove test '" + suiteTest.getTestHeader().getTestName() + "'?")) {
            testRepositoryTree.removeNode(actionNode);
            if (Utils.removeRepositoryNode(getConfiguration(), actionNode)) {
                saveConfigurationButton.setEnabled(true);
                saveConfigurationMenuItem.setEnabled(true);
            }
        }
    }

    public void handleDeleteTestSuite(DefaultMutableTreeNode actionNode) {
        TestSuite testSuite = (TestSuite) actionNode.getUserObject();
        if (UIUtils.promptForDelete(this, "Delete Test Suite",
            "Delete test suite'" + testSuite.getName() + "'?")) {
            testRepositoryTree.removeNode(actionNode);
            if (Utils.removeRepositoryNode(getConfiguration(), actionNode)) {
                saveConfigurationButton.setEnabled(true);
                saveConfigurationMenuItem.setEnabled(true);
            }
        }
    }

    private void handleEmailSetup(ActionEvent evt) {
        EmailDlg dlg = new EmailDlg(this, getConfiguration().getEmailSetup());

        if (dlg.isSaved()) {
            saveConfigurationButton.setEnabled(true);
            saveConfigurationMenuItem.setEnabled(true);
        }
    }

    public void handleRemoveDatabaseConnection(DefaultMutableTreeNode actionNode) {
        DatabaseConnection dbconn = (DatabaseConnection) actionNode.getUserObject();
        if (UIUtils.promptForDelete(this, "Delete Database Connection",
            "Delete database connection '" + dbconn.getName() + "'?")) {
            databaseTree.removeNode(actionNode);
            if (Utils.removeRepositoryNode(getConfiguration(), actionNode)) {
                saveConfigurationButton.setEnabled(true);
                saveConfigurationMenuItem.setEnabled(true);
            }
        }
    }

    private void handleLoadConfiguation(ActionEvent evt) {
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("XML files", "xml");
        chooser.setFileFilter(filter);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            testRepositoryTree.loadConfiguration(chooser.getSelectedFile());
        }
    }

    private void handleScheduleTests(ActionEvent evt) {
        new ScheduleTestsDlg(this);
    }
    public void handleTestExecutionParameterNamesSetup() {
        TestExecutionParameterNamesDlg dlg = new TestExecutionParameterNamesDlg(this);

        if (dlg.isSaved()) {
            saveConfigurationButton.setEnabled(true);
            saveConfigurationMenuItem.setEnabled(true);
        }
    }

    public KualiTestConfigurationDocument.KualiTestConfiguration getConfiguration() {
        return testRepositoryTree.getConfiguration();
    }

    public JButton getCreateTestButton() {
        return createTestButton;
    }

    public JMenuItem getCreateTestMenuItem() {
        return createTestMenuItem;
    }

    public JButton getSaveConfigurationButton() {
        return saveConfigurationButton;
    }

    public JMenuItem getSaveConfigurationMenuItem() {
        return saveConfigurationMenuItem;
    }
    
    public static void main(final String args[]) {
        try {
            javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
        } 
        
        
        catch (Exception ex) {
            LOG.error(ex.toString(), ex);
        }

        if (!ApplicationInstanceManager.registerInstance(ApplicationInstanceManager.SINGLE_INSTANCE_NETWORK_SOCKET1)) {
            // instance already running.
            System.out.println("Another instance of this application is already running.  Exiting.");
            System.exit(0);
        } else {
            ApplicationInstanceManager.setApplicationInstanceListener(new ApplicationInstanceListener() {
                @Override
                public void newInstanceCreated() {
                }
            });

            java.awt.EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    String filename = null;
                    if (args.length > 0) {
                        filename = args[0];
                    }
                    try {
                        new TestCreator(filename).setVisible(true);
                    }
                    
                    catch (Exception ex) {
                        LOG.error(ex.toString(), ex);
                        System.exit(-1);
                    }
                }
            });

        }
    }

    public String getConfigFileName() {
        return configFileName;
    }

    public void setConfigFileName(String configFileName) {
        this.configFileName = configFileName;
    }

    public RepositoryTree getTestRepositoryTree() {
        return testRepositoryTree;
    }

    public PlatformTestsPanel getPlatformTestsPanel() {
        return platformTestsPanel;
    }

    @Override
    public void windowOpened(WindowEvent e) {
    }

    @Override
    public void windowClosing(WindowEvent e) {
        exitApplication.doClick();
    }

    @Override
    public void windowClosed(WindowEvent e) {
    }

    @Override
    public void windowIconified(WindowEvent e) {
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
    }

    @Override
    public void windowActivated(WindowEvent e) {
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
    }

    @Override
    public synchronized void addWindowStateListener(WindowStateListener l) {
        super.addWindowStateListener(l);
    }

    @Override
    public void lostOwnership(Clipboard clipboard, Transferable contents) {
    }

    private JPanel createToolBar() {
        JPanel retval = new JPanel(new BorderLayout(2, 2));
        
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.setMargin(new Insets(1, 5, 2, 0));
        ToolbarButton b;
        toolbar.add(b = new ToolbarButton(Constants.PLATFORM_TOOLBAR_ICON, "add platform"));
        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleAddPlatform(e);
            }
            
        });
        toolbar.add(b = new ToolbarButton(Constants.DATABASE_TOOLBAR_ICON, "add database connection"));
        b.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                handleAddDatabaseConnection(e);
            }
            
        });
        toolbar.add(b = new ToolbarButton(Constants.JMX_CONNECTION_TOOLBAR_ICON, "add JMX connection"));
        b.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                handleAddJmxConnection(e);
            }
            
        });

        toolbar.add(b = new ToolbarButton(Constants.WEB_SERVICE_TOOLBAR_ICON, "add web service"));
        b.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                handleAddWebService(e);
            }
            
        });
        
        toolbar.add(b = new ToolbarButton(Constants.SCHEDULE_TEST_TOOLBAR_ICON, "schedule test"));
        b.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                handleScheduleTests(e);
            }
            
        });
            
        toolbar.addSeparator();

        toolbar.add(saveConfigurationButton = new ToolbarButton(Constants.SAVE_CONFIGURATION_ICON, "save repository configuration") {
            @Override
            public void setEnabled(boolean enabled) {
                if (enabled) {
                    getConfiguration().setModified(true);
                }

                super.setEnabled(enabled);
            }
        });
        saveConfigurationButton.setEnabled(false);
        saveConfigurationButton.setMargin(new Insets(1, 1, 1, 1));
        saveConfigurationButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                testRepositoryTree.saveConfiguration();
                saveConfigurationButton.setEnabled(false);
                saveConfigurationMenuItem.setEnabled(false);
            }
        });

        toolbar.add(createTestButton = new ToolbarButton(Constants.TEST_ICON, "create new test"));
        createTestButton.setEnabled(false);
        createTestButton.setMargin(new Insets(1, 1, 1, 1));

        createTestButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                handleCreateTest(null);
            }
        });
        
        toolbar.addSeparator();
        
        toolbar.add(exitApplication = new ToolbarButton(Constants.EXIT_APPLICATION_TOOLBAR_ICON, "exit application"));
        exitApplication.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (JOptionPane.showConfirmDialog(TestCreator.this, "Exit Test Application?", "Exit", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    new SplashDisplay(TestCreator.this, "Application Closing", "Shutting down test application...") {
                        @Override
                        protected void runProcess() {
                            handleExit();
                        }
                    };
                }
            }
        });
        

        retval.add(new JSeparator(), BorderLayout.NORTH);
        retval.add(toolbar, BorderLayout.CENTER);
        
        return retval;
    }
}
