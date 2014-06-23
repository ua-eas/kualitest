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

package org.kuali.test.ui.components.jmxtree;

import javax.swing.tree.DefaultMutableTreeNode;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.impl.values.XmlValueDisconnectedException;
import org.kuali.test.JmxConnection;
import org.kuali.test.KualiTestConfigurationDocument;

/**
 *
 * @author rbtucker
 */
public class JmxNode extends DefaultMutableTreeNode {

    /**
     *
     */
    protected static Logger LOG = Logger.getLogger(JmxNode.class);
    private KualiTestConfigurationDocument.KualiTestConfiguration configuration;
    
    /**
     *
     * @param configuration
     * @param userObject
     */
    public JmxNode(KualiTestConfigurationDocument.KualiTestConfiguration configuration, Object userObject) {
        super(userObject);
        this.configuration = configuration;
        
        if (isRoot()) {
            if (configuration.getJmxConnections() != null) {
                for (JmxConnection jmx: configuration.getJmxConnections().getJmxConnectionArray()) {
                    add(new JmxNode(configuration, jmx));
                }
            }
        } else if (userObject instanceof JmxConnection) {
            JmxConnection jmx = (JmxConnection)userObject;
            if (LOG.isDebugEnabled()) {
                LOG.debug("jmx connection" );
                LOG.debug("  name: " + jmx.getName());
                LOG.debug("   url: " + jmx.getJmxUrl());
            }
            
            add(new JmxNode(configuration,jmx.getJmxUrl()));
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
                    JmxConnection jmx = (JmxConnection)getUserObject();
                    retval = jmx.getName();
                }
            }
        }
        
        catch (XmlValueDisconnectedException ex) {
        }

        return retval;
    }
}
