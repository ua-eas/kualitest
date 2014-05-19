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
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.ui.base.BasePanel;
import org.kuali.test.ui.components.sqlquerytree.SqlQueryTree;


public class SqlQueryPanel extends BasePanel {
    private SqlQueryTree sqlQueryTree;
    
    public SqlQueryPanel(TestCreator mainframe) {
        super(mainframe);
        initComponents();
    }

    private void initComponents() {
        JPanel p = new JPanel(new BorderLayout());
        p.add(new JLabel("Root Table:", JLabel.RIGHT), BorderLayout.WEST);
        
        JComboBox cb;
        p.add(cb = new JComboBox(getAvailableDatabaseTables()), BorderLayout.CENTER);
        
        add(p, BorderLayout.NORTH);
        add(sqlQueryTree = new SqlQueryTree(getMainframe()), BorderLayout.CENTER);
    }
    
    
    private String[] getAvailableDatabaseTables() {
        List <String> retval = new ArrayList<String>();
        
        
        return retval.toArray(new String[retval.size()]);
    }
}