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

/**
 *
 * @author rbtucker
 */
public class WhereColumnData extends BaseColumnData {
    private String openParenthesis;
    private String closeParenthesis;
    private String andOr;
    private String value;
    private String operator;
    private int row;
    private int column;

    /**
     *
     * @return
     */
    public String getOpenParenthesis() {
        return openParenthesis;
    }

    /**
     *
     * @param openParenthesis
     */
    public void setOpenParenthesis(String openParenthesis) {
        this.openParenthesis = openParenthesis;
    }

    /**
     *
     * @return
     */
    public String getCloseParenthesis() {
        return closeParenthesis;
    }

    /**
     *
     * @param closeParenthesis
     */
    public void setCloseParenthesis(String closeParenthesis) {
        this.closeParenthesis = closeParenthesis;
    }

    /**
     *
     * @return
     */
    public String getAndOr() {
        return andOr;
    }

    /**
     *
     * @param andOr
     */
    public void setAndOr(String andOr) {
        this.andOr = andOr;
    }

    /**
     *
     * @return
     */
    public String getValue() {
        return value;
    }

    /**
     *
     * @param value
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     *
     * @return
     */
    public String getOperator() {
        return operator;
    }

    /**
     *
     * @param operator
     */
    public void setOperator(String operator) {
        this.operator = operator;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }
    
    
}
