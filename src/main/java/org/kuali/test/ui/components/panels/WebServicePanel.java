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

package org.kuali.test.ui.components.panels;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.dynamic.DynamicClientFactory;
import org.apache.log4j.Logger;
import org.kuali.test.Platform;
import org.kuali.test.TestHeader;
import org.kuali.test.creator.TestCreator;


public class WebServicePanel extends BaseCreateTestPanel {
    private static final Logger LOG = Logger.getLogger(WebServicePanel.class);
    
    public WebServicePanel(TestCreator mainframe, Platform platform, TestHeader testHeader) {
        super(mainframe, platform, testHeader);
    }

    @Override
    protected void handleCancelTest() {
    }
   
    @Override
    protected void handleStartTest() {
        DynamicClientFactory dcf = DynamicClientFactory.newInstance();

        if (LOG.isDebugEnabled()) {
            LOG.debug("ws url: " + getPlatform().getWebServiceUrl());
        }
        
        Client client = dcf.createClient(getPlatform().getWebServiceUrl());
    }
    
    @Override
    protected void handleCreateCheckpoint() {
    }

    @Override
    protected boolean handleSaveTest() {
        return false;
    }

    protected boolean isStartTestRequired() { 
        return true; 
    }
}
