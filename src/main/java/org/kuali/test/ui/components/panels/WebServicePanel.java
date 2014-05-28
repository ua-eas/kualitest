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

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.description.AxisOperation;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.test.Platform;
import org.kuali.test.TestHeader;
import org.kuali.test.WebService;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.ui.base.BasePanel;
import org.kuali.test.ui.components.splash.SplashDisplay;
import org.kuali.test.utils.Utils;


public class WebServicePanel extends BaseCreateTestPanel {
    private static final Logger LOG = Logger.getLogger(WebServicePanel.class);
    private JComboBox operations;
    public WebServicePanel(TestCreator mainframe, Platform platform, TestHeader testHeader) {
        super(mainframe, platform, testHeader);
        initComponents();
    }

    private void initComponents() {
    }
    
    @Override
    protected void handleCancelTest() {
    }
   
    @Override
    protected void handleStartTest() {
        final WebService ws = Utils.findWebServiceByName(getMainframe().getConfiguration(), getPlatform().getWebServiceName());
        
        if (ws != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("web service: " + ws.getName());
                LOG.debug("wsdl: " + ws.getWsdlUrl());
            }

            BasePanel p = new BasePanel(getMainframe());
            JPanel p2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
            p2.add(new JLabel("Web Service Operations: "));
            p2.add(operations = new JComboBox());
            operations.addActionListener(this);
            p.add(p2, BorderLayout.NORTH);
            p.add(new JSeparator(), BorderLayout.CENTER);
            add(p, BorderLayout.CENTER);

            new SplashDisplay(getMainframe(), "Loading WSDL", "Loading web service definition...") {
                @Override
                protected void runProcess() {
                    try {
                        ServiceClient wsClient = new ServiceClient(null, new URL(ws.getWsdlUrl()), null, null);
                        Options options = wsClient.getOptions();
                        
                        if (StringUtils.isNotBlank(ws.getUsername())) {
                            options.setUserName(ws.getUsername());
                            options.setPassword(ws.getPassword());
                        }
                        
                        Iterator <AxisOperation> it = wsClient.getAxisService().getOperations();

                        List <OperationWrapper> l = new ArrayList<OperationWrapper>();
                        while (it.hasNext()) {
                           l.add(new OperationWrapper(it.next()));
                        }

                        Collections.sort(l);

                        for (OperationWrapper ow : l) {
                            operations.addItem(ow);
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("operation: " + ow);
                            }
                        }
                    }

                    catch (Exception ex) {
                        LOG.error(ex.toString(), ex);
                    }
                }
            };
        }
    }
    
    @Override
    protected void handleCreateCheckpoint() {
    }

    @Override
    protected boolean handleSaveTest() {
        return false;
    }

    @Override
    protected boolean isStartTestRequired() { 
        return true; 
    }
    
    private class OperationWrapper implements Comparable <OperationWrapper> {
        private AxisOperation operation;
        
        public OperationWrapper(AxisOperation operation) {
            this.operation = operation;
        }

        public AxisOperation getOperation() {
            return operation;
        }

        @Override
        public String toString() {
            return operation.getName().getLocalPart();
        }

        @Override
        public int compareTo(OperationWrapper o) {
            return getOperation().getName().getLocalPart().compareTo(o.getOperation().getName().getLocalPart());
        }
    }

    @Override
    protected void handleUnprocessedActionEvent(ActionEvent e) {
        if (e.getSource() == operations) {
            OperationWrapper ow = (OperationWrapper)operations.getSelectedItem();
            AxisOperation ao = ow.getOperation();
            
            for(org.apache.axis2.description.Parameter param : ao.getParameters()) {
                LOG.error("param: " + param.getName() + ", type=" + param.getParameterType());
            }
        }
    }
}
