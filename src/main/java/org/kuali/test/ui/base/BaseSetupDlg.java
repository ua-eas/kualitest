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
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.prefs.Preferences;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.WindowConstants;
import org.apache.log4j.Logger;
import org.kuali.test.DatabaseConnection;
import org.kuali.test.JmxConnection;
import org.kuali.test.KualiTestConfigurationDocument;
import org.kuali.test.WebService;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.ui.utils.UIUtils;
import org.kuali.test.utils.Constants;

/**
 *
 * @author rbtucker
 */
public abstract class BaseSetupDlg extends JDialog implements ActionListener {
    private static final Logger LOG = Logger.getLogger(BaseSetupDlg.class);
    private JButton saveButton;
    private JButton cancelButton;
    private String saveActionCommand = Constants.SAVE_ACTION;
    private String cancelActionCommand = Constants.CANCEL_ACTION;
    private TestCreator mainframe;
    private boolean saved = false;
    private boolean editmode = false;
    
    /**
     *
     * @param mainframe
     */
    public BaseSetupDlg(TestCreator mainframe) {
        super(mainframe, true);
        this.mainframe = mainframe;
        getContentPane().setLayout(new BorderLayout());
    }

    /**
     *
     * @param mainframe
     * @param parent
     */
    public BaseSetupDlg(TestCreator mainframe, JDialog parent) {
        super(parent, true);
        this.mainframe = mainframe;
        getContentPane().setLayout(new BorderLayout());
    }

    /**
     *
     */
    protected void loadPreferences() {
      Preferences proot = Preferences.userRoot();
      Preferences node = proot.node(Constants.PREFS_DLG_NODE);
      Dimension dim = getPreferredSize();
      
      String nm = getDialogName();
      int left = node.getInt(nm + Constants.PREFS_DLG_LEFT, Constants.DEFAULT_DIALOG_LEFT);
      int top = node.getInt(nm + Constants.PREFS_DLG_TOP, Constants.DEFAULT_DIALOG_TOP);
      int width = node.getInt(nm + Constants.PREFS_DLG_WIDTH, dim.width);
      int height = node.getInt(nm + Constants.PREFS_DLG_HEIGHT, dim.height);
      
      setPreferredSize(new Dimension(width, height));
      
      setBounds(left, top, width, height);
    }
    
    /**
     *
     */
    protected void savePreferences() {
      Preferences proot = Preferences.userRoot();
      Preferences node = proot.node(Constants.PREFS_DLG_NODE);
      
      Rectangle rect = getBounds();

      String nm = getDialogName();
      
      node.putInt(nm + Constants.PREFS_DLG_LEFT, rect.x);
      node.putInt(nm + Constants.PREFS_DLG_TOP, rect.y);
      node.putInt(nm + Constants.PREFS_DLG_WIDTH, rect.width);
      node.putInt(nm + Constants.PREFS_DLG_HEIGHT, rect.height);
    }
    
    /**
     *
     * @return
     */
    protected abstract String getDialogName();

    @Override
    public void dispose() {
        savePreferences();
        super.dispose(); 
    }

    /**
     *
     * @return
     */
    protected TestCreator getMainframe() {
        return mainframe;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        handleAction(e);
    }

    /**
     *
     * @return
     */
    public KualiTestConfigurationDocument.KualiTestConfiguration getConfiguration() {
        return getMainframe().getConfiguration();
    }

    /**
     *
     * @param saved
     */
    public void setSaved(boolean saved) {
        this.saved = saved;
    }
    
    /**
     *
     * @return
     */
    public boolean isSaved() {
        return saved;
    }
    
    /**
     *
     * @param editmode
     */
    public void setEditmode(boolean editmode) {
        this.editmode = editmode;
    }
    
    /**
     *
     * @return
     */
    public boolean isEditmode() {
        return editmode;
    }
    
    /**
     *
     * @return
     */
    protected boolean getInitialSavedState() {
        return true;
    }

