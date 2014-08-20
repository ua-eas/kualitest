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

package org.kuali.test.ui.components.buttons;

import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;

/**
 *
 * @author rbtucker
 */
public abstract class ExtraInfoToolbarButton extends JPanel {
    private ToolbarButton tbButton;
    private JButton extraInfoButton;
    
    /**
     *
     * @param actionCommand
     * @param icon
     */
    public ExtraInfoToolbarButton(String actionCommand, ImageIcon icon) {
        super(new BorderLayout(1,0));
        tbButton = new ToolbarButton(actionCommand, actionCommand, icon);
        initComponents();
    }

    /**
     *
     * @param icon
     * @param tooltip
     */
    public ExtraInfoToolbarButton(ImageIcon icon, String tooltip) {
        super(new BorderLayout(1,0));
        tbButton = new ToolbarButton(icon, tooltip);
        initComponents();
    }

    /**
     *
     * @param actionCommand
     * @param txt
     * @param icon
     */
    public ExtraInfoToolbarButton(String actionCommand, String txt, ImageIcon icon) {
        tbButton = new ToolbarButton(actionCommand, txt, icon);
        initComponents();
    }
    
    private void initComponents() {
        this.
        extraInfoButton = new JButton("+") {
            @Override
            public Insets getInsets() {
                return new Insets(1, 1, 1, 1);
            }
        };
        
        extraInfoButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                showExtraInfo();
            }
        });
        
        add(tbButton, BorderLayout.CENTER);
        add(extraInfoButton, BorderLayout.EAST);
    }
    
    @Override
    public void setEnabled(boolean enable) {
        tbButton.setEnabled(enable);
        extraInfoButton.setEnabled(enable);
    }
    
    @Override
    public void setToolTipText(String txt) {
        tbButton.setToolTipText(txt);
    }
    
    public void addActionListener(ActionListener listener) {
        tbButton.addActionListener(listener);
    }
    
    public abstract void showExtraInfo();
}
