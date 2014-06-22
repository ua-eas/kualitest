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
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.xml.namespace.QName;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.description.AxisOperation;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaObject;
import org.apache.ws.commons.schema.XmlSchemaParticle;
import org.apache.ws.commons.schema.XmlSchemaSequence;
import org.kuali.test.Checkpoint;
import org.kuali.test.FailureAction;
import org.kuali.test.Operation;
import org.kuali.test.Platform;
import org.kuali.test.TestHeader;
import org.kuali.test.TestOperation;
import org.kuali.test.TestOperationType;
import org.kuali.test.WebService;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.ui.base.BaseTable;
import org.kuali.test.ui.base.TableConfiguration;
import org.kuali.test.ui.components.dialogs.WebServiceCheckPointDlg;
import org.kuali.test.ui.components.splash.SplashDisplay;
import org.kuali.test.ui.utils.UIUtils;
import org.kuali.test.utils.Utils;


public class WebServicePanel extends BaseCreateTestPanel {
    private static final Logger LOG = Logger.getLogger(WebServicePanel.class);
    private List <TestOperation> testOperations = new ArrayList<TestOperation>();
    private BaseTable inputParameters;
    private JComboBox operations;
    private XmlSchema xmlSchema;
    private JLabel returnType;
    private JTextField expectedResult;
    private JComboBox failureAction;
    private boolean forCheckpoint;
    
    public WebServicePanel(TestCreator mainframe, Platform platform, TestHeader testHeader) {
        this (mainframe, platform, testHeader, false);
    }
    
    public WebServicePanel(TestCreator mainframe, Platform platform, TestHeader testHeader, boolean forCheckpoint) {
        super(mainframe, platform, testHeader);
        this.forCheckpoint = forCheckpoint;
        initComponents();
    }

      @Override
    protected JToolBar createToolbar() {
        if (forCheckpoint) {
            handleStartTest();
            return null;
        } else {
            return super.createToolbar();
        }
    }
    
