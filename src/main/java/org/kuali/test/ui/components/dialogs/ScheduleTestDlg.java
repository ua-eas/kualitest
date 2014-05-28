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

import com.toedter.calendar.JDateChooser;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.Date;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.kuali.test.Platform;
import org.kuali.test.ScheduledTest;
import org.kuali.test.ScheduledTestType;
import org.kuali.test.TestHeader;
import org.kuali.test.TestSuite;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.ui.base.BaseSetupDlg;
import org.kuali.test.ui.components.editmasks.IntegerTextField;
import org.kuali.test.utils.Constants;
import org.kuali.test.utils.Utils;

/**
 *
 * @author rbtucker
 */
public class ScheduleTestDlg extends BaseSetupDlg implements ListSelectionListener {
    private JComboBox platforms;
    private JDateChooser startDateTime;
    private IntegerTextField testRuns;
    private JList testSuites;
    private JList platformTests;
    private ScheduledTest scheduledTest;
    
    public ScheduleTestDlg(TestCreator mainFrame, JDialog parent) {
        super(mainFrame, parent);
        initComponents();
    }

    private void initComponents() {

        String[] labels = {
            "Platform",
            "Start Date/Time",
            "Test Runs"
        };
        
        platforms = new JComboBox(getPlatformArray());

        startDateTime = new JDateChooser();
        startDateTime.setDateFormatString(Constants.SELECT_DATETIME_FORMAT_STRING);
        startDateTime.setMinSelectableDate(new Date());
        
        testRuns = new IntegerTextField();
        testRuns.setInt(1);
        JComponent[] components = {platforms, startDateTime, testRuns};
        
        platforms.addActionListener(this);
        platforms.setActionCommand(Constants.PLATFORM_SELECTED_ACTION);
        getContentPane().add(buildEntryPanel(labels, components), BorderLayout.NORTH);
        
        testSuites = new JList(new DefaultListModel());
        testSuites.setSelectionMode(ListSelectionModel.SINGLE_SELECTION );

        platformTests = new JList(new DefaultListModel());
        platformTests.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JPanel p = new JPanel(new GridLayout(1, 2));
        JPanel p2 = new JPanel(new BorderLayout());
        p2.add(new JLabel("Test Suites"), BorderLayout.NORTH);
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(testSuites);
        p2.add(scrollPane, BorderLayout.CENTER);
        p.add(p2);
        
        p2 = new JPanel(new BorderLayout());
        p2.add(new JLabel("Platform Tests"), BorderLayout.NORTH);
        scrollPane = new JScrollPane();
        scrollPane.setViewportView(platformTests);
        p2.add(scrollPane, BorderLayout.CENTER);
        p.add(p2);

        getContentPane().add(p, BorderLayout.CENTER);
    
        populateLists();
        
        addStandardButtons();
        setDefaultBehavior();
    }
    
    @Override
    protected boolean save() {
        boolean retval = false;
        boolean oktosave = true;
        if ((platforms.getSelectedIndex() > -1)
            && (startDateTime.getCalendar() != null)
            && ((testSuites.getSelectedIndex() > -1) || (platformTests.getSelectedIndex() > -1))) {
            oktosave = true;
            scheduledTest = ScheduledTest.Factory.newInstance();
            
            if (testSuites.getSelectedIndex() > -1) {
                scheduledTest.setName((String)testSuites.getSelectedValue());
                scheduledTest.setType(ScheduledTestType.TEST_SUITE);
            } else {
                scheduledTest.setName((String)platformTests.getSelectedValue());
                scheduledTest.setType(ScheduledTestType.PLATFORM_TEST);
            }
            
            scheduledTest.setPlaformName((String)platforms.getSelectedItem());
            scheduledTest.setStartTime(startDateTime.getCalendar());
            scheduledTest.setTestRuns(testRuns.getInt());
        } else {
            StringBuilder msg = new StringBuilder(256);
            
            if (startDateTime.getCalendar() == null) {
                msg.append("Start Date/Time is required<br />");
            }
            
            if ((testSuites.getSelectedIndex() < 0) && (platformTests.getSelectedIndex() < 0)) {
                msg.append("Test Site or Platform Test selection is required");
            }
            
            inputDataErrorsAlert("Schedule Test", msg.toString());
            
            oktosave = false;
        }
        
        if (oktosave) {
            setSaved(true);
            dispose();
            retval = true;
        }
        
        
        return retval;
    }
    
    private String[] getPlatformArray() {
        Platform[] p = getMainframe().getConfiguration().getPlatforms().getPlatformArray();
        String[] retval = new String[p.length];
        
        for (int i = 0; i < retval.length; ++i) {
            retval[i] = p[i].getName();
        }
        
        return retval;
    }
    
    @Override
    protected String getDialogName() {
        return "schedule-test-dlg";
    }

    private void populateLists() {
        if (platforms.getSelectedIndex() > -1) {
            populateTestSuites();
            populatePlatformTests();
        }
    }
    
    @Override
    protected void handleOtherActions(String actionCommand) {
        if (Constants.PLATFORM_SELECTED_ACTION.equals(actionCommand)) {
            populateLists();
        }
    }

    private void populateTestSuites() {
        testSuites.removeAll();
        String platformName = (String)platforms.getSelectedItem();
        
        Platform p = Utils.findPlatform(getMainframe().getConfiguration(), platformName);
        
        if (p != null) {
            TestSuite[] t = p.getTestSuites().getTestSuiteArray();
            
            if (t != null) {
                DefaultListModel lm = (DefaultListModel)testSuites.getModel();
                for (int i = 0; i < t.length; ++i) {
                    lm.addElement(t[i].getName());
                }
            }
        }
    }
    
    private void populatePlatformTests() {
        platformTests.removeAll();
        String platformName = (String)platforms.getSelectedItem();
        
        Platform p = Utils.findPlatform(getMainframe().getConfiguration(), platformName);
        
        if (p != null) {
            TestHeader[] t = p.getPlatformTests().getTestHeaderArray();
            
            if (t != null) {
                DefaultListModel lm = (DefaultListModel)platformTests.getModel();
                for (int i = 0; i < t.length; ++i) {
                    lm.addElement(t[i].getTestName());
                }
            }
        }
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (e.getSource() == testSuites) {
            if (testSuites.getSelectedIndex() > -1) {
                platformTests.clearSelection();
            }
        } else if (e.getSource() == platformTests) {
            if (platformTests.getSelectedIndex() > -1) {
                testSuites.clearSelection();
            }
        }
    }

    @Override
    public Object getNewRepositoryObject() {
        return scheduledTest;
    }
}
