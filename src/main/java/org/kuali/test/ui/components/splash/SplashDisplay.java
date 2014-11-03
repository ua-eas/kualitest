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

/**
 *
 * @author rbtucker
 */
public class SplashDisplay {
    private static final Logger LOG = Logger.getLogger(SplashDisplay.class);

    private JLabel label;
    private JLabel elapsedTime;
    private JProgressBar progressBar;
    private Window parentWindow;
    private String title;
    private String message;
    private int progressMaxLimit;
    private JDialog dlg;
    
    /**
     *
     * @param parent
     * @param title
     * @param message
     */
    public SplashDisplay(Window parent, String title, String message) {
        this(parent, title, message, 0, false);
    }
    
    /**
     * 
     * @param parent
     * @param title
     * @param msg
     * @param progressMaxLimit
     * @param showElapsedTime 
     */
    public SplashDisplay(Window parent, String title, String msg, int progressMaxLimit, boolean showElapsedTime) {
        createDialog(parent, title, msg, progressMaxLimit, showElapsedTime);
    }
    
    public SplashDisplay(Window parent, String title, String msg, boolean showElapsedTime) {
        createDialog(parent, title, msg, 0, showElapsedTime);
    }

    private void createDialog(Window parentWindow, String title, String message, int progressMaxLimit, final boolean showElapsedTime) {
        this.parentWindow = parentWindow;
        this.title = title;
        this.message = message;
        this.progressMaxLimit = progressMaxLimit;

        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                if ((getParentWindow() == null) || (getParentWindow() instanceof JFrame)) {
                        dlg = new JDialog((JFrame)getParentWindow(), getTitle(), true);
                    } else {
                        dlg = new JDialog((JDialog)getParentWindow(), getTitle(), true);
                    }

                    dlg.setSize(Constants.DEFAULT_SPLASH_WIDTH, Constants.DEFAULT_SPLASH_HEIGHT);
                    dlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                    dlg.setResizable(false);
                    JPanel p = new JPanel(new BorderLayout(10, 3));
                    p.add(label = new JLabel(getMessage(), getIcon(), JLabel.LEFT), BorderLayout.NORTH);
                    
                    if (showElapsedTime) {
                        p.add(elapsedTime = new JLabel(""), BorderLayout.CENTER);
                    }
                    
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
                
                catch(SecurityException ex) {
                    getDlg().dispose();
                    LOG.error(ex.toString(), ex);
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
                    
                if (errmsg != null) {
                    UIUtils.showError(getParentWindow(), "Error", errmsg.toString());
                }
                getDlg().dispose();
                processCompleted();
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
    
    /**
     *
     * @return
     */
    public JProgressBar getProgressBar() {
        return progressBar;
    }
    
    /**
     *
     * @return
     */
    protected ImageIcon getIcon() {
        return Constants.CLOCK_ICON;
    }
    
    /**
     *
     */
    protected void updateProgress() {};

    /**
     *
     */
    protected void runProcess() {};

    /**
     *
     */
    protected void processCompleted() {};

    /**
     *
     * @return
     */
    public Window getParentWindow() {
        return parentWindow;
    }

    /**
     *
     * @return
     */
    public String getTitle() {
        return title;
    }

    /**
     *
     * @return
     */
    public String getMessage() {
        return message;
    }

    /**
     *
     * @return
     */
    public int getProgressMaxLimit() {
        return progressMaxLimit;
    }

    /**
     *
     * @return
     */
    public JDialog getDlg() {
        return dlg;
    }

    public JLabel getElapsedTime() {
        return elapsedTime;
    }

    public void updateElapsedTime(String txt) {
        elapsedTime.setText(txt);
        elapsedTime.validate();
    }
    
    public void updateElapsedTime(final long timeInSeconds) {
        elapsedTime.setText(" Elapsed Time: " + timeInSeconds + " (sec)");
        elapsedTime.validate();
    }
}