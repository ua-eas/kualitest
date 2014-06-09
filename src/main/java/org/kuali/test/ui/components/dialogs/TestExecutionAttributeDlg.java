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
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.apache.commons.lang3.StringUtils;
import org.kuali.test.TestExecutionAttribute;
import org.kuali.test.TestHeader;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.ui.base.BaseSetupDlg;
import org.kuali.test.ui.base.BaseTable;
import org.kuali.test.ui.base.TableConfiguration;
import org.kuali.test.ui.components.buttons.TableCellIconButton;
import org.kuali.test.ui.components.panels.TablePanel;
import org.kuali.test.ui.utils.UIUtils;
import org.kuali.test.utils.Constants;

/**
 *
 * @author rbtucker
 */
public class TestExecutionAttributeDlg extends BaseSetupDlg {
    private JTextField name;
    private JTextField value;
    private TestExecutionAttribute testExecutionAttribute;
    private BaseTable attributeTable;
    private List <TestExecutionAttribute> testExecutionAttributes;
    
    /**
     * Creates new form TestExecutionAttributeDlg
     * @param mainFrame
     * @param testExecutionAttributes
     * @param testExecutionAttribute
     */
    public TestExecutionAttributeDlg(TestCreator mainFrame,  
        List <TestExecutionAttribute> testExecutionAttributes, TestExecutionAttribute testExecutionAttribute) {
        super(mainFrame);
        this.testExecutionAttributes = testExecutionAttributes;
        
        if (testExecutionAttribute != null) {
            setTitle("Edit test execution attribute");
            setEditmode(true);
        } else {
            setTitle("Add new test execution attribute");
            this.testExecutionAttribute = TestExecutionAttribute.Factory.newInstance();
            this.testExecutionAttribute.setName("new attribute");
        }
        
        initComponents();
    }

    private void initComponents() {
        String[] labels = new String[] {
            "Name", 
            "Value"
        };
        
        name = new JTextField(testExecutionAttribute.getName(), 20);
        name.setEditable(!isEditmode());
        
        value = new JTextField(testExecutionAttribute.getValue(), 30);
        JComponent[] components = new JComponent[] {
            name,
            value
        };

        JPanel p = new JPanel(new BorderLayout(3, 3));
        p.add(UIUtils.buildEntryPanel(labels, components), BorderLayout.NORTH);

        p.add(new TablePanel(attributeTable = buildAttributeTable()), BorderLayout.CENTER);
        
        getContentPane().add(p, BorderLayout.CENTER);

        addStandardButtons();
        setDefaultBehavior();
    }
    
    private BaseTable buildAttributeTable() {
        TableConfiguration config = new TableConfiguration();
        config.setTableName("test-execution-attributes");
        config.setDisplayName("Attributes");
        
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
            "Remove",
            "Attribute Name",
            "Attribute Value"
        });
        
        config.setPropertyNames(new String[] {
            Constants.IGNORE_TABLE_DATA_INDICATOR,
            "name",
            "value",
        });
            
        config.setColumnTypes(new Class[] {
            String.class,
            String.class,
            String.class
        });
        
        config.setColumnWidths(new int[] {
            15,
            30,
            30
        });

        
        config.setData(testExecutionAttributes);
        
        BaseTable retval = new BaseTable(config) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return (column == 0);
            }
        };
        
        TableCellIconButton b = new TableCellIconButton(Constants.DELETE_ICON);
        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                TableCellIconButton b = (TableCellIconButton)e.getSource();
                List <TestHeader> l = attributeTable.getTableData();
                if ((b.getCurrentRow() > -1) && (l.size() > b.getCurrentRow())) {
                    TestExecutionAttribute att = (TestExecutionAttribute)attributeTable.getTableData().get(b.getCurrentRow());
                    
                    if (UIUtils.promptForDelete(TestExecutionAttributeDlg.this, 
                        "Delete Attribute", "Delete test execution attribute '" + att.getName() + "'?")) {
                        // do delete here
                    }
                }
            }
        });
        
        retval.getColumnModel().getColumn(0).setCellRenderer(b);
        retval.getColumnModel().getColumn(0).setCellEditor(b);

        return retval;
    }
    
    @Override
    protected boolean save() {
        boolean retval = false;
        boolean oktosave = true;
        if (StringUtils.isNotBlank(name.getText()) 
            && StringUtils.isNotBlank(value.getText())) {
            
            if (!isEditmode()) {
                if (attributeNameExists()) {
                    oktosave = false;
                    displayExistingNameAlert("Test Execution Attribute", name.getText());
                }
            }
        } else {
            displayRequiredFieldsMissingAlert("Test Exexcution Attribute", "name, value");
            oktosave = false;
        }
        
        if (oktosave) {
            if (!isEditmode()) {
                testExecutionAttribute.setName(name.getText());
                testExecutionAttribute.setValue(value.getText());
            }
            
            getConfiguration().setModified(true);
            setSaved(true);
            dispose();
            retval = true;
        }
        
        return retval;
    }
    
    private boolean attributeNameExists() {
        boolean retval = false;
        String newname = name.getText();
        return retval;
    }

    @Override
    public Object getNewRepositoryObject() {
        return testExecutionAttribute;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(600, 300);
    }

    @Override
    protected String getDialogName() {
        return "test-execution-attribute-setup";
    }
}
