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
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.test.FailureAction;
import org.kuali.test.Platform;
import org.kuali.test.TestHeader;
import org.kuali.test.TestType;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.ui.base.BaseSetupDlg;
import org.kuali.test.ui.components.editmasks.IntegerTextField;
import org.kuali.test.ui.utils.UIUtils;
import org.kuali.test.utils.Constants;
import org.kuali.test.utils.Utils;

/**
 *
 * @author rbtucker
 */
public class CreateTestDlg extends BaseSetupDlg {
    private static final Logger LOG = Logger.getLogger(CreateTestDlg.class);
    private final Platform platform;
    private JTextField testName;
    private JComboBox testType;
    private JTextField description;
    private IntegerTextField maxRunTime;
    private JComboBox runtimeFailure;
    private TestHeader testHeader;
    
    /**
     * Creates new form InitNewTestDlg
     * @param mainFrame
     * @param platform
     */
    public CreateTestDlg(TestCreator mainFrame, Platform platform) {
        super(mainFrame);
        this.platform = platform;
        if (platform != null) {
            setTitle("Initialize New Test");
        }
        
        initComponents();
    }

    private void initComponents() {
        String[] labels = new String[] {
            "Platform",
            "Test Name", 
            "Test Type",
            "Description",
            "Max Run Time (sec)",
            "On Max Time Failure"
        };
        
        JLabel platformName = new JLabel(platform.getName());
        testName = new JTextField("new test", 20);
        testType = new JComboBox(Utils.getValidTestTypesForPlatform(platform));
        testType.setSelectedItem(TestType.WEB.toString());
        description = new JTextField("new test description", 30);
        runtimeFailure = new JComboBox(Utils.getXmlEnumerations(FailureAction.class, true));
        maxRunTime= new IntegerTextField();
        
        JComponent[] components = new JComponent[] {
            platformName,
            testName,
            testType,
            description,
            maxRunTime,
            runtimeFailure
        };

        getContentPane().add(buildEntryPanel(labels, components), BorderLayout.CENTER);

        addStandardButtons();
        setDefaultBehavior();
    }
    
    @Override
    protected boolean save() {
        boolean retval = false;
        boolean oktosave = true;
        if (StringUtils.isNotBlank(testName.getText()) 
            && StringUtils.isNotBlank((String)testType.getSelectedItem())) {
            if (testNameExists()) {
                oktosave = false;
                displayExistingNameAlert("Test", testName.getText());
            }
        } else {
            displayRequiredFieldsMissingAlert("Test", "test name, test type");
            oktosave = false;
        }
        
        if (oktosave) {
            try {
                testHeader = TestHeader.Factory.newInstance();
                testHeader.setTestName(testName.getText());
                testHeader.setTestType(TestType.Enum.forString((String)testType.getSelectedItem()));
                testHeader.setDescription(description.getText());
                testHeader.setDateCreated(Calendar.getInstance());
                testHeader.setPlatformName(platform.getName());
                testHeader.setTestSuiteName("no-test-suite");
                testHeader.setCreatedBy("default-user");
                if (StringUtils.isBlank(maxRunTime.getText())) {
                    testHeader.setMaxRunTime(0);
                }
                
                if (runtimeFailure.getSelectedIndex() > 0) {
                    testHeader.setOnRuntimeFailure(FailureAction.Enum.forString((String)runtimeFailure.getSelectedItem()));
                } else {
                    testHeader.setOnRuntimeFailure(FailureAction.IGNORE);
                }
                
                testHeader.setTestFileName(File.createTempFile("temp-test", "xml").getPath());
                setSaved(true);
                retval = true;
                dispose();
            } catch (IOException ex) {
                UIUtils.showError(this, "New Test Initialization Error", "Error occured while initializing a new test - " + ex.toString());
                LOG.error(ex.toString());
            }
        }
        
        return retval;
    }

    private boolean testNameExists() {
        boolean retval = false;
        String newTestName = testName.getText();
        
        for (TestHeader th : platform.getPlatformTests().getTestHeaderArray()) {
            if (StringUtils.equalsIgnoreCase(th.getTestName(), newTestName)) {
                retval = true;
                break;
            }
        }
        
        return retval;
    }
    
    @Override
    public Object getNewRepositoryObject() {
        return platform;
    }
    
    public TestHeader getTestHeader() {
        return testHeader;
    }
    
    @Override
    protected String getSaveText() {
        return Constants.CONTINUE_ACTION;
    }
    
    @Override
    protected String getDialogName() {
        return "new-test-setup";
    }

}
