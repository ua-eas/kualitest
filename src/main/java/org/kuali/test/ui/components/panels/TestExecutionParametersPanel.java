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

package org.kuali.test.ui.components.panels;

import java.awt.BorderLayout;
import java.util.Iterator;
import java.util.List;
import javax.swing.JDialog;
import javax.swing.JLabel;
import org.apache.commons.lang3.StringUtils;
import org.kuali.test.TestExecutionParameter;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.ui.base.BasePanel;
import org.kuali.test.ui.base.BaseTable;
import org.kuali.test.ui.base.TableConfiguration;


public class TestExecutionParametersPanel extends BasePanel {
    private BaseTable parametersTable;
    public TestExecutionParametersPanel(TestCreator mainframe, JDialog parentDialog, List <TestExecutionParameter> parameters) {
        super(mainframe);
        add(createParametersTable(parentDialog, parameters), BorderLayout.CENTER);
    }
    
    private TablePanel createParametersTable(final JDialog parentDialog, List <TestExecutionParameter> parameters) {
        TableConfiguration config = new TableConfiguration();
        config.setTableName("test-execution-parameters-table");
        config.setDisplayName("Execution Parameters");
        
        int[] alignment = new int[4];
        for (int i = 0; i < alignment.length; ++i) {
            alignment[i] = JLabel.LEFT;
        }
            
        config.setColumnAlignment(alignment);
        
        config.setHeaders(new String[] {
            "Parameter Name",
            "Property Group",
            "Property Name",
            "Current Value"
        });
        
        config.setPropertyNames(new String[] {
            "name",
            "valueProperty.propertyGroup",
            "valueProperty.displayName",
            "valueProperty.propertyValue"
        });
            
        config.setColumnTypes(new Class[] {
            String.class,
            String.class,
            String.class,
            String.class
        });
        
        config.setColumnWidths(new int[] {
            50,
            50,
            100,
            30
        });

       Iterator <TestExecutionParameter> it = parameters.iterator();
        
        while (it.hasNext()) {
            if (StringUtils.isBlank(it.next().getName())) {
                it.remove();
            }
        }

        
        config.setData(parameters);
        
        parametersTable = new BaseTable(config);
        
       return new TablePanel(parametersTable);
    }
}
