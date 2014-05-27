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
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import org.apache.log4j.Logger;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.utils.Constants;

public class SqlDisplayPanel extends BaseSqlPanel {

    private static final Logger LOG = Logger.getLogger(SqlDisplayPanel.class);

    private JEditorPane editorPane;

    public SqlDisplayPanel(TestCreator mainframe, DatabasePanel dbPanel) {
        super(mainframe, dbPanel, null);
        initComponents();
    }

    private void initComponents() {
        add(new JScrollPane(editorPane = new JEditorPane(Constants.HTML_MIME_TYPE,"")), BorderLayout.CENTER);
    }

    @Override
    protected void handlePanelShown() {
        editorPane.setText(buildHtml()); 
    }
    
    private String buildHtml() {
        return getDbPanel().getSqlQueryString(true);
    }
}
