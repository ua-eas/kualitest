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
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import org.apache.log4j.Logger;
import org.kuali.test.KualiTestRunnerDocument;
import org.kuali.test.KualiTestRunnerDocument.KualiTestRunner;
import org.kuali.test.ScheduledTest;
import org.kuali.test.comparators.ScheduledTestComparator;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.ui.base.BaseSetupDlg;
import org.kuali.test.ui.base.BaseTable;
import org.kuali.test.ui.base.BaseTableModel;
import org.kuali.test.ui.base.TableConfiguration;
import org.kuali.test.ui.components.editmasks.IntegerTextField;
import org.kuali.test.ui.components.panels.TablePanel;
import org.kuali.test.ui.components.renderers.CalendarTableCellRenderer;
import org.kuali.test.ui.utils.UIUtils;
import org.kuali.test.utils.Constants;
import org.kuali.test.utils.Utils;

/**
 *
 * @author rbtucker
 */
public class ScheduleTestsDlg extends BaseSetupDlg {
    private static final Logger LOG = Logger.getLogger(ScheduleTestsDlg.class);
    private IntegerTextField configurationUpdate;
    private IntegerTextField scheduledTestInquiry;
    private KualiTestRunner testRunnerConfiguration;
    private BaseTable scheduledTestsTable;
    
    /**
     *
     * @param mainFrame
     */
    public ScheduleTestsDlg(TestCreator mainFrame) {
        super(mainFrame);
        setTitle("Schedule Tests");
        initComponents();
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {
        String[] labels = new String[] {
            "Configuration Update Interval", 
            "Test Inquiry Interval"
        };

        configurationUpdate = new IntegerTextField();
        scheduledTestInquiry = new IntegerTextField();
        

        File trConfigFile =  Utils.getTestRunnerConfigurationFile(getMainframe().getConfiguration());
                    
        if (trConfigFile.exists() && trConfigFile.isFile()) {
            try {
                testRunnerConfiguration = KualiTestRunnerDocument.Factory.parse(trConfigFile).getKualiTestRunner();
                configurationUpdate.setInt(testRunnerConfiguration.getConfigurationUpdateInterval());
                scheduledTestInquiry.setInt(testRunnerConfiguration.getScheduledTestInquiryInterval());
            }
            
            catch (Exception ex) {
                LOG.error(ex.toString(), ex);
            }
        }
        
        JComponent[] components = new JComponent[] {
            configurationUpdate,
            scheduledTestInquiry
        };

        JPanel p = new JPanel(new BorderLayout(3, 3));
        p.add(UIUtils.buildEntryPanel(labels, components), BorderLayout.NORTH);

        TablePanel tpanel = new TablePanel(scheduledTestsTable = buildScheduledTestsTable());
        tpanel.addAddButton(this, Constants.SCHEDULE_TEST_ACTION, "schedule a test");
        tpanel.addDeleteButton(this, Constants.REMOVE_TEST_ACTION, "remove scheduled test");
        p.add(tpanel, BorderLayout.CENTER);
        
        getContentPane().add(p, BorderLayout.CENTER);

        addStandardButtons();
        getSaveButton().setEnabled(testRunnerConfiguration != null);
        setDefaultBehavior();
    }
    
    private BaseTable buildScheduledTestsTable() {
        BaseTable retval = null;
        TableConfiguration config = new TableConfiguration();
        config.setTableName("scheduled-tests");
        config.setDisplayName("Scheduled Tests");
        
        int[] alignment = new int[4];
        for (int i = 0; i < alignment.length; ++i) {
            alignment[i] = JLabel.LEFT;
        }
         
        config.setColumnAlignment(alignment);
        
        config.setHeaders(new String[] {
            "Type",
            "Name",
            "Scheduled Date/Time",
            "Test Runs (multi-threaded)",
            "Repeat Interval"
        });
        
        config.setPropertyNames(new String[] {
            "type",
            "name",
            "startTime",
            "testRuns",
            "repeatInterval"
        });
            
        config.setColumnTypes(new Class[] {
            String.class,
            String.class,
            Calendar.class,
            Integer.class,
            String.class
        });
        
        config.setColumnWidths(new int[] {
            20,
            100,
            30,
            20,
            20
        });

        if (testRunnerConfiguration != null) {
            if (testRunnerConfiguration.getScheduledTests() != null) {
                List <ScheduledTest> data = new ArrayList<ScheduledTest>();
                data.addAll(Arrays.asList(testRunnerConfiguration.getScheduledTests().getScheduledTestArray()));
                Collections.sort(data, new ScheduledTestComparator());
                config.setData(data);
            }
        }
        
        retval = new BaseTable(config);
        
        retval.getColumnModel().getColumn(2).setCellRenderer(new CalendarTableCellRenderer());
        retval.setSelectionMode(ListSelectionModel.SINGLE_SELECTION );

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
        
        int configUpdateInterval = configurationUpdate.getInt();
        int inquiryUpdateInterval = scheduledTestInquiry.getInt();
        
        if ((configUpdateInterval > 0) && (inquiryUpdateInterval > 0)) {
            oktosave = true;
        } else {
            displayRequiredFieldsMissingAlert("Test Scheduler", "Configuration Update Interval, Test Inquiry Interval");
            oktosave = false;
        }
        
        if (oktosave) {
            saveTestSchedule();
            setSaved(true);
            dispose();
            retval = true;

        }
        
        return retval;
    }
    
    private void saveTestSchedule() {
        List <ScheduledTest> scheduledTests = scheduledTestsTable.getTableData();
        
        Iterator <ScheduledTest> it = scheduledTests.iterator();
        
        while (it.hasNext()) {
            if (it.next().getStartTime().getTimeInMillis() < System.currentTimeMillis()) {
                it.remove();
            }
        }
        
        Collections.sort(scheduledTests, new ScheduledTestComparator());
        
        testRunnerConfiguration.getScheduledTests().setScheduledTestArray(scheduledTests.toArray(new ScheduledTest[scheduledTests.size()]));
        
        KualiTestRunnerDocument doc = KualiTestRunnerDocument.Factory.newInstance();
        doc.setKualiTestRunner(testRunnerConfiguration);
        try {
            doc.save(Utils.getTestRunnerConfigurationFile(getMainframe().getConfiguration()));
        }

        catch (IOException ex) {
            UIUtils.showError(getMainframe(), "Schedule Test Error", "An IOException occurred while attempting save scheduled tests - " + ex.toString());
        }
    }
    
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(750, 400);
    }

