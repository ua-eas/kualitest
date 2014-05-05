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
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.Preferences;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.WindowConstants;
import org.apache.log4j.Logger;
import org.kuali.test.DatabaseConnection;
import org.kuali.test.KualiTestConfigurationDocument;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.ui.utils.UIUtils;
import org.kuali.test.utils.Constants;


public abstract class BaseSetupDlg extends JDialog implements ActionListener {
    protected static final Logger LOG = Logger.getLogger(BaseSetupDlg.class);
    private JButton saveButton;
    private JButton cancelButton;
    private String saveActionCommand = Constants.SAVE_ACTION;
    private String cancelActionCommand = Constants.CANCEL_ACTION;

    private boolean saved = false;
    private boolean editmode = false;
    
    public BaseSetupDlg(TestCreator mainframe) {
        super(mainframe, true);
        getContentPane().setLayout(new BorderLayout());
    }

    protected void loadPreferences() {
      Preferences proot = Preferences.userRoot();
      Preferences node = proot.node(Constants.PREFS_DLG_NODE);
      Dimension dim = getPreferredSize();
      
      String nm = getDialogName();
      int left = node.getInt(nm + Constants.PREFS_DLG_LEFT, Constants.DEFAULT_DIALOG_LEFT);
      int top = node.getInt(nm + Constants.PREFS_DLG_TOP, Constants.DEFAULT_DIALOG_TOP);
      int width = node.getInt(nm + Constants.PREFS_DLG_WIDTH, dim.width);
      int height = node.getInt(nm + Constants.PREFS_DLG_HEIGHT, dim.height);
      setBounds(left, top, width, height);
        
    }
    
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
    
    protected abstract String getDialogName();

    @Override
    public void dispose() {
        savePreferences();
        super.dispose(); 
    }

    
    protected TestCreator getMainframe() {
        return (TestCreator)getParent();
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        handleAction(e);
    }

    public KualiTestConfigurationDocument.KualiTestConfiguration getConfiguration() {
        return getMainframe().getConfiguration();
    }

    public void setSaved(boolean saved) {
        this.saved = saved;
    }
    
    public boolean isSaved() {
        return saved;
    }
    
    public void setEditmode(boolean editmode) {
        this.editmode = editmode;
    }
    
    public boolean isEditmode() {
        return editmode;
    }

    protected void addStandardButtons() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER));
        
        p.add(saveButton = new JButton(saveActionCommand = getSaveText()));
        saveButton.addActionListener(this);
        
        p.add(cancelButton = new JButton(cancelActionCommand = getCancelText()));
        cancelButton.addActionListener(this);

        JPanel p2 = new JPanel(new BorderLayout());
        p2.add(new JSeparator(), BorderLayout.NORTH);
        p2.add(p, BorderLayout.CENTER);
        getContentPane().add(p2, BorderLayout.SOUTH);
    }
    
    protected void setDefaultBehavior() {
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(getMainframe());
        loadPreferences();
        pack();
        setVisible(true);
    }
    
    protected void displayRequiredFieldsMissingAlert(String type, String requiredFields) {
        UIUtils.showError(this, type, "Required fields are missing - please check fields:\n" + requiredFields);
    }
    
    protected void displayExistingNameAlert(String type, String name) {
        UIUtils.showError(this, type, type + "'" + name + "' name already exists");
    }
    
    protected JPanel wrapPanel(JComponent c) {
        JPanel retval = new JPanel(new FlowLayout(FlowLayout.LEFT));
        retval.add(c);
        return retval;
    }
    
    protected JPanel buildLabelGridPanel(String[] labels) {
        JPanel retval = new JPanel(new GridLayout(labels.length, 1, 1, 2));
        for (String label : labels) {
            JLabel l = new JLabel(label + ":", JLabel.RIGHT);
            l.setVerticalAlignment(JLabel.CENTER);
            retval.add(l);
        }
        
        return retval;
    }

    protected JPanel buildComponentGridPanel(JComponent[] components) {
        JPanel retval = new JPanel(new GridLayout(components.length, 1, 1, 2));
        for (JComponent c : components) {
            retval.add(wrapPanel(c));
        }
        
        return retval;
    }
    
    protected JPanel buildEntryPanel(String[] labels, JComponent[] components) {
        JPanel retval = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JPanel entryPanel = new JPanel(new BorderLayout(2, 1));
        entryPanel.add(buildLabelGridPanel(labels), BorderLayout.WEST);
        entryPanel.add(buildComponentGridPanel(components), BorderLayout.CENTER);
        
        retval.add(entryPanel);
        
        return retval;
    }
    
    @Override
    public Insets getInsets() {
        return new Insets(20, 5, 5, 5);
    }
    
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(600, 300);
    }
    
    protected String[] getDatabaseConnectionNames() {
        DatabaseConnection[] dbconns = getConfiguration().getDatabaseConnections().getDatabaseConnectionArray();
        String[] retval = new String[dbconns.length];
        
        for (int i = 0; i < dbconns.length; ++i) {
            retval[i] = dbconns[i].getName();
        }
        
        return retval;
    }

    
    protected void handleAction(ActionEvent e) {
        if (saveActionCommand.equalsIgnoreCase(e.getActionCommand())) {
            save();
        } else if (cancelActionCommand.equalsIgnoreCase(e.getActionCommand())) {
            dispose();
        } else {
            handleOtherActions(e.getActionCommand());
        }
    }

    protected abstract boolean save();
    protected void handleOtherActions(String actionCommend) {};
    
    public Object getNewRepositoryObject() {
        return null;
    }
    
    protected JButton getSaveButton() {
        return saveButton;
    }
    
    protected JButton getCancelButton() {
        return cancelButton;
    }

    protected String getSaveText() {
        return Constants.SAVE_ACTION;
    }

    protected String getCancelText() {
        return Constants.CANCEL_ACTION;
    }
}
