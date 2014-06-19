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
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.kuali.test.CheckpointType;
import org.kuali.test.Platform;
import org.kuali.test.TestType;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.ui.base.BaseSetupDlg;
import org.kuali.test.ui.utils.UIUtils;
import org.kuali.test.utils.Constants;
import org.kuali.test.utils.Utils;

/**
 *
 * @author rbtucker
 */
public class CheckPointTypeSelectDlg extends BaseSetupDlg {
    private JComboBox checkPointTypes;
    
    public CheckPointTypeSelectDlg(TestCreator mainFrame, TestType.Enum testType, Platform platform) {
        super(mainFrame);
        setTitle("Select Check Point Type");
        
        initComponents(testType, platform);
    }

    private void initComponents(TestType.Enum testType, Platform platform) {
        String[] labels = new String[] {
            "Check Point Type", 
        };
        
        checkPointTypes = new JComboBox(Utils.getValidCheckpointTypesForPlatform(testType, platform));

        JComponent[] components = new JComponent[] {
            checkPointTypes
        };

        JPanel p = new JPanel(new BorderLayout(3, 3));
        p.add(UIUtils.buildEntryPanel(labels, components), BorderLayout.NORTH);
        getContentPane().add(p, BorderLayout.CENTER);
        

        switch(testType.intValue()) {
            case TestType.INT_DATABASE:
                break;
            case TestType.INT_FILE:
                break;
            case TestType.INT_WEB:
                checkPointTypes.setSelectedItem(CheckpointType.HTTP.toString());
                break;
            case TestType.INT_WEB_SERVICE:
                break;
        }
        
        addStandardButtons();
        setDefaultBehavior();
    }
    
    @Override
    protected boolean save() {
        boolean retval = false;
        if (checkPointTypes.getSelectedIndex() > -1) {
            dispose();
            setSaved(true);
            retval = true;
        }
        
        return retval;
    }
    
    public CheckpointType.Enum getCheckpointType() {
        return CheckpointType.Enum.forString((String)checkPointTypes.getSelectedItem());
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(325, 100);
    }

    @Override
    protected String getSaveText() {
        return Constants.OK_ACTION;
    }

    
    @Override
    protected String getDialogName() {
        return "checkpoint-type";
    }
}
