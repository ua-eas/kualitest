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
import java.util.Arrays;
import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.apache.commons.lang3.StringUtils;
import org.kuali.test.AutoReplaceParameter;
import org.kuali.test.TagAttribute;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.ui.base.BaseSetupDlg;
import org.kuali.test.ui.base.BaseTable;
import org.kuali.test.ui.base.NameValueInputDlg;
import org.kuali.test.ui.base.TableConfiguration;
import org.kuali.test.ui.components.panels.TablePanel;
import org.kuali.test.ui.utils.UIUtils;
import org.kuali.test.utils.Constants;

/**
 *
 * @author rbtucker
 */
public class AutoReplaceParameterDlg extends BaseSetupDlg {
    private JTextField parameterName;
    private JTextField tagName;
    private JCheckBox fromInputParameter;
    private BaseTable attributesTable;
    private AutoReplaceParameter parameter;
    private TablePanel tp;
    
    public AutoReplaceParameterDlg(TestCreator mainframe, JDialog parent) {
        super(mainframe, parent);
        setTitle("Add new auto replace parameter");
        parameter = AutoReplaceParameter.Factory.newInstance();
        initComponents();
    }

    private void initComponents() {
        String[] labels = new String[] {
            "Parameter Name", 
            "Tag/Parameter",
            ""
        };
        
        parameterName = new JTextField(parameter.getParameterName(), 20);
        tagName = new JTextField(parameter.getTagName(), 20);
        fromInputParameter = new JCheckBox();
        fromInputParameter.setSelected(parameter.getFromInputParameter());
        fromInputParameter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (fromInputParameter.isSelected()) {
                    List l = attributesTable.getModel().getData();
                    if (!l.isEmpty()) {
                        int rows = l.size();
                        l.clear();
                        attributesTable.getModel().fireTableRowsDeleted(0, rows-1);
                    }
                }
                
                tp.getAddButton().setEnabled(!fromInputParameter.isSelected());
            }
        });
        
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 1));
        p.add(fromInputParameter);
        p.add(new JLabel("From Input Parameter"));
        
        JComponent[] components = new JComponent[] {
            parameterName,
            tagName,
            p
        };

        p = new JPanel(new BorderLayout(3, 3));
        p.add(UIUtils.buildEntryPanel(labels, components), BorderLayout.NORTH);
        tp = new TablePanel(attributesTable = buildAttributesTable());
        
        tp.addAddButton(this, Constants.ADD_ACTION, "add attribute");
        tp.addDeleteButton(this, Constants.DELETE_ACTION, "delete attribute");
        
        p.add(tp, BorderLayout.CENTER);
        getContentPane().add(p, BorderLayout.CENTER);

        addStandardButtons();
        setDefaultBehavior();
    }
    
    private BaseTable buildAttributesTable() {
        TableConfiguration config = new TableConfiguration();
        config.setTableName("tag-attribues");
        config.setDisplayName("Tag Attributes");
        
        int[] alignment = new int[2];
        for (int i = 0; i < alignment.length; ++i) {
            alignment[i] = JLabel.LEFT;
        }
            
        config.setColumnAlignment(alignment);
        
        config.setHeaders(new String[] {
            "Name",
            "Value"
        });
        
        config.setPropertyNames(new String[] {
            "name",
            "value"
        });
            
        config.setColumnTypes(new Class[] {
            String.class,
            String.class
        });
        
        config.setColumnWidths(new int[] {
            20,
            30
        });

        if ((parameter.getTagAttributes() != null) && (parameter.getTagAttributes().sizeOfAttributeArray() > 0)) {
            config.setData(Arrays.asList(parameter.getTagAttributes().getAttributeArray()));
        }
        
        BaseTable retval = new BaseTable(config);

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
        if (StringUtils.isNotBlank(parameterName.getText()) 
            && StringUtils.isNotBlank(tagName.getText())) {
            
            if (!isEditmode()) {
                if (parameterNameExists()) {
                    oktosave = false;
                    displayExistingNameAlert("Parameter", parameterName.getText());
                }
            }
        } else {
            displayRequiredFieldsMissingAlert("Parameter", "parameter name, tag/cookie name");
            oktosave = false;
        }
        
        if (oktosave) {
            List <TagAttribute> attributes = attributesTable.getTableData();
            
            if (parameter.getTagAttributes() == null) {
                parameter.addNewTagAttributes();
            }
            
            parameter.setParameterName(parameterName.getText());
            parameter.setTagName(tagName.getText());
            parameter.setFromInputParameter(fromInputParameter.isSelected());
            parameter.getTagAttributes().setAttributeArray(attributes.toArray(new TagAttribute[attributes.size()]));
            setSaved(true);
            dispose();
            retval = true;
        }
        
        return retval;
    }
    
    private boolean parameterNameExists() {
        boolean retval = false;
        String newname = parameterName.getText();
        return retval;
    }

    /**
     *
     * @return
     */
    @Override
    public Object getNewRepositoryObject() {
        return parameter;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(400, 350);
    }

    /**
     *
     * @return
     */
    @Override
    protected String getDialogName() {
        return "auto-replace-parameter-setup";
    }

    @Override
    protected void handleOtherActions(String actionCommand) {
        if (Constants.DELETE_ACTION.equals(actionCommand)) {
            List <TagAttribute> l = attributesTable.getTableData();
            if ((l != null) && !l.isEmpty()) {
                int selrow = attributesTable.getSelectedRow();
                TagAttribute att = l.get(selrow);

                if (UIUtils.promptForDelete(this, "Remove Attribute", "Remove attribute '" + att.getName() + "'?")) {
                    l.remove(selrow);
                    attributesTable.getModel().fireTableRowsDeleted(selrow, selrow);
                }
            }
        } else if (Constants.ADD_ACTION.equals(actionCommand)) {
            NameValueInputDlg dlg = new NameValueInputDlg(getMainframe(), this, "Attribute Name", "Attribute Value");
            
            if (dlg.isSaved()) {
                if (parameter.getTagAttributes() == null) {
                    parameter.addNewTagAttributes();
                }
                
                
                TagAttribute attribute = parameter.getTagAttributes().addNewAttribute();
                attribute.setName(dlg.getEnteredName());
                attribute.setValue(dlg.getEnteredValue());
                
                List <TagAttribute> l = attributesTable.getTableData();
                int newrow = l.size();
                l.add(attribute);
                attributesTable.getModel().fireTableRowsInserted(newrow, newrow);
            }
        }
    }
}
