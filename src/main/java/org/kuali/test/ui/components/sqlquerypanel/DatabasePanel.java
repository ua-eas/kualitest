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
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.tree.DefaultMutableTreeNode;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.test.AdditionalDatabaseInfoDocument;
import org.kuali.test.Application;
import org.kuali.test.Checkpoint;
import org.kuali.test.Column;
import org.kuali.test.CustomForeignKey;
import org.kuali.test.DatabaseConnection;
import org.kuali.test.DatabaseType;
import org.kuali.test.Operation;
import org.kuali.test.Platform;
import org.kuali.test.Table;
import org.kuali.test.TestHeader;
import org.kuali.test.TestOperation;
import org.kuali.test.TestOperationType;
import org.kuali.test.comparators.SqlHierarchyComparator;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.ui.components.dialogs.SqlCheckPointDlg;
import org.kuali.test.ui.components.panels.BaseCreateTestPanel;
import org.kuali.test.ui.components.splash.SplashDisplay;
import org.kuali.test.ui.components.sqlquerytree.ColumnData;
import org.kuali.test.ui.components.sqlquerytree.SqlQueryNode;
import org.kuali.test.ui.components.sqlquerytree.SqlQueryTree;
import org.kuali.test.ui.components.sqlquerytree.TableData;
import org.kuali.test.ui.utils.UIUtils;
import org.kuali.test.utils.Constants;
import org.kuali.test.utils.Utils;
import org.kuali.test.utils.XMLFileFilter;


public class DatabasePanel extends BaseCreateTestPanel  {
    private static final Logger LOG = Logger.getLogger(DatabasePanel.class);
    
    public static int SQL_FORMAT_DISPLAY = 0;
    public static int SQL_FORMAT_CLIPBOARD = 1;
    public static int SQL_FORMAT_VALIDATE = 2;
    public static int SQL_FORMAT_EXECUTE = 2;
    
    private JComboBox tableDropdown;
    private SqlQueryTree sqlQueryTree;
    private SqlSelectPanel sqlSelectPanel;
    private SqlWherePanel sqlWherePanel;
    private SqlDisplayPanel sqlDisplayPanel;
    private boolean forCheckpoint = false;
    private JTabbedPane tabbedPane;
    
    private List <TestOperation> testOperations = new ArrayList<TestOperation>();
    
    private final Map <String, Table> additionalDbInfo = new HashMap<String, Table>();
    
    public DatabasePanel(TestCreator mainframe, Platform platform, TestHeader testHeader, boolean forCheckpoint) {
        super(mainframe, platform, testHeader);
        this.forCheckpoint = forCheckpoint;
        initComponents();
    }

    public DatabasePanel(TestCreator mainframe, Platform platform, TestHeader testHeader) {
        this(mainframe, platform, testHeader, false);
    }

    @Override
    protected JToolBar createToolbar() {
        if (!forCheckpoint) {
            return super.createToolbar();
        } else {
            return null;
        }
    }
    
    private void initComponents() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        p.add(new JLabel("Base Table:", JLabel.RIGHT));
        p.add(tableDropdown = new JComboBox());
        loadAvailableDatabaseTables();
        
        JPanel p2 = new JPanel(new BorderLayout());
        p2.add(p, BorderLayout.NORTH);
        
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Columns", new JScrollPane(sqlQueryTree = new SqlQueryTree(getMainframe(), this, getPlatform())));
        tabbedPane.addTab("Select", sqlSelectPanel = new SqlSelectPanel(getMainframe(), this));
        tabbedPane.addTab("Where", sqlWherePanel = new SqlWherePanel(getMainframe(), this));
        tabbedPane.addTab("SQL", sqlDisplayPanel = new SqlDisplayPanel(getMainframe(), this));
        
        
        p2.add(tabbedPane, BorderLayout.CENTER);
        
        add(p2, BorderLayout.CENTER);
        
