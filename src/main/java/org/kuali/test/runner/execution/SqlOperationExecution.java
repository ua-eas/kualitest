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

package org.kuali.test.runner.execution;

import java.io.File;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.kuali.test.CheckpointProperty;
import org.kuali.test.DatabaseConnection;
import org.kuali.test.KualiTestConfigurationDocument;
import org.kuali.test.Operation;
import org.kuali.test.Platform;
import org.kuali.test.runner.exceptions.TestException;
import org.kuali.test.utils.Constants;
import org.kuali.test.utils.Utils;


public class SqlOperationExecution extends AbstractOperationExecution {
    public SqlOperationExecution(Operation op) {
        super(op);
    }
    
    @Override
    public void execute(KualiTestConfigurationDocument.KualiTestConfiguration configuration, Platform platform) throws TestException {
        String sqlQuery = getParameter(Constants.SQL_QUERY);
        boolean saveQueryResults = Boolean.parseBoolean(getParameter(Constants.SAVE_QUERY_RESULTS));
        String errorMessage = null;
        
        try {
            Connection conn = null;
            Statement stmt = null;
            ResultSet res = null;
            PrintWriter pw = null;
            
            try {
                DatabaseConnection dbconn = Utils.findDatabaseConnectionByName(configuration, platform.getDatabaseConnectionName());
                
                if (dbconn == null) {
                    errorMessage = "cannot find database connection information for platform '" + platform.getName() + "'";
                } else {
                    conn = Utils.getDatabaseConnection(configuration, dbconn);
                    stmt = conn.createStatement();
                    res = stmt.executeQuery(sqlQuery);
                    ResultSetMetaData md = res.getMetaData();
                    if (saveQueryResults) {
                        File f = new File(getQueryResultsFileName(configuration, platform));
                        
                        if (!f.getParentFile().exists()) {
                            f.getParentFile().mkdirs();
                        }
                        
                        pw = new PrintWriter(f);
                        writeOutputHeader(pw, md);
                    }

                    int rowcount = 0;
                    
                    while (res.next()) {
                        rowcount++;
                        
                        if (saveQueryResults) {
                            writeOutputLine(pw, res, md.getColumnCount());
                        }
                    }
                    
                    CheckpointProperty cp = getProperty(Constants.ROW_COUNT);
                    cp.setActualValue("" + rowcount);
                    if (!evaluateCheckpointProperty(cp)) {
                        throw new TestException("row count of " 
                            + rowcount 
                            + " does not match comparison value", getOperation());
                    } 
                }
            }

            finally {
                if (pw != null) {
                    pw.close();
                }
                
                Utils.closeDatabaseResources(conn, stmt, res);
            }
        }
        
        catch (SQLException ex) {
            throw new TestException("sql exception occured while executing query on database '" 
                + platform.getDatabaseConnectionName() + "' - " + ex.toString(), getOperation(), ex);
        }
        
        catch (Exception ex) {
            throw new TestException("exception occured while connecting to database '" 
                + platform.getDatabaseConnectionName() + "' - " + ex.toString(), getOperation(), ex);
        }
    }
    
    private String getQueryResultsFileName(KualiTestConfigurationDocument.KualiTestConfiguration configuration, 
        Platform platform) {
        StringBuilder retval = new StringBuilder(256);
        
        retval.append(configuration.getTestResultLocation());
        retval.append("/");
        retval.append(platform.getName());
        retval.append("/query-results/");
        retval.append(Constants.DEFAULT_DATE_FORMAT.format(new Date()));
        retval.append("/");
        retval.append(getOperation().getCheckpointOperation().getTestName());
        retval.append(getOperation().getCheckpointOperation().getName().toLowerCase().replace(" ", "-"));
        retval.append("-");
        retval.append(Constants.FILENAME_TIMESTAMP_FORMAT.format(new Date()));
        retval.append(".csv");
        
        return retval.toString();
    }
    
    private void writeOutputHeader(PrintWriter pw, ResultSetMetaData md) throws SQLException {
        List <String> headerList = new ArrayList<String>();
        
        for (int i = 0; i < md.getColumnCount(); ++i) {
            headerList.add(md.getColumnName(i+1));
        }
        
        pw.println(Utils.buildCsvLine(headerList));
    }

    private void writeOutputLine(PrintWriter pw, ResultSet rs, int columnCount) throws SQLException {
        List <String> dataList = new ArrayList<String>();
        
        for (int i = 0; i < columnCount; ++i) {
            dataList.add(rs.getString(i+1));
        }
        
        pw.println(Utils.buildCsvLine(dataList));
    }
}
