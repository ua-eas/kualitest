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
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.tree.DefaultMutableTreeNode;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.test.AdditionalDatabaseInfoDocument;
import org.kuali.test.Application;
import org.kuali.test.Column;
import org.kuali.test.CustomForeignKey;
import org.kuali.test.DatabaseConnection;
import org.kuali.test.Platform;
import org.kuali.test.Table;
import org.kuali.test.TestHeader;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.ui.components.splash.SplashDisplay;
import org.kuali.test.ui.components.sqlquerytree.ColumnData;
import org.kuali.test.ui.components.sqlquerytree.SqlQueryNode;
import org.kuali.test.ui.components.sqlquerytree.SqlQueryTree;
import org.kuali.test.ui.components.sqlquerytree.TableData;
import org.kuali.test.ui.utils.UIUtils;
import org.kuali.test.utils.Constants;
import org.kuali.test.utils.Utils;
import org.kuali.test.utils.XMLFileFilter;


public class DatabasePanel extends BaseCreateTestPanel {
    private static final Logger LOG = Logger.getLogger(DatabasePanel.class);

    private JComboBox tableDropdown;
    private SqlQueryTree sqlQueryTree;
    private final Map <String, Table> additionalDbInfo = new HashMap<String, Table>();
    
    public DatabasePanel(TestCreator mainframe, Platform platform, TestHeader testHeader) {
        super(mainframe, platform, testHeader);
        initComponents();
    }

    private void initComponents() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p.add(new JLabel("Base Table:", JLabel.RIGHT));
        
        p.add(tableDropdown = new JComboBox(getAvailableDatabaseTables()));
        tableDropdown.addActionListener(this);

        JPanel p2 = new JPanel(new BorderLayout());
        p2.add(p, BorderLayout.NORTH);
        p2.add(new JScrollPane(sqlQueryTree = new SqlQueryTree(getMainframe(), this, getPlatform())), BorderLayout.CENTER);
        
