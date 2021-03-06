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

import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.test.CheckpointProperty;
import org.kuali.test.Parameter;
import org.kuali.test.Platform;
import org.kuali.test.TestExecutionParameter;
import org.kuali.test.TestHeader;
import org.kuali.test.TestOperation;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.handlers.parameter.ExecutionContextParameterHandler;
import org.kuali.test.handlers.parameter.ParameterHandler;
import org.kuali.test.handlers.parameter.SelectEditDocumentLookupHandler;
import org.kuali.test.ui.base.BaseSetupDlg;
import org.kuali.test.ui.components.panels.HtmlCheckpointPanel;
import org.kuali.test.ui.components.renderers.ComboBoxTooltipRenderer;
import org.kuali.test.ui.utils.UIUtils;
import org.kuali.test.utils.Constants;
import org.kuali.test.utils.Utils;

/**
 *
 * @author rbtucker
 */
public class TestExecutionParameterDlg extends BaseSetupDlg {

    private static final Logger LOG = Logger.getLogger(TestExecutionParameterDlg.class);
    private HtmlCheckpointPanel valuesPanel;
    private JTextField name;
    private JTextField additionalInformationEntry;
    private JComboBox parameterHandlers;
    private JComboBox additionalInformationSelect;
    private TestExecutionParameter testExecutionParameter;
    private List<TestOperation> testOperations;
    private TestHeader testHeader;

    /**
     * 
     * @param mainframe
     * @param wb
     * @param testOperations
     * @param testHeader 
     */
    public TestExecutionParameterDlg(TestCreator mainframe, 
        JWebBrowser wb, 
        List<TestOperation> testOperations, 
        TestHeader testHeader) {
        super(mainframe);
        setTitle("Test Execution Parameter Select");
        this.testOperations = testOperations;
        this.testHeader = testHeader;
        initComponents(wb, testHeader);
    }

