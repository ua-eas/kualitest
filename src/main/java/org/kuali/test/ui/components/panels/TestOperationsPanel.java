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

package org.kuali.test.ui.components.panels;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.JDialog;
import javax.swing.JLabel;
import org.kuali.test.TestOperation;
import org.kuali.test.TestOperationType;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.ui.base.BasePanel;
import org.kuali.test.ui.base.BaseTable;
import org.kuali.test.ui.base.TableConfiguration;
import org.kuali.test.ui.components.buttons.TableCellIconButton;
import org.kuali.test.ui.components.dialogs.CheckpointDetailsDlg;
import org.kuali.test.ui.components.dialogs.HtmlRequestDetailsDlg;
import org.kuali.test.ui.components.dialogs.TestExecutionParameterDetailsDlg;
import org.kuali.test.utils.Constants;


public class TestOperationsPanel extends BasePanel {
    private BaseTable operationsTable;
    public TestOperationsPanel(TestCreator mainframe, JDialog parentDialog, List <TestOperation> operations) {
        super(mainframe);
        add(createCheckpointTable(parentDialog, operations), BorderLayout.CENTER);
    }
    
    private TablePanel createCheckpointTable(final JDialog parentDialog, List <TestOperation> operations) {
        TableConfiguration config = new TableConfiguration();
        config.setTableName("test-operations-table");
        config.setDisplayName("Test Opertions");
        
        int[] alignment = new int[4];
        for (int i = 0; i < alignment.length; ++i) {
            if ((i == 0) || (i == 3)) {
                alignment[i] = JLabel.CENTER;
            } else {
                alignment[i] = JLabel.LEFT;
            }
        }
            
        config.setColumnAlignment(alignment);
        
        config.setHeaders(new String[] {
            "Index",
            "Operation Type",
            "Operation Name",
            "Details"
        });
        
        config.setPropertyNames(new String[] {
            "operation.index",
            "operationType",
            "type",
            Constants.IGNORE_TABLE_DATA_INDICATOR
        });
            
        config.setColumnTypes(new Class[] {
            String.class,
            String.class,
            String.class,
            String.class
        });
        
        config.setColumnWidths(new int[] {
            15,
            30,
            30,
            30
        });

        config.setData(operations);
        
        operationsTable = new BaseTable(config) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return (column == 3);
            }

            @Override
            public Object getValueAt(int row, int column) {
                Object retval = "";
                if (column == 1) {
                    TestOperation op = (TestOperation)getRowData(row);
                    if (op.getOperationType().intValue() == TestOperationType.INT_CHECKPOINT) {
                        retval = ("checkpoint[" + op.getOperation().getCheckpointOperation().getType().toString() + "]");
                    } else {
                        retval = super.getValueAt(row, column);
                    }
                    
                } else if  (column == 2) {
                    TestOperation op = (TestOperation)getRowData(row);
                    switch (op.getOperationType().intValue()) {
                        case TestOperationType.INT_CHECKPOINT:
                            retval = op.getOperation().getCheckpointOperation().getName();
                            break;
                        case 
                            TestOperationType.INT_HTTP_REQUEST:
                            retval = op.getOperation().getHtmlRequestOperation().getMethod();
                            break;
                        case TestOperationType.INT_TEST_EXECUTION_PARAMETER:
                            retval = op.getOperation().getTestExecutionParameter().getName();
                            break;
                    }
                } else {
                    retval = super.getValueAt(row, column);
                }
                
                return retval;
            }
            
            
        };
        
        final TableCellIconButton b = new TableCellIconButton(Constants.DETAILS_ICON);
        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                List <TestOperation> l = operationsTable.getTableData();
                if ((b.getCurrentRow() > -1) && (l.size() > b.getCurrentRow())) {
                    TestOperation op = (TestOperation)l.get(b.getCurrentRow());
                    
                    switch (op.getOperationType().intValue()) {
                        case TestOperationType.INT_CHECKPOINT:
                            new CheckpointDetailsDlg(getMainframe(), parentDialog, op.getOperation().getCheckpointOperation());
                            break;
                        case 
                            TestOperationType.INT_HTTP_REQUEST:
                            new HtmlRequestDetailsDlg(getMainframe(), parentDialog, op.getOperation().getHtmlRequestOperation());
                            break;
                        case TestOperationType.INT_TEST_EXECUTION_PARAMETER:
                            new TestExecutionParameterDetailsDlg(getMainframe(), parentDialog, op.getOperation().getTestExecutionParameter());
                            break;
                    }
                    
                }
                
                b.stopCellEditing();
            }
        });
        
        
        
        operationsTable.getColumnModel().getColumn(3).setCellRenderer(b);
        operationsTable.getColumnModel().getColumn(3).setCellEditor(b);
        
        return new TablePanel(operationsTable);
    }
}
