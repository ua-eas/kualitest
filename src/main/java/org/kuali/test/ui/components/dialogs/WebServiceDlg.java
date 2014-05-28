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
import javax.swing.JTextArea;
import javax.swing.JTextField;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.kuali.test.WebService;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.ui.base.BaseSetupDlg;

/**
 *
 * @author rbtucker
 */
public class WebServiceDlg extends BaseSetupDlg {
    private WebService webService;
    private JTextField name;
    private JTextField wsdlUrl;
    private JTextArea security;
    private boolean editmode = false;
    
    /**
     * 
     * Creates new form PlatformDlg
     * @param mainFrame
     */
    public WebServiceDlg(TestCreator mainFrame) {
        this(mainFrame, null);
    }
    /**
     * Creates new form WebServiceDlg
     * @param mainFrame
     * @param webService
     */
    public WebServiceDlg(TestCreator mainFrame, WebService webService) {
        super(mainFrame);
        this.webService = webService;
        if (webService != null) {
            setTitle("Edit web service " + webService.getName());
            setEditmode(true);
        } else {
            setTitle("Add new web service");
            this.webService = WebService.Factory.newInstance();
            this.webService.setName("new web service");
        }
        
        initComponents();
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {

        String[] labels = {
            "Name",
            "WSDL URL", 
            "Security Config"
        };
        
        name = new JTextField(webService.getName(), 20);
        name.setEditable(!isEditmode());
        
        wsdlUrl = new JTextField(webService.getWsdlUrl(), 30);
        security = new JTextArea(webService.getSecurity(), 3, 30);
        
        JComponent[] components = {name, wsdlUrl, security};

        
        getContentPane().add(buildEntryPanel(labels, components), BorderLayout.CENTER);

        addStandardButtons();
        setDefaultBehavior();
    }
    
    @Override
    protected boolean save() {
        boolean retval = false;
        boolean oktosave = true;
        if (StringUtils.isNotBlank(name.getText()) 
            && StringUtils.isNotBlank(wsdlUrl.getText())) {
            
            if (!editmode) {
                if (webServiceNameExists()) {
                    oktosave = false;
                    displayExistingNameAlert("Web Service", name.getText());
                }
            }
        } else {
            displayRequiredFieldsMissingAlert("Web Service", "name, wsdl url"); 
            oktosave = false;
        }
        
        if (oktosave) {
            webService = getConfiguration().getWebServices().addNewWebService();
            webService.setName(name.getText());
            webService.setWsdlUrl(wsdlUrl.getText());
            
            if (StringUtils.isNotBlank(security.getText())) {
                webService.setSecurity(StringEscapeUtils.escapeXml11(security.getText()));
            }
            
            setSaved(true);
            getConfiguration().setModified(true);
            dispose();
            retval = true;
        }
        
        
        return retval;
    }
    
    private boolean webServiceNameExists() {
        boolean retval = false;
        String newname = name.getText();
        
        for (String name : getWebServiceNames()) {
            if (name.equalsIgnoreCase(newname)) {
                retval = false;
                break;
            }
        }
        
        return retval;
    }

    @Override
    public Object getNewRepositoryObject() {
        return webService;
    }
    
    @Override
    protected String getDialogName() {
        return "web-service-setup";
    }

        @Override
    public Dimension getPreferredSize() {
        return new Dimension(600, 375);
    }

}
