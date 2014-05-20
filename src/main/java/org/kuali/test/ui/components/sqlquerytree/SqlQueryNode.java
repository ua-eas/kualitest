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

import javax.swing.tree.DefaultMutableTreeNode;
import org.apache.log4j.Logger;
import org.kuali.test.KualiTestConfigurationDocument;
import org.kuali.test.Platform;

/**
 *
 * @author rbtucker
 */
public class SqlQueryNode extends DefaultMutableTreeNode {
    protected static Logger LOG = Logger.getLogger(SqlQueryNode.class);
    private KualiTestConfigurationDocument.KualiTestConfiguration configuration;
    
    // if platforms are passed in then this is root - handle a little differently
    public SqlQueryNode(KualiTestConfigurationDocument.KualiTestConfiguration configuration, Object userObject) {
        super(userObject);
        this.configuration = configuration;
        
        try {
        }
        
        catch (Exception ex) {
            LOG.error(ex.toString(), ex);
        }
    }

    @Override
    public boolean isLeaf() {
        return (getUserObject() instanceof ColumnData);
    }
    
    @Override
    public boolean isRoot() {
        return (getUserObject() instanceof Platform);
    }

    @Override
    public String toString() {
        if (isRoot()) {
            Platform p = (Platform)getUserObject();
            return p.getDatabaseConnectionName();
        } else {
            return getUserObject().toString();
        }
    }
}
