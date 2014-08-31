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
import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.test.Checkpoint;
import org.kuali.test.CheckpointProperty;
import org.kuali.test.CheckpointType;
import org.kuali.test.InputParameters;
import org.kuali.test.Parameter;
import org.kuali.test.TestHeader;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.ui.base.BasePanel;
import org.kuali.test.ui.base.BaseSetupDlg;
import org.kuali.test.ui.components.panels.HtmlCheckpointPanel;
import org.kuali.test.ui.utils.UIUtils;
import org.kuali.test.utils.Constants;

/**
 *
 * @author rbtucker
 */
public class HtmlCheckPointDlg extends BaseSetupDlg {

    private static final Logger LOG = Logger.getLogger(HtmlCheckPointDlg.class);
    private final TestHeader testHeader;
    private Checkpoint checkpoint;
    private JTextField name;
    private HtmlCheckpointPanel checkpointPanel;
    private JCheckBox saveScreen;
    /**
     * 
     * @param mainFrame
     * @param testHeader
     * @param webBrowser
     * @param html 
     */
    public HtmlCheckPointDlg(TestCreator mainFrame, TestHeader testHeader, JWebBrowser webBrowser, String html) {
        super(mainFrame);
        this.testHeader = testHeader;

        if (checkpoint != null) {
            setTitle("Edit checkpoint " + checkpoint.getName());
            setEditmode(true);
        } else {
            setTitle("Add new checkpoint");
            checkpoint = Checkpoint.Factory.newInstance();
            checkpoint.setName("new checkpoint");
            checkpoint.setTestName(testHeader.getTestName());
            checkpoint.setType(CheckpointType.HTTP);
        }

        initComponents(webBrowser, html);
    }

    private void initComponents(JWebBrowser webBrowser, String html) {
        String[] labels = new String[]{
            "Checkpoint Name",
            ""
        };

        name = new JTextField(checkpoint.getName(), 30);
        name.setEditable(!isEditmode());

        saveScreen = new JCheckBox("Save screen with checkpoint");
        
        JComponent[] components = new JComponent[]{
            name, saveScreen};

        JPanel p = new BasePanel(getMainframe());

        addStandardButtons();

        p.add(UIUtils.buildEntryPanel(labels, components), BorderLayout.NORTH);

        checkpointPanel = new HtmlCheckpointPanel(this, webBrowser, testHeader, html);
        p.add(checkpointPanel, BorderLayout.CENTER);

        getContentPane().add(p, BorderLayout.CENTER);

        getSaveButton().setEnabled(!checkpointPanel.isEmpty());

        setDefaultBehavior();
        setResizable(true);
    }

    @Override
    protected boolean save() {
        boolean retval = false;
        boolean oktosave = true;
        List <CheckpointProperty> selectedProperties = checkpointPanel.getSelectedProperties();
        if (StringUtils.isNotBlank(name.getText())
            && !selectedProperties.isEmpty()) {
            if (!isEditmode()) {
                if (checkpointNameExists()) {
                    oktosave = false;
                    displayExistingNameAlert("Checkpoint", name.getText());
                }
            }
        } else {
            if (StringUtils.isBlank(name.getText())) {
                displayRequiredFieldsMissingAlert("Checkpoint", "name");
            } else {
                displayRequiredFieldsMissingAlert("Checkpoint", "parameter(s) entry");
            }

            oktosave = false;
        }

        if (oktosave) {
            checkpoint.setName(name.getText());
            InputParameters params = checkpoint.addNewInputParameters();
            Parameter param= params.addNewParameter();
            param.setName(Constants.SAVE_SCREEN);
            param.setValue("" + saveScreen.isSelected());
            
            checkpoint.addNewCheckpointProperties().setCheckpointPropertyArray(selectedProperties.toArray(new CheckpointProperty[selectedProperties.size()]));
            
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
        return new Dimension(800, 400);
    }

    /**
     *
     * @return
     */
    @Override
    protected String getDialogName() {
        return "html-checkpoint-entry";
    }

    @Override
    public boolean isResizable() {
        return true;
    }
}
