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
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.test.CheckpointProperty;
import org.kuali.test.Parameter;
import org.kuali.test.TestExecutionParameter;
import org.kuali.test.TestHeader;
import org.kuali.test.TestOperation;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.handlers.parameter.ExecutionContextParameterHandler;
import org.kuali.test.handlers.parameter.ParameterHandler;
import org.kuali.test.handlers.parameter.RandomListSelectionHandler;
import org.kuali.test.handlers.parameter.SaveValueHandler;
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
    private JComboBox parameterHandlers;
    private JComboBox existingParameters;
    private TestExecutionParameter testExecutionParameter;
    private Set<String> randomListSelectParameterToIgnore = new HashSet<String>();
    private List<TestOperation> testOperations;
    /**
     * 
     * @param mainframe
     * @param wb
     * @param testHeader
     * @param html 
     */
    public TestExecutionParameterDlg(TestCreator mainframe, JWebBrowser wb, List<TestOperation> testOperations, TestHeader testHeader, String html) {
        super(mainframe);
        setTitle("Test Execution Parameter Select");
        this.testOperations = testOperations;
        
        if (getConfiguration().getRandomListAccessParametersToIgnore() != null) {
            for (String s : getConfiguration().getRandomListAccessParametersToIgnore().getParameterNameArray()) {
                randomListSelectParameterToIgnore.add(s);
            }
        }
        
        initComponents(wb, testHeader, html);
    }

    private void initComponents(final JWebBrowser wb, final TestHeader testHeader, String html) {
        String[] labels = new String[]{
            "Name",
            "Handler",
            "Existing Parameter"
        };

        name = new JTextField(30);
        
        List <ParameterHandler> handlers = new ArrayList(Utils.PARAMETER_HANDLERS.values());
        Collections.sort(handlers);
        
        parameterHandlers = new JComboBox(handlers.toArray(new ParameterHandler[handlers.size()]));
        existingParameters = new JComboBox();
        
        parameterHandlers.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                existingParameters.removeAllItems();
                existingParameters.setEnabled(false);
                if (parameterHandlers.getSelectedItem().getClass().equals(ExecutionContextParameterHandler.class)) {
                    List <String> existingParameterNames = getExistingParameterNames();
                    if ((existingParameterNames != null) && !existingParameterNames.isEmpty()) {
                        for (String s : existingParameterNames) {
                            existingParameters.addItem(s);
                        }
                        
                        existingParameters.setEnabled(true);
                    }
                }
            }
        });
        
        List <String> tooltips = new ArrayList<String>();
        
        for (ParameterHandler ph: handlers) {
            tooltips.add("<html><div style='width: 300px;'>" + ph.getDescription() + "</div></html>");
        }
        
        parameterHandlers.setRenderer(new ComboBoxTooltipRenderer(tooltips));
        
        parameterHandlers.setSelectedItem(Utils.PARAMETER_HANDLERS.get(SaveValueHandler.class.getName()));
        
        JComponent[] components = new JComponent[]{
            name,
            parameterHandlers,
            existingParameters
        };

        JPanel p = new JPanel(new BorderLayout(1, 1));
        
        p.add(UIUtils.buildEntryPanel(labels, components), BorderLayout.CENTER);
        p.add(new JSeparator(), BorderLayout.SOUTH);
        getContentPane().add(p, BorderLayout.NORTH);

        getContentPane().add(valuesPanel = new HtmlCheckpointPanel(TestExecutionParameterDlg.this, wb, testHeader, true), BorderLayout.CENTER);
        
        addStandardButtons();
        setDefaultBehavior();
    }
    
    
    private List <String> getExistingParameterNames() {
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

    private boolean isExistingParameterNameRequired() {
        return (parameterHandlers.getSelectedItem().getClass().equals(ExecutionContextParameterHandler.class)) 
            && (!existingParameters.isEnabled() || (existingParameters.getSelectedIndex() < 0));
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
        } else if (isExistingParameterNameRequired()) {
            displayRequiredFieldsMissingAlert("Existing Parameter", "existing parameter selection");
        }else {
            testExecutionParameter = TestExecutionParameter.Factory.newInstance();
            testExecutionParameter.setName(name.getText());
            
            if (parameterHandlers.getSelectedItem().getClass().equals(ExecutionContextParameterHandler.class)) {
                testExecutionParameter.setAdditionalInfo(existingParameters.getSelectedItem().toString());
            }

            cp.setPropertySection(Utils.formatHtmlForComparisonProperty(cp.getPropertySection()));

            if (parameterHandlers.getSelectedItem().getClass().equals(RandomListSelectionHandler.class)) {
                Parameter param = Utils.getCheckpointPropertyTagParameter(cp, Constants.ANCHOR_PARAMETERS);

                if ((param != null) && StringUtils.isNotBlank(param.getValue())) {
                    try {
                        List <NameValuePair> l = Utils.getNameValuePairsFromUrlEncodedParams(param.getValue());
                        if (l != null) {
                            for (NameValuePair nvp : l) {
                                if (!randomListSelectParameterToIgnore.contains(nvp.getName())) {
                                    cp.setPropertyValue(nvp.getValue());
                                    break;
                                }
                            }
                        }
                    } 

                    catch (UnsupportedEncodingException ex) {
                        LOG.error(ex.toString(), ex);
                    }
                }
            }

            testExecutionParameter.setValueProperty(cp);
            testExecutionParameter.setParameterHandler(parameterHandlers.getSelectedItem().getClass().getName());

            setSaved(true);
            dispose();
            retval = true;
        }
        
        return retval;
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
}