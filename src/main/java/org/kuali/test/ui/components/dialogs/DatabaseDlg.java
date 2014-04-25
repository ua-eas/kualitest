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
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import org.apache.commons.lang3.StringUtils;
import org.kuali.test.DatabaseConnection;
import org.kuali.test.DatabaseType;
import org.kuali.test.ui.KualiTestApp;
import org.kuali.test.ui.base.BaseSetupDlg;
import org.kuali.test.utils.Utils;

/**
 *
 * @author rbtucker
 */
public class DatabaseDlg extends BaseSetupDlg {
    private DatabaseConnection dbconnection;
    private JTextField name;
    private JTextField url;
    private JTextField driver;
    private JTextField username;
    private JPasswordField password;
    private JComboBox <String> type;
    private boolean editmode = false;
    
    /**
     * 
     * Creates new form PlatformDlg
     * @param mainFrame
     */
    public DatabaseDlg(KualiTestApp mainFrame) {
        this(mainFrame, null);
    }
    /**
     * Creates new form PlatformDlg
     * @param mainFrame
     * @param platform
     */
    public DatabaseDlg(KualiTestApp mainFrame, DatabaseConnection dbconnection) {
        super(mainFrame);
        this.dbconnection = dbconnection;
        if (dbconnection != null) {
            setTitle("Edit database connection " + dbconnection.getName());
            setEditmode(true);
        } else {
            setTitle("Add new platform");
            this.dbconnection = DatabaseConnection.Factory.newInstance();
            this.dbconnection.setName("new database connection");
            this.dbconnection.setType(DatabaseType.ORACLE);
        }
        
        initComponents();
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {

        String[] labels = {
            "Name",
            "DB Type",
            "Connection URL", 
            "Driver",
            "User Name",
            "Password"};
        
        name = new JTextField(dbconnection.getName(), 20);
        name.setEditable(!isEditmode());
        
        type = new JComboBox(Utils.getXmlEnumerations(DatabaseType.class));
        type.setSelectedItem(dbconnection.getType());
        url = new JTextField(dbconnection.getJdbcUrl(), 30);
        driver = new JTextField(dbconnection.getJdbcDriver(), 30);
        username = new JTextField(dbconnection.getUsername(), 20);
        password = new JPasswordField(dbconnection.getPassword(), 20);
        
        JComponent[] components = {name, type, url, driver, username, password};

        
        getContentPane().add(buildEntryPanel(labels, components), BorderLayout.CENTER);

        addStandardButtons();
        setDefaultBehavior();
    }
    
    @Override
    protected boolean save() {
        boolean retval = false;
        boolean oktosave = true;
        if (StringUtils.isNotBlank(name.getText()) 
            && StringUtils.isNotBlank(url.getText())
            && StringUtils.isNotBlank(username.getText())
            && StringUtils.isNotBlank(password.getText())
            && StringUtils.isNotBlank(driver.getText())) {
            
            if (!editmode) {
                if (databaseConnectionNameExists()) {
                    oktosave = false;
                    displayExistingNameAlert("Database Connection", name.getText());
                }
            }
        } else {
            displayRequiredFieldsMissingAlert("Database Connection", "name, type, url, driver user name password");
            oktosave = false;
        }
        
        if (oktosave) {
            dbconnection = getConfiguration().getDatabaseConnections().addNewDatabaseConnection();
            dbconnection.setName(name.getText());
            dbconnection.setJdbcUrl(url.getText());
            dbconnection.setJdbcDriver(driver.getText());
            dbconnection.setUsername(username.getText());
            dbconnection.setPassword(password.getText());
            dbconnection.setType(DatabaseType.Enum.forString(type.getSelectedItem().toString()));
            setSaved(true);
            getConfiguration().setModified(true);
            dispose();
            retval = true;
        }
        
        
        return retval;
    }
    
    private boolean databaseConnectionNameExists() {
        boolean retval = false;
        String newname = name.getText();
        
        for (String name : getDatabaseConnectionNames()) {
            if (name.equalsIgnoreCase(newname)) {
                retval = false;
                break;
            }
        }
        
        return retval;
    }

    @Override
    public Object getNewRepositoryObject() {
        return dbconnection;
    }
}
