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
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import org.apache.log4j.Logger;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.ui.base.BaseTable;
import org.kuali.test.ui.base.TableConfiguration;
import org.kuali.test.utils.Constants;


public class SqlSelectPanel extends BaseSqlPanel implements ActionListener {
    private static final Logger LOG = Logger.getLogger(SqlSelectPanel.class);
    private TablePanel tp;
    public SqlSelectPanel(TestCreator mainframe, DatabasePanel dbPanel) {
        super(mainframe, dbPanel);
        initComponents();
    }

    private void initComponents() {
        tp = new TablePanel(getSelectColumnTable());
        tp.addAddButton(this, Constants.ADD_COLUMN_ACTION, "add new select column");
        tp.getAddButton().setEnabled(false);
        tp.addDeleteButton(this, Constants.DELETE_COLUMN_ACTION, "delete selected row");
        add(tp, BorderLayout.CENTER);
    }
    
    private BaseTable getSelectColumnTable() {
        TableConfiguration tc = new TableConfiguration();
        
        tc.setHeaders(new String[] {
            "table",
            "function",
            "column",
            "order",
            "asc/desc"
        });
        
        tc.setPropertyNames(new String[] {
            "table",
            "function",
            "column",
            "order",
            "asc/desc"
        });
        
        tc.setColumnTypes(new Class[] {
            String.class,
            String.class,
            String.class,
            Integer.class,
            String.class
        });
        
        tc.setColumnWidths(new int[] {
            30,
            20,
            30,
            15,
            15
        });

        tc.setTableName("sql-select-column-table");
        tc.setDisplayName("Select Columns");

        return new BaseTable(tc) {
            @Override
            protected TableCellEditor getTableCellEditor(TableConfiguration config, int col) {
                return super.getTableCellEditor(config, col); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            protected TableCellRenderer getTableCellRenderer(TableConfiguration config, int col) {
                return super.getTableCellRenderer(config, col); //To change body of generated methods, choose Tools | Templates.
            }
        };
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (Constants.ADD_COLUMN_ACTION.equals(e.getSource())) {
            
        } else if (Constants.DELETE_COLUMN_ACTION.equals(e.getSource())) {
            
        }
    }
    
    protected void handlePanelShown() {
        tp.getAddButton().setEnabled(getDbPanel().haveSelectedColumns());
    }
}
