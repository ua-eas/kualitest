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
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.test.TestExecutionParameter;
import org.kuali.test.TestOperation;
import org.kuali.test.TestOperationType;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.ui.base.BaseSetupDlg;
import org.kuali.test.ui.base.BaseTable;
import org.kuali.test.ui.base.TableConfiguration;
import org.kuali.test.ui.components.buttons.SearchButton;
import org.kuali.test.ui.components.panels.TablePanel;
import org.kuali.test.ui.components.panels.WebTestPanel;
import org.kuali.test.ui.utils.UIUtils;
import org.kuali.test.utils.Constants;
import org.w3c.dom.Element;

/**
 *
 * @author rbtucker
 */
public class TestExecutionParameterDlg extends BaseSetupDlg {
    public static final Logger LOG = Logger.getLogger(TestExecutionParameterDlg.class);
    
    private JComboBox name;
    private JTextField value;
    private TestExecutionParameter testExecutionParameter;
    private BaseTable parameterTable;
    private WebTestPanel webTestPanel;
    private List <TestExecutionParameter> testExecutionParameters;
    private List <TestExecutionParameter> removedParameters;
    
    /**
     * Creates new form TestExecutionParameterDlg
     * @param mainFrame
     * @param webTestPanel
     * @param testExecutionParameter
     */
    public TestExecutionParameterDlg(TestCreator mainFrame,  WebTestPanel webTestPanel, TestExecutionParameter testExecutionParameter) {
        super(mainFrame);
        this.testExecutionParameter = testExecutionParameter;
        this.webTestPanel = webTestPanel;
        
        if (testExecutionParameter != null) {
            setTitle("Edit test execution attribute");
            setEditmode(true);
        } else {
            setTitle("Add new test execution parameter");
            this.testExecutionParameter = TestExecutionParameter.Factory.newInstance();
            this.testExecutionParameter.setName("new parameter");
        }
        
        initComponents();
    }