    /**
     *
     * @return
     */
    @Override
    protected String getDialogName() {
        return "schedule-tests";
    }
    
    /**
     *
     * @param actionCommand
     */
    @Override
    protected void handleOtherActions(String actionCommand) {
        if (Constants.REMOVE_TEST_ACTION.equals(actionCommand)) {
            int row = scheduledTestsTable.getSelectedRow();
            if (row > -1) {
                List <ScheduledTest> l = scheduledTestsTable.getTableData();
                
                if (UIUtils.promptForDelete(getMainframe(), 
                    "Remove Test", "Remove scheduled " 
                        + l.get(row).getType().toString() 
                        + " '" + l.get(row).getName() + "'?")) {
                    l.remove(row);
                    BaseTableModel tm = (BaseTableModel)scheduledTestsTable.getModel();
                    tm.fireTableRowsDeleted(row, row);
                }
            } else {
                JOptionPane.showMessageDialog(getMainframe(), "Please select a scheduled test/test suite");
            }
            
        } else if (Constants.SCHEDULE_TEST_ACTION.equals(actionCommand)) {
            scheduleTest();
        }
    }
    
    private void scheduleTest() {
        ScheduleTestDlg dlg = new ScheduleTestDlg(getMainframe(), this);

        if (dlg.isSaved()) {
            ScheduledTest test = (ScheduledTest)dlg.getNewRepositoryObject();
            
            if (test != null) {
                List <ScheduledTest> tests =  scheduledTestsTable.getTableData();
                tests.add(test);
                Collections.sort(tests, new ScheduledTestComparator());
                BaseTableModel tm = (BaseTableModel)scheduledTestsTable.getModel();
                tm.fireTableDataChanged();
            }
        }
    }
}
