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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import javax.swing.JComboBox;
import javax.swing.JComponent;
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
    private JComboBox platforms;
    private JTextField testName;
    private JComboBox testType;
    private JTextField description;
    private IntegerTextField maxRunTime;
    private JComboBox runtimeFailure;
    private TestHeader testHeader;
    
    /**
     * 
     * @param mainFrame 
     */
    public CreateTestDlg(TestCreator mainFrame) {
        this(mainFrame, null);
    }
    
    /**
     * 
     * @param mainFrame
     * @param platform 
     */
    public CreateTestDlg(TestCreator mainFrame, Platform platform) {
        super(mainFrame);
        setTitle("Initialize New Test");
        initComponents(platform);
    }

    private void initComponents(Platform platform) {
        String[] labels = new String[] {
            "Platform",
            "Test Name", 
            "Test Type",
            "Description",
            "Max Run Time (sec)",
            "On Max Time Failure"
        };
        
        platforms = new JComboBox(Utils.loadPlatformNames(getConfiguration()));
        
        if (platform != null) {
            platforms.setSelectedItem(platform.getName());
            platforms.setEnabled(false);
        }
        
        testName = new JTextField("new test", 20);
        
        Platform currentPlatform = Utils.findPlatform(getConfiguration(), (String)platforms.getSelectedItem());
        
        String[] testTypes = Utils.getValidTestTypesForPlatform(currentPlatform);
        testType = new JComboBox(testTypes);
        
        if (testTypes.length > 0) {
            testType.setSelectedItem(TestType.WEB.toString());
        }
        
        description = new JTextField("new test description", 30);
        runtimeFailure = new JComboBox(Utils.getXmlEnumerations(FailureAction.class, true));
        maxRunTime= new IntegerTextField();
        
        JComponent[] components = new JComponent[] {
            platforms,
            testName,
            testType,
            description,
            maxRunTime,
            runtimeFailure
        };

        getContentPane().add(UIUtils.buildEntryPanel(labels, components), BorderLayout.CENTER);

        platforms.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Platform p = Utils.findPlatform(getConfiguration(), (String)platforms.getSelectedItem());
                testType.removeAllItems();
                
                if (p != null) {
                    String[] testTypes = Utils.getValidTestTypesForPlatform(p);
                    for (String type : testTypes) {
                        testType.addItem(type);
                    }
                }
            }
        });

        addStandardButtons();
        setDefaultBehavior();
    }
    
    @Override
    protected boolean save() {
        boolean retval = false;
        boolean oktosave = true;
        if (StringUtils.isNotBlank(testName.getText()) 
            && StringUtils.isNotBlank((String)testType.getSelectedItem())
            && (platforms.getSelectedIndex() > -1)) {
            if (testNameExists()) {
                oktosave = false;
                displayExistingNameAlert("Test", testName.getText());
            }
        } else {
            displayRequiredFieldsMissingAlert("Test", "platform, test name, test type");
            oktosave = false;
        }
        
        if (oktosave) {
            try {
                testHeader = TestHeader.Factory.newInstance();
                testHeader.setTestName(testName.getText());
                testHeader.setTestType(TestType.Enum.forString((String)testType.getSelectedItem()));
                testHeader.setDescription(description.getText());
                testHeader.setDateCreated(Calendar.getInstance());
                testHeader.setPlatformName((String)platforms.getSelectedItem());
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
        
        Platform p = Utils.findPlatform(getConfiguration(), (String)platforms.getSelectedItem());
        
        if (p != null) {
            for (TestHeader th : p.getPlatformTests().getTestHeaderArray()) {
                if (StringUtils.equalsIgnoreCase(th.getTestName(), newTestName)) {
                    retval = true;
                    break;
                }
            }
        }
        
        return retval;
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
