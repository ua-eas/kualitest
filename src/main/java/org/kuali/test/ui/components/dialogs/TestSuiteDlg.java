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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.apache.commons.lang3.StringUtils;
import org.kuali.test.Platform;
import org.kuali.test.SuiteTest;
import org.kuali.test.TestHeader;
import org.kuali.test.TestSuite;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.ui.base.BaseSetupDlg;
import org.kuali.test.ui.base.BaseTable;
import org.kuali.test.ui.base.TableConfiguration;
import org.kuali.test.ui.components.buttons.TableCellIconButton;
import org.kuali.test.ui.components.editmasks.FileNameField;
import org.kuali.test.ui.components.editmasks.IntegerTextField;
import org.kuali.test.ui.components.panels.TablePanel;
import org.kuali.test.ui.utils.UIUtils;
import org.kuali.test.utils.Constants;
import org.kuali.test.utils.Utils;

/**
 *
 * @author rbtucker
 */
public class TestSuiteDlg extends BaseSetupDlg {
    private Platform platform;
    private TestSuite testSuite;
    private IntegerTextField maxRunTime;
    private JTextField name;
    private JTextField platformName;
    private JTextField emailAddresses;
    private JCheckBox collectPerformanceData;
    private BaseTable testTable;
    
    /**
     * Creates new form TestSuiteDlg
     * @param mainFrame
     * @param platform
     * @param testSuite
     */
    public TestSuiteDlg(TestCreator mainFrame, Platform platform, TestSuite testSuite) {
        super(mainFrame);
        this.platform = platform;
        this.testSuite = testSuite;
        if (testSuite != null) {
            setTitle("Edit test suite");
            setEditmode(true);
        } else {
            setTitle("Add new test suite");
            this.testSuite = TestSuite.Factory.newInstance();
            this.testSuite.setName("new test suite");
            this.testSuite.setPlatformName(platform.getName());
            this.testSuite.setMaxRunTime(0);
        }
        
        initComponents();
    }

    private void initComponents() {
        String[] labels = new String[] {
            "Name", 
            "Email Addresses",
            "Platform",
            "Max Run Time(min)",
            ""
            
        };
        
        name = new FileNameField(testSuite.getName());
        name.setEditable(!isEditmode());
        
        platformName = new JTextField(testSuite.getPlatformName(), 20);
        platformName.setEditable(false);

        emailAddresses = new JTextField(testSuite.getEmailAddresses(), 30);
        
        maxRunTime = new IntegerTextField();
        maxRunTime.setInt(testSuite.getMaxRunTime());
        
        collectPerformanceData = new JCheckBox("Collect performance data during test runs");
        collectPerformanceData.setSelected(testSuite.getCollectPerformanceData());
        JComponent[] components = new JComponent[] {
            name,
            emailAddresses,
            platformName,
            maxRunTime,
            collectPerformanceData
        };

        JPanel p = new JPanel(new BorderLayout(3, 3));
        p.add(UIUtils.buildEntryPanel(labels, components), BorderLayout.NORTH);

        p.add(new TablePanel(testTable = buildTestTable()), BorderLayout.CENTER);
        
        getContentPane().add(p, BorderLayout.CENTER);

        addStandardButtons();
        setDefaultBehavior();
    }
    
    private BaseTable buildTestTable() {
        TableConfiguration config = new TableConfiguration();
        config.setTableName("test-suite-tests");
        config.setDisplayName("Tests");
        
        int[] alignment = new int[7];
        for (int i = 0; i < alignment.length; ++i) {
            alignment[i] = JLabel.LEFT;
        }
            
        config.setColumnAlignment(alignment);
        
        config.setHeaders(new String[] {
            "Test Name",
            "Type",
            "Created By",
            "Create Date",
            "Max Run Time(min)",
            "Description",
            "Details"
        });
        
        config.setPropertyNames(new String[] {
            "testName",
            "testType",
            "createdBy",
            "dateCreated",
            "maxRunTime",
            "description",
            Constants.IGNORE_TABLE_DATA_INDICATOR
        });
            
        config.setColumnTypes(new Class[] {
            String.class,
            String.class,
            String.class,
            Calendar.class,
            Integer.class,
            String.class,
            String.class
        });
        
        config.setColumnWidths(new int[] {
            75,
            20,
            50,
            75,
            20,
            100,
            15
        });

        if (testSuite.getSuiteTests() != null) {
            List <TestHeader> data = new ArrayList<TestHeader>();
            for (SuiteTest test : testSuite.getSuiteTests().getSuiteTestArray()) {
                data.add(test.getTestHeader());
            }
            
            config.setData(data);
        }
        
        BaseTable retval = new BaseTable(config) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return (column == 6);
            }
        };
        
        TableCellIconButton b = new TableCellIconButton(Constants.DETAILS_ICON);
        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                TableCellIconButton b = (TableCellIconButton)e.getSource();
                List <TestHeader> l = testTable.getTableData();
                if ((b.getCurrentRow() > -1) && (l.size() > b.getCurrentRow())) {
                    TestInformationDlg dlg = new TestInformationDlg(getMainframe(), TestSuiteDlg.this, l.get(b.getCurrentRow()));
                    if (dlg.isSaved()) {
                        Utils.saveKualiTest(testTable, getConfiguration().getRepositoryLocation(), platform, 
                            dlg.getTestHeader(), dlg.getOperations(), dlg.getTestDescription());
                    }
                }
            }
        });
        
        retval.getColumnModel().getColumn(6).setCellRenderer(b);
        retval.getColumnModel().getColumn(6).setCellEditor(b);

        return retval;
    }
    
    /**
     *
     * @return
     */
    @Override
    protected boolean save() {
        boolean retval = false;
        boolean oktosave = true;
        if (StringUtils.isNotBlank(name.getText()) 
            && StringUtils.isNotBlank(platformName.getText())) {
            
            if (!isEditmode()) {
                if (testSuiteNameExists()) {
                    oktosave = false;
                    displayExistingNameAlert("Test Suite", name.getText());
                }
            }
        } else {
            displayRequiredFieldsMissingAlert("Test Suite", "name");
            oktosave = false;
        }
        
        if (oktosave) {
            if (!isEditmode()) {
                if (platform.getTestSuites() == null) {
                    platform.addNewTestSuites();
                }
                
                testSuite = platform.getTestSuites().addNewTestSuite();
                testSuite.setName(name.getText());
                testSuite.setCollectPerformanceData(collectPerformanceData.isSelected());
                testSuite.setPlatformName(platform.getName());
                testSuite.addNewSuiteTests();
            }
            
            String s = emailAddresses.getText();

            if (StringUtils.isNotBlank(s)) {
                testSuite.setEmailAddresses(s);
            }
                
            testSuite.setMaxRunTime(maxRunTime.getInt());
            
            getConfiguration().setModified(true);
            setSaved(true);
            dispose();
            retval = true;
        }
        
        return retval;
    }
    
    private boolean testSuiteNameExists() {
        boolean retval = false;
        String newname = name.getText();
        
        if (platform.getTestSuites() != null) {
            for (TestSuite t: platform.getTestSuites().getTestSuiteArray()) {
                if (t.getName().equalsIgnoreCase(newname)) {
                    retval = false;
                    break;
                }
            }
        }
        
        return retval;
    }

    /**
     *
     * @return
     */
    @Override
    public Object getNewRepositoryObject() {
        return testSuite;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(800, 400);
    }

    /**
     *
     * @return
     */
    @Override
    protected String getDialogName() {
        return "test-suite-setup";
    }
}
