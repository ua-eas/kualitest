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
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import org.apache.log4j.Logger;
import org.kuali.test.Platform;
import org.kuali.test.TestHeader;
import org.kuali.test.ui.base.ToolbarButton;
import org.kuali.test.utils.Constants;


public abstract class BaseCreateTestPanel extends JPanel implements ActionListener {
    protected static final Logger LOG = Logger.getLogger(BaseCreateTestPanel.class);
    
    private Platform platform;
    private TestHeader testHeader;
    
    public BaseCreateTestPanel(Platform platform, TestHeader testHeader) {
        super(new BorderLayout(3, 3));
        this.platform = platform;
        this.testHeader = testHeader;
    
        if (LOG.isDebugEnabled()) {
            LOG.debug("creating " + testHeader.getTestType() + " test for platform: " + platform.getName());
        }
        
        add(createToolbar(), BorderLayout.NORTH);
    }

    protected JToolBar createToolbar() {
        JToolBar retval = new JToolBar();
        
        ToolbarButton b = new ToolbarButton(Constants.CREATE_CHECKPOINT_ACTION, Constants.CREATE_CHECKPOINT_ICON);
        b.addActionListener(this);
        retval.add(b);
        retval.addSeparator();
        
        b = new ToolbarButton(Constants.SAVE_TEST_ACTION, Constants.SAVE_TEST_ICON);
        b.addActionListener(this);
        retval.add(b);

        retval.add(new JLabel("        Platform: " + platform.getName()));
        retval.add(new JLabel("  Test Name: " + testHeader.getTestName()));
        
        return retval;
    }
    
    public Platform getPlatform() {
        return platform;
    }

    public TestHeader getTestHeader() {
        return testHeader;
    }

    public Insets getInsets() {
        return new Insets(3, 3, 3, 3);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals(Constants.CREATE_CHECKPOINT_ACTION)) {
            handleCreateCheckpoint();
        } else if (e.getActionCommand().equals(Constants.SAVE_TEST_ACTION)) {
            handleSaveTest();
        }
    }
    
    protected abstract void handleCreateCheckpoint();

    protected abstract void handleSaveTest();
}
