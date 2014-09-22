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

import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.ui.base.BaseSetupDlg;

/**
 *
 * @author rbtucker
 */
public abstract class BaseCheckpointDlg extends BaseSetupDlg {
    protected JTextArea commentField;
    
    /**
     *
     * @param mainframe
     */
    public BaseCheckpointDlg(TestCreator mainframe) {
        super(mainframe);
    }

    /**
     *
     * @param mainframe
     * @param parent
     */
    public BaseCheckpointDlg(TestCreator mainframe, JDialog parent) {
        super(mainframe, parent);
    }

    public JTextArea getCommentField() {
        return commentField;
    }
    
    public String getComment() {
        String retval = null;
        if (commentField != null) {
            retval = commentField.getText();
        }
        return retval;
    }
    
    
    protected JScrollPane createCommentField() {
        commentField = new JTextArea(3, 30);
        commentField.setWrapStyleWord(true);
        commentField.setLineWrap(true);
        return new JScrollPane(commentField, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    }
}
