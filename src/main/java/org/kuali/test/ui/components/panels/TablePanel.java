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
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.kuali.test.ui.base.BaseTable;


public class TablePanel extends JPanel {
    private BaseTable table;
    public TablePanel(BaseTable table) {
        super(new BorderLayout(3, 3));
        this.table = table;
        initComponents();
    }
    
    protected void initComponents() {
        setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), table.getConfig().getDisplayName()));
        add(new JScrollPane(table));
    }
    
    public Insets getInsets() {
        return new Insets(3, 3, 3, 3);
    }
}
