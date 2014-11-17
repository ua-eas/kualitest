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
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import javax.swing.JTextField;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.test.EmailSetup;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.ui.base.BaseSetupDlg;
import org.kuali.test.ui.utils.UIUtils;
import org.kuali.test.utils.Constants;

/**
 *
 * @author rbtucker
 */
public class EmailDlg extends BaseSetupDlg {
    private static final Logger LOG = Logger.getLogger(EmailDlg.class);
    
    private EmailSetup emailSetup;
    private JTextField mailHost;
    private JTextField fromAddress;
    private JTextField subject;
    private JTextField toAddresses;
    private JTextField localRunAddress;
    
    /**
     * 
     * Creates new form PlatformDlg
     * @param mainFrame
     */
    public EmailDlg(TestCreator mainFrame) {
        this(mainFrame, null);
    }
    /**
     * Creates new EmailDlg
     * @param mainFrame
     * @param emailSetup
     */
    public EmailDlg(TestCreator mainFrame, EmailSetup emailSetup) {
        super(mainFrame);
        setTitle("Application Email Setup");
        this.emailSetup = emailSetup;
        initComponents();
    }

    private void initComponents() {

        String[] labels = {
            "Mail Host",
            "Subject",
            "From Address", 
            "To Addresses",
            "Local To Address"
        };
        
        mailHost = new JTextField(emailSetup.getMailHost(), 20);
        subject = new JTextField(emailSetup.getSubject(), 30);
        fromAddress = new JTextField(emailSetup.getFromAddress(), 20);
        toAddresses = new JTextField(emailSetup.getToAddresses(), 40);
        localRunAddress = new JTextField(getMainframe().getLocalRunEmailAddress(), 40);
        JComponent[] components = {mailHost, subject, fromAddress, toAddresses, localRunAddress};
        
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
            
            Preferences proot = Preferences.userRoot();
            Preferences node = proot.node(Constants.PREFS_ROOT_NODE);
            node.put(Constants.LOCAL_RUN_EMAIL, localRunAddress.getText());
            
            try {
                node.flush();
            } catch (BackingStoreException ex) {
                LOG.warn(ex.toString(), ex);
            }
            
            setSaved(true);
            getConfiguration().setModified(true);
            dispose();
            retval = true;
        }
        
        
        return retval;
    }
    
    /**
     *
     * @return
     */
    @Override
    protected String getDialogName() {
        return "email-setup";
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(700, 300);
    }
}
