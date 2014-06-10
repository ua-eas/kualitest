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
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.ui.base.BaseSetupDlg;
import org.kuali.test.ui.base.BaseTable;
import org.kuali.test.ui.components.panels.TablePanel;
import org.kuali.test.ui.utils.UIUtils;

/**
 *
 * @author rbtucker
 */
public class TestExecutionParameterNamesDlg extends BaseSetupDlg {
    private JTextField name;
    private BaseTable nameTable;
    
    /**
     * Creates new form TestSuiteDlg
     * @param mainFrame
     */
    public TestExecutionParameterNamesDlg(TestCreator mainFrame) {
        super(mainFrame);
        setTitle("Add/Edit test execution parameter names");
        initComponents();
    }

    private void initComponents() {
        String[] labels = new String[] {
            "Name"
        };
        
        name = new JTextField(20);
        
        JComponent[] components = new JComponent[] {
            name
        };

        JPanel p = new JPanel(new BorderLayout(3, 3));
        p.add(UIUtils.buildEntryPanel(labels, components), BorderLayout.NORTH);

        p.add(new TablePanel(nameTable = buildNameTable()), BorderLayout.CENTER);
        
        getContentPane().add(p, BorderLayout.CENTER);

        addStandardButtons();
        setDefaultBehavior();
    }
    
    private BaseTable buildNameTable() {
        return null;
    }
    
    
    @Override
    protected boolean save() {
        boolean retval = false;
        getConfiguration().setModified(true);
        setSaved(true);
        dispose();
        retval = true;
        
        return retval;
    }
    
    private boolean parameterNameExists() {
        boolean retval = false;
        String newname = name.getText();
        
        
        if (getConfiguration().getTestExecutionParameterNames() != null) {
            for (String nm : getConfiguration().getTestExecutionParameterNames().getNameArray()) {
                if (nm.equalsIgnoreCase(newname)) {
                    retval = true;
                    break;
                }
            }
        }
        
        return retval;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(200, 400);
    }

    @Override
    protected String getDialogName() {
        return "test-execution-paramter-names";
    }
}
