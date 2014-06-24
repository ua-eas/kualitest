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
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JTextField;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.ui.utils.UIUtils;

/**
 *
 * @author rbtucker
 */
public class NameValueInputDlg extends BaseSetupDlg {
    private static final Logger LOG = Logger.getLogger(NameValueInputDlg.class);
    private JTextField name;
    private JTextField value;
    private String enteredName;
    private String enteredValue;
    
    public NameValueInputDlg(TestCreator mainframe, String nameLabelTxt, String valueLabelTxt) {
        super(mainframe);
        initComponents(nameLabelTxt, valueLabelTxt);
    }

    public NameValueInputDlg(TestCreator mainframe, JDialog dlg, String nameLabelTxt, String valueLabelTxt) {
        super(mainframe, dlg);
        initComponents(nameLabelTxt, valueLabelTxt);
    }

    private void initComponents(String nameLabelTxt, String valueLabelTxt) {
        String[] labels = new String[] {
            nameLabelTxt, 
            valueLabelTxt
        };
        
        name = new JTextField(20);
        value = new JTextField(20);
        
        JComponent[] components = new JComponent[] {
            name,
            value
        };

        getContentPane().add(UIUtils.buildEntryPanel(labels, components), BorderLayout.CENTER);
        
        addStandardButtons();
        setDefaultBehavior();
    }
    
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(450, 150);
    }
    
    /**
     *
     * @return
     */
    public String getEnteredName() {
        return enteredName;
    }

    /**
     *
     * @return
     */
    public String getEnteredValue() {
        return enteredValue;
    }
    
    /**
     *
     * @return
     */
    @Override
    protected boolean save() {
        boolean retval = false;
        boolean oktosave = true;
        if (StringUtils.isBlank(name.getText()) 
            || StringUtils.isBlank(value.getText())) {
            displayRequiredFieldsMissingAlert("Name/Value", "name, value");
            oktosave = false;
        } else {
            enteredName = name.getText();
            enteredValue = value.getText();
        }
        
        if (oktosave) {
            setSaved(true);
            dispose();
            retval = true;
        }
        
        return retval;
    }

    @Override
    protected String getDialogName() {
        return "name-value-input";
    }
}