    private BaseTable createInputParametersTable() {
        TableConfiguration tc = new TableConfiguration();
        tc.setHeaders(new String[] {
            "Parameter Name",
            "Type",
            "Value",
        });
        
        tc.setPropertyNames(new String[] {
            "parameterName",
            "parameterType",
            "value",
        });
        
        tc.setColumnTypes(new Class[] {
            String.class,
            String.class,
            String.class
        });
        
        tc.setColumnWidths(new int[] {
            30,
            15,
            20
        });

        tc.setTableName("web-service-input-parameters");
        tc.setDisplayName("Input Parameters");
        
        return new BaseTable(tc) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return (column == 2);
            }
        };
    }
    
    @Override
    protected void handleCancelTest() {
        getMainframe().getCreateTestPanel().clearPanel("test '" + getTestHeader().getTestName() + "' cancelled");
        getMainframe().getCreateTestButton().setEnabled(true);
        getMainframe().getCreateTestMenuItem().setEnabled(true);
        testOperations.clear();
    }
   
    @Override
    protected void handleStartTest() {
        final WebService ws = Utils.findWebServiceByName(getMainframe().getConfiguration(), getPlatform().getWebServiceName());
        
        if (ws != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("web service: " + ws.getName());
                LOG.debug("wsdl: " + ws.getWsdlUrl());
            }

            TablePanel tp = new TablePanel(inputParameters = createInputParametersTable());
            
            JPanel p = new JPanel(new BorderLayout());
            JPanel p2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
            p2.add(new JLabel("Web Service Operations: "));
            p2.add(operations = new JComboBox());
            operations.addActionListener(this);
            p.add(p2, BorderLayout.NORTH);
            p.add(new JSeparator(), BorderLayout.CENTER);
            tp.add(p, BorderLayout.NORTH);
            add(tp, BorderLayout.CENTER);
            
            p = new JPanel(new FlowLayout(FlowLayout.LEFT));
            p.add(new JLabel("Return Type: "));
            p.add(returnType = new JLabel("           "));
            p.add(new JLabel("    Expected Result: "));
            p.add(expectedResult = new JTextField(20));
            expectedResult.setEnabled(false);
            p.add(new JLabel("    On Failure: "));
            p.add(failureAction = new JComboBox(Utils.getXmlEnumerations(FailureAction.class)));
            failureAction.setEnabled(false);
            tp.add(p, BorderLayout.SOUTH);
            
            new SplashDisplay(getMainframe(), "Loading WSDL", "Loading web service definition...") {
                @Override
                protected void runProcess() {
                    try {
                        ServiceClient wsClient = new ServiceClient(null, new URL(ws.getWsdlUrl()), null, null);
                        Options options = wsClient.getOptions();

                        if (StringUtils.isNotBlank(ws.getUsername())) {
                            options.setUserName(ws.getUsername());
                            options.setPassword(Utils.decrypt(getMainframe().getEncryptionPassword(), ws.getPassword()));
                        }
                        
                        Iterator <AxisOperation> it = wsClient.getAxisService().getOperations();

                        List <OperationWrapper> l = new ArrayList<OperationWrapper>();
                        while (it.hasNext()) {
                           l.add(new OperationWrapper(it.next()));
                        }

                        Collections.sort(l);

                        l.add(0, new OperationWrapper(null));
                        
                        for (OperationWrapper ow : l) {
                            operations.addItem(ow);
                            if (LOG.isDebugEnabled()) {
                                if (ow.getOperation() != null) {
                                    LOG.debug("operation: " + ow.getOperation().getName());
                                }
                            }
                        }
                        
                        if (LOG.isDebugEnabled()) {
                            ByteArrayOutputStream bos = new ByteArrayOutputStream();
                            wsClient.getAxisService().printSchema(bos);
                            LOG.debug(new String(bos.toByteArray()));
                        }
                        
                        xmlSchema = wsClient.getAxisService().getSchema(0);
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
        if (isValidWebServiceSetup()) {
            WebServiceCheckPointDlg dlg = new WebServiceCheckPointDlg(getMainframe(), getTestHeader(), this);

            if (dlg.isSaved()) {
                addCheckpoint((Checkpoint)dlg.getNewRepositoryObject());
                getSaveTest().setEnabled(true);
            }
        } 
    }
    
    public boolean isValidWebServiceSetup() {
        boolean retval = false;
        OperationWrapper ow = (OperationWrapper)operations.getSelectedItem();
        
        if ((ow != null) && (ow.getOperation() != null)) {
            List <WebServiceInputParameter> l = inputParameters.getTableData();
            if (!l.isEmpty()) {
                retval = true;
                for (WebServiceInputParameter param : l) {
                    if (StringUtils.isBlank(param.getValue())) {
                        UIUtils.showError(getMainframe(), "Input parameter missing", "Please provide values for all input parameters");
                        retval = false;
                        break;
                    }
                }
            } else {
                retval = true;
            }
        } else {
            UIUtils.showError(getMainframe(), "Web Servive Operation Required", "Plase select a web service operation");
        }
        
        return retval;
    }
    private void addCheckpoint(Checkpoint checkpoint) {
        TestOperation testOp = TestOperation.Factory.newInstance();
        testOp.setOperationType(TestOperationType.CHECKPOINT);
        Operation op = testOp.addNewOperation();
        op.addNewCheckpointOperation();
        op.setCheckpointOperation(checkpoint);
        testOperations.add(testOp);
    }

    @Override
    protected boolean handleSaveTest() {
        boolean retval = saveTest(getMainframe().getConfiguration().getRepositoryLocation(),
            getTestHeader(), testOperations);

        if (retval) {
            getMainframe().getTestRepositoryTree().saveConfiguration();
            getMainframe().getCreateTestPanel().clearPanel("test '" + getTestHeader().getTestName() + "' created");
        }

        return retval;
    }

    @Override
    protected boolean isStartTestRequired() { 
        return true; 
    }
    
    private class OperationWrapper implements Comparable <OperationWrapper> {
        private final AxisOperation operation;
        
        public OperationWrapper(AxisOperation operation) {
            this.operation = operation;
        }

        public AxisOperation getOperation() {
            return operation;
        }

        @Override
        public String toString() {
            if (operation == null) {
                return "";
            } else {
                return operation.getName().getLocalPart();
            }
        }

        @Override
        public int compareTo(OperationWrapper o) {
            if (operation == null) {
                return -1;
            } else if (o.getOperation() == null) {
                return 1;
            } else {
                return getOperation().getName().getLocalPart().compareTo(o.getOperation().getName().getLocalPart());
            }
        }
    }

    @Override
    protected void handleUnprocessedActionEvent(ActionEvent e) {
        if (e.getSource() == operations) {
            OperationWrapper ow = (OperationWrapper)operations.getSelectedItem();
            AxisOperation ao = ow.getOperation();
            if (ao != null) {
                if (xmlSchema != null) {
                    XmlSchemaElement element = xmlSchema.getElementByName(ao.getName());
                    populateInputParametersTable(element);
                    
                    // build a QName for the response
                    QName qname = new QName(ao.getName().getNamespaceURI(), 
                        ao.getName().getLocalPart() + "Response",  ao.getName().getPrefix());
                    element = xmlSchema.getElementByName(qname);
                    populateExpectedResultFields(element);
                }
            }
        }
    }

    private void populateInputParametersTable(XmlSchemaElement parentElement) {
        if (parentElement.getSchemaType() instanceof XmlSchemaComplexType) {
            XmlSchemaComplexType type = (XmlSchemaComplexType)parentElement.getSchemaType();
            XmlSchemaParticle particle = type.getParticle();
            
            if (particle instanceof XmlSchemaSequence) {
                XmlSchemaSequence seq = (XmlSchemaSequence)particle;
                
                if (seq.getItems() != null) {
                    List <WebServiceInputParameter> parameters = new ArrayList<WebServiceInputParameter>();
                    Iterator <XmlSchemaObject> it = seq.getItems().getIterator();
                    while (it.hasNext()) {
                        XmlSchemaObject obj = it.next();
                        
                        if (obj instanceof XmlSchemaElement) {
                            XmlSchemaElement element = (XmlSchemaElement)obj;
                            WebServiceInputParameter param = new WebServiceInputParameter();
                            param.setParameterName(element.getName());
                            param.setParameterType(element.getSchemaType().getName());
                            parameters.add(param);
                        }
                    }
                    
                    inputParameters.setTableData(parameters);
                }
            }
        }
    }
    
    private void populateExpectedResultFields(XmlSchemaElement parentElement) {
        boolean foundResultType = false;
        if ((parentElement != null) && (parentElement.getSchemaType() instanceof XmlSchemaComplexType)) {
            XmlSchemaComplexType type = (XmlSchemaComplexType)parentElement.getSchemaType();
            XmlSchemaParticle particle = type.getParticle();
            
            if (particle instanceof XmlSchemaSequence) {
                XmlSchemaSequence seq = (XmlSchemaSequence)particle;
                
                if (seq.getItems() != null) {
                    Iterator <XmlSchemaObject> it = seq.getItems().getIterator();
                    if (it.hasNext()) {
                        XmlSchemaObject obj = it.next();
                        
                        if (obj instanceof XmlSchemaElement) {
                            XmlSchemaElement element = (XmlSchemaElement)obj;
                            returnType.setText(element.getSchemaType().getName());
                            expectedResult.setEnabled(true);
                            failureAction.setEnabled(true);
                            foundResultType = true;
                        }
                    }
                }
            }
        }
        
        if (!foundResultType) {
            returnType.setText("none");
            expectedResult.setText("");
            expectedResult.setEnabled(false);
            failureAction.setSelectedItem(null);
            failureAction.setEnabled(false);
        }
    }

    public class WebServiceInputParameter {
        private String parameterName;
        private String parameterType;
        private String value;

        public String getParameterName() {
            return parameterName;
        }

        public void setParameterName(String parameterName) {
            this.parameterName = parameterName;
        }

        public String getParameterType() {
            return parameterType;
        }

        public void setParameterType(String parameterType) {
            this.parameterType = parameterType;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
    
    public String getWebServiceOperation() {
        String retval = null;
        
        OperationWrapper ow = (OperationWrapper)operations.getSelectedItem();
        
        if (ow.getOperation() != null) {
            retval = ow.getOperation().getName().toString();
        }
        
        return retval;
    }
    
    public List <WebServiceInputParameter> getInputParameters() {
        return inputParameters.getTableData();
    }
    
    public String getExpectedResult() {
        return expectedResult.getText();
    }

    public String getFailureAction() {
        return (String)failureAction.getSelectedItem();
    }
    
    public boolean isForCheckpoint() {
        return forCheckpoint;
    }
}
