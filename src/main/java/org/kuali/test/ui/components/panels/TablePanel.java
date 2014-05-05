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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.kuali.test.ui.base.BaseTable;
import org.kuali.test.utils.Constants;


public class TablePanel extends JPanel {
    private BaseTable table;
    private JPanel labelPanel;
    public TablePanel(BaseTable table, int numberOfRowsToDisplay) {
        super(new BorderLayout(3, 3));
        this.table = table;
        initComponents(numberOfRowsToDisplay);
    }
    
    public TablePanel(BaseTable table) {
        this(table, Constants.DEFAULT_DISPLAY_TABLE_ROWS);
    }
    
    private void initComponents(int numberOfRowsToDisplay) {
        labelPanel = new JPanel(new BorderLayout());
        labelPanel.add(new JLabel(table.getConfig().getDisplayName()), BorderLayout.NORTH);
        add (labelPanel, BorderLayout.NORTH);
        labelPanel.add(new JPanel(new FlowLayout(FlowLayout.LEFT)), BorderLayout.CENTER);
        add(new JScrollPane(table), BorderLayout.CENTER);
        table.setPreferredScrollableViewportSize(new Dimension( 
            table.getPreferredScrollableViewportSize().width, 
            numberOfRowsToDisplay*table.getRowHeight())); 

    }
    
    @Override
    public Insets getInsets() {
        return new Insets(3, 3, 3, 3);
    }
    
    private JPanel getButtonPanel() {
        BorderLayout l = (BorderLayout)labelPanel.getLayout();
        return (JPanel)l.getLayoutComponent(BorderLayout.CENTER);
    }
    
    public void addAddButton(ActionListener listener, String text, String tooltipText) {;
        JPanel p = getButtonPanel();
        JButton b = new JButton(text, Constants.ADD_ICON);
        b.setToolTipText(tooltipText);
        b.addActionListener(listener);
        p.add(b);
    }
    
    public void addDeleteButton(ActionListener listener, String text, String tooltipText) {
        JPanel p = getButtonPanel();
        JButton b = new JButton(text, Constants.DELETE_ICON);
        b.setToolTipText(tooltipText);
        b.addActionListener(listener);
        p.add(b);
    }
}
