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

package org.kuali.test.ui.components.dialogs;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.apache.commons.lang3.StringUtils;
import org.kuali.test.FailureAction;
import org.kuali.test.KualiTestDocument;
import org.kuali.test.TestHeader;
import org.kuali.test.TestOperation;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.ui.base.BaseSetupDlg;
import org.kuali.test.ui.components.editmasks.IntegerTextField;
import org.kuali.test.ui.components.labels.DataDisplayLabel;
import org.kuali.test.ui.components.panels.TestOperationsPanel;
import org.kuali.test.ui.utils.UIUtils;
import org.kuali.test.utils.Constants;
import org.kuali.test.utils.Utils;

/**
 *
 * @author rbtucker
 */
public class TestInformationDlg extends BaseSetupDlg {
    private TestHeader testHeader;
    private JCheckBox useTestEntryTimes;
    private JCheckBox collectPerformanceData;
    private IntegerTextField maxExecutionTime;
    private JComboBox failureActions;
    private TestOperationsPanel testOperationsPanel;
    private JTextArea testDescription;
    
    /**
     * Creates new form TestInformationDlg
     * @param mainFrame
     * @param testHeader
     */
    public TestInformationDlg(TestCreator mainFrame, TestHeader testHeader) {
        super(mainFrame);
        this.testHeader = (TestHeader)testHeader.copy();
        setTitle("Test Information");
        initComponents();
    }

    /**
     *
     * @param mainFrame
     * @param parentDlg
     * @param testHeader
     */
    public TestInformationDlg(TestCreator mainFrame, JDialog parentDlg, TestHeader testHeader) {
        super(mainFrame, parentDlg);
        this.testHeader = (TestHeader)testHeader.copy();
        initComponents();
    }

    private void initComponents() {
        String[] labels = new String[] {
            "Platform",
            "Test Name", 
            "Test Type",
            "Test File Name",
            "Max Run Time (sec)",
            "On Max Time Failure",
            "",
            ""
        };

        File f = new File(Utils.getTestFilePath(getConfiguration(), testHeader));
        StringBuilder nm = new StringBuilder(64);
        nm.append("[repository]/");
        nm.append(testHeader.getPlatformName());
        nm.append(Constants.FORWARD_SLASH);
        nm.append(f.getName());
        
        failureActions = new JComboBox(Utils.getXmlEnumerations(FailureAction.class, true));
        failureActions.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                testUpdated();
            }
        });

        if (testHeader.getOnRuntimeFailure() != null) {
            failureActions.setSelectedItem(testHeader.getOnRuntimeFailure().toString());
        }
        
        useTestEntryTimes = new JCheckBox("Use test entry times during test execution");
        useTestEntryTimes.setSelected(testHeader.getUseTestEntryTimes());
        useTestEntryTimes.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                testUpdated();
            }
        });
        
        collectPerformanceData = new JCheckBox("Collect performance data during test runs");
        collectPerformanceData.setSelected(testHeader.getCollectPerformanceData());
        collectPerformanceData.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                testUpdated();
            }
        });

        maxExecutionTime = new IntegerTextField();
        maxExecutionTime.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                testUpdated();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                testUpdated();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                testUpdated();
            }
        });

        if (testHeader.getMaxRunTime() > 0) {
            maxExecutionTime.setInt(testHeader.getMaxRunTime());
        }
        
        JComponent[] components = new JComponent[] {
            new DataDisplayLabel(testHeader.getPlatformName()),
            new DataDisplayLabel(testHeader.getTestName()),
            new DataDisplayLabel(testHeader.getTestType().toString()),
            new DataDisplayLabel(nm.toString()),
            maxExecutionTime,
            failureActions,
            collectPerformanceData,
            useTestEntryTimes
        };

        JTabbedPane tabs = new JTabbedPane();
        
        JPanel p = new JPanel(new BorderLayout());
        p.add(UIUtils.buildEntryPanel(labels, components), BorderLayout.NORTH);
        p.add(testOperationsPanel = new TestOperationsPanel(getMainframe(), this, getTestOperations()), BorderLayout.CENTER);

        tabs.add(p, "Details");

        p = new JPanel(new BorderLayout());
        testDescription = new JTextArea(Utils.getTestDescription(f));
        testDescription.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                testUpdated();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                testUpdated();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                testUpdated();
            }
        });
        
        testDescription.setLineWrap(true);
        testDescription.setWrapStyleWord(true);
        tabs.add(new JScrollPane(testDescription), "Description");

        getContentPane().add(tabs, BorderLayout.CENTER);
        addStandardButtons();
        setDefaultBehavior();
    }

    @Override
    public boolean isResizable() {
        return true;
    }
    
    public void testUpdated() {
        if ((getSaveButton() != null) && isVisible()){
            getSaveButton().setEnabled(true);
        }
    }
    
    /**
     *
     * @return
     */
    @Override
    protected String getCancelText() {
        return Constants.CLOSE_ACTION;
    }
    
    /**
     *
     * @return
     */
    @Override
    protected String getSaveText() {
        return Constants.UPDATE_ACTION;
    }
    
    private List <TestOperation> getTestOperations() {
        List <TestOperation> retval = new ArrayList<TestOperation>();
        File f = new File(Utils.getTestFilePath(getConfiguration(), testHeader));
        
        if (f.exists() && f.isFile()) {
            try {
                KualiTestDocument doc = KualiTestDocument.Factory.parse(f);
                
                if (doc.getKualiTest().getOperations() != null) {
                    retval = Arrays.asList(doc.getKualiTest().getOperations().getOperationArray());
                }
            } 
            
            catch (Exception ex) {
                UIUtils.showError(this, "Error Loading Test File", 
                    "Error occured while loading test filem - " + ex.toString());
            }
        }
        
        return retval;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(600, 700);
    }

    /**
     *
     * @return
     */
    @Override
    protected String getDialogName() {
        return "test-information";
    }

    /**
     *
     * @return
     */
    @Override
    protected boolean save() {
        setSaved(true);
        testHeader.setMaxRunTime(maxExecutionTime.getInt());

        if ((failureActions.getSelectedItem() == null)
            || StringUtils.isBlank(failureActions.getSelectedItem().toString())) {
            testHeader.setOnRuntimeFailure(FailureAction.IGNORE);
        } else {
            testHeader.setOnRuntimeFailure(FailureAction.Enum.forString(failureActions.getSelectedItem().toString()));
        }
        
        testHeader.setUseTestEntryTimes(useTestEntryTimes.isSelected());
        testHeader.setCollectPerformanceData(collectPerformanceData.isSelected());
        dispose();
        return true;
    }

    @Override
    protected boolean getInitialSavedState() {
        return false;
    }
    
    public List<TestOperation> getOperations() {
        return testOperationsPanel.getOperations();
    }

    public TestHeader getTestHeader() {
        return testHeader;
    }
    
    public String getTestDescription() {
        return testDescription.getText();
    }
}
