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
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


public class ItemSelectDlg extends JDialog implements ListSelectionListener {
    private String selectedValue;
    private JList list;
    
    public ItemSelectDlg(JFrame frame, String title, List <String> items) {
        super(frame, true);
        setTitle(title);
        init(items);
    }

    private void init(List <String> items) {
        getContentPane().setLayout(new BorderLayout());
        DefaultListModel model = new DefaultListModel();
        list = new JList(model);
        list.addListSelectionListener(this);
        
        for (String item : items) {
            model.addElement(item);
        }
        getContentPane().add(new JScrollPane(list), BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);       
        setVisible(true);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(250, 400);
    }
    
    @Override
    public void valueChanged(ListSelectionEvent lse) {
        JList list = (JList)lse.getSource();
        selectedValue = (String)list.getSelectedValue();
        dispose();
    }

    public String getSelectedValue() {
        return selectedValue;
    }
}
