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
import javax.swing.table.AbstractTableModel;
import org.apache.log4j.Logger;
import org.kuali.test.utils.Constants;
import org.kuali.test.utils.Utils;

/**
 *
 * @author rbtucker
 */
public class BaseTableModel extends AbstractTableModel {
    private static Logger LOG = Logger.getLogger(BaseTableModel.class);
    
    private TableConfiguration config;
    private List data = new ArrayList();
    
    /**
     *
     * @param config
     */
    public BaseTableModel(TableConfiguration config) {
        super();
        this.config = config;
        if (!config.getData().isEmpty()) {
            data.addAll(config.getData());
        }
    }
    
    @Override
    public boolean isCellEditable(int row, int col) { 
        return false; 
    }

    @Override
    public String getColumnName(int col) {
        return config.getHeaders()[col];
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public int getColumnCount() {
        return config.getHeaders().length;
    }

    @Override
    public Class getColumnClass(int col) {
        return config.getColumnTypes()[col];
    }
    
    @Override
    public Object getValueAt(int row, int col) {
        Object retval = null;
        
        if ((row < data.size()) && (col < config.getPropertyNames().length)) {
            Object o = data.get(row);
            if (!Constants.IGNORE_TABLE_DATA_INDICATOR.equals(config.getPropertyNames()[col])) {
                retval = Utils.getObjectProperty(o, config.getPropertyNames()[col]);
            }
        }
        
        return retval;
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        if ((row < data.size()) && (col < config.getPropertyNames().length)) {
            Object o = data.get(row);
            Utils.setObjectProperty(o, config.getPropertyNames()[col], value);
            fireTableCellUpdated(row, col);
        }
    }

    /**
     *
     * @param inputData
     */
    public void setData(List inputData) {
        data.clear();
        data.addAll(inputData);
        fireTableDataChanged();
    }
    
    /**
     *
     * @return
     */
    public List getData() {
        return data;
    }

    /**
     *
     * @return
     */
    public TableConfiguration getConfig() {
        return config;
    }
}
