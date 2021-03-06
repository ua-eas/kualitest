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

package org.kuali.test.ui.utils;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.apache.commons.lang3.StringUtils;
import org.kuali.test.creator.TestCreator;

/**
 *
 * @author rbtucker
 */
public class UIUtils {

    /**
     *
     * @param c
     * @return
     */
    public static Window findMainframe(Component c) {
        if (c instanceof TestCreator) {
            return (Window) c;
        } else {
            return findMainframe(c.getParent());
        }
    }
    
    /**
     *
     * @param c
     * @return
     */
    public static Window findWindow(Component c) {
        if (c == null) {
            return JOptionPane.getRootFrame();
        } else if (c instanceof Window) {
            return (Window) c;
        } else {
            return findWindow(c.getParent());
        }
    }

    /**
     *
     * @param msg
     * @param style
     * @return
     */
    public static String buildFormattedHtmlMessage(String msg, String style) {
        String retval = "";
        
        if (StringUtils.isNotBlank(msg)) {
            StringBuilder buf = new StringBuilder(msg.length());

            buf.append("<html><div style='");
            buf.append(style);
            buf.append("'>");
            buf.append(msg);
            buf.append("</div></html>");
            retval = buf.toString();
        }
        
        return retval;
    }
    
    /**
     *
     * @param msg
     * @param width
     * @return
     */
    public static String buildFormattedHtmlMessage(String msg, int width) {
        return buildFormattedHtmlMessage(msg, "width: " + width + "px;");
    }
    
    /**
     *
     * @param c
     * @param title
     * @param msg
     */
    public static void showError(Component c, String title, String msg) {
        JOptionPane.showMessageDialog(findWindow(c), buildFormattedHtmlMessage(msg, 250), title, JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     *
     * @param c
     * @param title
     * @param prompt
     * @return
     */
    public static boolean promptForDelete(Component c, String title, String prompt) {
        return (JOptionPane.showConfirmDialog(findWindow(c), prompt, title, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION);
    }

    /**
     *
     * @param c
     * @param title
     * @param prompt
     * @return
     */
    public static boolean promptForCancel(Component c, String title, String prompt) {
        return (JOptionPane.showConfirmDialog(findWindow(c), prompt, title, JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION);
    }
    
    /**
     *
     * @param c
     * @return
     */
    public static JPanel wrapPanel(JComponent c) {
        JPanel retval = new JPanel(new FlowLayout(FlowLayout.LEFT));
        retval.add(c);
        return retval;
    }
    
    /**
     *
     * @param labels
     * @return
     */
    public static JPanel buildLabelGridPanel(String[] labels) {
        JPanel retval = new JPanel(new GridLayout(labels.length, 1, 1, 1));
        
        for (int i = 0; i < labels.length; ++i) {
            String colon = ":";
            if (StringUtils.isBlank(labels[i])) {
                colon = "";
            }
            
            retval.add(new JLabel(labels[i] + colon, JLabel.RIGHT));
        }
        
        return retval;
    }
    
    /**
     *
     * @param components
     * @return
     */
    public static JPanel buildComponentGridPanel(JComponent[] components) {
        JPanel retval = new JPanel(new GridLayout(components.length, 1, 1, 1));
        for (JComponent c : components) {
           retval.add(wrapPanel(c));
        }
        
        return retval;
    }
    
    /**
     *
     * @param labels
     * @param components
     * @return
     */
    public static JPanel buildEntryPanel(String[] labels, JComponent[] components) {
        JPanel retval = new JPanel(new FlowLayout(FlowLayout.CENTER));
        
        JPanel entryPanel = new JPanel(new BorderLayout());
        entryPanel.add(buildComponentGridPanel(components), BorderLayout.CENTER);
        entryPanel.add(buildLabelGridPanel(labels), BorderLayout.WEST);
        retval.add(entryPanel);
        
        return retval;
    }
}
