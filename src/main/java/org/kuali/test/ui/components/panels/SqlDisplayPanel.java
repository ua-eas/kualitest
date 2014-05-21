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

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.StringContent;
import javax.swing.text.html.HTMLDocument;
import org.apache.log4j.Logger;
import org.kuali.test.creator.TestCreator;


public class SqlDisplayPanel extends BaseSqlPanel {
    private static final Logger LOG = Logger.getLogger(SqlDisplayPanel.class);
    
    private HTMLDocument sqlDoc;
    
    public SqlDisplayPanel(TestCreator mainframe, DatabasePanel dbPanel) {
        super(mainframe, dbPanel);
        initComponents();
    }

    private void initComponents() {
        try {
            StringContent sc = new StringContent();
            sc.insertString(0, "<html><body>select column1, column2, column3<br/>from table1 join table2 on (table1.key = table2.key)<br />where column 3 = 'xxx' and column 4 = 'yyy'<br /> order by 1 desc</body></html>");
            add(new JScrollPane(new JTextArea(sqlDoc = new HTMLDocument(sc, null))));
        }
        
        catch (Exception ex) {};
    }
}
