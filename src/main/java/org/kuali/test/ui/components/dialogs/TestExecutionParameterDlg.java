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
import javax.swing.JLabel;
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
import org.kuali.test.handlers.parameter.ParameterHandler;
import org.kuali.test.handlers.parameter.SaveValueHandler;
import org.kuali.test.ui.base.BaseSetupDlg;
import org.kuali.test.ui.components.labels.DataDisplayLabel;
import org.kuali.test.ui.components.panels.HtmlCheckpointPanel;
import org.kuali.test.ui.components.renderers.ComboBoxTooltipRenderer;
import org.kuali.test.ui.utils.UIUtils;
import org.kuali.test.utils.Constants;
import org.kuali.test.utils.Utils;

/**
 *
 * @author rbtucker
 */
public class TestExecutionParameterDlg extends BaseSetupDlg 
    implements ListSelectionListener, DocumentListener {
    private HtmlCheckpointPanel valuesPanel;
    private JTextField name;
    private JLabel autoReplace;
    private JComboBox parameterHandlers;
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
            "Handler",
            "Auto Replace"
        };

        name = new JTextField(30);
        name.getDocument().addDocumentListener(this);
        
        List <ParameterHandler> handlers = new ArrayList(Utils.PARAMETER_HANDLERS.values());
        Collections.sort(handlers);
        
        parameterHandlers = new JComboBox(handlers.toArray(new ParameterHandler[handlers.size()]));
        
        List <String> tooltips = new ArrayList<String>();
        
        for (ParameterHandler ph: handlers) {
            tooltips.add("<html><div style='width: 300px;'>" + ph.getDescription() + "</div></html>");
        }
        
        parameterHandlers.setRenderer(new ComboBoxTooltipRenderer(tooltips));
        
        parameterHandlers.setSelectedItem(Utils.PARAMETER_HANDLERS.get(SaveValueHandler.class.getName()));
        parameterHandlers.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ParameterHandler ph = (ParameterHandler)parameterHandlers.getSelectedItem();
                autoReplace.setText("" + ph.isAutoReplace());
            }
        });
        
        autoReplace = new DataDisplayLabel("false");
        
        JComponent[] components = new JComponent[]{
            name,
            parameterHandlers,
            autoReplace
        };

        JPanel p = new JPanel(new BorderLayout(1, 1));
        
        p.add(UIUtils.buildEntryPanel(labels, components), BorderLayout.CENTER);
        p.add(new JSeparator(), BorderLayout.SOUTH);
        
        getContentPane().add(p, BorderLayout.NORTH);
        
        getContentPane().add(valuesPanel = new HtmlCheckpointPanel(this, wb, testHeader, true), BorderLayout.CENTER);
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
        CheckpointProperty cp = valuesPanel.getSelectedProperties().get(0);
        cp.setPropertySection(Utils.formatHtmlForComparisonProperty(cp.getPropertySection()));
        testExecutionParameter.setValueProperty(cp);
        testExecutionParameter.setParameterHandler(parameterHandlers.getClass().getName());
        
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
        return ((l != null) && !l.isEmpty() && StringUtils.isNotBlank(name.getText()) && (parameterHandlers.getSelectedItem() != null));
    }

    @Override
    public void insertUpdate(DocumentEvent de) {
        getSaveButton().setEnabled(canSave());
    }

    @Override
    public void removeUpdate(DocumentEvent de) {
        getSaveButton().setEnabled(canSave());
    }

    @Override
    public void changedUpdate(DocumentEvent de) {
        getSaveButton().setEnabled(canSave());
    }
}