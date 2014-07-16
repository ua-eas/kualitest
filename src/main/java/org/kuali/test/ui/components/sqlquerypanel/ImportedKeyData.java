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

import java.sql.ResultSet;
import java.sql.SQLException;


public class ImportedKeyData {
    private String pkSchema;
    private String pkTable;
    private String pkColumn;
    private String fkColumn;
    private String fkName;
    
    public ImportedKeyData(ResultSet res) throws SQLException {
        pkSchema = res.getString(2);
        pkTable = res.getString(3);
        pkColumn = res.getString(4);
        fkColumn = res.getString(8);
        fkName = res.getString(12);
    }

    public String getPkSchema() {
        return pkSchema;
    }

    public void setPkSchema(String pkSchema) {
        this.pkSchema = pkSchema;
    }

    public String getPkTable() {
        return pkTable;
    }

    public void setPkTable(String pkTable) {
        this.pkTable = pkTable;
    }

    public String getPkColumn() {
        return pkColumn;
    }

    public void setPkColumn(String pkColumn) {
        this.pkColumn = pkColumn;
    }

    public String getFkColumn() {
        return fkColumn;
    }

    public void setFkColumn(String fkColumn) {
        this.fkColumn = fkColumn;
    }

    public String getFkName() {
        return fkName;
    }

    public void setFkName(String fkName) {
        this.fkName = fkName;
    }
    
    
}
