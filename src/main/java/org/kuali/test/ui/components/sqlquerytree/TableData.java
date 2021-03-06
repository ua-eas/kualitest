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

package org.kuali.test.ui.components.sqlquerytree;

import java.util.ArrayList;
import java.util.List;
import javax.swing.tree.DefaultMutableTreeNode;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author rbtucker
 */
public class TableData extends DBObjectData {
    private String schema;
    private DefaultMutableTreeNode treeNode;
    private List <ColumnData> columns = new ArrayList<ColumnData>();
    private List <TableData> relatedTables = new ArrayList<TableData>();
    private List <String[]> linkColumns = null;
    private String foreignKeyName = null;
    private boolean outerJoin = false;
    
    /**
     *
     */
    public TableData() {
        super(null, null, null);
    }

    /**
     *
     * @param schema
     * @param name
     * @param displayName
     */
    public TableData(String schema, String name, String displayName) {
        super(schema, name, displayName);
    }

    @Override
    public String toString() {
        String retval = "";
        
        StringBuilder buf = new StringBuilder(128);
        if (StringUtils.isNotBlank(getDisplayName())) {
            if (getDisplayName().endsWith("Impl")) {
                buf.append(getDisplayName().substring(0, getDisplayName().length() - 4));
            } else {
                buf.append(getDisplayName());
            }
            
            retval = buf.toString();
        } else if (StringUtils.isNotBlank(getName())) {
            buf.append(getName());
        }

        if (buf.length() > 0) {
            retval = buf.toString();
        }
        
        return retval;
    }

    @Override
    public int compareTo(Object o) {
        return toString().compareTo(o.toString());
    }

    /**
     *
     * @return
     */
    public List<ColumnData> getColumns() {
        return columns;
    }

    /**
     *
     * @param columns
     */
    public void setColumns(List<ColumnData> columns) {
        this.columns = columns;
    }
    
    /**
     *
     * @param col
     */
    public void addColumn(ColumnData col) {
        columns.add(col);
    }

    /**
     *
     * @return
     */
    public List<TableData> getRelatedTables() {
        return relatedTables;
    }

    /**
     *
     * @param relatedTables
     */
    public void setRelatedTables(List<TableData> relatedTables) {
        this.relatedTables = relatedTables;
    }
    
    /**
     *
     * @param table
     */
    public void addRelatedTable(TableData table) {
        relatedTables.add(table);
    }
    
    /**
     *
     * @return
     */
    public List <String[]> getLinkColumns() {
        if (linkColumns == null) {
            linkColumns = new ArrayList<String[]>();
        }
        
        return linkColumns;
    }

    /**
     *
     * @return
     */
    public String getForeignKeyName() {
        return foreignKeyName;
    }

    /**
     *
     * @param foreignKeyName
     */
    public void setForeignKeyName(String foreignKeyName) {
        this.foreignKeyName = foreignKeyName;
    }

    /**
     *
     * @return
     */
    public String getFullTableName() {
        StringBuilder retval = new StringBuilder(128);

        retval.append(getDbTableName());
        retval.append(".");
        if (StringUtils.isNotBlank(foreignKeyName)) {
            retval.append(getForeignKeyName());
        } else {
            retval.append("basetable");
        }
        
        return retval.toString();
    }

    @Override
    public boolean equals(Object obj) {
        boolean retval = false;
        
        if (obj == this) {
            retval = true;
        } else if (obj instanceof TableData) {
            retval = getFullTableName().equals(((TableData)obj).getFullTableName());
        }
        
        return retval;
    }

    @Override
    public int hashCode() {
        return getFullTableName().hashCode();
    }

    /**
     *
     * @return
     */
    public boolean isOuterJoin() {
        return outerJoin;
    }

    /**
     *
     * @param outerJoin
     */
    public void setOuterJoin(boolean outerJoin) {
        this.outerJoin = outerJoin;
    }

    /**
     *
     * @return
     */
    public DefaultMutableTreeNode getTreeNode() {
        return treeNode;
    }

    /**
     *
     * @param treeNode
     */
    public void setTreeNode(DefaultMutableTreeNode treeNode) {
        this.treeNode = treeNode;
    }
    
    /**
     *
     * @return
     */
    public String getDbTableName() {
        StringBuilder retval = new StringBuilder(128);
        
        retval.append(schema);
        retval.append(".");
        retval.append(getName());
        
        return retval.toString();
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        TableData retval = new TableData();
        return retval;
    }
}

