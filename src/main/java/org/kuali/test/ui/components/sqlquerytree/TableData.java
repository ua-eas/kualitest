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
import org.apache.commons.lang3.StringUtils;


public class TableData extends DBObjectData {
    private List <ColumnData> columns = new ArrayList<ColumnData>();
    private List <TableData> relatedTables = new ArrayList<TableData>();

    public TableData(String name, String displayName) {
        super(name, displayName);
    }

    @Override
    public String toString() {
        String retval = getName();
        if (StringUtils.isNotBlank(getDisplayName())) {
            if (getDisplayName().endsWith("Impl")) {
                retval = getDisplayName().substring(0, getDisplayName().length() - 4);
            } else {
                retval =  getDisplayName();
            }
        } 

        return retval;
    }

    @Override
    public int compareTo(Object o) {
        return toString().compareTo(o.toString());
    }

    public List<ColumnData> getColumns() {
        return columns;
    }

    public void setColumns(List<ColumnData> columns) {
        this.columns = columns;
    }
    
    public void addColumn(ColumnData col) {
        columns.add(col);
    }

    public List<TableData> getRelatedTables() {
        return relatedTables;
    }

    public void setRelatedTables(List<TableData> relatedTables) {
        this.relatedTables = relatedTables;
    }
    
    public void addRelatedTable(TableData table) {
        relatedTables.add(table);
    }
}

