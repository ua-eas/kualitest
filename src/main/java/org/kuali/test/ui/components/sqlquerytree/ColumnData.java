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

import org.apache.commons.lang3.StringUtils;
import org.kuali.test.utils.Constants;
import org.kuali.test.utils.Utils;


public class ColumnData extends DBObjectData {
    private Integer primaryKeyIndex = Integer.MAX_VALUE;
    private int dataType;
    boolean selected = false;

    public ColumnData(String schema, String name, String displayName) {
        super(schema, name, displayName);
    }

    public Integer getPrimaryKeyIndex() {
        return primaryKeyIndex;
    }

    public void setPrimaryKeyIndex(Integer primaryKeyIndex) {
        this.primaryKeyIndex = primaryKeyIndex;
    }

    public int getDataType() {
        return dataType;
    }

    public void setDataType(int dataType) {
        this.dataType = dataType;
    }

    @Override
    public int compareTo(Object o) {
        ColumnData cd = (ColumnData)o;
        int retval = getPrimaryKeyIndex().compareTo(cd.getPrimaryKeyIndex());
        
        if (retval == 0) {
            retval = getDisplayName().compareTo(cd.getDisplayName());
        }
        
        return retval;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
    
    
    public String toString() {
        StringBuilder retval = new StringBuilder(64);
        
        retval.append("<html>");
        
        if (StringUtils.isNotBlank(getDisplayName())) {
            retval.append(getDisplayName());
        } else {
            retval.append(getName());
        }
        
        retval.append("[<span style='color: ");
        retval.append(Constants.COLOR_DARK_RED);
        retval.append("; font-weight: 700;'>");
        retval.append(Utils.getJdbcTypeName(getDataType()));
        retval.append("</span>]");
        
        return retval.toString();
    }
}
