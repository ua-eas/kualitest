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
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.test.ui.utils.UIUtils;
import org.kuali.test.utils.Constants;

/**
 *
 * @author rbtucker
 */
public class SimpleInputDlg2 extends JDialog implements ActionListener {
    private static final Logger LOG = Logger.getLogger(SimpleInputDlg2.class);
    private boolean saved = false;
    private JTextArea value;
    private String enteredValue;
    
    /**
     *
     * @param frame
     * @param labelTxt
     */
    public SimpleInputDlg2(JFrame frame, String labelTxt) {
        super(frame, true);
        initComponents(labelTxt);
    }

    /**
     *
     * @param dlg
     * @param labelTxt
     */
    public SimpleInputDlg2(JDialog dlg, String labelTxt) {
        super(dlg, true);
        initComponents(labelTxt);
    }

    private void initComponents(String labelTxt) {
        setTitle(labelTxt);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(new JLabel(labelTxt + ":"), BorderLayout.NORTH);
        value = new JTextArea(5, 40);
        value.setWrapStyleWord(true);
        value.setLineWrap(true);
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 1));
        p.add(new JScrollPane(value, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER));
        getContentPane().add(p, BorderLayout.CENTER);
        addStandardButtons();
        setDefaultBehavior();
    }
    
    /**
     *
     */
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
    
    /**
     *
     */
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
        return new Insets(25, 5, 5, 5);
    }
    
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(500, 175);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (Constants.OK_ACTION.equalsIgnoreCase(e.getActionCommand())) {
            if (isInputError(value.getText())) {
                String msg = getErrorMessage(value.getText());
                
                if (StringUtils.isNotBlank(msg)) {
                    UIUtils.showError(this, "Input Error", msg);
                }
            } else {
                enteredValue = value.getText();
                dispose();
            }
        } else if (Constants.CANCEL_ACTION.equalsIgnoreCase(e.getActionCommand())) {
            enteredValue = null;
            dispose();
        }
    }

    /**
     *
     * @param inputValue
     * @return
     */
    protected boolean isInputError(String inputValue) {
        return false;
    }

    /**
     *
     * @param inputValue
     * @return
     */
    protected String getErrorMessage(String inputValue) {
        return null;
    }
    
    /**
     *
     * @return
     */
    public String getEnteredValue() {
        return enteredValue;
    }
}
