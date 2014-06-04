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
import javax.swing.JComponent;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import org.apache.commons.lang3.StringUtils;
import org.kuali.test.JmxConnection;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.ui.base.BaseSetupDlg;
import org.kuali.test.ui.utils.UIUtils;

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
    private boolean editmode = false;
    
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
        password = new JPasswordField(jmx.getPassword(), 20);
        
        JComponent[] components = {name, jmxUrl, username, password};

        
        getContentPane().add(UIUtils.buildEntryPanel(labels, components), BorderLayout.CENTER);
        
        addStandardButtons();
        setDefaultBehavior();
    }
    
    @Override
    protected boolean save() {
        boolean retval = false;
        boolean oktosave = true;
        if (StringUtils.isNotBlank(name.getText()) 
            && StringUtils.isNotBlank(jmxUrl.getText())) {
            
            if (!editmode) {
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
            if (!editmode) {
                if (getConfiguration().getJmxConnections() == null) {
                    getConfiguration().addNewJmxConnections();
                }
                
                jmx = getConfiguration().getJmxConnections().addNewJmxConnection();
            }
        
            jmx.setName(name.getText());
            jmx.setJmxUrl(jmxUrl.getText());

            if (StringUtils.isNotBlank(username.getText())) {
                jmx.setUsername(username.getText());
                jmx.setPassword(password.getText());
            } else {
                jmx.setUsername("");
                jmx.setPassword("");
            }
            
            setSaved(true);
            getConfiguration().setModified(true);
            dispose();
            retval = true;
        }
        
        
        return retval;
    }
    
    private boolean jmxConnectionNameExists() {
        boolean retval = false;
        String newname = name.getText();
        
        for (String name : getJmxConnectionNames()) {
            if (name.equalsIgnoreCase(newname)) {
                retval = false;
                break;
            }
        }
        
        return retval;
    }

    @Override
    public Object getNewRepositoryObject() {
        return jmx;
    }
    
    @Override
    protected String getDialogName() {
        return "jmx-connection-setup";
    }

        @Override
    public Dimension getPreferredSize() {
        return new Dimension(600, 300);
    }
}
