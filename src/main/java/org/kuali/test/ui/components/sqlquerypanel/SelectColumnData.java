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
public class SelectColumnData extends BaseColumnData {
    private String function;
    private String order;
    private String ascDesc;

    /**
     *
     * @return
     */
    public String getFunction() {
        return function;
    }

    /**
     *
     * @param function
     */
    public void setFunction(String function) {
        this.function = function;
    }

    /**
     *
     * @return
     */
    public String getOrder() {
        return order;
    }

    /**
     *
     * @param order
     */
    public void setOrder(String order) {
        this.order = order;
    }

    /**
     *
     * @return
     */
    public String getAscDesc() {
        return ascDesc;
    }

    /**
     *
     * @param ascDesc
     */
    public void setAscDesc(String ascDesc) {
        this.ascDesc = ascDesc;
    }
}
