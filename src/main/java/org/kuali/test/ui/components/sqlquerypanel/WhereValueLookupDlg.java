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

package org.kuali.test.ui.components.sqlquerypanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.apache.log4j.Logger;
import org.kuali.test.DatabaseConnection;
import org.kuali.test.Platform;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.ui.utils.UIUtils;
import org.kuali.test.utils.Utils;


public class WhereValueLookupDlg extends JDialog implements ListSelectionListener {
    private static final Logger LOG = Logger.getLogger(WhereValueLookupDlg.class);
    private LookupValue lookupValue;
    private JList list;
    private TestCreator mainframe;
    private Platform platform;
    private String sql;
    
    public WhereValueLookupDlg(TestCreator mainframe, Platform platform, String sql) {
        super(mainframe, true);
        setTitle("Look Up");
        
        this.mainframe = mainframe;
        this.platform = platform;
        this.sql = sql;
        
        getContentPane().setLayout(new BorderLayout());
        DefaultListModel model = new DefaultListModel();
        list = new JList(model);
        list.addListSelectionListener(this);
        
        loadLookup();
        
        getContentPane().add(new JScrollPane(list), BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);       
        setVisible(true);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(250, 400);
    }
    
    @Override
    public void valueChanged(ListSelectionEvent lse) {
        JList list = (JList)lse.getSource();
        lookupValue = (LookupValue)list.getSelectedValue();
        dispose();
    }

    public LookupValue getLookupValue() {
        return lookupValue;
    }
    
    private void loadLookup() {
        Connection conn = null;
        Statement stmt = null;
        ResultSet res = null;

        try {
            DatabaseConnection dbconn = Utils.findDatabaseConnectionByName(mainframe.getConfiguration(), platform.getDatabaseConnectionName());

            if (dbconn != null) {
                List <LookupValue> values = new ArrayList <LookupValue>();
                conn = Utils.getDatabaseConnection(mainframe.getEncryptionPassword(), dbconn);
                stmt = conn.createStatement();
                res = stmt.executeQuery(sql);

                while (res.next()) {
                    LookupValue val = new LookupValue();
                    val.setName(res.getString(1));
                    val.setValue(res.getString(2));

                    values.add(val);
                }
            
                if (values.isEmpty()) {
                    JOptionPane.showMessageDialog(mainframe, "No lookup values found");
                    dispose();
                } else {
                    DefaultListModel model = (DefaultListModel)list.getModel();
                        
                    for (LookupValue lookup : values) {
                        model.addElement(lookup);
                    }
                }
            }
        }

        catch (Exception ex) {
            LOG.error(ex.toString(), ex);
            UIUtils.showError(mainframe, 
                "Lookup Error", "Error occurred while attempting to load lookup data from database - " + ex.toString());
        }

        finally {
            Utils.closeDatabaseResources(conn, stmt, res);
        }
    }
}
