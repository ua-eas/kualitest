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

package org.kuali.test.ui.components.webservicetree;

import javax.swing.tree.DefaultMutableTreeNode;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.impl.values.XmlValueDisconnectedException;
import org.kuali.test.KualiTestConfigurationDocument;
import org.kuali.test.WebService;

/**
 *
 * @author rbtucker
 */
public class WebServiceNode extends DefaultMutableTreeNode {

    /**
     *
     */
    protected static Logger LOG = Logger.getLogger(WebServiceNode.class);
    private KualiTestConfigurationDocument.KualiTestConfiguration configuration;
    
    // if platforms are passed in then this is root - handle a little differently

    /**
     *
     * @param configuration
     * @param userObject
     */
        public WebServiceNode(KualiTestConfigurationDocument.KualiTestConfiguration configuration, Object userObject) {
        super(userObject);
        this.configuration = configuration;
        
        if (isRoot()) {
            for (WebService ws : configuration.getWebServices().getWebServiceArray()) {
                add(new WebServiceNode(configuration, ws));
            }
        } else if (userObject instanceof WebService) {
            WebService ws = (WebService)userObject;
            if (LOG.isDebugEnabled()) {
                LOG.debug("web service" );
                LOG.debug("  name: " + ws.getName());
                LOG.debug("  wsdl: " + ws.getWsdlUrl());
            }
            
            add(new WebServiceNode(configuration, ws.getWsdlUrl()));
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
                    WebService ws = (WebService)getUserObject();
                    retval = ws.getName();
                }
            }
        }
        
        catch (XmlValueDisconnectedException ex) {
        }

        return retval;
    }
}
