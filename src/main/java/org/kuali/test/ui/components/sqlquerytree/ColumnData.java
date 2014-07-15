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

/**
 *
 * @author rbtucker
 */
public class ColumnData extends DBObjectData {
    private Integer primaryKeyIndex = Integer.MAX_VALUE;
    private int dataType;
    private int width = 0;
    private int decimalDigits = 0;
    boolean selected = false;

    /**
     *
     * @param schema
     * @param name
     * @param displayName
     */
    public ColumnData(String schema, String name, String displayName) {
        super(schema, name, displayName);
    }

    /**
     *
     * @return
     */
    public Integer getPrimaryKeyIndex() {
        return primaryKeyIndex;
    }

    /**
     *
     * @param primaryKeyIndex
     */
    public void setPrimaryKeyIndex(Integer primaryKeyIndex) {
        this.primaryKeyIndex = primaryKeyIndex;
    }

    /**
     *
     * @return
     */
    public int getDataType() {
        return dataType;
    }

    /**
     *
     * @param dataType
     */
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

    /**
     *
     * @return
     */
    public int getWidth() {
        return width;
    }

    /**
     *
     * @param width
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     *
     * @return
     */
    public int getDecimalDigits() {
        return decimalDigits;
    }

    /**
     *
     * @param decimalDigits
     */
    public void setDecimalDigits(int decimalDigits) {
        this.decimalDigits = decimalDigits;
    }

    /**
     *
     * @return
     */
    public boolean isSelected() {
        return selected;
    }

    /**
     *
     * @param selected
     */
    public void setSelected(boolean selected) {
        this.selected = selected;
    }
    
    /**
     *
     * @return
     */
    public String getDataTypeName() {
        return Utils.getJdbcTypeName(getDataType(), getDecimalDigits());
    }
    
    @Override
    public String toString() {
        StringBuilder retval = new StringBuilder(64);
        
        retval.append("<html><span style='white-space: nowrap; font:10px arial,sans-serif'>");
        
        if (StringUtils.isNotBlank(getDisplayName())) {
            retval.append(getDisplayName());
        } else {
            retval.append(getName());
        }
        
        retval.append(" [");
        retval.append(Utils.buildHtmlStyle(Constants.HTML_DARK_RED_STYLE, getDataTypeName()));
        retval.append("]</span></html>");
        
        return retval.toString();
    }

    @Override
    public boolean equals(Object obj) {
        boolean retval = false;
        if (obj == this) {
            retval = true;
        } else if (obj instanceof ColumnData) {
            ColumnData cd = (ColumnData)obj;
            retval = getName().endsWith(cd.getName());
        }
        
        return retval;
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        ColumnData retval = new ColumnData(getSchema(), getName(), getDisplayName());
        
        retval.setDataType(dataType);
        retval.setDecimalDigits(decimalDigits);
        retval.setPrimaryKeyIndex(primaryKeyIndex);
        retval.setSelected(false);
        retval.setWidth(width);

        return retval;
    }
}
