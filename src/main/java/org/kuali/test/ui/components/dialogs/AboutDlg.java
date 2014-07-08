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

package org.kuali.test.ui.components.dialogs;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import org.kuali.test.utils.Constants;


public class AboutDlg extends JDialog {
    public AboutDlg(JFrame parent) {
        super(parent, true);
        setTitle("About Kuali Test");
        getContentPane().setLayout(new BorderLayout());
        
        getContentPane().add(new JLabel("<html><body><div style='text-align: center'>Kuali Test Ver. 1.0b<br />University of Arizona</div></body></html>"), BorderLayout.CENTER);

        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER));
        
        JButton b;
        p.add(b = new JButton(Constants.CLOSE_ACTION));
        
        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        
        getContentPane().add(p, BorderLayout.SOUTH);
        
        setLocationRelativeTo(this);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        pack();
        setResizable(false);
        setVisible(true);
    }

    @Override
    public Insets getInsets() {
        return new Insets(20, 5, 5, 5);
    }
    
      @Override
    public Dimension getPreferredSize() {
        return new Dimension(200, 150);
    }

}
