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
import javax.swing.JComponent;
import javax.swing.JLabel;
import org.apache.log4j.Logger;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.ui.base.BaseSetupDlg;
import org.kuali.test.ui.components.editmasks.IntegerTextField;
import org.kuali.test.ui.utils.UIUtils;
import org.kuali.test.utils.Constants;

/**
 *
 * @author rbtucker
 */
public class LoadTestDlg extends BaseSetupDlg {
    private static final Logger LOG = Logger.getLogger(LoadTestDlg.class);
    private IntegerTextField testRuns;
    private IntegerTextField rampUpTime;
    /**
     * 
     * @param mainFrame
     * @param testName
     * @param typeTestSuite 
     */
    public LoadTestDlg(TestCreator mainFrame, String testName, boolean typeTestSuite) {
        super(mainFrame);
        setTitle("Load Test Setup");
        
        if (typeTestSuite) {
            initComponents("Test Suite \"" + testName + "\"");
        } else {
            initComponents("Test \"" + testName + "\"");
        }
    }

    private void initComponents(String label) {

        String[] labels = {
            "Test Runs (multi-threaded)",
            "Ramp Up Time (sec)"
        };
        
        testRuns = new IntegerTextField();
        testRuns.setInt(1);

        rampUpTime = new IntegerTextField();
        rampUpTime.setInt(0);

        
        getContentPane().add(new JLabel("<html><div style='text-align: left; font-weight: normal;'>Clicking \"Run\" will run the test in the background by spawning off multiple instances of " + label + " as defined by the 'Test Runs' input</div></html>"), BorderLayout.NORTH);
        getContentPane().add(UIUtils.buildEntryPanel(labels, new JComponent[] {testRuns, rampUpTime}), BorderLayout.CENTER);

        addStandardButtons();
        setDefaultBehavior();
    }
    
    /**
     *
     * @return
     */
    @Override
    protected boolean save() {
        setSaved(true);
        dispose();
        return true;
    }

    @Override
    protected String getSaveText() {
        return Constants.RUN_ACTION;
    }
    
    /**
     *
     * @return
     */
    @Override
    protected String getDialogName() {
        return "load-test-setup";
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(400, 200);
    }
    
    public int getTestRuns() {
        return testRuns.getInt();
    }
    
    public int getRampUpTime() {
        return rampUpTime.getInt();
    }
}
