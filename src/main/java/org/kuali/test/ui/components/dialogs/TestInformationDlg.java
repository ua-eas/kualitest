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
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JLabel;
import org.kuali.test.Checkpoint;
import org.kuali.test.KualiTestDocument;
import org.kuali.test.TestHeader;
import org.kuali.test.TestOperation;
import org.kuali.test.TestOperationType;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.ui.base.BaseSetupDlg;
import org.kuali.test.ui.base.BaseTable;
import org.kuali.test.ui.base.TableConfiguration;
import org.kuali.test.ui.components.panels.TablePanel;
import org.kuali.test.ui.utils.UIUtils;
import org.kuali.test.utils.Constants;

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
        initComponents();
    }

    @SuppressWarnings("unchecked")
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
        
        JComponent[] components = new JComponent[] {
            new JLabel(testHeader.getPlatformName()),
            new JLabel(testHeader.getTestName()),
            new JLabel(testHeader.getTestType().toString()),
            new JLabel(testHeader.getDescription()),
            new JLabel(testHeader.getTestFileName()),
            new JLabel("" + testHeader.getMaxRunTime()),
            new JLabel(testHeader.getOnRuntimeFailure().toString())
        };

        getContentPane().add(buildEntryPanel(labels, components), BorderLayout.NORTH);

        getContentPane().add(new TablePanel(buildCheckpointTable()), BorderLayout.CENTER);
        
        addStandardButtons();
        
        getSaveButton().setVisible(false);
        
        setDefaultBehavior();
    }

    @Override
    protected String getCancelText() {
        return Constants.OK_ACTION;
    }
    
    private BaseTable buildCheckpointTable() {
        TableConfiguration config = new TableConfiguration();
        config.setTableName("test-checkpoint-table");
        config.setDisplayName("Checkpoints");
        
        int[] alignment = new int[6];
        for (int i = 0; i < alignment.length; ++i) {
            alignment[i] = JLabel.LEFT;
        }
            
        config.setColumnAlignment(alignment);
        
        config.setHeaders(new String[] {
            "Name",
            "Type"
            
        });
        
        config.setPropertyNames(new String[] {
            "name",
            "type"
        });
            
        config.setColumnTypes(new Class[] {
            String.class,
            String.class,
        });
        
        config.setColumnWidths(new int[] {
            30,
            20
        });

        config.setData(getTestCheckpoints());
        
        return new BaseTable(config);
    }
    
    private List <Checkpoint> getTestCheckpoints() {
        List <Checkpoint> retval = new ArrayList<Checkpoint>();
        
        File f = new File(testHeader.getTestFileName());
        
        if (f.exists() && f.isFile()) {
            try {
                KualiTestDocument doc = KualiTestDocument.Factory.parse(f);
                
                if (doc.getKualiTest().getOperations().sizeOfOperationArray() > 0) {
                    for (TestOperation op : doc.getKualiTest().getOperations().getOperationArray()) {
                        if (op.getOperationType().equals(TestOperationType.CHECKPOINT)) {
                            retval.add(op.getOperation().getCheckpointOperation());
                        }
                    }      
                }
            } 
            
            catch (Exception ex) {
                UIUtils.showError(this, "Error Loading Test File", "Error occured while loading test filem - " + ex.toString());
            }
        }
        return retval;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(600, 400);
    }

    @Override
    protected String getDialogName() {
        return "test-information-dlg";
    }

    @Override
    protected boolean save() {
        return false;
    }
}
