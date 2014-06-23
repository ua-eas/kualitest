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

import org.kuali.test.ui.components.sqlquerytree.ColumnData;
import org.kuali.test.ui.components.sqlquerytree.TableData;

/**
 *
 * @author rbtucker
 */
public class BaseColumnData {
    private TableData tableData;
    private ColumnData columnData;

    /**
     *
     * @return
     */
    public TableData getTableData() {
        return tableData;
    }

    /**
     *
     * @param tableData
     */
    public void setTableData(TableData tableData) {
        this.tableData = tableData;
    }

    /**
     *
     * @return
     */
    public ColumnData getColumnData() {
        return columnData;
    }

    /**
     *
     * @param columnData
     */
    public void setColumnData(ColumnData columnData) {
        this.columnData = columnData;
    }
}
