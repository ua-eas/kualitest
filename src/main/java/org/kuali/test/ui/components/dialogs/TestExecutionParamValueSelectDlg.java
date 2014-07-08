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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.kuali.test.CheckpointProperty;
import org.kuali.test.TestExecutionParameter;
import org.kuali.test.TestHeader;
import org.kuali.test.comparators.HtmlCheckpointTabComparator;
import org.kuali.test.comparators.TestExecutionParameterComparator;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.ui.base.BasePanel;
import org.kuali.test.ui.base.BaseSetupDlg;
import org.kuali.test.ui.base.BaseTable;
import org.kuali.test.ui.base.TableConfiguration;
import org.kuali.test.ui.components.panels.TablePanel;
import org.kuali.test.utils.Constants;
import org.kuali.test.utils.HtmlDomProcessor;
import org.kuali.test.utils.Utils;

/**
 *
 * @author rbtucker
 */
public class TestExecutionParamValueSelectDlg extends BaseSetupDlg  implements ListSelectionListener {
    private TestExecutionParameter testExecutionParameter;
    private TestHeader testHeader;
    private List <BaseTable> parameterTables = new ArrayList<BaseTable>();
    
    /**
     *
     * @param mainframe
     * @param parent
     * @param testHeader
     */
    public TestExecutionParamValueSelectDlg(TestCreator mainframe, 
        JDialog parent, TestHeader testHeader, JWebBrowser wb, String html) {
        super(mainframe, parent);
        setTitle("Test Execution Parameter Select");
        this.testHeader = testHeader;
        initComponents(wb, html);
    }

    private void initComponents(final JWebBrowser wb, final String html) {
        final BasePanel basePanel = new BasePanel(getMainframe());

        HtmlDomProcessor.DomInformation dominfo 
            = HtmlDomProcessor.getInstance().processDom(Utils.findPlatform(getMainframe().getConfiguration(), 
                testHeader.getPlatformName()), wb, html);

        Map<String, List<CheckpointProperty>> pmap = loadPropertiesMap(dominfo.getCheckpointProperties());

        // if we have more than 1 group then we will use tabs
        if (pmap.size() > 1) {
            JTabbedPane tp = new JTabbedPane();
            List<String> tabNames = new ArrayList(pmap.keySet());
            Collections.sort(tabNames, new HtmlCheckpointTabComparator());

            for (String s : tabNames) {
                if (!Constants.DEFAULT_HTML_PROPERTY_GROUP.equals(s)) {
                    List<CheckpointProperty> properties = pmap.get(s);

                    if ((properties != null) && !properties.isEmpty()) {
                        BaseTable t = buildParameterTable(s, properties, true);
                        parameterTables.add(t);

                        tp.addTab(s, new TablePanel(t));
                    }
                }
            }

            basePanel.add(tp, BorderLayout.CENTER);

        } else if (pmap.size() == 1) {
            BaseTable t;
            TablePanel p = new TablePanel(t = buildParameterTable(basePanel.getName(), pmap.get(Constants.DEFAULT_HTML_PROPERTY_GROUP), false));
            basePanel.add(p, BorderLayout.CENTER);
            parameterTables.add(t);
        } else {
            basePanel.add(new JLabel("No test execution parameters found", JLabel.CENTER), BorderLayout.CENTER);
        }
      
        getContentPane().add(basePanel, BorderLayout.CENTER);
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
        for (BaseTable t : parameterTables) {
            int selrow = t.getSelectedRow();
            if (t.getSelectedRow() > -1) {
                List <CheckpointProperty> l = t.getModel().getData();
                
                if ((selrow >= 0) && (selrow < l.size())) {
                    CheckpointProperty property = l.get(selrow);
                    testExecutionParameter = TestExecutionParameter.Factory.newInstance();
                    testExecutionParameter.setDisplayName(property.getDisplayName());
                    testExecutionParameter.setGroup(property.getPropertyGroup());
                    testExecutionParameter.setName(property.getPropertyName());
                    testExecutionParameter.setSection(property.getPropertySection());
                    testExecutionParameter.setSubSection(property.getPropertySubSection());
                    testExecutionParameter.setValue(property.getPropertyValue());
                    
                    setSaved(true);
                    dispose();
                    break;
                }
            }
        }
        
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

    private BaseTable buildParameterTable(String groupName, List<CheckpointProperty> properties, boolean forTab) {
        TableConfiguration config = new TableConfiguration();
        config.setTableName("test-execution-select-parameter-value");
        if (!forTab) {
            config.setDisplayName("Available HTML Objects - " + groupName);
        }

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
        retval.getSelectionModel().addListSelectionListener(this);

        return retval;
    }
    
    private Map<String, List<CheckpointProperty>> loadPropertiesMap(List<CheckpointProperty> testExecutionParameters) {
        Map<String, List<CheckpointProperty>> retval = new HashMap<String, List<CheckpointProperty>>();

        for (CheckpointProperty property : testExecutionParameters) {
            List<CheckpointProperty> l = retval.get(property.getPropertyGroup());

            if (l == null) {
                retval.put(property.getPropertyGroup(), l = new ArrayList<CheckpointProperty>());
            }

            l.add(property);
        }

        TestExecutionParameterComparator c = new TestExecutionParameterComparator();
        for (List<CheckpointProperty> params : retval.values()) {
            Collections.sort(params, c);
        }

        return retval;
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        for (BaseTable t : parameterTables) {
            if (t.getSelectionModel() == e.getSource()) {
                getSaveButton().setEnabled(!t.getSelectionModel().isSelectionEmpty());
            } else {
                t.getSelectionModel().clearSelection();
            }
        }
    }
}
