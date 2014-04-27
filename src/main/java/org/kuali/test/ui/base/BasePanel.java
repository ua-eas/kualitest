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

package org.kuali.test.ui.base;

import java.awt.BorderLayout;
import java.awt.Insets;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.kuali.test.KualiTestApp;


public class BasePanel extends JPanel {
    private KualiTestApp mainframe;
    public BasePanel(KualiTestApp mainframe) {
        this.mainframe = mainframe;
        setLayout(new BorderLayout(3, 3));
    }

    public void replaceCenterComponent(JComponent newComponent) {
        replaceComponent(newComponent, BorderLayout.CENTER);
    }
    
    public void replaceComponent(JComponent newComponent, Object constraints) {
        BorderLayout l = (BorderLayout)getLayout();
        remove(l.getLayoutComponent(constraints));
        add(newComponent, constraints);
        getParent().validate();
    }

    public KualiTestApp getMainframe() {
        return mainframe;
    }
    
    public Insets getInsets() {
        return new Insets(3, 3, 3, 3);
    }
}
