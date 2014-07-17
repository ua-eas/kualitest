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
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import org.kuali.test.CheckpointProperty;
import org.kuali.test.TestExecutionParameter;
import org.kuali.test.TestHeader;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.ui.base.BaseSetupDlg;
import org.kuali.test.ui.base.BaseTable;
import org.kuali.test.ui.base.TableConfiguration;
import org.kuali.test.ui.components.panels.HtmlCheckpointPanel;
import org.kuali.test.ui.utils.UIUtils;
import org.kuali.test.utils.Constants;

/**
 *
 * @author rbtucker
 */
public class TestExecutionParamValueSelectDlg extends BaseSetupDlg {
    private TestHeader testHeader;
    private HtmlCheckpointPanel valuesPanel;
    private HtmlCheckpointPanel matchPanel;
    private JTextField name;
    private JComboBox parameter;
    private TestExecutionParameter testExecutionParameter;
    private List<TestExecutionParameter> removedExecutionParameters;

    /**
     * 
     * @param mainframe
     * @param wb
     * @param testHeader
     * @param html 
     */
    public TestExecutionParamValueSelectDlg(TestCreator mainframe, JWebBrowser wb, TestHeader testHeader, String html) {
        super(mainframe);
        setTitle("Test Execution Parameter Select");
        this.testHeader = testHeader;
        
        initComponents(wb, testHeader, html);
    }

    private void initComponents(JWebBrowser wb, TestHeader testHeader, String html) {
        String[] labels = new String[]{
            "Name",
            "Parameter"
        };

        name = new JTextField(30);
        
        parameter = new JComboBox(getMainframe().getConfiguration().getTestExecutionParameterNames().getNameArray());

        JComponent[] components = new JComponent[]{
            name,
            parameter
        };

        JPanel p = new JPanel(new BorderLayout(1, 1));
        
        p.add(UIUtils.buildEntryPanel(labels, components), BorderLayout.CENTER);
        p.add(new JSeparator(), BorderLayout.SOUTH);
        
        getContentPane().add(p, BorderLayout.NORTH);
        
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Match Values", matchPanel = new HtmlCheckpointPanel(getMainframe(), wb, testHeader, html));
        tabbedPane.addTab("Replace Value", valuesPanel = new HtmlCheckpointPanel(getMainframe(), wb, testHeader, html));
        tabbedPane.addTab("Current Parameters", new JPanel());

        matchPanel.setBorder(BorderFactory.createLoweredBevelBorder());
        valuesPanel.setBorder(BorderFactory.createLoweredBevelBorder());
        
        
        getContentPane().add(tabbedPane, BorderLayout.CENTER);

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
        return Constants.OK_ACTION;
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
        return (testExecutionParameter != null);
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

    private BaseTable buildCurrentParametersTable(String groupName, List<CheckpointProperty> properties) {
        TableConfiguration config = new TableConfiguration();
        config.setTableName("test-execution-select-parameter-value");

        int[] alignment = new int[3];
        for (int i = 0; i < alignment.length; ++i) {
            alignment[i] = JLabel.LEFT;
        }

        config.setColumnAlignment(alignment);

        config.setHeaders(new String[]{
            "Section",
            "Name",
            "Current Value"
        });

        config.setPropertyNames(new String[]{
            "propertySection",
            "displayName",
            "propertyValue"
        });

        config.setColumnTypes(new Class[]{
            String.class,
            String.class,
            String.class
        });

        config.setColumnWidths(new int[]{
            60,
            100,
            40
        });

        config.setData(properties);
        
        BaseTable retval = new BaseTable(config);
        
        retval.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        retval.setRowSelectionAllowed(true);
        retval.setColumnSelectionAllowed(false);
   //     retval.getSelectionModel().addListSelectionListener(this);

        return retval;
    }

    public TestExecutionParameter getTestExecutionParameter() {
        return testExecutionParameter;
    }

    public List<TestExecutionParameter> getRemovedExecutionParameters() {
        return removedExecutionParameters;
    }
}
