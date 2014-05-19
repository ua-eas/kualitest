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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.apache.log4j.Logger;
import org.kuali.test.DatabaseConnection;
import org.kuali.test.Platform;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.ui.base.BasePanel;
import org.kuali.test.ui.components.sqlquerytree.SqlQueryTree;
import org.kuali.test.ui.utils.UIUtils;
import org.kuali.test.utils.Utils;

public class SqlQueryPanel extends BasePanel implements ActionListener {
    private static Logger LOG = Logger.getLogger(SqlQueryPanel.class);
    
    private SqlQueryTree sqlQueryTree;
    private final Platform platform;
    
    public SqlQueryPanel(TestCreator mainframe, Platform platform) {
        super(mainframe);
        this.platform = platform;
        initComponents();
    }

    private void initComponents() {
        JPanel p = new JPanel(new BorderLayout());
        p.add(new JLabel("Base Table:", JLabel.RIGHT), BorderLayout.WEST);
        
        JComboBox cb;
        p.add(cb = new JComboBox(getAvailableDatabaseTables()), BorderLayout.CENTER);
        cb.addActionListener(this);
        
        add(p, BorderLayout.NORTH);
        add(sqlQueryTree = new SqlQueryTree(getMainframe()), BorderLayout.CENTER);
    }
    
    
    private String[] getAvailableDatabaseTables() {
        List <String> retval = new ArrayList<String>();
        
        Connection conn = null;
        ResultSet res = null;
        
        try {
            DatabaseConnection dbconn = Utils.findDatabaseConnectionByName(getMainframe().getConfiguration(), platform.getDatabaseConnectionName());
            
            if (dbconn != null) {
                conn = Utils.getDatabaseConnection(getMainframe().getConfiguration(), dbconn);
                DatabaseMetaData dmd = conn.getMetaData();
                res = dmd.getTables(null, dbconn.getSchema(), null, new String[] {"TABLE", "VIEW"});

                while (res.next()) {
                    String tname = res.getString(3);
                    retval.add(tname);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("table: " + tname);
                    }
                }
            }
        }
        
        catch (Exception ex) {
            UIUtils.showError(this, "Database Connection Error", "An error occurred while attempting to connect to database - " + ex.toString());
        }
        
        finally {
            Utils.closeDatabaseResources(conn, null, res);
        }
        
        return retval.toArray(new String[retval.size()]);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
    }
}