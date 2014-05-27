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
package org.kuali.test.ui.components.sqlquerypanel;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.apache.log4j.Logger;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.utils.Constants;

public class SqlDisplayPanel extends BaseSqlPanel {
    private static final Logger LOG = Logger.getLogger(SqlDisplayPanel.class);
    private static final String CHECK_GENERATED_SQL_ACTION = "Check Generated SQL";
    private JEditorPane editorPane;
    private JButton checkSql;
    
    public SqlDisplayPanel(TestCreator mainframe, DatabasePanel dbPanel) {
        super(mainframe, dbPanel, null);
        initComponents();
    }

    private void initComponents() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p.add(checkSql = new JButton(CHECK_GENERATED_SQL_ACTION));
        add(p, BorderLayout.NORTH);
        checkSql.addActionListener(this);
        add(new JScrollPane(editorPane = new JEditorPane(Constants.HTML_MIME_TYPE,"")), BorderLayout.CENTER);
    }

    @Override
    protected void handlePanelShown() {
        String sql = buildHtml();
        checkSql.setEnabled(sql.contains("select"));
        editorPane.setText(sql); 
    }
    
    private String buildHtml() {
       return getDbPanel().getSqlQueryString(true);
    }

    protected void handleUnprocessedActions(ActionEvent e) {
        if (CHECK_GENERATED_SQL_ACTION.equals(e.getActionCommand())) {
            if (getDbPanel().isValidSqlQuery()) {
                JOptionPane.showMessageDialog(this, "SQL query is valid");
            }
        }
    }
}
