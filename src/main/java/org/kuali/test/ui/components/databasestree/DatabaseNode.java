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

package org.kuali.test.ui.components.databasestree;

import javax.swing.tree.DefaultMutableTreeNode;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.impl.values.XmlValueDisconnectedException;
import org.kuali.test.DatabaseConnection;
import org.kuali.test.KualiTestConfigurationDocument;

/**
 *
 * @author rbtucker
 */
public class DatabaseNode extends DefaultMutableTreeNode {
    protected static final Logger LOG = Logger.getLogger(DatabaseNode.class);
    private KualiTestConfigurationDocument.KualiTestConfiguration configuration;

    /**
     *
     * @param configuration
     * @param userObject
     */
        public DatabaseNode(KualiTestConfigurationDocument.KualiTestConfiguration configuration, Object userObject) {
        super(userObject);
        this.configuration = configuration;
        
        if (isRoot()) {
            // if we have any platform definitions without a corresponding directory then create
            for (DatabaseConnection dbconn :configuration.getDatabaseConnections().getDatabaseConnectionArray()) {
                add(new DatabaseNode(configuration, dbconn));
            }
        } else if (userObject instanceof DatabaseConnection) {
            DatabaseConnection dbconn = (DatabaseConnection)userObject;
            if (LOG.isDebugEnabled()) {
                LOG.debug("database connection" );
                LOG.debug("  name: " + dbconn.getName());
                LOG.debug("  type: " + dbconn.getType());
                LOG.debug("driver: " + dbconn.getJdbcDriver());
                LOG.debug("    url: " + dbconn.getJdbcUrl());
            }
            
            add(new DatabaseNode(configuration, "  name: " + dbconn.getName()));
            add(new DatabaseNode(configuration, "  type: " + dbconn.getType()));
            add(new DatabaseNode(configuration, "driver: " + dbconn.getJdbcDriver()));
            add(new DatabaseNode(configuration, "   url: " + dbconn.getJdbcUrl()));
        }
    }
    
    @Override
    public boolean isLeaf() {
        return (getUserObject() instanceof String);
    }

    @Override
    public boolean isRoot() {
        return(getUserObject() == null);
    }
    
    @Override
    public String toString() {
        String retval = "unknown";
        try {
            Object o = getUserObject();
            if (o != null) {
                if (o instanceof String) {
                    retval = (String)o;
                } else {
                    DatabaseConnection dbconn = (DatabaseConnection)getUserObject();
                    retval = dbconn.getName();
                }
            }
        }
        
        catch (XmlValueDisconnectedException ex) {
        }

        return retval;
    }
}
