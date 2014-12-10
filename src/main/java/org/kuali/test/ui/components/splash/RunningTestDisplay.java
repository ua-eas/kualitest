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

package org.kuali.test.ui.components.splash;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import org.apache.log4j.Logger;
import org.kuali.test.ui.utils.UIUtils;

/**
 *
 * @author rbtucker
 */
public class RunningTestDisplay {
    private static final Logger LOG = Logger.getLogger(RunningTestDisplay.class);

    private JLabel label;
    private JProgressBar progressBar;
    private JDialog dlg;
    private boolean cancelTest = false;

    /**
     * 
     * @param parentWindow
     * @param title 
     */
    public RunningTestDisplay(final JFrame parentWindow, final String title) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    dlg = new JDialog(parentWindow, title, true);
                    dlg.setSize(450, 225);
                    dlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                    dlg.setResizable(false);
                    JPanel p = new JPanel(new BorderLayout(10, 3));
                    p.add(label = new JLabel("  Initializing test execution context...", JLabel.LEFT), BorderLayout.CENTER);
                    

                    JPanel p2 = new JPanel(new BorderLayout());
                    JPanel p3 = new JPanel(new FlowLayout(FlowLayout.CENTER));
                    
                    JButton b = new JButton("Cancel Test");
                    p3.add(b);
                    p2.add(p3, BorderLayout.CENTER);
                    b.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            cancelTest = true;
                            dlg.dispose();
                        }
                    });
                    
                    
                    p2.add(progressBar = new JProgressBar(), BorderLayout.SOUTH);
                    p.add(p2, BorderLayout.SOUTH);
                    dlg.getContentPane().add(p);

                    progressBar.setIndeterminate(true);
                    dlg.setAlwaysOnTop(true);
                    dlg.setLocationRelativeTo(parentWindow);
                    dlg.setVisible(true);
                }
                
                catch(Exception ex) {
                    LOG.error(ex.toString(), ex);
                    dlg.dispose();
                }
            }
        });

        new SwingWorker() {
            @Override
            protected Object doInBackground() throws Exception {
                String retval = null;
                try {
                    runProcess();
                } 
                
                catch (Exception ex) {
                    retval = ex.toString();
                    LOG.error(ex.toString(), ex);
                }
                
                return retval;
            };

            @Override
            protected void done() {
                Object errmsg = null;
                try {
                    errmsg = this.get();
                } 
                
                catch (Exception ex) {
                    LOG.error(ex.toString(), ex);
                }
                    
                dlg.dispose();
                processCompleted();
                if (errmsg != null) {
                    UIUtils.showError(parentWindow, "Error", errmsg.toString());
                } 
            }
        }.execute();
    }

    /**
     *
     * @return
     */
    public JLabel getLabel() {
        return label;
    }

    public void updateDisplay(String message) {
        if (label != null) {
            label.setText(message);
            label.validate();
        }
    }
    
    /**
     *
     * @return
     */
    public JProgressBar getProgressBar() {
        return progressBar;
    } 

    public boolean isCancelTest() {
        return cancelTest;
    }

    public JDialog getDlg() {
        return dlg;
    }

    protected void updateProgress() {};
    protected void runProcess() {};
    protected void processCompleted() {};
}