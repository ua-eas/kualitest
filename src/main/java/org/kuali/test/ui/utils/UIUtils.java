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

import java.awt.Component;
import java.awt.Window;
import javax.swing.JOptionPane;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;


public class UIUtils {
    public static Window findWindow(Component c) {
        if (c == null) {
            return JOptionPane.getRootFrame();
        } else if (c instanceof Window) {
            return (Window) c;
        } else {
            return findWindow(c.getParent());
        }
    }

    public static String buildFormattedHtmlMessage(String msg, String style) {
        String retval = "";
        
        if (StringUtils.isNotBlank(msg)) {
            StringBuilder buf = new StringBuilder(msg.length());

            buf.append("<html><div style='");
            buf.append(style);
            buf.append("'>");
            buf.append(StringEscapeUtils.escapeHtml4(msg));
            buf.append("</div></html>");
            retval = buf.toString();
        }
        
        return retval;
    }
    
    public static String buildFormattedHtmlMessage(String msg, int width) {
        return buildFormattedHtmlMessage(msg, "width: " + width + "px;");
    }
    
    public static void showError(Component c, String title, String msg) {
        JOptionPane.showMessageDialog(findWindow(c), buildFormattedHtmlMessage(msg, 250), title, JOptionPane.ERROR_MESSAGE);
    }
    
    public static boolean promptForDelete(Component c, String title, String prompt) {
        return (JOptionPane.showConfirmDialog(findWindow(c), prompt, title, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION);
    }

    public static boolean promptForCancel(Component c, String title, String prompt) {
        return (JOptionPane.showConfirmDialog(findWindow(c), prompt, title, JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION);
    }
}
