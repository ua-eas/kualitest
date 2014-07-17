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
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.apache.commons.lang3.StringUtils;
import org.kuali.test.CheckpointProperty;
import org.kuali.test.TestExecutionParameter;
import org.kuali.test.TestHeader;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.ui.base.BaseSetupDlg;
import org.kuali.test.ui.components.panels.HtmlCheckpointPanel;
import org.kuali.test.ui.utils.UIUtils;
import org.kuali.test.utils.Constants;

/**
 *
 * @author rbtucker
 */
public class TestExecutionParameterDlg extends BaseSetupDlg 
    implements ListSelectionListener, DocumentListener {
    private HtmlCheckpointPanel valuesPanel;
    private JTextField name;
    private TestExecutionParameter testExecutionParameter;

    /**
     * 
     * @param mainframe
     * @param wb
     * @param testHeader
     * @param html 
     */
    public TestExecutionParameterDlg(TestCreator mainframe, JWebBrowser wb, TestHeader testHeader, String html) {
        super(mainframe);
        setTitle("Test Execution Parameter Select");
        initComponents(wb, testHeader, html);
    }

    private void initComponents(JWebBrowser wb, TestHeader testHeader, String html) {
        String[] labels = new String[]{
            "Name",
        };

        name = new JTextField(30);
        name.getDocument().addDocumentListener(this);

        JComponent[] components = new JComponent[]{
            name,
        };

        JPanel p = new JPanel(new BorderLayout(1, 1));
        
        p.add(UIUtils.buildEntryPanel(labels, components), BorderLayout.CENTER);
        p.add(new JSeparator(), BorderLayout.SOUTH);
        
        getContentPane().add(p, BorderLayout.NORTH);
        
        getContentPane().add(valuesPanel = new HtmlCheckpointPanel(getMainframe(), wb, testHeader, html, true), BorderLayout.CENTER);
        valuesPanel.addListSelectionListener(this);
        
        addStandardButtons();
        getSaveButton().setEnabled(false);
        setDefaultBehavior();
    }
    
    /**
     *
     * @return
     */
    @Override
    protected String getSaveText() {
        return Constants.SELECT_ACTION;
    }

    /**
     *
     * @return
     */
    @Override
    protected String getDialogName() {
        return "test-execution-param-value-select";
    }

    /**
     *
     * @return
     */
    @Override
    protected boolean save() {
        testExecutionParameter = TestExecutionParameter.Factory.newInstance();
        testExecutionParameter.setName(name.getText());
        testExecutionParameter.setValueProperty(valuesPanel.getSelectedProperties().get(0));
        setSaved(true);
        dispose();
        return true;
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

    @Override
    public void valueChanged(ListSelectionEvent lse) {
        getSaveButton().setEnabled(canSave());
    }
    
    private boolean canSave() {
        List <CheckpointProperty> l = valuesPanel.getSelectedProperties();
        return ((l != null) && !l.isEmpty() && StringUtils.isNotBlank(name.getText()));
    }

    @Override
    public void insertUpdate(DocumentEvent de) {
    }

    @Override
    public void removeUpdate(DocumentEvent de) {
    }

    @Override
    public void changedUpdate(DocumentEvent de) {
        getSaveButton().setEnabled(canSave());
    }
}