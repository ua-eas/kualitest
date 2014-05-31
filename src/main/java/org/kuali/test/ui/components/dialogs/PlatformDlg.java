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
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTextField;
import org.apache.commons.lang3.StringUtils;
import org.kuali.test.KualiApplication;
import org.kuali.test.Platform;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.ui.base.BaseSetupDlg;
import org.kuali.test.ui.utils.UIUtils;
import org.kuali.test.utils.Utils;

/**
 *
 * @author rbtucker
 */
public class PlatformDlg extends BaseSetupDlg {
    private Platform platform;
    private JTextField name;
    private JTextField weburl;
    private JComboBox webService;
    private JTextField jmxurl;
    private JTextField emailAddresses;
    private JTextField version;
    private JComboBox <String> application;
    private JComboBox <String> dbconnection;
    
    /**
     * 
     * Creates new form PlatformDlg
     * @param mainFrame
     */
    public PlatformDlg(TestCreator mainFrame) {
        this(mainFrame, null);
    }
    /**
     * Creates new form PlatformDlg
     * @param mainFrame
     * @param platform
     */
    public PlatformDlg(TestCreator mainFrame, Platform platform) {
        super(mainFrame);
        this.platform = platform;
        if (platform != null) {
            setTitle("Edit platform " + platform.getName());
            setEditmode(true);
        } else {
            setTitle("Add new platform");
            this.platform = Platform.Factory.newInstance();
            this.platform.setName("new platform");
            this.platform.setApplication(KualiApplication.KFS);
        }
        
        initComponents();
    }

    private void initComponents() {
        String[] labels = new String[] {
            "Name", 
            "Application",
            "Version",
            "Web URL",
            "Web Service",
            "JMX URL",
            "Email Addresses",
            "DB Connection"
        };
        
        name = new JTextField(platform.getName(), 20);
        name.setEditable(!isEditmode());
        application = new JComboBox(Utils.getXmlEnumerations(KualiApplication.class));
        application.setSelectedItem(platform.getApplication().toString());
        version = new JTextField(platform.getVersion(), 10);
        weburl = new JTextField(platform.getWebUrl(), 30);
        webService = new JComboBox(getWebServiceNames());

        if (StringUtils.isNotBlank(platform.getWebServiceName())) {
            webService.setSelectedItem(platform.getWebServiceName());
        } else {
            webService.setSelectedIndex(0);
        }
        
        jmxurl = new JTextField(platform.getJmxUrl(), 30);
        emailAddresses = new JTextField(platform.getEmailAddresses(), 30);
        dbconnection = new JComboBox<String>(getDatabaseConnectionNames());
        if (platform.getDatabaseConnectionName() != null) {
            dbconnection.setSelectedItem(platform.getDatabaseConnectionName());
        }

        JComponent[] components = new JComponent[] {
            name,
            application,
            version,
            weburl,
            webService,
            jmxurl,
            emailAddresses,
            dbconnection};

        getContentPane().add(UIUtils.buildEntryPanel(labels, components), BorderLayout.CENTER);

        addStandardButtons();
        setDefaultBehavior();
    }
    
    @Override
    protected boolean save() {
        boolean retval = false;
        boolean oktosave = true;
        if (StringUtils.isNotBlank(name.getText()) 
            && StringUtils.isNotBlank((String)application.getSelectedItem())
            && StringUtils.isNotBlank(version.getText())) {
            
            if (!isEditmode()) {
                if (platformNameExists()) {
                    oktosave = false;
                    displayExistingNameAlert("Platform", name.getText());
                }
            }
        } else {
            displayRequiredFieldsMissingAlert("Platform", "name, application, version");
            oktosave = false;
        }
        
        if (oktosave) {
            if (!isEditmode()) {
                platform = getConfiguration().getPlatforms().addNewPlatform();
                platform.setName(name.getText());
                platform.addNewPlatformTests();
                platform.addNewTestSuites();
            }
            platform.setApplication(KualiApplication.Enum.forString(application.getSelectedItem().toString()));
            platform.setWebServiceName(webService.getSelectedItem().toString());
            platform.setWebUrl(weburl.getText());
            platform.setJmxUrl(jmxurl.getText());
            platform.setVersion(version.getText());

            String s = emailAddresses.getText();
            
            if (StringUtils.isNotBlank(s)) {
                platform.setEmailAddresses(s);
            }

            s = (String)dbconnection.getSelectedItem();
            
            if (StringUtils.isNoneBlank(s)) {
                platform.setDatabaseConnectionName(s);
            }
            
            getConfiguration().setModified(true);
            setSaved(true);
            dispose();
            retval = true;
        }
        
        return retval;
    }
    
    private boolean platformNameExists() {
        boolean retval = false;
        String newname = name.getText();
        
        for (Platform p: getConfiguration().getPlatforms().getPlatformArray()) {
            if (p.getName().equalsIgnoreCase(newname)) {
                retval = false;
                break;
            }
        }
        
        return retval;
    }

    @Override
    public Object getNewRepositoryObject() {
        return platform;
    }
    
    @Override
    protected String getDialogName() {
        return "platform-setup";
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(600, 375);
    }
}
