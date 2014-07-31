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
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JDialog;
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
import org.kuali.test.ui.components.buttons.TableCellIconButton;
import org.kuali.test.ui.components.panels.TablePanel;
import org.kuali.test.ui.utils.UIUtils;
import org.kuali.test.utils.Constants;
import org.kuali.test.utils.Utils;

/**
 *
 * @author rbtucker
 */
public class TestInformationDlg extends BaseSetupDlg {
    private TestHeader testHeader;
    private BaseTable checkpointTable;
    
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

        File f = new File(testHeader.getTestFileName());
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
            new JLabel(Utils.getLabelDataDisplay(testHeader.getDescription())),
            new JLabel(Utils.getLabelDataDisplay(nm.toString())),
            new JLabel(Utils.getLabelDataDisplay(maxTime)),
            new JLabel(Utils.getLabelDataDisplay(failureAction))
        };

        getContentPane().add(UIUtils.buildEntryPanel(labels, components), BorderLayout.NORTH);

        getContentPane().add(new TablePanel(checkpointTable = buildCheckpointTable()), BorderLayout.CENTER);
        
        addStandardButtons();
        
        getSaveButton().setVisible(false);
        
        setDefaultBehavior();
    }

    /**
     *
     * @return
     */
    @Override
    protected String getCancelText() {
        return Constants.CLOSE_ACTION;
    }
    
    private BaseTable buildCheckpointTable() {
        TableConfiguration config = new TableConfiguration();
        config.setTableName("test-checkpoint-table");
        config.setDisplayName("Checkpoints");
        
        int[] alignment = new int[3];
        for (int i = 0; i < alignment.length; ++i) {
            alignment[i] = JLabel.LEFT;
        }
            
        config.setColumnAlignment(alignment);
        
        config.setHeaders(new String[] {
            "Name",
            "Type",
            "Details"
        });
        
        config.setPropertyNames(new String[] {
            "name",
            "type",
            Constants.IGNORE_TABLE_DATA_INDICATOR
        });
            
        config.setColumnTypes(new Class[] {
            String.class,
            String.class,
            String.class
        });
        
        config.setColumnWidths(new int[] {
            30,
            20,
            30
        });

        config.setData(getTestCheckpoints());
        
        BaseTable retval = new BaseTable(config) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return (column == 2);
            }
        };
        
        TableCellIconButton b = new TableCellIconButton(Constants.DETAILS_ICON);
        b.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                TableCellIconButton b = (TableCellIconButton)e.getSource();
                List <Checkpoint> l = checkpointTable.getTableData();
                if ((b.getCurrentRow() > -1) && (l.size() > b.getCurrentRow())) {
                    new CheckpointDetailsDlg(getMainframe(), TestInformationDlg.this, l.get(b.getCurrentRow()));
                }
            }
        });
        
        retval.getColumnModel().getColumn(2).setCellRenderer(b);
        retval.getColumnModel().getColumn(2).setCellEditor(b);
        
        return retval;
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
                UIUtils.showError(this, "Error Loading Test File", 
                    "Error occured while loading test filem - " + ex.toString());
            }
        }
        return retval;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(600, 400);
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
