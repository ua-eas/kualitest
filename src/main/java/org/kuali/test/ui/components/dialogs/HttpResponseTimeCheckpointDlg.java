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
import org.kuali.test.Checkpoint;
import org.kuali.test.CheckpointProperty;
import org.kuali.test.CheckpointType;
import org.kuali.test.ComparisonOperator;
import org.kuali.test.FailureAction;
import org.kuali.test.TestHeader;
import org.kuali.test.ValueType;
import org.kuali.test.WebService;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.ui.components.editmasks.IntegerTextField;
import org.kuali.test.ui.utils.UIUtils;
import org.kuali.test.utils.Constants;
import org.kuali.test.utils.Utils;

/**
 *
 * @author rbtucker
 */
public class HttpResponseTimeCheckpointDlg extends BaseCheckpointDlg {
    private WebService webService;
    private JTextField name;
    private IntegerTextField maxResponseTime;
    private JComboBox responseTimeFailure;
    private final TestHeader testHeader;
    private Checkpoint checkpoint;

    /**
     * 
     * Creates new form PlatformDlg
     * @param mainFrame
     */
    public HttpResponseTimeCheckpointDlg(TestCreator mainFrame, TestHeader testHeader) {
        super(mainFrame);
        this.testHeader = testHeader;
        initComponents();
    }

    private void initComponents() {

        String[] labels = {
            "Name",
            "Max Response Time(sec)", 
            "On Failure"
        };
        
        name = new JTextField(20);
        maxResponseTime = new IntegerTextField();
        responseTimeFailure = new JComboBox(Utils.getXmlEnumerations(FailureAction.class));
        
        JComponent[] components = {name, maxResponseTime, responseTimeFailure};
        
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
        try {
            boolean oktosave = true;
            if (StringUtils.isBlank(name.getText()) 
                && StringUtils.isBlank(maxResponseTime.getText())) {
                displayRequiredFieldsMissingAlert("Respomse Time", "name, max response time");
                oktosave = false;
            }
            
            if (oktosave) {
                checkpoint = Checkpoint.Factory.newInstance();
                checkpoint.setName(name.getText());
                checkpoint.setType(CheckpointType.HTTP_RESPONSE_TIME);
                CheckpointProperty cp = checkpoint.addNewCheckpointProperties().addNewCheckpointProperty();
                cp.setValueType(ValueType.INT);
                cp.setOnFailure(FailureAction.Enum.forString(responseTimeFailure.getSelectedItem().toString()));

                cp.setDisplayName("Max Response Time (sec)");
                cp.setPropertyName(Constants.MAX_RUNTIME_PROPERTY_NAME);
                cp.setOperator(ComparisonOperator.LESS_THAN_OR_EQUAL);
                cp.setPropertyValue("" + maxResponseTime.getInt());

                setSaved(true);
                dispose();
                retval = true;
            }
        }
        
        catch (Exception ex) {
            UIUtils.showError(this, "Save Error", "Error occurred while attempting to save web service - " + ex.toString());
        }
        
        return retval;
    }
    /**
     *
     * @return
     */
    @Override
    public Object getNewRepositoryObject() {
        return checkpoint;
    }
    
    /**
     *
     * @return
     */
    @Override
    protected String getDialogName() {
        return "http-response-time-checkpoint";
    }

        @Override
    public Dimension getPreferredSize() {
        return new Dimension(400, 200);
    }
}
