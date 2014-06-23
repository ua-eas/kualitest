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

package org.kuali.test.ui.base;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JLabel;
import org.kuali.test.utils.Constants;

/**
 *
 * @author rbtucker
 */
public class TableConfiguration {
    private String tableName;
    private String displayName;
    private String[] headers;
    private String[] propertyNames;
    private int[] columnWidths = new int[0];
    private int[] columnAlignment = new int[0];
    private Class[] columnTypes = new Class[0];
    private List data;

    /**
     *
     * @return
     */
    public String getTableName() {
        return tableName;
    }

    /**
     *
     * @param tableName
     */
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
    
    /**
     *
     * @return
     */
    public String[] getHeaders() {
        return headers;
    }

    /**
     *
     * @param headers
     */
    public void setHeaders(String[] headers) {
        this.headers = headers;
    }

    /**
     *
     * @return
     */
    public String[] getPropertyNames() {
        return propertyNames;
    }

    /**
     *
     * @param propertyNames
     */
    public void setPropertyNames(String[] propertyNames) {
        this.propertyNames = propertyNames;
    }

    /**
     *
     * @return
     */
    public int[] getColumnWidths() {
        if (columnWidths.length == 0) {
            columnWidths = new int[headers.length];
            
            for (int i = 0; i < columnWidths.length; ++i) {
                columnWidths[i] = Constants.DEFAULT_TABLE_COLUMN_WIDTH;
            }
        }

        return columnWidths;
    }

    /**
     *
     * @param columnWidths
     */
    public void setColumnWidths(int[] columnWidths) {
        this.columnWidths = columnWidths;
    }

    /**
     *
     * @return
     */
    public int[] getColumnAlignment() {
        if (columnAlignment.length == 0) {
            columnAlignment = new int[headers.length];
            
            for (int i = 0; i < columnAlignment.length; ++i) {
                columnAlignment[i] = JLabel.LEFT;
            }
        }

        return columnAlignment;

    }

    /**
     *
     * @param columnAlignment
     */
    public void setColumnAlignment(int[] columnAlignment) {
        this.columnAlignment = columnAlignment;
    }

    /**
     *
     * @return
     */
    public Class[] getColumnTypes() {
        if (columnTypes.length == 0) {
            columnTypes = new Class[headers.length];
            
            for (int i = 0; i < columnTypes.length; ++i) {
                columnTypes[i] = String.class;
            }
        }
        
        return columnTypes;
    }

    /**
     *
     * @param columnTypes
     */
    public void setColumnTypes(Class[] columnTypes) {
        this.columnTypes = columnTypes;
    }

    /**
     *
     * @return
     */
    public List getData() {
        if (data == null) {
            data = new ArrayList();
        }
        return data;
    }

    /**
     *
     * @param data
     */
    public void setData(List data) {
        this.data = data;
    }

    /**
     *
     * @return
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     *
     * @param displayName
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
