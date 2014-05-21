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
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import org.apache.log4j.Logger;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.utils.Constants;

public class SqlDisplayPanel extends BaseSqlPanel {

    private static final Logger LOG = Logger.getLogger(SqlDisplayPanel.class);

    private JEditorPane editorPane;

    public SqlDisplayPanel(TestCreator mainframe, DatabasePanel dbPanel) {
        super(mainframe, dbPanel);
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
        StringBuilder retval = new StringBuilder(512);
        retval.append("<body>");
        retval.append("<span style='color: ");
        retval.append(Constants.COLOR_DARK_BLUE);
        retval.append("; font-weight: 700'>select</span><br />&nbsp;&nbsp;&nbsp;&nbsp;");
        retval.append("column1, column2, column3<br/>");
        retval.append("<span style='color: ");
        retval.append(Constants.COLOR_DARK_BLUE);
        retval.append("; font-weight: 700'>from</span><br />&nbsp;&nbsp;&nbsp;&nbsp;");
        retval.append("table1 join table2 on (table1.key = table2.key)<br />");
        retval.append("<span style='color: ");
        retval.append(Constants.COLOR_DARK_BLUE);
        retval.append("; font-weight: 700'>where</span><br >&nbsp;&nbsp;&nbsp;&nbsp;");
        retval.append("column 3 = 'xxx' and column 4 = 'yyy'<br /> ");
        retval.append("<span style='color: ");
        retval.append(Constants.COLOR_DARK_BLUE);
        retval.append("; font-weight: 700'>order by</span> 1 desc");
        retval.append("</body>");
        return retval.toString();
    }
}
