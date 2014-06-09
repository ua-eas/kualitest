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

package org.kuali.test.ui.base;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.WindowConstants;
import org.apache.log4j.Logger;
import org.kuali.test.utils.Constants;


public class SelectObjectDlg extends JDialog implements ActionListener {
    private static final Logger LOG = Logger.getLogger(SelectObjectDlg.class);
    private Object selectedObject;
    private JList list;
    
    public SelectObjectDlg(JDialog dialog, List inputList) {
        super(dialog, true);
        initComponents();
    }

    public SelectObjectDlg(JFrame frame, List inputList) {
        super(frame, true);
        initComponents();
    }

    private void initComponents() {
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(new JScrollPane(list = new JList()), BorderLayout.CENTER);
        addStandardButtons();
        setDefaultBehavior();
    }

    protected void setDefaultBehavior() {
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(getParent());
        pack();
        setVisible(true);
    }

    @Override
    public boolean isResizable() {
        return false;
    }
    
    @Override
    public Insets getInsets() {
        return new Insets(20, 5, 5, 5);
    }
    
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(400, 300);
    }
    

    @Override
    public void actionPerformed(ActionEvent e) {
        if (Constants.OK_ACTION.equalsIgnoreCase(e.getActionCommand())) {
            selectedObject = list.getSelectedValue();
        } else {
            selectedObject = null;
        }

        dispose();
    }

    public Object getSelectedObject() {
        return selectedObject;
    }
    
    protected void addStandardButtons() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER));
        
        JButton b;
        p.add(b = new JButton(Constants.OK_ACTION));
        b.addActionListener(this);
        
        p.add(b = new JButton(Constants.CANCEL_ACTION));
        b.addActionListener(this);

        JPanel p2 = new JPanel(new BorderLayout());
        p2.add(new JSeparator(), BorderLayout.NORTH);
        p2.add(p, BorderLayout.CENTER);
        getContentPane().add(p2, BorderLayout.SOUTH);
    }
}
