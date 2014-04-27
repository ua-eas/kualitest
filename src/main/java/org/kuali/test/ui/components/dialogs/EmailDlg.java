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
import javax.swing.JComponent;
import javax.swing.JTextField;
import org.apache.commons.lang3.StringUtils;
import org.kuali.test.EmailSetup;
import org.kuali.test.KualiTestApp;
import org.kuali.test.ui.base.BaseSetupDlg;

/**
 *
 * @author rbtucker
 */
public class EmailDlg extends BaseSetupDlg {
    private EmailSetup emailSetup;
    private JTextField mailHost;
    private JTextField fromAddress;
    private JTextField subject;
    private JTextField toAddresses;
    
    /**
     * 
     * Creates new form PlatformDlg
     * @param mainFrame
     */
    public EmailDlg(KualiTestApp mainFrame) {
        this(mainFrame, null);
    }
    /**
     * Creates new form PlatformDlg
     * @param mainFrame
     * @param platform
     */
    public EmailDlg(KualiTestApp mainFrame, EmailSetup emailSetup) {
        super(mainFrame);
        this.emailSetup = emailSetup;
        initComponents();
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {

        String[] labels = {
            "Mail Host",
            "Subject",
            "From Address", 
            "To Addresses"};
        
        mailHost = new JTextField(emailSetup.getMailHost(), 20);
        subject = new JTextField(emailSetup.getSubject(), 30);
        fromAddress = new JTextField(emailSetup.getFromAddress(), 20);
        toAddresses = new JTextField(emailSetup.getToAddresses(), 40);
        
        JComponent[] components = {mailHost, subject, fromAddress, toAddresses};

        
        getContentPane().add(buildEntryPanel(labels, components), BorderLayout.CENTER);

        addStandardButtons();
        setDefaultBehavior();
    }
    
    @Override
    protected boolean save() {
        boolean retval = false;
        boolean oktosave = true;
        if (StringUtils.isNotBlank(mailHost.getText()) 
            && StringUtils.isNotBlank(subject.getText())
            && StringUtils.isNotBlank(fromAddress.getText())
            && StringUtils.isNotBlank(toAddresses.getText())) {
        } else {
            displayRequiredFieldsMissingAlert("Email Setup", "mail host, subject, from address, to addreses");
            oktosave = false;
        }
        
        if (oktosave) {
            emailSetup.setMailHost(mailHost.getText());
            emailSetup.setSubject(subject.getText());
            emailSetup.setFromAddress(fromAddress.getText());
            emailSetup.setToAddresses(toAddresses.getText());
            setSaved(true);
            getConfiguration().setModified(true);
            dispose();
            retval = true;
        }
        
        
        return retval;
    }
    
    @Override
    protected String getDialogName() {
        return "emailsetup";
    }

}
