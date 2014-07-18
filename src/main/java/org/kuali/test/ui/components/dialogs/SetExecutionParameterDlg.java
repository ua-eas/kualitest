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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import org.kuali.test.ParameterReplacement;
import org.kuali.test.TestOperation;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.ui.base.BaseSetupDlg;
import org.kuali.test.ui.utils.UIUtils;

/**
 *
 * @author rbtucker
 */
public class SetExecutionParameterDlg extends BaseSetupDlg {
    private JComboBox availableExecutionParameters;
    private JComboBox availableRequestParameters;
    private List <TestOperation> testOperations;
    private ParameterReplacement parameterReplacement;
    
    /**
     * 
     * @param mainFrame
     * @param testOperations 
     */
    public SetExecutionParameterDlg(TestCreator mainFrame, List <TestOperation> testOperations) {
        super(mainFrame);
        setTitle("Set Test Execution Parameter");
        this.testOperations = testOperations;
        initComponents();
    }

    private void initComponents() {

        String[] labels = {
            "Test Execution Parameters",
            "HTTP Request Parameters", 
        };
        
        availableExecutionParameters = new JComboBox(getAvailableExecutionParameters());
        availableRequestParameters = new JComboBox(getAvailableRequestParameters());
        
        JComponent[] components = {availableExecutionParameters, availableRequestParameters};

        
        getContentPane().add(UIUtils.buildEntryPanel(labels, components), BorderLayout.CENTER);
        
        addStandardButtons();
        setDefaultBehavior();
    }
    
    private String[] getAvailableExecutionParameters() {
        List <String> retval = new ArrayList<String>();
        
        for (TestOperation op : testOperations) {
            if (op.getOperation().getTestExecutionParameter() != null) {
                retval.add(op.getOperation().getTestExecutionParameter().getName());
            }
        }
        
        Collections.sort(retval);
        
        return retval.toArray(new String[retval.size()]);
    }

    private String[] getAvailableRequestParameters() {
        List <String> retval = new ArrayList<String>();
        
        for (TestOperation op : testOperations) {
            if (op.getOperation().getTestExecutionParameter() != null) {
                retval.add(op.getOperation().getTestExecutionParameter().getName());
            }
        }
        
        Collections.sort(retval);
        
        return retval.toArray(new String[retval.size()]);
    }

    /**
     *
     * @return
     */
    @Override
    protected boolean save() {
        boolean retval = false;
        boolean oktosave = true;
        if ((availableExecutionParameters.getSelectedIndex() == -1) 
            && (this.availableRequestParameters.getSelectedIndex() == -1)) {
            displayRequiredFieldsMissingAlert("JSet Execution Parameter", "execution parameter, request parameter"); 
            oktosave = false;
        }
        
        if (oktosave) {
            parameterReplacement = ParameterReplacement.Factory.newInstance();
            parameterReplacement.setTestExecutionParameterName(availableExecutionParameters.getSelectedItem().toString());
            parameterReplacement.setReplaceParameterName(this.availableRequestParameters.getSelectedItem().toString());
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
    public Object getNewRepositoryObject() {
        return parameterReplacement;
    }
    
    /**
     *
     * @return
     */
    @Override
    protected String getDialogName() {
        return "set-test-execution-parameter";
    }

        @Override
    public Dimension getPreferredSize() {
        return new Dimension(500, 200);
    }
}
