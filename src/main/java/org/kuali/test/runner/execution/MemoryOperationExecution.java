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

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import org.kuali.test.CheckpointProperty;
import org.kuali.test.JmxConnection;
import org.kuali.test.KualiTestConfigurationDocument;
import org.kuali.test.Operation;
import org.kuali.test.Platform;
import org.kuali.test.runner.exceptions.TestException;
import org.kuali.test.utils.Constants;
import org.kuali.test.utils.Utils;

/**
 *
 * @author rbtucker
 */
public class MemoryOperationExecution extends AbstractOperationExecution {

    /**
     *
     * @param context
     * @param op
     */
    public MemoryOperationExecution(TestExecutionContext context, Operation op) {
        super(context, op);
    }
    
    /**
     * 
     * @param configuration
     * @param platform
     * @param testWrapper
     * @throws TestException 
     */
    @Override
    public void execute(KualiTestConfigurationDocument.KualiTestConfiguration configuration, Platform platform, KualiTestWrapper testWrapper) throws TestException {
        TestExecutionContext tec = getTestExecutionContext();
        tec.setCurrentOperationIndex(Integer.valueOf(getOperation().getIndex()));
        tec.setCurrentTest(testWrapper);

        JMXConnector conn = null;
        try {   
            JmxConnection jmx = Utils.findJmxConnection(configuration, platform.getJmxConnectionName());
            
            if (jmx != null) {
                conn = Utils.getJMXConnector(configuration, jmx);
                MBeanServerConnection mbeanConn = conn.getMBeanServerConnection();    
                MemoryMXBean mbean = ManagementFactory.newPlatformMXBeanProxy(mbeanConn, ManagementFactory.MEMORY_MXBEAN_NAME, MemoryMXBean.class); 

                CheckpointProperty cp = getProperty(Constants.MAX_MEMORY_PERCENT);
                
                if (cp != null) {
                    double used = (double) mbean.getHeapMemoryUsage().getUsed();
                    double committed = (double) mbean.getHeapMemoryUsage().getCommitted();
                    Double usedMemoryPercent = new Double((used/committed) * 100.0);
                    cp.setActualValue("" + usedMemoryPercent.intValue());
                
                    if (!evaluateCheckpointProperty(testWrapper, cp)) {
                        throw new TestException("memory usage of " 
                            + cp.getActualValue() 
                            + "% is outside of specified range", getOperation(), cp.getOnFailure());
                    } 
                }
            }
        }
        
        catch (IOException ex) {
            throw new TestException("an IOException was throw while attempting to connect via jmx - " + ex.toString(), getOperation(), ex);
        }
        
        finally {
            if (conn != null) {
                try {
                    conn.close();
                }
                
                catch (Exception ex) {};
            }
        }
    }
}