        add(p2, BorderLayout.CENTER);
    }
    
    
    private TableData[] getAvailableDatabaseTables() {
        List <TableData> retval = new ArrayList<TableData>();
        
        Connection conn = null;
        ResultSet res = null;
        
        try {
            // load any additional database info - this will give us user-friendly names
            loadAdditionalDbInfo();

            // this is the empty place holder for base table selection
            retval.add(new TableData());
            
            DatabaseConnection dbconn = Utils.findDatabaseConnectionByName(getMainframe().getConfiguration(), getPlatform().getDatabaseConnectionName());
            
            if (dbconn != null) {
                conn = Utils.getDatabaseConnection(getMainframe().getConfiguration(), dbconn);
                DatabaseMetaData dmd = conn.getMetaData();
                res = dmd.getTables(null, dbconn.getSchema(), null, new String[] {"TABLE", "VIEW"});

                while (res.next()) {
                    String tableName = res.getString(3);
                    
                    Table t = additionalDbInfo.get(tableName);
                    
                    if (t != null) {
                        retval.add(new TableData(dbconn.getSchema(), tableName, t.getDisplayName()));
                    } 
                }
            }
        }
        
        catch (Exception ex) {
            UIUtils.showError(this, "Database Connection Error", "An error occurred while attemption to connect to database - " + ex.toString());
        }
        
        finally {
            Utils.closeDatabaseResources(conn, null, res);
        }
        
        Collections.sort(retval);
        
        return retval.toArray(new TableData[retval.size()]);
    }

    private void loadAdditionalDbInfo() {
        File fdir = new File(getMainframe().getConfiguration().getAdditionalDbInfoLocation());
        if (fdir.exists() && fdir.isDirectory()) {
            File f = null;
            File[] files = fdir.listFiles(new XMLFileFilter());
            
            for (int i = 0; i < files.length; ++i) {

                if (files[i].getName().toLowerCase().startsWith(getPlatform().getApplication().toString().toLowerCase())) {
                    f = files[i];
                }
            }
            
            if (f != null) {
                try {
                    AdditionalDatabaseInfoDocument doc = AdditionalDatabaseInfoDocument.Factory.parse(f);

                    if (doc != null) {
                        Application app = doc.getAdditionalDatabaseInfo().getApplication();
                        if (app != null) {
                            if (app.getApplicationName().equalsIgnoreCase(getPlatform().getApplication().toString())) {
                                if (app.getTables().sizeOfTableArray() > 0) {
                                    for (Table table : app.getTables().getTableArray()) {
                                        additionalDbInfo.put(table.getTableName(), table);
                                    }
                                }
                            }
                        }
                    }
                }
                
                catch (Exception ex) {
                    LOG.warn("Error reading additional database info file", ex);
                }
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final TableData td = (TableData)tableDropdown.getSelectedItem();
        
        if (StringUtils.isNotBlank(td.getName())) {
            new SplashDisplay(getMainframe(), "Related Tables", "Loading table relationships...") {
                @Override
                protected void runProcess() {
                    try {
                        DefaultMutableTreeNode rootNode = sqlQueryTree.getRootNode();
                        rootNode.removeAllChildren();
                        loadTables(td, rootNode);
                        sqlQueryTree.getModel().nodeStructureChanged(rootNode);
                        
                        getCreateCheckpoint().setEnabled(rootNode.getChildCount() > 0);
                    }

                    catch (Exception ex) {
                        UIUtils.showError(getMainframe(), "Error loading table relationships", "An error occured while loading table relationships - " + ex.toString());
                    }
                }
            };
        }
    }

    private void loadTables(TableData td, DefaultMutableTreeNode rootNode) throws Exception {
        Connection conn = null;
        ResultSet res = null;
        
        try {
            DatabaseConnection dbconn = Utils.findDatabaseConnectionByName(getMainframe().getConfiguration(), getPlatform().getDatabaseConnectionName());
            
            if (dbconn != null) {
                conn = Utils.getDatabaseConnection(getMainframe().getConfiguration(), dbconn);
                DatabaseMetaData dmd = conn.getMetaData();
                SqlQueryNode baseTableNode = new SqlQueryNode(getMainframe().getConfiguration(), td);
                rootNode.add(baseTableNode);
                loadTableRelationships(dmd, td, 0, baseTableNode);
            }
        }
        
        catch (Exception ex) {
            UIUtils.showError(this, "Error loading table relationships", "An error occurred while loading table relationships - " + ex.toString());
        }
        
        finally {
            Utils.closeDatabaseResources(conn, null, res);
        }        
    }
    
    private void loadTableRelationships(DatabaseMetaData dmd, TableData td, 
        int currentDepth, SqlQueryNode parentNode) throws Exception {
        ResultSet res = null;
        
        try {
            res = dmd.getImportedKeys(null, td.getSchema(), td.getName());
            currentDepth++;
            
            Map <String, TableData> map = new HashMap<String, TableData>();
            
            while (res.next()) {
                String schema = res.getString(2);
                String tname = res.getString(3);
                
                // for now do not follow links to self
                if (!tname.equals(td.getName())) {
                    String pkcname = res.getString(4);
                    String fkcname = res.getString(8);
                    String fkname = res.getString(12);

                    String key = fkname;
                    if (StringUtils.isBlank(key)) {
                        key = (td.getName() + "-" + tname);
                    }

                    TableData tdata = map.get(key);

                    if (tdata == null) {
                        Table t = additionalDbInfo.get(tname);

                        if (t != null) {
                            map.put(key, tdata = new TableData(schema, tname, t.getDisplayName()));
                        } else {
                            map.put(key, tdata = new TableData(schema, tname, tname));
                        }

                        td.getRelatedTables().add(tdata);
                        SqlQueryNode curnode = new SqlQueryNode(getMainframe().getConfiguration(), tdata);
                        parentNode.add(curnode);

                        if (currentDepth < Constants.MAX_TABLE_RELATIONSHIP_DEPTH) {
                            loadTableRelationships(dmd, tdata, currentDepth, curnode);
                        } 
                        
                        tdata.setForeignKeyName(fkname);
                    }

                    tdata.getLinkColumns().add(new String[] {fkcname, pkcname});
                }
            }

            if (currentDepth < Constants.MAX_TABLE_RELATIONSHIP_DEPTH) {
                CustomForeignKey[] customForeignKeys = getCustomForeignKeys(td);

                if (customForeignKeys != null) {
                    for (CustomForeignKey cfk : customForeignKeys) {
                        TableData tdata = new TableData(td.getSchema(), cfk.getPrimaryTableName(), getTableDisplayName(cfk.getPrimaryTableName()));
                        loadTableRelationships(dmd, tdata, currentDepth, parentNode);
                    }
                }
            }

            loadTableColumns(dmd, parentNode);
        }
        
        finally {
            Utils.closeDatabaseResources(null, null, res);
        }
    }

    public String getTableDisplayName(String tname) {
        String retval = tname;
        
        Table t = additionalDbInfo.get(tname);

        if (t != null) {
            retval = Utils.cleanTableDisplayName(t.getDisplayName());
        }
        
        return retval;
    }

    private CustomForeignKey[] getCustomForeignKeys(TableData td) {
        CustomForeignKey[] retval = null;
        
        Table t = additionalDbInfo.get(td.getName());
        
        if (t != null) {
            if ((t.getCustomForeignKeys() != null) 
                && (t.getCustomForeignKeys().sizeOfCustomForeignKeyArray() > 0)) {
                retval = t.getCustomForeignKeys().getCustomForeignKeyArray();
            }
        }
        
        return retval;
    }
    
    private void loadTableColumns(DatabaseMetaData dmd, SqlQueryNode node) throws Exception {
        ResultSet res = null;
        
        try {
            TableData td = (TableData)node.getUserObject();
            res = dmd.getColumns(null, td.getSchema(), td.getName(), null);
            
            while (res.next()) {
                String cname = res.getString(4);
                int dataType = res.getInt(5);
                
                ColumnData cd = new ColumnData(td.getSchema(), cname, getColumnDisplayName(td.getName(), cname));
                cd.setDataType(dataType);
                td.getColumns().add(cd);
            }
            
            HashMap <String, ColumnData> map = new HashMap<String, ColumnData>();
            for (ColumnData cd : td.getColumns()) {
                map.put(cd.getName(), cd);
            }
            
            res.close();
            try {
                res = dmd.getPrimaryKeys(null, td.getSchema(), td.getName());
                while (res.next()) {
                    String cname = res.getString(4);
                    int seq = res.getInt(5);

                    ColumnData cd = map.get(cname);
                    
                    if (cd!= null) {
                        cd.setPrimaryKeyIndex(seq);
                    }
                }
            }
            
            catch (Exception ex) {
                LOG.warn("error obtaining primary keys for table " + td.getName());
            }
            
            Collections.sort(td.getColumns());
            
            for (ColumnData cd : td.getColumns()) {
                node.add(new SqlQueryNode(getMainframe().getConfiguration(), cd));
            }
        }
        
        finally {
            Utils.closeDatabaseResources(null, null, res);
        }
    }
    
    public String getColumnDisplayName(String tname, String cname) {
        String retval = cname;
        
        Table t = additionalDbInfo.get(tname);
        
        if (t != null) {
            if (t.getColumns().sizeOfColumnArray() > 0) {
                for (Column c : t.getColumns().getColumnArray()) {
                    if (c.getColumnName().equals(cname)) {
                        retval = c.getDisplayName();
                        break;
                    }
                }
            }
        }
        
        return retval;
    }
    
    @Override
    protected void handleStartTest() {
    }

    @Override
    protected void handleCancelTest() {
    }

    @Override
    protected void handleCreateCheckpoint() {
    }

    @Override
    protected boolean handleSaveTest() {
        return false;
    }
}
