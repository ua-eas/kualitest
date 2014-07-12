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
import java.awt.Dimension;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.kuali.test.creator.TestCreator;


public class WhereValueLookupDlg extends JDialog implements ListSelectionListener {
    private LookupValue lookupValue;
    
    public WhereValueLookupDlg(TestCreator mainframe, List <LookupValue> lookupValues) {
        super(mainframe, true);
        setTitle("Look Up");
        getContentPane().setLayout(new BorderLayout());
        DefaultListModel model = new DefaultListModel();
        JList list = new JList(model);
        list.addListSelectionListener(this);
        
        for (LookupValue val : lookupValues) {
            model.addElement(val);
        }
        
        getContentPane().add(new JScrollPane(list));
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
        lookupValue = (LookupValue)list.getSelectedValue();
        dispose();
    }

    public LookupValue getLookupValue() {
        return lookupValue;
    }
    
    
}
