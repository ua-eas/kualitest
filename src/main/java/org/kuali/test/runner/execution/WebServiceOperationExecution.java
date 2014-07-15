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

import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.rpc.client.RPCServiceClient;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.test.CheckpointProperty;
import org.kuali.test.KualiTestConfigurationDocument;
import org.kuali.test.Operation;
import org.kuali.test.Parameter;
import org.kuali.test.Platform;
import org.kuali.test.WebService;
import org.kuali.test.runner.exceptions.TestException;
import org.kuali.test.utils.Constants;
import org.kuali.test.utils.Utils;

/**
 *
 * @author rbtucker
 */
public class WebServiceOperationExecution extends AbstractOperationExecution {
    private static final Logger LOG = Logger.getLogger(WebServiceOperationExecution.class);
    
    /**
     *
     * @param context
     * @param op
     */
    public WebServiceOperationExecution(TestExecutionContext context, Operation op) {
        super(context, op);
    }
    
    /**
     *
     * @param configuration
     * @param platform
     * @throws TestException
     */
    @Override
    public void execute(KualiTestConfigurationDocument.KualiTestConfiguration configuration, Platform platform) throws TestException {
        try {
            WebService ws = Utils.findWebServiceByName(configuration, platform.getWebServiceName());
            String wsOperation = getParameter(Constants.WEB_SERVICE_OPERATION);
            RPCServiceClient serviceClient = new RPCServiceClient();
            Options options = serviceClient.getOptions();

            if (StringUtils.isNotBlank(ws.getUsername())) {
                options.setUserName(ws.getUsername());
                options.setPassword(Utils.decrypt(Utils.getEncryptionPassword(configuration), ws.getPassword()));
            }
            
            options.setTo(new EndpointReference(ws.getWsdlUrl().substring(0, ws.getWsdlUrl().indexOf("?"))));
            String[] wsparts = Utils.getWebServiceOperationParts(wsOperation);
            QName wsMethod = new QName(wsparts[0], wsparts[1]);
            List wsArgs = new ArrayList();
            List <Class> wsArgTypes = new ArrayList<Class>();
            
            for (Parameter param : getOperation().getCheckpointOperation().getInputParameters().getParameterArray()) {
                if (StringUtils.isNotBlank(param.getName())) {
                    if (!Constants.WEB_SERVICE_OPERATION.equals(param.getName())) {
                        wsArgs.add(param.getValue());
                        wsArgTypes.add(Class.forName(param.getJavaType()));
                    }
                }
            }
            
            int maxRuntime = Constants.DEFAULT_WEB_SERVICE_WAIT_TIME;
            
            CheckpointProperty cp = getProperty(Constants.MAX_RUNTIME_PROPERTY_NAME);
            if (cp != null) {
                if (StringUtils.isNotBlank(cp.getPropertyValue())) {
                    try {
                        int i = Integer.parseInt(cp.getPropertyValue());
                        if (i > 0) {
                            maxRuntime = i * 1000; 
                        }
                    }
                    
                    catch (NumberFormatException ex) {};
                }
            }

            CheckpointProperty resultProperty = getProperty(Constants.EXPECTED_RESULT);
            long start = System.currentTimeMillis();
            Object[] result = null;
            String poll = getTestExecutionContext().getKualiTest().getTestHeader().getAdditionalParameters();
            
            // special handling for UA batch calls
            if (StringUtils.equalsIgnoreCase(poll, "true")) {
                while ((System.currentTimeMillis() - start) <= maxRuntime) {
                    result = serviceClient.invokeBlocking(wsMethod, wsArgs.toArray(), new Class[] { getClassForValueType(resultProperty.getValueType()) });

                    if ((result.length > 0) && (result[0] != null)) {
                        if ((result.length > 0) && StringUtils.equalsIgnoreCase(result[0].toString(), "false")) {
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("sleeping - elapsed time: " + (System.currentTimeMillis() - start)/1000 + " seconds" );
                            }
                            try {
                                Thread.sleep(Constants.WEB_SERVICE_SLEEP_TIME);
                            }

                            catch (InterruptedException ex) {};
                        } else {
                            break;
                        }
                    } else {
                        throw new TestException("no results returned", getOperation());
                    }
                }
            } else {
                result = serviceClient.invokeBlocking(wsMethod, wsArgs.toArray(), new Class[] { getClassForValueType(cp.getValueType()) });
            }

            if ((result != null) && (result.length > 0)) {
                if (resultProperty != null) {
                    if (result[0] != null) {
                        resultProperty.setActualValue(result[0].toString());
                    }

                    if (LOG.isDebugEnabled()) {
                        LOG.debug("result=" + resultProperty.getActualValue());
                    }
                }

                if (cp != null) {
                    cp.setActualValue("" + ((System.currentTimeMillis() - start)/1000));
                }
            } else {
                getTestExecutionContext().incrementErrorCount();
                throw new TestException("no web service result", getOperation());
            }
        }

        catch (TestException ex) {
            throw ex;
        }
        
        catch (Exception ex) {
            LOG.error(ex.toString(), ex);
            getTestExecutionContext().incrementErrorCount();
            throw new TestException("web service error - " + ex.toString(), getOperation());
        }
    }
}
