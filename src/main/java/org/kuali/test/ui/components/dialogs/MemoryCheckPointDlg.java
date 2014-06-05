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
import org.apache.log4j.Logger;
import org.kuali.test.Checkpoint;
import org.kuali.test.CheckpointProperty;
import org.kuali.test.CheckpointType;
import org.kuali.test.ComparisonOperator;
import org.kuali.test.FailureAction;
import org.kuali.test.TestHeader;
import org.kuali.test.ValueType;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.ui.base.BasePanel;
import org.kuali.test.ui.base.BaseSetupDlg;
import org.kuali.test.ui.components.editmasks.IntegerTextField;
import org.kuali.test.ui.utils.UIUtils;
import org.kuali.test.utils.Constants;
import org.kuali.test.utils.Utils;

/**
 *
 * @author rbtucker
 */
public class MemoryCheckPointDlg extends BaseSetupDlg {

    private static final Logger LOG = Logger.getLogger(MemoryCheckPointDlg.class);
    private final TestHeader testHeader;
    private Checkpoint checkpoint;
    private JTextField name;
    private IntegerTextField maxMemoryPercent;
    private JComboBox maxMemoryFailure;

    public MemoryCheckPointDlg(TestCreator mainFrame, TestHeader testHeader) {
        super(mainFrame);
        this.testHeader = testHeader;

        if (checkpoint != null) {
            setTitle("Edit checkpoint " + checkpoint.getName());
            setEditmode(true);
        } else {
            setTitle("Add new checkpoint");
            this.checkpoint = Checkpoint.Factory.newInstance();
            this.checkpoint.setName("new checkpoint");
            this.checkpoint.setTestName(testHeader.getTestName());
        }

        initComponents();
    }

    private void initComponents() {
        String[] labels = new String[]{
            "Checkpoint Name",
            "Max Memory Usage (percent)",
            "On Max Memory Failure"
        };

        name = new JTextField(checkpoint.getName(), 20);
        name.setEditable(!isEditmode());
        maxMemoryPercent = new IntegerTextField();
        maxMemoryFailure = new JComboBox(Utils.getXmlEnumerations(FailureAction.class));
        
        JComponent[] components = new JComponent[]{
            name,
            maxMemoryPercent,
            maxMemoryFailure
        };

        BasePanel p = new BasePanel(getMainframe());
        p.add(UIUtils.buildEntryPanel(labels, components), BorderLayout.CENTER);
        getContentPane().add(p, BorderLayout.CENTER);

        addStandardButtons();
        setDefaultBehavior();
    }

    @Override
    protected boolean save() {
        boolean retval = false;
        boolean oktosave = true;
        if (StringUtils.isNotBlank(name.getText()) 
            && StringUtils.isNotBlank(maxMemoryPercent.getText())) {
            if (!isEditmode()) {
                if (checkpointNameExists()) {
                    oktosave = false;
                    displayExistingNameAlert("Checkpoint", name.getText());
                }
            }
        } else {
            displayRequiredFieldsMissingAlert("Checkpoint", "name, max memory usage");
            oktosave = false;
        }

        if (oktosave) {
            if (!isEditmode()) {
            }

            checkpoint.setName(name.getText());
            checkpoint.setType(CheckpointType.MEMORY);
            CheckpointProperty cp = checkpoint.addNewCheckpointProperties().addNewCheckpointProperty();
            cp.setValueType(ValueType.INT);
            cp.setPropertyGroup(Constants.SYSTEM_PROPERTY_GROUP);
            cp.setOnFailure(FailureAction.Enum.forString(maxMemoryFailure.getSelectedItem().toString()));
            cp.setOperator(ComparisonOperator.LESS_THAN_OR_EQUAL);
            cp.setDisplayName("Max Memory Usage (percent)");
            cp.setPropertyName(Constants.MAX_MEMORY_PERCENT);
            cp.setPropertyValue(maxMemoryPercent.getText());
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
        return new Dimension(500, 200);
    }

    @Override
    protected String getDialogName() {
        return "memory-checkpoint-entry";
    }
}
