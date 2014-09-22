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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.JButton;
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
import org.kuali.test.ui.components.editmasks.IntegerTextField;
import org.kuali.test.ui.components.panels.WebServicePanel;
import org.kuali.test.ui.utils.UIUtils;
import org.kuali.test.utils.Constants;
import org.kuali.test.utils.Utils;

/**
 *
 * @author rbtucker
 */
public class WebServiceCheckPointDlg extends BaseCheckpointDlg {
    private static final Logger LOG = Logger.getLogger(WebServiceCheckPointDlg.class);
    private final TestHeader testHeader;
    private Checkpoint checkpoint;
    private JTextField name;
    private JComboBox wsFailure;
    private IntegerTextField maxRunTime;
    private WebServicePanel wsPanel;
    private JButton saveAndRun;
    private boolean runWebService = false;

    /**
     *
     * @param mainFrame
     * @param testHeader
     * @param wsPanel
     */
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
            "Comment",
            "Max Run Time (sec)",
            "On Failure"
        };

        name = new JTextField(checkpoint.getName(), 20);
        name.setEditable(!isEditmode());
        
        wsFailure = new JComboBox(Utils.getXmlEnumerations(FailureAction.class));
        maxRunTime = new IntegerTextField();
        
        JComponent[] components = new JComponent[]{
            name,
            createCommentField(),
            maxRunTime,
            wsFailure
        };

        retval.add(UIUtils.buildEntryPanel(labels, components), BorderLayout.CENTER);

        return retval;
    }
    
    /**
     *
     * @return
     */
    @Override
    protected boolean save() {
        boolean retval = false;
        boolean oktosave = true;
        if (StringUtils.isNotBlank(name.getText()) && StringUtils.isNotBlank(wsPanel.getExpectedResult())) {
            if (!isEditmode()) {
                if (checkpointNameExists()) {
                    oktosave = false;
                    displayExistingNameAlert("Checkpoint", name.getText());
                }
            }
        } else {
            displayRequiredFieldsMissingAlert("Checkpoint", "name, result type (void for none)");
            oktosave = false;
        }

        if (oktosave) {
            checkpoint.setName(name.getText());
            checkpoint.setType(CheckpointType.WEB_SERVICE);

            Parameter param = checkpoint.addNewInputParameters().addNewParameter();
            param.setName(Constants.WEB_SERVICE_OPERATION);
            param.setValue(wsPanel.getWebServiceOperation());

            List <WebServicePanel.WebServiceInputParameter> params = wsPanel.getInputParameters();

            for (WebServicePanel.WebServiceInputParameter inputParam : params) {
                param = checkpoint.getInputParameters().addNewParameter();
                param.setName(inputParam.getParameterName());
                param.setValue(inputParam.getValue());
                param.setJavaType(inputParam.getParameterType()); 
            }
            
            CheckpointProperty cp = checkpoint.addNewCheckpointProperties().addNewCheckpointProperty();
            cp.setValueType(ValueType.STRING);
            cp.setPropertyGroup(Constants.WEB_SERVICE_PROPERTY_GROUP);
            cp.setDisplayName("Expected Result");
            cp.setPropertyValue(wsPanel.getExpectedResult());
            cp.setPropertyName(Constants.EXPECTED_RESULT);
            cp.setValueType(wsPanel.getExpectedResultType());
            cp.setOnFailure(FailureAction.Enum.forString(wsPanel.getFailureAction()));
            
            cp = checkpoint.getCheckpointProperties().addNewCheckpointProperty();
            
            if (maxRunTime.getInt() > 0) {
                cp.setValueType(ValueType.INT);
                cp.setPropertyGroup(Constants.WEB_SERVICE_PROPERTY_GROUP);
                cp.setOnFailure(FailureAction.Enum.forString(wsFailure.getSelectedItem().toString()));
                cp = checkpoint.getCheckpointProperties().addNewCheckpointProperty();
                cp.setDisplayName("Max Run Time (sec)");
                cp.setPropertyName(Constants.MAX_RUNTIME_PROPERTY_NAME);
                cp.setOperator(ComparisonOperator.LESS_THAN_OR_EQUAL);
                cp.setPropertyValue("" + maxRunTime.getInt());
            }
            
            getConfiguration().setModified(true);
            setSaved(true);
            dispose();
            retval = true;
        }

        return retval;
    }

    private boolean checkpointNameExists() {
        boolean retval = false;
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

    @Override
    public Dimension getPreferredSize() {
        if (!wsPanel.isForCheckpoint()) {
            return new Dimension(500, 300);
        } else {
            return new Dimension(800, 600);
        }
    }

    /**
     *
     * @return
     */
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

    @Override
    protected void addAdditionalButtons(JPanel p) {
        p.add(saveAndRun = new JButton(Constants.SAVE_AND_RUN_ACTION));
        
        saveAndRun.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                runWebService = true;
                save();
            }
        });
    }

    public boolean isRunWebService() {
        return runWebService;
    }

    public boolean isPoll() {
        return wsPanel.isPoll();
    }
}
