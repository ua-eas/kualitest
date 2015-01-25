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

package org.kuali.test.ui.components.spinners;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.kuali.test.utils.Constants;


public class Spinner extends JPanel {
    private JLabel label;
    private boolean cancelled = false;
    
    public Spinner() {
        this(false);
    }
    
    public Spinner(boolean canstop) {
        super(new FlowLayout(FlowLayout.LEFT, 3, 0));
        setBorder(BorderFactory.createEmptyBorder());
        setOpaque(false);
        if (canstop) {
            JButton b = new JButton(Constants.CANCEL_ICON);
            b.setBorder(BorderFactory.createEmptyBorder());
            b.setContentAreaFilled(false);
            b.setToolTipText("Cancel operation");
            add(b);
            
            b.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    cancelled = true;
                }
            });
        }
        add(label = new JLabel(Constants.LOADING_SPINNER_ICON));
        setVisible(false);
    }
    
    public Spinner(String msg) {
        this();
        startSpinner(msg);
    }

    public void startSpinner( String msg) {
        cancelled = false;
        label.setText(msg);
        setVisible(true);
        label.repaint(0);
    }
    
    public void stopSpinner() {
        label.setText("");
        setVisible(false);
        cancelled = false;
    }

    public boolean isCancelled() {
        return cancelled;
    }
    
    public void updateMessage(String msg) {
        label.setText(msg);
    }
}
