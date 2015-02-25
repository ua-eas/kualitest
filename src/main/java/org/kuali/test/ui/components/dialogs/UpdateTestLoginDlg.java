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

import com.gargoylesoftware.htmlunit.util.NameValuePair;
import java.awt.BorderLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.test.HtmlRequestOperation;
import org.kuali.test.KualiTestDocument;
import org.kuali.test.KualiTestDocument.KualiTest;
import org.kuali.test.RequestParameter;
import org.kuali.test.TestHeader;
import org.kuali.test.TestOperation;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.ui.base.BaseSetupDlg;
import org.kuali.test.ui.utils.UIUtils;
import org.kuali.test.utils.Utils;

/**
 *
 * @author rbtucker
 */
public class UpdateTestLoginDlg extends BaseSetupDlg {
    private static final Logger LOG = Logger.getLogger(UpdateTestLoginDlg.class);
    private TestHeader testHeader;
    private JTextField targetHost;
    private JTextField usernameParameter;
    private JTextField passwordParameter;
    private JTextField newUsername;
    private JPasswordField newPassword;

    /**
     * 
     * @param mainFrame
     * @param testHeader 
     */
    public UpdateTestLoginDlg(TestCreator mainFrame, TestHeader testHeader) {
        super(mainFrame);
        setTitle("Test Login Update");
        this.testHeader = testHeader;
        initComponents();
    }

    private void initComponents() {
        String[] labels = {
            "Target Host",
            "Username Parameter",
            "Password Parameter",
            "New Username", 
            "New Password",
        };
        
        targetHost = new JTextField(20);
        usernameParameter = new JTextField("username", 20);
        passwordParameter = new JTextField("password", 20);
        newUsername = new JTextField(20);
        newPassword = new JPasswordField(20);
        JComponent[] components = {targetHost, usernameParameter, passwordParameter, newUsername, newPassword};
        
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
        boolean oktosave = true;
        String newpass = null;
        
        if (newPassword.getPassword() != null) {
            newpass = new String(newPassword.getPassword());
        }
        
        if (StringUtils.isNotBlank(targetHost.getText()) 
            && StringUtils.isNotBlank(usernameParameter.getText())
            && StringUtils.isNotBlank(passwordParameter.getText())
            && StringUtils.isNotBlank(newpass)
            && StringUtils.isNotBlank(newUsername.getText())) {
        } else {
            displayRequiredFieldsMissingAlert("Test Login Update", "target host, username parameter, password parameter, new username, new password");
            oktosave = false;
        }
        
        if (oktosave) {
            if (updateTestLogin()) {
                setSaved(true);
                getConfiguration().setModified(true);
                dispose();
                retval = true;
            }
        }
        
        
        return retval;
    }
    
    /**
     *
     * @return
     */
    @Override
    protected String getDialogName() {
        return "test-login-update";
    }
    
    private boolean updateTestLogin() {
        boolean retval = false;
        
        try {
            KualiTest test = Utils.findKualiTest(getConfiguration(), testHeader);
            String newpass = new String(newPassword.getPassword());
            boolean testUpdated = false;
            for (TestOperation op : test.getOperations().getOperationArray()) {
                HtmlRequestOperation hop = op.getOperation().getHtmlRequestOperation();
                if (hop != null) {
                    if (Utils.getHostFromUrl(hop.getUrl(), false).equalsIgnoreCase(targetHost.getText())) {
                        List <NameValuePair> nvplist = null;
                        if (Utils.isMultipart(hop)) {
                            nvplist = Utils.getNameValuePairsFromMultipartParams(Utils.getContentParameterFromRequestOperation(hop));
                        } else {
                            nvplist = Utils.getNameValuePairsFromUrlEncodedParams(Utils.getContentParameterFromRequestOperation(hop));
                        }
                        
                        if (nvplist != null) {
                            NameValuePair username = null;
                            NameValuePair password = null;
                            String epass = Utils.getEncryptionPassword(getConfiguration());
                            List <NameValuePair> newnvplist = new ArrayList<NameValuePair>();
                            for (NameValuePair nvp : nvplist) {
                                if (nvp.getName().equalsIgnoreCase(usernameParameter.getText())) {
                                    username = new NameValuePair(usernameParameter.getText(), newUsername.getText());
                                    newnvplist.add(username);
                                } else if (nvp.getName().equalsIgnoreCase(passwordParameter.getText())) {
                                    String ep = Utils.encrypt(epass, newpass);
                                    password = new NameValuePair(passwordParameter.getText(), ep);
                                    newnvplist.add(password);
                                } else {
                                    newnvplist.add(nvp);
                                }
                            }
                            
                            if ((username != null) && (password != null)) {
                                testUpdated = true;
                                RequestParameter rp = Utils.getContentParameter(hop);
                                
                                if (Utils.isMultipart(hop)) {
                                    rp.setValue(Utils.buildMultipartParameterString(newnvplist));
                                } else {
                                    rp.setValue(Utils.buildUrlEncodedParameterString(newnvplist));
                                }
                            }
                        }
                    }
                }
            }
            
            if (testUpdated) {
                KualiTestDocument doc = KualiTestDocument.Factory.newInstance();
                doc.setKualiTest(test);
                doc.save(new File(Utils.getTestFilePath(getConfiguration(), testHeader)), Utils.getSaveXmlOptions());
            }
            
            retval = true;
        }

        catch (Exception ex) {
            UIUtils.showError(this, "Update Test Login Error", "Error occured while attempting to update test login - " + ex.toString());
        }
        
        
        return retval;
    }
}
