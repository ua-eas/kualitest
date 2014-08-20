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
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.kuali.test.ui.base.BaseTable;
import org.kuali.test.utils.Constants;

/**
 *
 * @author rbtucker
 */
public class TablePanel extends JPanel implements ListSelectionListener {
    private BaseTable table;
    private JPanel labelPanel;
    private JButton addButton;
    private JButton deleteButton;
    
    /**
     *
     * @param table
     * @param numberOfRowsToDisplay
     */
    public TablePanel(BaseTable table, int numberOfRowsToDisplay) {
        super(new BorderLayout(3, 3));
        this.table = table;
        initComponents(numberOfRowsToDisplay);
    }
    
    /**
     *
     * @param table
     */
    public TablePanel(BaseTable table) {
        this(table, Constants.DEFAULT_DISPLAY_TABLE_ROWS);
    }
    
    private void initComponents(int numberOfRowsToDisplay) {
        labelPanel = new JPanel(new BorderLayout());
        labelPanel.add(new JLabel(table.getConfig().getDisplayName()), BorderLayout.NORTH);
        add (labelPanel, BorderLayout.NORTH);
        labelPanel.add(new JSeparator(), BorderLayout.CENTER);
        labelPanel.add(new JPanel(new FlowLayout(FlowLayout.LEFT)), BorderLayout.SOUTH);
        
        add(new JScrollPane(table), BorderLayout.CENTER);
        table.setPreferredScrollableViewportSize(new Dimension( 
            table.getPreferredScrollableViewportSize().width, 
            numberOfRowsToDisplay*table.getRowHeight())); 

        table.getSelectionModel().addListSelectionListener(this);
    }
    
    @Override
    public Insets getInsets() {
        return new Insets(3, 3, 3, 3);
    }
    
    /**
     *
     * @return
     */
    public JPanel getButtonPanel() {
        BorderLayout l = (BorderLayout)labelPanel.getLayout();
        return (JPanel)l.getLayoutComponent(BorderLayout.SOUTH);
    }
    
    /**
     *
     * @param listener
     * @param text
     * @param tooltipText
     */
    public void addAddButton(ActionListener listener, String text, String tooltipText) {;
        getButtonPanel().add(addButton = addButton(listener, text, Constants.ADD_ICON, tooltipText));
    }

    /**
     * 
     * @param listener
     * @param text
     * @param icon
     * @param tooltipText
     * @return 
     */
    public JButton addButton(ActionListener listener, String text, ImageIcon icon, String tooltipText) {;
        JPanel p = getButtonPanel();
        JButton retval = new JButton(text, icon);
        retval.setMargin(new Insets(1, 1, 1, 1));
        retval.setToolTipText(tooltipText);
        retval.addActionListener(listener);
        p.add(retval);
        
        return retval;
    }
    

    /**
     *
     * @param listener
     * @param text
     * @param tooltipText
     */
    public void addDeleteButton(ActionListener listener, String text, String tooltipText) {
        getButtonPanel().add(deleteButton = addButton(listener, text, Constants.DELETE_ICON, tooltipText));
        deleteButton.setEnabled(false);
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (deleteButton != null) {
            deleteButton.setEnabled(table.getSelectedRow() > -1);
        }
    }

    /**
     *
     * @return
     */
    public JButton getAddButton() {
        return addButton;
    }

    /**
     *
     * @return
     */
    public JButton getDeleteButton() {
        return deleteButton;
    }

    /**
     *
     * @return
     */
    public BaseTable getTable() {
        return table;
    }
}
