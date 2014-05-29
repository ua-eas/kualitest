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
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.test.Checkpoint;
import org.kuali.test.CheckpointProperty;
import org.kuali.test.CheckpointType;
import org.kuali.test.ComparisonOperator;
import org.kuali.test.FailureAction;
import org.kuali.test.Parameter;
import org.kuali.test.TestHeader;
import org.kuali.test.ValueType;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.ui.base.BasePanel;
import org.kuali.test.ui.base.BaseSetupDlg;
import org.kuali.test.ui.components.editmasks.IntegerTextField;
import org.kuali.test.ui.components.panels.WebServicePanel;
import org.kuali.test.utils.Constants;
import org.kuali.test.utils.Utils;

/**
 *
 * @author rbtucker
 */
public class WebServiceCheckPointDlg extends BaseSetupDlg {

    private static final Logger LOG = Logger.getLogger(WebServiceCheckPointDlg.class);
    private final TestHeader testHeader;
    private Checkpoint checkpoint;
    private JTextField name;
    private JComboBox wsFailure;
    private IntegerTextField maxRunTime;
    private WebServicePanel wsPanel;

    public WebServiceCheckPointDlg(TestCreator mainFrame, TestHeader testHeader, WebServicePanel wsPanel) {
        super(mainFrame);
        this.testHeader = testHeader;
        this.wsPanel = wsPanel;

        if (checkpoint != null) {
            setTitle("Edit checkpoint " + checkpoint.getName());
            setEditmode(true);
        } else {
            setTitle("Add new checkpoint");
            this.checkpoint = Checkpoint.Factory.newInstance();
            this.checkpoint.setName("new checkpoint");
            this.checkpoint.setTestName(testHeader.getTestName());
            this.checkpoint.setType(CheckpointType.WEB_SERVICE);
        }

        initComponents();
    }

    private void initComponents() {
        if (wsPanel == null) {
            wsPanel = new WebServicePanel(getMainframe(), Utils.findPlatform(getMainframe().getConfiguration(), 
                testHeader.getPlatformName()), testHeader, true);
            getContentPane().add(getCheckpointPanel(), BorderLayout.NORTH);
            getContentPane().add(wsPanel, BorderLayout.CENTER);
        } else {
            getContentPane().add(getCheckpointPanel(), BorderLayout.CENTER);
        }

        addStandardButtons();
        setDefaultBehavior();
    }

    private JPanel getCheckpointPanel() {
        BasePanel retval = new BasePanel(getMainframe());

        String[] labels = new String[]{
            "Checkpoint Name",
            "Max Run Time (sec)",
            "On Failure"
        };

        name = new JTextField(checkpoint.getName(), 20);
        name.setEditable(!isEditmode());
        wsFailure = new JComboBox(Utils.getXmlEnumerations(FailureAction.class));
        maxRunTime = new IntegerTextField();
        
        JComponent[] components = new JComponent[]{
            name,
            maxRunTime,
            wsFailure
        };

        retval.add(buildEntryPanel(labels, components), BorderLayout.CENTER);

        return retval;
    }
    
    @Override
    protected boolean save() {
        boolean retval = false;
        boolean oktosave = true;
        if (StringUtils.isNotBlank(name.getText())) {
            if (!isEditmode()) {
                if (checkpointNameExists()) {
                    oktosave = false;
                    displayExistingNameAlert("Checkpoint", name.getText());
                }
            }
        } else {
            displayRequiredFieldsMissingAlert("Checkpoint", "name");
            oktosave = false;
        }

        if (oktosave) {
            if (!isEditmode()) {
            }

            checkpoint.setName(name.getText());
            checkpoint.addNewInputParameters();

            Parameter param = checkpoint.getInputParameters().addNewParameter();
            param.setName("web-service-operation");
            param.setValue(wsPanel.getWebServiceOperation());

            List <WebServicePanel.WebServiceInputParameter> params = wsPanel.getInputParameters();

            for (WebServicePanel.WebServiceInputParameter inputParam : params) {
                param = checkpoint.getInputParameters().addNewParameter();
                param.setName(inputParam.getParameterName());
                param.setValue(inputParam.getValue());
            
                param = checkpoint.getInputParameters().addNewParameter();
                param.setName(inputParam.getParameterName() + ".type");
                param.setValue(inputParam.getParameterType());
            }
            
            CheckpointProperty cp = checkpoint.addNewCheckpointProperties().addNewCheckpointProperty();
            cp.setValueType(ValueType.STRING);
            cp.setPropertyGroup(Constants.WEB_SERVICE_PROPERTY_GROUP);
            cp.setOnFailure(FailureAction.Enum.forString(wsPanel.getFailureAction()));
            
            cp.setDisplayName("Expected Result");
            cp.setPropertyName("expected-result");
            cp.setPropertyName(wsPanel.getExpectedResult());
            
            cp = checkpoint.getCheckpointProperties().addNewCheckpointProperty();
            
            cp.setValueType(ValueType.INT);
            cp.setPropertyGroup(Constants.WEB_SERVICE_PROPERTY_GROUP);
            cp.setOnFailure(FailureAction.Enum.forString(wsFailure.getSelectedItem().toString()));
            
            cp.setDisplayName("Max Run Time (sec)");
            cp.setPropertyName("max-run-time");
            cp.setOperator(ComparisonOperator.LESS_THAN_OR_EQUAL);
            cp.setPropertyValue("" + maxRunTime.getInt());

            getConfiguration().setModified(true);
            setSaved(true);
            dispose();
            retval = true;
        }

        return retval;
    }

    private boolean checkpointNameExists() {
        boolean retval = false;
        String newname = name.getText();
        return retval;
    }

    @Override
    public Object getNewRepositoryObject() {
        return checkpoint;
    }

    @Override
    public Dimension getPreferredSize() {
        if (!wsPanel.isForCheckpoint()) {
            return new Dimension(500, 300);
        } else {
            return new Dimension(800, 600);
        }
    }

    @Override
    protected String getDialogName() {
        if (!wsPanel.isForCheckpoint()) {
            return "web-service-checkpoint-entry";
        } else {
            return "web-service-checkpoint-entry2";
        }
    }

    @Override
    public boolean isResizable() {
        return wsPanel.isForCheckpoint();
    }
}
