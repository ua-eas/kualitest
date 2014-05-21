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


public class SqlWherePanel extends BaseSqlPanel implements ActionListener {
    private static final Logger LOG = Logger.getLogger(SqlWherePanel.class);
    private TablePanel tp;
    
    public SqlWherePanel(TestCreator mainframe, DatabasePanel dbPanel) {
        super(mainframe, dbPanel);
        initComponents();
    }

    private void initComponents() {
        tp = new TablePanel(getSelectColumnTable());
        tp.addAddButton(this, Constants.ADD_COMPARISON_ACTION, "add new where comparison");
        tp.getAddButton().setEnabled(false);
        tp.addDeleteButton(this, Constants.DELETE_COMPARISON_ACTION, "delete selected row");
        add(tp, BorderLayout.CENTER);
    }
    
    private BaseTable getSelectColumnTable() {
        TableConfiguration tc = new TableConfiguration();
        
        tc.setHeaders(new String[] {
            "and/or",
            "(",
            "table",
            "column",
            "oerator",
            "value",
            ")"
        });
        
        tc.setPropertyNames(new String[] {
            "andOr",
            "openParenthesis",
            "table",
            "column",
            "oerator",
            "value",
            "closeParenthesis"
        });
        
        tc.setColumnTypes(new Class[] {
            String.class,
            String.class,
            String.class,
            String.class,
            String.class,
            String.class,
            String.class
        });
        
        tc.setColumnWidths(new int[] {
            15,
            15,
            30,
            30,
            20,
            20,
            15
        });

        tc.setTableName("sql-where-column-table");
        tc.setDisplayName("Where Comparisons");

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
        if (Constants.ADD_COMPARISON_ACTION.equals(e.getSource())) {
            
        } else if (Constants.DELETE_COMPARISON_ACTION.equals(e.getSource())) {
            
        }
    }

    protected void handlePanelShown() {
        tp.getAddButton().setEnabled(getDbPanel().haveSelectedColumns());
    }
}
