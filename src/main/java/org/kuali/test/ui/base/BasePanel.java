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
import java.awt.Component;
import java.awt.Insets;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.kuali.test.creator.TestCreator;

/**
 *
 * @author rbtucker
 */
public class BasePanel extends JPanel implements ContainerListener {
    private TestCreator mainframe;

    /**
     *
     * @param mainframe
     */
    public BasePanel(TestCreator mainframe) {
        this.mainframe = mainframe;
        setLayout(new BorderLayout(3, 3));
        addContainerListener(this);
    }

    /**
     *
     * @param mainframe
     */
    public BasePanel() {
        this(null);
    }

    /**
     *
     * @param newComponent
     */
    public void replaceCenterComponent(JComponent newComponent) {
        replaceComponent(newComponent, BorderLayout.CENTER);
    }
    
    /**
     *
     * @return
     */
    public JComponent getCenterComponent() {
        BorderLayout l = (BorderLayout)getLayout();
        return (JComponent)l.getLayoutComponent(BorderLayout.CENTER);
    }
    
    /**
     *
     * @param newComponent
     * @param constraints
     */
    public void replaceComponent(JComponent newComponent, Object constraints) {
        BorderLayout l = (BorderLayout)getLayout();
        Component c = l.getLayoutComponent(constraints);
        remove(c);
        add(newComponent, constraints);
        getParent().validate();
    }

    /**
     *
     * @return
     */
    public TestCreator getMainframe() {
        return mainframe;
    }
    
    @Override
    public Insets getInsets() {
        return new Insets(3, 3, 3, 3);
    }

    @Override
    public void componentAdded(ContainerEvent e) {
        handleComponentAdded(e.getChild());
    }

    @Override
    public void componentRemoved(ContainerEvent e) {
        handleComponentRemoved(e.getChild());
    }
    
    protected void handleComponentAdded(Component child) {
    }

    protected void handleComponentRemoved(Component child) {
    }
}