    private void initComponents() {
        String[] labels = new String[] {
            "Parameter Name", 
            "Parameter Value"
        };
        
        name = new JComboBox(getTestExecutionParameterNames());
        
        if (isEditmode()) {
            name.setSelectedItem(testExecutionParameter.getName());
        }
        
        name.setEditable(!isEditmode());
        
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 1));
        p.add(value = new JTextField(testExecutionParameter.getValue(), 30));
        value.setEditable(false);
        
        SearchButton b = new SearchButton();
        p.add(new SearchButton());
        
        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showSearch();
            }
        });
        
        
        JComponent[] components = new JComponent[] {
            name,
            p
        };

        p = new JPanel(new BorderLayout(3, 3));
        p.add(UIUtils.buildEntryPanel(labels, components), BorderLayout.NORTH);

        TablePanel tp = new TablePanel(parameterTable = buildParameterTable());
        p.add(tp, BorderLayout.CENTER);
        tp.addDeleteButton(this, Constants.REMOVE_PARAMETER_ACTION, "remove test execution parameter");
        getContentPane().add(p, BorderLayout.CENTER);

        addStandardButtons();
        tp.getDeleteButton().setEnabled(false);
        setDefaultBehavior();
    }
    
    private String[] getTestExecutionParameterNames() {
        return getMainframe().getConfiguration().getTestExecutionParameterNames().getNameArray();
    }
    
    private BaseTable buildParameterTable() {
        TableConfiguration config = new TableConfiguration();
        config.setTableName("test-execution-parameters");
        config.setDisplayName("Parameters");
        
        int[] alignment = new int[3];
        for (int i = 0; i < alignment.length; ++i) {
            if (i == 0) {
                alignment[i] = JLabel.CENTER;
            } else {
                alignment[i] = JLabel.LEFT;
            }
        }
            
        config.setColumnAlignment(alignment);
        
        config.setHeaders(new String[] {
            "Parameter Name",
            "Parameter Value"
        });
        
        config.setPropertyNames(new String[] {
            "name",
            "value",
        });
            
        config.setColumnTypes(new Class[] {
            String.class,
            String.class
        });
        
        config.setColumnWidths(new int[] {
            30,
            30
        });

        
        config.setData(testExecutionParameters = loadTestExecutionParameters());
        
        return new BaseTable(config);
    }
    
    private List <TestExecutionParameter> loadTestExecutionParameters() {
        List <TestExecutionParameter> retval = new ArrayList<TestExecutionParameter>();
        
        for (TestOperation op : webTestPanel.getTestProxyServer().getTestOperations()) {
            if (op.getOperationType().equals(TestOperationType.TEST_EXECUTION_PARAMETER)) {
                retval.add(op.getOperation().getTestExecutionParameter());
            }
        }
        
        return retval;
    }
    
    @Override
    protected boolean save() {
        boolean retval = false;
        boolean oktosave = true;
        String nm = (String)name.getSelectedItem();
        if (StringUtils.isNotBlank(nm) 
            && StringUtils.isNotBlank(value.getText())) {
            
            if (!isEditmode()) {
                if (parameterNameExists()) {
                    oktosave = false;
                    displayExistingNameAlert("Test Execution Parameter", nm);
                }
            }
        } else {
            displayRequiredFieldsMissingAlert("Test Exexcution Parameter", "name, value");
            oktosave = false;
        }
        
        if (oktosave) {
            if (!isEditmode()) {
                testExecutionParameter.setName(nm);
                testExecutionParameter.setValue(value.getText());
            }
            
            getConfiguration().setModified(true);
            setSaved(true);
            dispose();
            retval = true;
        }
        
        return retval;
    }
    
    private boolean parameterNameExists() {
        boolean retval = false;
        String newname = (String)name.getSelectedItem();
        return retval;
    }

    @Override
    public Object getNewRepositoryObject() {
        return testExecutionParameter;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(600, 300);
    }

    @Override
    protected String getDialogName() {
        return "test-execution-parameter";
    }

    public List<TestExecutionParameter> getRemovedParameters() {
        return removedParameters;
    }
    
    private void showSearch() {
       final List <Element> labelNodes = new ArrayList<Element>();
       final Element rootNode = webTestPanel.getHtmlRootNode(labelNodes);

        TestExecutionParamValueSelectDlg dlg 
            = new TestExecutionParamValueSelectDlg(getMainframe(), this, labelNodes, rootNode, webTestPanel.getTestHeader());
        
        if (dlg.isSaved()) {
            List <TestExecutionParameter> l = parameterTable.getTableData();
            
            int row = l.size();
            
            testExecutionParameter = (TestExecutionParameter)dlg.getNewRepositoryObject();
            
            if (testExecutionParameter != null) {
                value.setText(testExecutionParameter.getDisplayName() + " - current screen value=" + testExecutionParameter.getValue());
            }
        }
    }  

    public TestExecutionParameter getTestExecutionParameter() {
        return testExecutionParameter;
    }

    @Override
    protected void handleOtherActions(String actionCommand) {
        if (Constants.REMOVE_PARAMETER_ACTION.equalsIgnoreCase(actionCommand)) {
            int selrow = parameterTable.getSelectedRow();
            TestExecutionParameter param = (TestExecutionParameter)parameterTable.getTableData().get(selrow);

            if (UIUtils.promptForDelete(TestExecutionParameterDlg.this, 
                "Remove Parameter", "Remove test execution parameter '" + param.getName() + "'?")) {
                if (removedParameters == null) {
                    removedParameters = new ArrayList<TestExecutionParameter>();
                }

                removedParameters.add(testExecutionParameters.remove(selrow));
                parameterTable.getTableData().remove(selrow);
                parameterTable.getModel().fireTableRowsDeleted(selrow, selrow);
            }
        }
    }


}