    private void initComponents(JWebBrowser wb, TestHeader testHeader) {
        String[] labels = new String[]{
            "Name",
            "Handler",
            "Existing Parameters",
            "Value"
        };

        name = new JTextField(30);

        getContentPane().add(valuesPanel = new HtmlCheckpointPanel(TestExecutionParameterDlg.this, wb, testHeader, true), BorderLayout.CENTER);
        
        ParameterHandler[] handlers;
        parameterHandlers = new JComboBox(handlers = getParameterHandlers());
        additionalInformationSelect = new JComboBox(); 
        
        parameterHandlers.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                additionalInformationSelect.removeAllItems();
                additionalInformationSelect.setEnabled(false);
                if (isAdditionalInformationHandlerClass()) {
                    List <String> additionalInformation = getAdditionalInformation();
                    if ((additionalInformation != null) && !additionalInformation.isEmpty()) {
                        for (String s : additionalInformation) {
                            additionalInformationSelect.addItem(s);
                        }
                    }
                        
                    additionalInformationSelect.setEnabled(true);
                }
                
                additionalInformationEntry.setText("");
                ParameterHandler ph = (ParameterHandler)parameterHandlers.getSelectedItem();
                additionalInformationEntry.setEnabled(ph.isTextEntryAllowed() || ph.isTextEntryRequired());
            }
        });
        
        List <String> tooltips = new ArrayList<String>();
        
        for (ParameterHandler ph: handlers) {
            tooltips.add("<html><div style='width: 300px;'>" + ph.getDescription() + "</div></html>");
        }
        
        parameterHandlers.setRenderer(new ComboBoxTooltipRenderer(tooltips));
        additionalInformationEntry = new JTextField(20);
        additionalInformationEntry.setEnabled(false);
        
        JComponent[] components = new JComponent[]{
            name,
            parameterHandlers,
            additionalInformationSelect,
            additionalInformationEntry
        };

        JPanel p = new JPanel(new BorderLayout(1, 1));
        
        p.add(UIUtils.buildEntryPanel(labels, components), BorderLayout.CENTER);
        p.add(new JSeparator(), BorderLayout.SOUTH);
        getContentPane().add(p, BorderLayout.NORTH);
        
        addStandardButtons();
        setDefaultBehavior();
    }
    
    
    private List <String> getAdditionalInformation() {
        List <String> retval = new ArrayList<String>();
        
        for (TestOperation top : testOperations) {
            if (top.getOperation().getTestExecutionParameter() != null) {
                retval.add(top.getOperation().getTestExecutionParameter().getName());
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
        return "test-execution-param-value-select";
    }
    
    private boolean isExecutionContextParameterHandler(ParameterHandler p) {
        return ((p != null) && ExecutionContextParameterHandler.class.equals(p.getClass()));
    }
    
    private boolean isSelectEditDocumentLookupHandler(ParameterHandler p) {
        return ((p != null) && SelectEditDocumentLookupHandler.class.equals(p.getClass()));
    }
    

    private boolean isAdditionalInformationHandlerClass() {
        ParameterHandler p = (ParameterHandler)parameterHandlers.getSelectedItem();
        return (isExecutionContextParameterHandler(p)|| isSelectEditDocumentLookupHandler(p));
    }
    
    private boolean isAdditionalInformationRequired() {
        boolean retval = false;
        
        if (isAdditionalInformationHandlerClass()) {
            if (!additionalInformationEntry.isEnabled() || StringUtils.isBlank(additionalInformationEntry.getText())) {
                retval = (!additionalInformationSelect.isEnabled() || (additionalInformationSelect.getSelectedIndex() < 0));
            }
        }
        
        return retval;
    }
    
    private boolean isLookupTableAvailable() {
        boolean retval = false;
        for (CheckpointProperty cp : valuesPanel.getAllCheckpointProperties()) {
            Parameter param = Utils.getCheckpointPropertyTagParameter(cp, Constants.TABLE_ID);
            
            if ((param != null) && Constants.LOOKUP_RESULTS_TABLE_ID.equals(param.getValue())) {
                retval = true;
                break;
            }
        }
        
        return retval;
    }
    
    private boolean isDuplicateName() {
        boolean retval = false;
        for (TestOperation top : testOperations) {
            if (top.getOperation().getTestExecutionParameter() != null) {
                if (top.getOperation().getTestExecutionParameter().getName().equalsIgnoreCase(name.getText())) {
                    retval = true;
                    break;
                }
            }
        }
        
        return retval;
    }

    /**
     *
     * @return
     */
    @Override
    protected boolean save() {
        boolean retval = false;
        
        CheckpointProperty cp = null;
        
        if ((valuesPanel.getSelectedProperties() != null) && !valuesPanel.getSelectedProperties().isEmpty()) {
            cp = valuesPanel.getSelectedProperties().get(0);
        }
        
        if ((cp == null) || StringUtils.isBlank(name.getText())) {
            displayRequiredFieldsMissingAlert("Name/Parameter", "name, parameter selection");
        } else if (isDuplicateName()) {
            this.displayExistingNameAlert("Parameter Name", name.getText());
        } else if (isAdditionalInformationRequired()) {
            displayRequiredFieldsMissingAlert("Existing Parameter", "existing parameter selection");
        } else if (isFreeformEntryRequired()) {
            displayRequiredFieldsMissingAlert("Value", "value entry");
        } else {
            testExecutionParameter = TestExecutionParameter.Factory.newInstance();
            testExecutionParameter.setName(name.getText());
            
            ParameterHandler p = (ParameterHandler)parameterHandlers.getSelectedItem();
            
            if (p.isTextEntryRequired() || (p.isTextEntryAllowed() && StringUtils.isNotBlank(additionalInformationEntry.getText()))) {
                testExecutionParameter.setAdditionalInfo(additionalInformationEntry.getText());
            } else if (isAdditionalInformationHandlerClass()) {
                testExecutionParameter.setAdditionalInfo(additionalInformationSelect.getSelectedItem().toString());
            }
            
            cp.setPropertySection(Utils.formatHtmlForComparisonProperty(cp.getPropertySection()));

            testExecutionParameter.setValueProperty(cp);
            testExecutionParameter.setParameterHandler(p.getClass().getName());

            setSaved(true);
            dispose();
            retval = true;
        }
        
        return retval;
    }

    private boolean isFreeformEntryRequired() {
        ParameterHandler ph = (ParameterHandler)parameterHandlers.getSelectedItem();
        return (ph.isTextEntryRequired() && StringUtils.isBlank(this.additionalInformationEntry.getText()));
    }
    
    @Override
    public boolean isResizable() {
        return true;
    }

    /**
     *
     * @return
     */
    @Override
    public Object getNewRepositoryObject() {
        return testExecutionParameter;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(800, 400);
    }

    public TestExecutionParameter getTestExecutionParameter() {
        return testExecutionParameter;
    }
    
    private ParameterHandler[] getParameterHandlers() {
        List <ParameterHandler> retval = new ArrayList<ParameterHandler>();

        if (getMainframe().getConfiguration().getParameterHandlers() != null) {
            Platform platform = Utils.findPlatform(getMainframe().getConfiguration(), testHeader.getPlatformName());
            for (String className : getMainframe().getConfiguration().getParameterHandlers().getParameterHandlerArray()) {
                try {
                    ParameterHandler p = (ParameterHandler)Class.forName(className).newInstance();
                    if (p.isValidForApplication(platform.getApplication())) {
                        if (p instanceof SelectEditDocumentLookupHandler) {
                            if (isLookupTableAvailable()) {
                                retval.add(p);
                            }
                        } else {
                            retval.add(p);
                        }
                    }
                } 
                
                catch (Exception ex) {
                    LOG.error(ex.toString(), ex);
                } 
            }

            Collections.sort(retval);
        }
        
        return retval.toArray(new ParameterHandler[retval.size()]);
    }
}