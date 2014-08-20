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
import java.util.Iterator;
import java.util.List;
import javax.swing.JDialog;
import javax.swing.JLabel;
import org.apache.commons.lang3.StringUtils;
import org.kuali.test.Checkpoint;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.ui.base.BasePanel;
import org.kuali.test.ui.base.BaseTable;
import org.kuali.test.ui.base.TableConfiguration;
import org.kuali.test.ui.components.buttons.TableCellIconButton;
import org.kuali.test.ui.components.dialogs.CheckpointDetailsDlg;
import org.kuali.test.utils.Constants;


public class TestCheckpointsPanel extends BasePanel {
    private BaseTable checkpointTable;
    public TestCheckpointsPanel(TestCreator mainframe, JDialog parentDialog, List <Checkpoint> checkpoints) {
        super(mainframe);
        add(createCheckpointTable(parentDialog, checkpoints), BorderLayout.CENTER);
    }
    
    private TablePanel createCheckpointTable(final JDialog parentDialog, List <Checkpoint> checkpoints) {
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

        Iterator <Checkpoint> it = checkpoints.iterator();
        
        while (it.hasNext()) {
            if (StringUtils.isBlank(it.next().getName())) {
                it.remove();
            }
        }

        config.setData(checkpoints);
        
        checkpointTable = new BaseTable(config) {
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
                    new CheckpointDetailsDlg(getMainframe(), parentDialog, l.get(b.getCurrentRow()));
                }
            }
        });
        
        checkpointTable.getColumnModel().getColumn(2).setCellRenderer(b);
        checkpointTable.getColumnModel().getColumn(2).setCellEditor(b);
        
        return new TablePanel(checkpointTable);
    }
}
