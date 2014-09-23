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
import java.util.List;
import javax.swing.JLabel;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.ui.base.BaseSetupDlg;
import org.kuali.test.ui.base.BaseTable;
import org.kuali.test.ui.base.TableConfiguration;
import org.kuali.test.ui.components.panels.TablePanel;
import org.kuali.test.utils.Constants;

/**
 *
 * @author rbtucker
 */
public class TestCommentsDlg extends BaseSetupDlg {
    /**
     * 
     * @param mainFrame
     * @param comments 
     */
    public TestCommentsDlg(TestCreator mainFrame, List <String> comments) {
        super(mainFrame);
        setTitle("Test Comments");
        initComponents(comments);
    }

    private void initComponents(List <String> comments) {
        
        getContentPane().add(getCommentsTable(comments), BorderLayout.CENTER);

        addStandardButtons();
        
        this.getSaveButton().setVisible(false);
        
        setDefaultBehavior();
    }
    
    private TablePanel getCommentsTable(List <String> comments) {
        TableConfiguration config = new TableConfiguration();
        config.setTableName("test-comments-table");
        config.setDisplayName("Comments");
        
        int[] alignment = {JLabel.CENTER, JLabel.LEFT};
            
        config.setColumnAlignment(alignment);
        
        config.setHeaders(new String[] {
            "",
            "Comment",
        });
        
        config.setPropertyNames(new String[] {
            null,
            null
        });
            
        config.setColumnTypes(new Class[] {
            Integer.class,
            String.class
        });
        
        config.setColumnWidths(new int[] {
            15,
            200
        });

        config.setData(comments);
        
        return new TablePanel(new BaseTable(config) {
            @Override
            public Object getValueAt(int row, int col) {
                Object retval = null;
                
                if (col == 0) {
                    retval = Integer.valueOf(row+1);
                } else {
                    retval = getRowData(row);
                }
                
                return retval;
            }
        });
    }


    @Override
    public Dimension getPreferredSize() {
        return new Dimension(700, 300);
    }

    @Override
    protected String getCancelText() {
        return Constants.CLOSE_ACTION;
    }

    /**
     *
     * @return
     */
    @Override
    protected String getDialogName() {
        return "test-comments";
    }

    @Override
    protected boolean save() {
        return false;
    }
}
