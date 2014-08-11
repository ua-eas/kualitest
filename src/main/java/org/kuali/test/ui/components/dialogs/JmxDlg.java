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
import java.io.UnsupportedEncodingException;
import javax.swing.JComponent;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import org.apache.commons.lang3.StringUtils;
import org.kuali.test.JmxConnection;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.ui.base.BaseSetupDlg;
import org.kuali.test.ui.utils.UIUtils;
import org.kuali.test.utils.Utils;

/**
 *
 * @author rbtucker
 */
public class JmxDlg extends BaseSetupDlg {
    private JmxConnection jmx;
    private JTextField name;
    private JTextField jmxUrl;
    private JTextField username;
    private JPasswordField password;
    
    /**
     * 
     * Creates new form PlatformDlg
     * @param mainFrame
     */
    public JmxDlg(TestCreator mainFrame) {
        this(mainFrame, null);
    }
    /**
     * Creates new form JmxDlg
     * @param mainFrame
     * @param jmx
     */
    public JmxDlg(TestCreator mainFrame,JmxConnection jmx) {
        super(mainFrame);
        this.jmx = jmx;
        if (jmx != null) {
            setTitle("Edit JMX connection " + jmx.getName());
            setEditmode(true);
        } else {
            setTitle("Add new JMX connection");
            this.jmx = JmxConnection.Factory.newInstance();
            this.jmx.setName("new jmx connection");
        }
        
        initComponents();
    }

    private void initComponents() {

        String[] labels = {
            "Name",
            "JMX URL", 
            "User Name",
            "Password"
        };
        
        name = new JTextField(jmx.getName(), 20);
        name.setEditable(!isEditmode());
        
        jmxUrl = new JTextField(jmx.getJmxUrl(), 30);
        username = new JTextField(jmx.getUsername(), 20);
        
        String pass = "";
        if (StringUtils.isNotBlank(jmx.getPassword())) {
            try {
                pass = Utils.decrypt(getMainframe().getEncryptionPassword(), jmx.getPassword());
            }
            
            catch (UnsupportedEncodingException ex) {
                UIUtils.showError(this, "Decrypt Exception", "Password decryption failed");
            }
            
        }
        
        password = new JPasswordField(pass, 20);
        
        JComponent[] components = {name, jmxUrl, username, password};

        
        getContentPane().add(UIUtils.buildEntryPanel(labels, components), BorderLayout.CENTER);
        
        addStandardButtons();
        setDefaultBehavior();
    }
    
    /**
     *
     * @return
     */
    @Override
    protected boolean save() {
        boolean retval = false;
        
        try {
            boolean oktosave = true;
            if (StringUtils.isNotBlank(name.getText()) 
                && StringUtils.isNotBlank(jmxUrl.getText())) {

                if (!isEditmode()) {
                    if (jmxConnectionNameExists()) {
                        oktosave = false;
                        displayExistingNameAlert("JMX Connection", name.getText());
                    }
                }
            } else {
                displayRequiredFieldsMissingAlert("JMX Connection", "name, JMX url"); 
                oktosave = false;
            }

            if (oktosave) {
                if (!isEditmode()) {
                    if (getConfiguration().getJmxConnections() == null) {
                        getConfiguration().addNewJmxConnections();
                    }

                    jmx = getConfiguration().getJmxConnections().addNewJmxConnection();
                }

                jmx.setName(name.getText());
                jmx.setJmxUrl(jmxUrl.getText());

                if (StringUtils.isNotBlank(username.getText())) {
                    jmx.setUsername(username.getText());
                    jmx.setPassword(Utils.encrypt(getMainframe().getEncryptionPassword(), password.getText()));
                } else {
                    jmx.setUsername("");
                    jmx.setPassword("");
                }

                setSaved(true);
                getConfiguration().setModified(true);
                dispose();
                retval = true;
            }
        }
        
        catch (Exception ex) {
            UIUtils.showError(this, "Save Error", "Error occurred while attempting to save JMX connection - " + ex.toString());
        }
        
        return retval;
    }
    
    private boolean jmxConnectionNameExists() {
        boolean retval = false;
        String newname = name.getText();
        
        for (String nm : getJmxConnectionNames()) {
            if (nm.equalsIgnoreCase(newname)) {
                retval = false;
                break;
            }
        }
        
        return retval;
    }

    /**
     *
     * @return
     */
    @Override
    public Object getNewRepositoryObject() {
        return jmx;
    }
    
    /**
     *
     * @return
     */
    @Override
    protected String getDialogName() {
        return "jmx-connection-setup";
    }

        @Override
    public Dimension getPreferredSize() {
        return new Dimension(600, 300);
    }
}
