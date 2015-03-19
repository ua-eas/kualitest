/**
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
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.apache.log4j.Logger;
import org.kuali.test.KualiTestDocument.KualiTest;
import org.kuali.test.Platform;
import org.kuali.test.TestHeader;
import org.kuali.test.TestOperation;
import org.kuali.test.TestOperationType;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.ui.base.BaseSetupDlg;
import org.kuali.test.utils.Constants;
import org.kuali.test.utils.Utils;

/**
 *
 * @author rbtucker
 */
public class ImportPlatformTestsDlg extends BaseSetupDlg {
    private static final Logger LOG = Logger.getLogger(ImportPlatformTestsDlg.class);
    private Platform targetPlatform;
    private Set <String> existingTargetPlatformTestNames;
    private JList availableTests;
    private JComboBox platformCombo;
    private JProgressBar progressBar;

    private int importedTestCount = 0;
    /**
     * 
     * @param mainFrame
     * @param targetPlatform 
     */
    public ImportPlatformTestsDlg(TestCreator mainFrame, Platform targetPlatform) {
        super(mainFrame);
        setTitle("Import Platform Tests");
        this.targetPlatform = targetPlatform;

        existingTargetPlatformTestNames = new HashSet<String>();
        for (TestHeader th : targetPlatform.getPlatformTests().getTestHeaderArray()) {
            existingTargetPlatformTestNames.add(th.getTestName());
        }
        
        initComponents();
    }
    
    private String[] getAvailablePlatforms() {
        List <String> retval = new ArrayList<String>();
        
        for (Platform p : getMainframe().getConfiguration().getPlatforms().getPlatformArray()) {
            if (!p.getName().equals(targetPlatform.getName())) {
                if (p.getApplication().equals(targetPlatform.getApplication())
                    && p.getVersion().equals(targetPlatform.getVersion())) {
                    retval.add(p.getName());
                }
            }
        }
        
        Collections.sort(retval);
        
        return retval.toArray(new String[retval.size()]);
    }

    private void initComponents() {
        JPanel p = new JPanel(new BorderLayout());
        JPanel p2 = new JPanel(new FlowLayout());
        
        p2.add(new JLabel("Available Platforms:", JLabel.RIGHT));
        
        String[] platforms = getAvailablePlatforms();
        p2.add(platformCombo = new JComboBox(platforms));
        
        p.add(progressBar = new JProgressBar(), BorderLayout.CENTER);
        progressBar.setIndeterminate(false);
        
        p.add(p2, BorderLayout.NORTH);
        
        platformCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadTestList(platformCombo.getSelectedItem().toString());
            }
        });

        getContentPane().add(p, BorderLayout.NORTH);
        getContentPane().add(new JScrollPane(availableTests = new JList()), BorderLayout.CENTER);
        availableTests.setSelectionMode(DefaultListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        
        availableTests.addListSelectionListener(new ListSelectionListener() { 
            @Override
            public void valueChanged(ListSelectionEvent e) {
                getSaveButton().setEnabled(!availableTests.getSelectedValuesList().isEmpty());
            }
        });
        
        if (platforms.length > 0) {
            loadTestList(platforms[0]);
        }
        
        addStandardButtons();
        
        getSaveButton().setEnabled(false);
        
        setDefaultBehavior();
    }
    
    private void loadTestList(String platformName) {
        Platform p = Utils.findPlatform(getMainframe().getConfiguration(), platformName);
        
        availableTests.removeAll();
        DefaultListModel lm = new DefaultListModel();
        availableTests.setModel(lm);
        
        List <String> l = new ArrayList<String>();
        for (TestHeader th : p.getPlatformTests().getTestHeaderArray()) {
            if (!existingTargetPlatformTestNames.contains(th.getTestName())) {
                l.add(th.getTestName());
            }
        }
        
        if (!l.isEmpty()) {
            Collections.sort(l);
            
            for (String s : l) {
                lm.addElement(s);
            }
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(400, 500);
    }

    @Override
    protected String getCancelText() {
        return Constants.CANCEL_ACTION;
    }

    /**
     *
     * @return
     */
    @Override
    protected String getDialogName() {
        return "import-platform-tests";
    }

    @Override
    protected boolean save() {
        new SwingWorker() {
            @Override
            protected Object doInBackground() throws Exception {
                return importTests();
            }

            @Override
            protected void done() {
                try {
                    Boolean b = (Boolean)get();
                    setSaved(b);
                    
                    if (b) {
                        dispose();
                    }
                } 
                catch (Exception ex) {
                   LOG.warn(ex.toString(), ex);
                } 
            }
            
        }.execute();
        
        return false;
    }

    private boolean importTests() {
        boolean retval = false;
        Platform sourcePlatform = Utils.findPlatform(getMainframe().getConfiguration(), platformCombo.getSelectedItem().toString());
        
        if (sourcePlatform != null) {
            progressBar.setMinimum(0);
            List <String> selectedTests = availableTests.getSelectedValuesList();
            
            progressBar.setMaximum(selectedTests.size());
            
            for (String testName : selectedTests) {
                KualiTest sourceTest = Utils.findKualiTest(getMainframe().getConfiguration(), sourcePlatform.getName(), testName);
                if (sourceTest != null) {
                    TestHeader testHeader = (TestHeader)sourceTest.getTestHeader().copy();
                    testHeader.setPlatformName(targetPlatform.getName());
                    
                    List <TestOperation> operations = new ArrayList<TestOperation>();

                    for (TestOperation op : sourceTest.getOperations().getOperationArray()) {
                        TestOperation newop = (TestOperation)op.copy();
                        importOperation(sourcePlatform, newop);
                        operations.add(newop);
                    }
                    
                    String testDescription = Utils.getTestDescription(Utils.buildTestFile(getMainframe().getConfiguration().getRepositoryLocation(), sourceTest.getTestHeader()));
                
                    if (Utils.saveKualiTest(getSaveButton(), getMainframe().getConfiguration().getRepositoryLocation(), targetPlatform, testHeader, operations, testDescription)) {
                        importedTestCount++;
                        progressBar.setValue(importedTestCount);
                    }
                }
            }
            
            getMainframe().handleSaveConfiguration();
            
            retval = true;
        }
        
        return retval;
    }
    
    private void importOperation(Platform sourcePlatform, TestOperation newop) {
        if (TestOperationType.HTTP_REQUEST.equals(newop.getOperationType())) {
            Utils.updateWebUrls(newop.getOperation().getHtmlRequestOperation(), sourcePlatform.getWebUrl(), targetPlatform.getWebUrl());
        }
    }

    public int getImportedTestCount() {
        return importedTestCount;
    }
}