    /**
     *
     */
    protected void addStandardButtons() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER));
        
        p.add(saveButton = new JButton(saveActionCommand = getSaveText()));
        saveButton.addActionListener(this);
        saveButton.setEnabled(getInitialSavedState());
        
        p.add(cancelButton = new JButton(cancelActionCommand = getCancelText()));
        cancelButton.addActionListener(this);

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
        setLocationRelativeTo(getMainframe());
        pack();
        loadPreferences();
        setVisible(true);
    }

    @Override
    public boolean isResizable() {
        return false;
    }
    
    /**
     *
     * @param type
     * @param msg
     */
    protected void inputDataErrorsAlert(String type, String msg) {
        UIUtils.showError(this, type, "Input data errors:<br />" + msg);
    }

    /**
     *
     * @param type
     * @param requiredFields
     */
    protected void displayRequiredFieldsMissingAlert(String type, String requiredFields) {
        UIUtils.showError(this, type, "Required fields are missing - please check fields:\n" + requiredFields);
    }
    
    /**
     *
     * @param type
     * @param name
     */
    protected void displayExistingNameAlert(String type, String name) {
        UIUtils.showError(this, type, type + "'" + name + "' name already exists");
    }
    
    @Override
    public Insets getInsets() {
        return new Insets(20, 5, 5, 5);
    }
    
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(600, 300);
    }
    
    /**
     *
     * @return
     */
    protected String[] getDatabaseConnectionNames() {
        DatabaseConnection[] dbconns = getConfiguration().getDatabaseConnections().getDatabaseConnectionArray();
        String[] retval = new String[dbconns.length];
        
        for (int i = 0; i < dbconns.length; ++i) {
            retval[i] = dbconns[i].getName();
        }
        
        return retval;
    }

    /**
     *
     * @return
     */
    protected String[] getWebServiceNames() {
        String[] retval = new String[0];
    
        if (getConfiguration().getWebServices() != null) { 
            WebService[] webServices = getConfiguration().getWebServices().getWebServiceArray();
            retval = new String[webServices.length + 1];
            retval[0] = "";
            for (int i = 0; i < webServices.length; ++i) {
                retval[i+1] = webServices[i].getName();
            }

            Arrays.sort(retval);
        }
        
        return retval;
    }
    
    /**
     *
     * @return
     */
    protected String[] getJmxConnectionNames() {
        String[] retval = new String[0];

        if (getConfiguration().getJmxConnections() != null) { 
            JmxConnection[] jmxConnections = getConfiguration().getJmxConnections().getJmxConnectionArray();
            retval = new String[jmxConnections.length + 1];
            retval[0] = "";
            for (int i = 0; i < jmxConnections.length; ++i) {
                retval[i+1] = jmxConnections[i].getName();
            }

            Arrays.sort(retval);
        }
        
        return retval;
    }

    /**
     *
     * @param e
     */
    protected void handleAction(ActionEvent e) {
        if (saveActionCommand.equalsIgnoreCase(e.getActionCommand())) {
            save();
        } else if (cancelActionCommand.equalsIgnoreCase(e.getActionCommand())) {
            dispose();
        } else {
            handleOtherActions(e.getActionCommand());
        }
    }

    /**
     *
     * @return
     */
    protected abstract boolean save();

    /**
     *
     * @param actionCommand
     */
    protected void handleOtherActions(String actionCommand) {};
    
    /**
     *
     * @return
     */
    public Object getNewRepositoryObject() {
        return null;
    }
    
    /**
     *
     * @return
     */
    protected JButton getSaveButton() {
        return saveButton;
    }
    
    /**
     *
     * @return
     */
    protected JButton getCancelButton() {
        return cancelButton;
    }

    /**
     *
     * @return
     */
    protected String getSaveText() {
        return Constants.SAVE_ACTION;
    }

    /**
     *
     * @return
     */
    protected String getCancelText() {
        return Constants.CANCEL_ACTION;
    }
}
