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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import org.kuali.test.KualiTestDocument;
import org.kuali.test.TestHeader;
import org.kuali.test.TestOperation;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.ui.base.BaseSetupDlg;
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
    
    /**
     * Creates new form TestInformationDlg
     * @param mainFrame
     * @param testHeader
     */
    public TestInformationDlg(TestCreator mainFrame, TestHeader testHeader) {
        super(mainFrame);
        this.testHeader = testHeader;
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
        this.testHeader = testHeader;
        initComponents();
    }

    private void initComponents() {
        String[] labels = new String[] {
            "Platform",
            "Test Name", 
            "Test Type",
            "Description",
            "Test File Name",
            "Max Run Time (sec)",
            "On Max Time Failure"
        };

        File f = new File(Utils.getTestFilePath(getConfiguration(), testHeader));
        StringBuilder nm = new StringBuilder(64);
        nm.append("[repository]/");
        nm.append(testHeader.getPlatformName());
        nm.append(Constants.FORWARD_SLASH);
        nm.append(f.getName());
        
        String maxTime = "";
        if (testHeader.getMaxRunTime() > 0) {
            maxTime = ("" + testHeader.getMaxRunTime());
        }
        
        String failureAction = "";

        if (testHeader.getOnRuntimeFailure() != null) {
            failureAction = testHeader.getOnRuntimeFailure().toString();
        }
        
        JComponent[] components = new JComponent[] {
            new JLabel(Utils.getLabelDataDisplay(testHeader.getPlatformName())),
            new JLabel(Utils.getLabelDataDisplay(testHeader.getTestName())),
            new JLabel(Utils.getLabelDataDisplay(testHeader.getTestType().toString())),
            new JLabel(Utils.getLabelDataDisplay(testHeader.getDescription(), 450)),
            new JLabel(Utils.getLabelDataDisplay(nm.toString())),
            new JLabel(Utils.getLabelDataDisplay(maxTime)),
            new JLabel(Utils.getLabelDataDisplay(failureAction))
        };

        getContentPane().add(UIUtils.buildEntryPanel(labels, components), BorderLayout.NORTH);

        getContentPane().add(new TestOperationsPanel(getMainframe(), this, getTestOperations()), BorderLayout.CENTER);
        
        addStandardButtons();
        
        getSaveButton().setVisible(false);
        
        setDefaultBehavior();
        
    }

    @Override
    public boolean isResizable() {
        return true;
    }
    
    
    /**
     *
     * @return
     */
    @Override
    protected String getCancelText() {
        return Constants.CLOSE_ACTION;
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
        return false;
    }
}