        if (!forCheckpoint) {
            getMainframe().getCreateTestButton().setEnabled(false);
        }
    }
    
    
    private void loadAvailableDatabaseTables() {
        new SplashDisplay(getMainframe(), "Loading tables", "Load available database tables...") {

            @Override
            protected void processCompleted() {
                tableDropdown.addActionListener(DatabasePanel.this);
            }

            @Override
            protected void runProcess() {
                List <TableData> tables = new ArrayList<TableData>();

                Connection conn = null;
                ResultSet res = null;

                try {
                    // load any additional database info - this will give us user-friendly names
                    loadAdditionalDbInfo();

                    // this is the empty place holder for base table selection
                    tables.add(new TableData());

                    DatabaseConnection dbconn = Utils.findDatabaseConnectionByName(getMainframe().getConfiguration(), getPlatform().getDatabaseConnectionName());

                    if (dbconn != null) {
                        conn = Utils.getDatabaseConnection(getMainframe().getConfiguration(), dbconn);
                        DatabaseMetaData dmd = conn.getMetaData();
                        res = dmd.getTables(null, dbconn.getSchema(), null, new String[] {"TABLE", "VIEW"});

                        while (res.next()) {
                            String tableName = res.getString(3);

                            Table t = additionalDbInfo.get(tableName);

                            if ((t != null) || !dbconn.getConfiguredTablesOnly()) {
                                if (t != null) {
                                    tables.add(new TableData(dbconn.getSchema(), tableName, t.getDisplayName()));
                                } else {
                                    tables.add(new TableData(dbconn.getSchema(), tableName, tableName));
                                }
                            } 
                        }
                    }
                }

                catch (Exception ex) {
                    UIUtils.showError(getMainframe(), "Database Connection Error", "An error occurred while attemption to connect to database - " + ex.toString());
                }

                finally {
                    Utils.closeDatabaseResources(conn, null, res);
                }

                Collections.sort(tables);

                for (TableData td : tables) {
                    tableDropdown.addItem(td);
                }
            }

        };
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
                loadTableRelationships(dbconn, dmd, td, 0, baseTableNode);
            }
        }
        
        catch (Exception ex) {
            UIUtils.showError(this, "Error loading table relationships", "An error occurred while loading table relationships - " + ex.toString());
        }
        
        finally {
            Utils.closeDatabaseResources(conn, null, res);
        }        
    }
    
    private void loadTableRelationships(DatabaseConnection dbconn, DatabaseMetaData dmd, TableData td, 
        int currentDepth, SqlQueryNode parentNode) throws Exception {
        ResultSet res = null;
        
        try {
            td.setTreeNode(parentNode);
            res = dmd.getImportedKeys(null, td.getSchema(), td.getName());
            currentDepth++;
            
            Map <String, TableData> map = new HashMap<String, TableData>();
            
            while (res.next()) {
                String schema = res.getString(2);
                String tname = res.getString(3);
                
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

                    tdata.setForeignKeyName(fkname);
                }

                tdata.getLinkColumns().add(new String[] {fkcname, pkcname});
            }

            List <TableData> l = new ArrayList(map.values());
            
            Collections.sort(l);
            
            for (TableData tdata : l) {
                td.getRelatedTables().add(tdata);
                SqlQueryNode curnode = new SqlQueryNode(getMainframe().getConfiguration(), tdata);

                parentNode.add(curnode);

                if (currentDepth < Constants.MAX_TABLE_RELATIONSHIP_DEPTH) {
                    // only move down the tree if this is not a circular relationship
                    if (!isCircularReference(curnode)) {
                        loadTableRelationships(dbconn, dmd, tdata, currentDepth, curnode);  
                    } else {
                        tdata.setTreeNode(curnode);
                        loadTableColumns(dbconn, dmd, curnode);
                    }
                } 
            }
            
            if (currentDepth < Constants.MAX_TABLE_RELATIONSHIP_DEPTH) {
                CustomForeignKey[] customForeignKeys = getCustomForeignKeys(td);

                if (customForeignKeys != null) {
                    for (CustomForeignKey cfk : customForeignKeys) {
                        TableData tdata = new TableData(td.getSchema(), cfk.getPrimaryTableName(), getTableDisplayName(cfk.getPrimaryTableName()));
                        SqlQueryNode curnode = new SqlQueryNode(getMainframe().getConfiguration(), tdata);
                        parentNode.add(curnode);
                        loadTableRelationships(dbconn, dmd, tdata, currentDepth, curnode);
                    }
                }
            }

            loadTableColumns(dbconn, dmd, parentNode);
        }
        
        finally {
            Utils.closeDatabaseResources(null, null, res);
        }
    }

    private boolean isCircularReference(DefaultMutableTreeNode node) {
        boolean retval = false;
        
        TableData td = (TableData)node.getUserObject();
        DefaultMutableTreeNode pnode = (DefaultMutableTreeNode)node.getParent();
        
        while (!retval && (pnode != null)) {
            if (pnode.getUserObject() instanceof TableData) {
                TableData tdata = (TableData)pnode.getUserObject();
                retval = td.getDbTableName().equals(tdata.getDbTableName());
            }

            pnode = (DefaultMutableTreeNode)pnode.getParent();
        }
        
        return retval;
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
    
    private void loadTableColumns(DatabaseConnection dbconn, DatabaseMetaData dmd, SqlQueryNode node) throws Exception {
        ResultSet res = null;
        
        try {
            TableData td = (TableData)node.getUserObject();
            res = dmd.getColumns(null, td.getSchema(), td.getName(), null);
            
            while (res.next()) {
                String cname = res.getString(4);
                int dataType = res.getInt(5);
                int width = res.getInt(7);
                int decimalDigits = res.getInt(9);

                String displayName = getColumnDisplayName(td.getName(), cname, dbconn.getConfiguredTablesOnly());
                
                if (StringUtils.isNotBlank(displayName)) {
                    ColumnData cd = new ColumnData(td.getSchema(), cname, displayName);
                    cd.setDataType(dataType);
                    cd.setDecimalDigits(decimalDigits);
                    cd.setWidth(width);
                    td.getColumns().add(cd);
                }
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
    
    public String getColumnDisplayName(String tname, String cname, boolean configuredTablesOnly) {
        String retval = null;
        
        if (!configuredTablesOnly) {
            retval = cname;
        }
        
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
        getMainframe().getCreateTestPanel().clearPanel("test '" + getTestHeader().getTestName() + "' cancelled");
        getMainframe().getCreateTestButton().setEnabled(true);
        testOperations.clear();
    }

    @Override
    protected void handleCreateCheckpoint() {
        if (isValidSqlQuery()) {
            SqlCheckPointDlg dlg = new SqlCheckPointDlg(getMainframe(), getTestHeader(), this);

            if (dlg.isSaved()) {
                addCheckpoint((Checkpoint)dlg.getNewRepositoryObject());
                getSaveTest().setEnabled(true);
            }
        }
    }
    
    private void addCheckpoint(Checkpoint checkpoint) {
        TestOperation testOp = TestOperation.Factory.newInstance();
        testOp.setOperationType(TestOperationType.CHECKPOINT);
        Operation op = testOp.addNewOperation();
        op.addNewCheckpointOperation();
        op.setCheckpointOperation(checkpoint);
        testOperations.add(testOp);
    }

    @Override
    protected boolean handleSaveTest() {
        boolean retval = saveTest(getMainframe().getConfiguration().getRepositoryLocation(),
            getTestHeader(), testOperations);

        if (retval) {
            getMainframe().getTestRepositoryTree().saveConfiguration();
            getMainframe().getCreateTestPanel().clearPanel("test '" + getTestHeader().getTestName() + "' created");
        }

        return retval;
    }

    public SqlQueryTree getSqlQueryTree() {
        return sqlQueryTree;
    }

    public SqlSelectPanel getSqlSelectPanel() {
        return sqlSelectPanel;
    }

    public SqlWherePanel getSqlWherePanel() {
        return sqlWherePanel;
    }

    public SqlDisplayPanel getSqlDisplayPanel() {
        return sqlDisplayPanel;
    }

    public boolean haveSelectedColumns() {
        return (sqlQueryTree.getSelectedColumnsCount() > 0);
    }
    
    public List <TableData> getSelectedDbObjects() {
        List <TableData> retval = new ArrayList<TableData>();
        
        loadSelectedDbObjects(sqlQueryTree.getRootNode(), retval);
        
        Collections.sort(retval);
        
        return retval;
    }
    
    
    private void loadSelectedDbObjects(DefaultMutableTreeNode node, List <TableData> selectedDbObjects) {
        if (node.getUserObject() instanceof TableData) {
            TableData td = (TableData)node.getUserObject();
            
            if (hasSelectedColumns(td)) {
                selectedDbObjects.add(td);
            }
        }
        
        for (int i = 0; i < node.getChildCount(); ++i) {
             loadSelectedDbObjects((DefaultMutableTreeNode)node.getChildAt(i), selectedDbObjects);
        }
    }
    
    private boolean hasSelectedColumns(TableData td) {
        boolean retval = false;
        
        for (ColumnData cd : td.getColumns()) {
            if (cd.isSelected()) {
                retval = true;
                break;
            }
        }
        
        return retval;
    }

    public String getTableDataTooltip(TableData td) {
        String retval = null;
        if (td.getTreeNode() != null) {
            retval = sqlQueryTree.getTooltip(td.getTreeNode());
        }
        
        return retval;
    }

    private String getTableAlias(TableData td, List <TableData> tables, int format) {
        String retval = td.getName();
        for (int i = 0; i < tables.size(); ++i) {
            if (td.equals(tables.get(i))) {
                retval = ("t"+ (i+1));
                if (format == SQL_FORMAT_DISPLAY) {
                    retval = Utils.buildHtmlStyle(Constants.HTML_DARK_GREEN_STYLE, retval);
                } 
                break;
            } 
        }
        
        return retval;
    }
    
    public String getSqlTabString(int format) {
        if (format == SQL_FORMAT_DISPLAY) {
            return Constants.HTML_TAB;
        } else {
            return Constants.TAB_SPACES;
        }
    }
    
    public String getSqlLineBreakString(int format) {
        if (format == SQL_FORMAT_DISPLAY) {
            return Constants.HTML_LINE_BREAK;
        } else {
            return "\r\n";
        }
    }

    public String getSqlKeywordString(int format, String keyword) {
        return getSqlKeywordString(format, keyword, true);
    }
    
    public String getSqlKeywordString(int format, String keyword, boolean includeLineBreak) {
        StringBuilder retval = new StringBuilder(64);
        if (format == SQL_FORMAT_DISPLAY) {
            retval.append(Utils.buildHtmlStyle(Constants.HTML_BOLD_BLUE_STYLE, keyword));
            retval.append("&nbsp;");
        } else {
            retval.append(keyword);
            retval.append(" ");
        }
        
        if (includeLineBreak) {
            retval.append(getSqlLineBreakString(format));
        }
        
        return retval.toString();
    }

    public String getSqlQueryString(int format) {
        StringBuilder retval = new StringBuilder(512);

        List <SelectColumnData> selcols = sqlSelectPanel.getColumnData();

        if ((selcols != null) && !selcols.isEmpty()) {
            if (format == SQL_FORMAT_DISPLAY) {
                retval.append("<body>");
            } 
            
            retval.append(getSqlKeywordString(format, "select"));
            
            if (sqlSelectPanel.isDistinct()) {
                retval.append(getSqlTabString(format));
                retval.append(getSqlKeywordString(format, "distinct"));
            }
            
            List <TableData> tableList = buildCompleteQueryTableList();
            List <SelectColumnData> orderbycols = new ArrayList<SelectColumnData>();
            

            for (int i = 0; i < selcols.size(); ++i) {
                retval.append(getSqlTabString(format));

                SelectColumnData scd = selcols.get(i);

                if (StringUtils.isNotBlank(scd.getOrder()) && (Integer.parseInt(scd.getOrder()) > 0)) {
                    orderbycols.add(scd);
                }

                if (StringUtils.isNotBlank(scd.getFunction())) {
                    retval.append(scd.getFunction());
                    retval.append("(");
                }
                
                retval.append(getTableAlias(scd.getTableData(), tableList, format));
                retval.append(".");
                retval.append(scd.getColumnData().getName());
                
                if (StringUtils.isNotBlank(scd.getFunction())) {
                    retval.append(")");
                }
                
                if (i < (selcols.size() - 1)) {
                    retval.append(", ");
                }

                retval.append(getSqlLineBreakString(format));
            }

            retval.append(getSqlKeywordString(format, "from"));

            for (int i = 0; i < tableList.size(); ++i) {
                TableData td = tableList.get(i);
                
                retval.append(getSqlTabString(format));

                String tdAlias = getTableAlias(td, tableList, format);
                
                if (i == 0) {
                    retval.append(td.getName());
                    retval.append(" ");
                    retval.append(tdAlias);
                    retval.append(getSqlLineBreakString(format));
                } else {
                    TableData ptd = getParentTableData(td);

                    // if we are doing a join there must be parent table
                    if (ptd != null) {
                        String ptdAlias = getTableAlias(ptd, tableList, format);

                        retval.append(getSqlTabString(format));
                        
                        if (td.isOuterJoin()) {
                            retval.append(this.getSqlKeywordString(format, " left outer join", false));
                        } else {
                            retval.append(this.getSqlKeywordString(format, " join", false));
                        }
                        
                        retval.append(td.getName());
                        retval.append(" ");
                        retval.append(tdAlias);
                        retval.append(this.getSqlKeywordString(format, " on ", false));

                        retval.append("(");

                        retval.append(getSqlLineBreakString(format));
                        
                        String and = "";
                        List <String[]> linkColumns = td.getLinkColumns();
                        for (int j = 0; j < linkColumns.size(); ++j) {
                            for (int k = 0; k < 3; ++k) {
                                retval.append(getSqlTabString(format));
                            }
                            retval.append(and);
                            retval.append(tdAlias);
                            retval.append(".");
                            retval.append(linkColumns.get(j)[1]);
                            retval.append(" = ");
                            retval.append(ptdAlias);
                            retval.append(".");
                            retval.append(linkColumns.get(j)[0]);
                            and = " and ";
                            
                            if (j < (linkColumns.size() - 1)) {
                                retval.append(getSqlLineBreakString(format));
                            }
                        }

                        retval.append(") ");
                        retval.append(getSqlLineBreakString(format));
                    }
                }
            }

            List <WhereColumnData> wherecols = sqlWherePanel.getColumnData();
            
            if (!wherecols.isEmpty()) {
                retval.append(getSqlKeywordString(format, "where"));
            
                for (int i = 0; i < wherecols.size(); ++i) {
                    WhereColumnData wcd = wherecols.get(i);
                    retval.append(getSqlTabString(format));
                    
                    if (i > 0) {
                        retval.append(getSqlTabString(format));
                    }
                    
                    retval.append(buildWhereClause(tableList, wcd, format));
                }
                
                // if we are just doing a syntax check add a condition that will always fail
                if (format == SQL_FORMAT_VALIDATE) {
                    retval.append(" and (1 = 0) ");
                }
            }
            
            if (isGroupByRequired(selcols)) {
                retval.append(getSqlKeywordString(format, "group by"));
                retval.append(getSqlTabString(format));

                String comma = "";
                for (SelectColumnData scd : selcols) {
                    if (StringUtils.isBlank(scd.getFunction())) {
                        retval.append(comma);
                        retval.append(getTableAlias(scd.getTableData(), tableList, format));
                        retval.append(".");
                        retval.append(scd.getColumnData().getName());
                        comma = ", ";
                    }
                }

                retval.append(getSqlLineBreakString(format));
            }
            
            if (!orderbycols.isEmpty()) {
                Collections.sort(orderbycols, new Comparator <SelectColumnData> () {
                    @Override
                    public int compare(SelectColumnData o1, SelectColumnData o2) {
                        return Integer.valueOf(o1.getOrder()).compareTo(Integer.valueOf(o2.getOrder()));
                    }
                });
                
                retval.append(getSqlKeywordString(format, "order by"));
                retval.append(getSqlTabString(format));
                
                String comma = "";
                for (SelectColumnData scd : orderbycols) {
                    retval.append(comma);
                    retval.append(this.getTableAlias(scd.getTableData(), tableList, format));
                    retval.append(".");
                    retval.append(scd.getColumnData().getName());
                    
                    if (Constants.SQL_ORDER_BY_DESC.equals(scd.getAscDesc())) {
                        retval.append(" ");
                        retval.append(Constants.SQL_ORDER_BY_DESC);
                    }
                    
                    comma = ", ";
                }
                
                retval.append(getSqlLineBreakString(format));

            }
            
            if (format == SQL_FORMAT_DISPLAY) {
                retval.append("</body>");
            }
        } else {
            retval.append("");
        }
        
        return retval.toString();
    }
    
    private String buildWhereClause(List <TableData> tableList, WhereColumnData wcd, int format) {
        StringBuilder retval = new StringBuilder(128);
        
        if (StringUtils.isNotBlank(wcd.getAndOr())) {
            retval.append(" ");
            retval.append(wcd.getAndOr());
        }
        
        if (StringUtils.isNotBlank(wcd.getOpenParenthesis())) {
            retval.append(" ");
            retval.append(wcd.getOpenParenthesis());
        }
        
        retval.append(" ");
        retval.append(getTableAlias(wcd.getTableData(), tableList, format));
        retval.append(".");
        retval.append(wcd.getColumnData().getName());
        retval.append(" ");
        retval.append(wcd.getOperator());
        retval.append(" ");

        if (Constants.IN.equals(wcd.getOperator())) {
            retval.append(buildInString(wcd));
        } else if (Constants.NULL.equals(wcd.getOperator())) {
            retval.append(" is null");
        } else if (Constants.NOT_NULL.equals(wcd.getOperator())){
            retval.append(" is not null");
        } else if (Constants.LIKE.equals(wcd.getOperator())) {
            retval.append(" like ('");
            retval.append(wcd.getValue());
            retval.append("')");
        } else if (Constants.NOT_LIKE.equals(wcd.getOperator())) {
            retval.append(" not like ('");
            retval.append(wcd.getValue());
            retval.append("')");
        } else {
            int jdbcType = wcd.getColumnData().getDataType();
            if (Utils.isNumericJdbcType(jdbcType)) {
                retval.append(" ");
                retval.append(wcd.getValue());
            } else if (Utils.isDateJdbcType(jdbcType) || Utils.isDateTimeJdbcType(jdbcType)) {
                retval.append(buildDateTimeString(wcd, format));
            } else {
                retval.append(" '");
                retval.append(wcd.getValue());
                retval.append("'");
            }
        }
        
        if (StringUtils.isNotBlank(wcd.getCloseParenthesis())) {
            retval.append(wcd.getCloseParenthesis());
        }
        
        retval.append(getSqlLineBreakString(format));
        
        return retval.toString();
    }
    
    private String buildDateTimeString(WhereColumnData wcd, int format) {
        StringBuilder retval = new StringBuilder(64);
        
        if ((format == SQL_FORMAT_DISPLAY) || (format == SQL_FORMAT_CLIPBOARD)) {
            DatabaseConnection dbconn = Utils.findDatabaseConnectionByName(getMainframe().getConfiguration(), getPlatform().getDatabaseConnectionName());
            if (DatabaseType.ORACLE.equals(dbconn.getType())) {
                retval.append(" to_date('");
                retval.append(wcd.getValue());
                retval.append("', 'YYYY-MM-DD')");
            } else {
                retval.append(" '");
                retval.append(wcd.getValue());
                retval.append("'");
            }
        } else {
            retval.append(" {d '");
            retval.append(wcd.getValue());
            retval.append("'}");
        }
        
        return retval.toString();
    }
    
    private String buildInString(WhereColumnData wcd) {
        StringBuilder retval = new StringBuilder(128);
        retval.append("(");
        if (Utils.isStringJdbcType(wcd.getColumnData().getDataType())) {
            StringTokenizer st = new StringTokenizer(wcd.getValue(), ",");
            String comma = "";
            
            while (st.hasMoreTokens()) {
                retval.append(comma);
                if (Utils.isStringJdbcType(wcd.getColumnData().getDataType())) {
                    retval.append("'");
                }

                retval.append(st.nextToken());

                if (Utils.isStringJdbcType(wcd.getColumnData().getDataType())) {
                    retval.append("'");
                }
                comma = ",";
            }
        } else {
            retval.append(wcd.getValue());
        }
        
        retval.append(")");
        
        return retval.toString();
    }
    
    private boolean isGroupByRequired(List <SelectColumnData> selcols) {
        boolean retval = false;
        int funccnt = 0;
        int nonfunccnt = 0;
        
        for (SelectColumnData scd : selcols) {
            if (StringUtils.isNotBlank(scd.getFunction())) {
                funccnt++;
            } else {
                nonfunccnt++;
            }
            
            if ((funccnt > 0) && (nonfunccnt > 0)) {
                retval = true;
                break;
            }
        }
        
        return retval;
    }
    
    private TableData getParentTableData(TableData td) {
        TableData retval = null;
        
        if (td.getTreeNode() != null) {
            DefaultMutableTreeNode pnode = (DefaultMutableTreeNode)td.getTreeNode().getParent();

            if ((pnode != null) && (pnode.getUserObject() instanceof TableData)) {
                retval = (TableData)pnode.getUserObject();
            }
        }
        
        return retval;
    }
    
    private List <TableData> buildCompleteQueryTableList() {
        List <TableData> retval = new ArrayList<TableData>();
        
        Set <TableData> hs = new HashSet<TableData>();
        
        for (SelectColumnData scd : sqlSelectPanel.getColumnData()) {
            TableData td = scd.getTableData();
            hs.add(td);
            
            if (td.getTreeNode() != null) {
                // run up the tree for each table to make sure
                // we have all linking tables even if no columns selected
                // for intermediate tables
                DefaultMutableTreeNode pnode = (DefaultMutableTreeNode)td.getTreeNode().getParent();

                while (pnode != null) {
                    if (pnode.getUserObject() instanceof TableData) {
                        hs.add((TableData)pnode.getUserObject());
                    }

                    pnode = (DefaultMutableTreeNode)pnode.getParent();
                }
            }
        }
        
        for (WhereColumnData wcd : sqlWherePanel.getColumnData()) {
            TableData td = wcd.getTableData();
            hs.add(td);
            
            if (td.getTreeNode() != null) {
                // run up the tree for each table to make sure
                // we have all linking tables even if no columns selected
                // for intermediate tables
                DefaultMutableTreeNode pnode = (DefaultMutableTreeNode)td.getTreeNode().getParent();

                while (pnode != null) {
                    if (pnode.getUserObject() instanceof TableData) {
                        hs.add((TableData)pnode.getUserObject());
                    }

                    pnode = (DefaultMutableTreeNode)pnode.getParent();
                }
            }
        }

        
        retval.addAll(hs);
        
        Collections.sort(retval, new SqlHierarchyComparator());
        
        if (LOG.isDebugEnabled()) {
            for (TableData td : retval) {
                int level = -1;
                if (td.getTreeNode() != null) {
                    level = td.getTreeNode().getLevel();
                }

                LOG.debug("table=" + td.getName() + ", level=" + level);
            }
        }
    
        return retval;
    }
    
    @Override
    protected void handleUnprocessedActionEvent(ActionEvent e) {
        if (e.getSource() == tableDropdown) {
            final TableData td = (TableData)tableDropdown.getSelectedItem();

            if (StringUtils.isNotBlank(td.getName())) {
                new SplashDisplay(getMainframe(), "Related Tables", "Loading table relationships...") {
                    @Override
                    protected void runProcess() {
                        try {
                            DefaultMutableTreeNode rootNode = sqlQueryTree.getRootNode();
                            rootNode.removeAllChildren();
                            loadTables(td, rootNode);
                        }

                        catch (Exception ex) {
                            UIUtils.showError(getMainframe(), "Error loading table relationships", "An error occured while loading table relationships - " + ex.toString());
                        }
                    }

                    @Override
                    protected void processCompleted() {
                        DefaultMutableTreeNode rootNode = sqlQueryTree.getRootNode();
                        getCreateCheckpoint().setEnabled(rootNode.getChildCount() > 0);
                        sqlQueryTree.getModel().nodeStructureChanged(rootNode);
                        sqlQueryTree.expandNode(rootNode, 1);
                        sqlSelectPanel.clear();
                        sqlWherePanel.clear();
                    }
                };
            }
        }
    }
    
    public boolean isValidSqlQuery() {
        boolean retval = false;
        
        if (sqlSelectPanel.haveEntries() && sqlWherePanel.haveEntries()) {
            retval = isGeneratedSqlValid();
        } else {
            UIUtils.showError(this, "Incomplete Entry", "SQL select and where column entry required");
        }
        
        return retval;
    }
    
    private boolean isGeneratedSqlValid() {
        boolean retval = false;
        Connection conn = null;
        Statement stmt = null;
        ResultSet res = null;
        DatabaseConnection dbconn = Utils.findDatabaseConnectionByName(getMainframe().getConfiguration(), getPlatform().getDatabaseConnectionName());

        if (dbconn != null) {
            try {
                conn = Utils.getDatabaseConnection(getMainframe().getConfiguration(), dbconn);
                stmt = conn.createStatement();

                String sql = getSqlQueryString(SQL_FORMAT_VALIDATE);
                
                if (LOG.isDebugEnabled()) {
                    LOG.error(sql);
                }
                
                res = stmt.executeQuery(sql);
                
                retval = true;
            }
            
            catch (Exception ex) {
                UIUtils.showError(this, "SQL Syntax Error", ex.toString());
            }
            
            finally {
                Utils.closeDatabaseResources(conn, stmt, res);
            }
        }
        
        return retval;
    }

    public boolean isForCheckpoint() {
        return forCheckpoint;
    }
    
    public void addTab(String title, JPanel panel) {
        tabbedPane.addTab(title, panel);
    }
}
