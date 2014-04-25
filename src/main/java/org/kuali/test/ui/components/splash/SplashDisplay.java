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
import java.awt.Window;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import org.apache.log4j.Logger;
import org.kuali.test.ui.utils.UIUtils;
import org.kuali.test.utils.Constants;


public class SplashDisplay {
    private static final Logger LOG = Logger.getLogger(SplashDisplay.class);

    private JLabel label;
    private JProgressBar progressBar;
    private Window parentWindow;
    private String title;
    private String message;
    private int progressMaxLimit;
    private JDialog dlg;
    
    public SplashDisplay(Window parent, String title, String message) {
        this(parent, title, message, 0);
    }
    
    public SplashDisplay(Window parent, String title, String msg, int progressMaxLimit) {
        createDialog(parent, title, msg, progressMaxLimit);
    }
    
    private void createDialog(Window parentWindow, String title, String message, int progressMaxLimit) {
        this.parentWindow = parentWindow;
        this.title = title;
        this.message = message;
        this.progressMaxLimit = progressMaxLimit;
        
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    if (getParentWindow() instanceof JFrame) {
                        dlg = new JDialog((JFrame)getParentWindow(), getTitle(), true);
                    } else {
                        dlg = new JDialog((JDialog)getParentWindow(), getTitle(), true);
                    }

                    dlg.setSize(Constants.DEFAULT_SPLASH_WIDTH, Constants.DEFAULT_SPLASH_HEIGHT);
                    dlg.setResizable(false);
                    JPanel p = new JPanel(new BorderLayout(10, 10));
                    p.add(label = new JLabel(getMessage(), getIcon(), JLabel.LEFT), BorderLayout.CENTER);
                    p.add(progressBar = new JProgressBar(), BorderLayout.SOUTH);

                    dlg.getContentPane().add(p);

                    if (getProgressMaxLimit() > 0) {
                        progressBar.setIndeterminate(false);
                        progressBar.setMinimum(0);
                        progressBar.setMaximum(getProgressMaxLimit());
                    } else {
                        progressBar.setIndeterminate(true);
                    }

                    dlg.setAlwaysOnTop(true);
                    dlg.setLocationRelativeTo(getParentWindow());
                    dlg.setVisible(true);
                }
                
                catch(Exception ex) {
                    if (getDlg() != null) {
                        getDlg().dispose();
                    }
                    
                    LOG.error(ex);
                }
            }
        });
        
        new SwingWorker() {
            @Override
            protected Object doInBackground() throws Exception {
                try {
                    runProcess();
                } 
                
                catch (Exception ex) {
                    if (getDlg() != null) {
                        getDlg().dispose();
                    }
                    
                    UIUtils.showError(getParentWindow(), "Error", ex.toString());
                }
                return null;
            };

            @Override
            protected void done() {
                getDlg().dispose();
            }
        }.execute();
    }

    public JLabel getLabel() {
        return label;
    }
    
    public JProgressBar getProgressBar() {
        return progressBar;
    }
    
    protected ImageIcon getIcon() {
        return Constants.CLOCK_ICON;
    }
    
    protected void updateProgress() {};
    protected void runProcess() {};

    public Window getParentWindow() {
        return parentWindow;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public int getProgressMaxLimit() {
        return progressMaxLimit;
    }

    public JDialog getDlg() {
        return dlg;
    }
}