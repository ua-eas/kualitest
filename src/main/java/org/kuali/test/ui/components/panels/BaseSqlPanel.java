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

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.ui.base.BasePanel;


public class BaseSqlPanel extends BasePanel implements ComponentListener {
    private DatabasePanel dbPanel;
    
    public BaseSqlPanel(TestCreator mainframe, DatabasePanel dbPanel) {
        super(mainframe);
        this.dbPanel = dbPanel;
        addComponentListener(this);
    }

    public DatabasePanel getDbPanel() {
        return dbPanel;
    }

    @Override
    public void componentResized(ComponentEvent e) {
    }

    @Override
    public void componentMoved(ComponentEvent e) {
    }

    @Override
    public void componentShown(ComponentEvent e) {
        handlePanelShown();
    }

    @Override
    public void componentHidden(ComponentEvent e) {
    }
    
    protected void handlePanelShown() {
    }
}
