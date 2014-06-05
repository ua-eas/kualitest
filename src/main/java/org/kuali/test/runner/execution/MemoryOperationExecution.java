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

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.util.HashMap;
import java.util.Map;
import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import org.apache.commons.lang3.StringUtils;
import org.kuali.test.CheckpointProperty;
import org.kuali.test.JmxConnection;
import org.kuali.test.KualiTestConfigurationDocument;
import org.kuali.test.Operation;
import org.kuali.test.Platform;
import org.kuali.test.runner.exceptions.TestException;
import org.kuali.test.utils.Constants;
import org.kuali.test.utils.Utils;


public class MemoryOperationExecution extends AbstractOperationExecution {
    public MemoryOperationExecution(Operation op) {
        super(op);
    }
    
    @Override
    public void execute(KualiTestConfigurationDocument.KualiTestConfiguration configuration, Platform platform) throws TestException {
        try {   
            JmxConnection jmx = Utils.findJmxConnection(configuration, platform.getJmxConnectionName());
            
            if (jmx != null) {
                JMXServiceURL serviceUrl = new JMXServiceURL(jmx.getJmxUrl());
                
                Map map = null;
                
                if (StringUtils.isNotBlank(jmx.getUsername())) {
                    map = new HashMap();
                    map.put(JMXConnector.CREDENTIALS, new String[]{jmx.getUsername(), jmx.getPassword()});
                }
                JMXConnector jmxConnector = JMXConnectorFactory.connect(serviceUrl, map);           
                MBeanServerConnection mbeanConn = jmxConnector.getMBeanServerConnection();    
                MemoryMXBean mbean = ManagementFactory.newPlatformMXBeanProxy(mbeanConn, ManagementFactory.MEMORY_MXBEAN_NAME, MemoryMXBean.class); 

                CheckpointProperty cp = getProperty(Constants.MAX_MEMORY_PERCENT);
                
                if (cp != null) {
                    int usedMemoryPercent = (int)((double)mbean.getHeapMemoryUsage().getUsed() / (double)mbean.getHeapMemoryUsage().getCommitted()) * 100;
                    cp.setActualValue("" + usedMemoryPercent);
                }
            }
        }
        
        catch (Exception ex) {
            throw new TestException("an exception was throw while attempting to connect via jmx - " + ex.toString(), getOperation(), ex);
        }
    }
}
