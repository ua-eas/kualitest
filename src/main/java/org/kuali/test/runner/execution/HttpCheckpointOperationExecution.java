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
import org.apache.commons.lang3.StringUtils;
import org.kuali.test.Checkpoint;
import org.kuali.test.CheckpointProperty;
import org.kuali.test.KualiTestConfigurationDocument;
import org.kuali.test.Operation;
import org.kuali.test.Platform;
import org.kuali.test.runner.exceptions.TestException;
import org.kuali.test.utils.HtmlDomProcessor;
import org.kuali.test.utils.Utils;

/**
 *
 * @author rbtucker
 */
public class HttpCheckpointOperationExecution extends AbstractOperationExecution {

    /**
     *
     * @param context
     * @param op
     */
    public HttpCheckpointOperationExecution(TestExecutionContext context, Operation op) {
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
        for (String html : getTestExecutionContext().getRecentHttpResponseData()) {
            if (StringUtils.isNotBlank(html)) {
                HtmlDomProcessor.DomInformation dominfo = HtmlDomProcessor.getInstance().processDom(platform, html);

                Checkpoint cp = getOperation().getCheckpointOperation();

                if (cp != null) {
                    if (cp.getCheckpointProperties() != null) {
                        List <CheckpointProperty> matchingProperties = findCurrentProperties(cp, dominfo);
                        if (matchingProperties.size() == cp.getCheckpointProperties().sizeOfCheckpointPropertyArray()) {
                            CheckpointProperty[] properties = cp.getCheckpointProperties().getCheckpointPropertyArray();
                            boolean success = true;
                            for (int i = 0; i < properties.length; ++i) {
                                properties[i].setActualValue(matchingProperties.get(i).getPropertyValue());
                                
                                if (!evaluateCheckpointProperty(properties[i])) {
                                    success = false;
                                }
                            }

                            if (!success) {
                                throw new TestException("Current web document values do not match test criteria", getOperation());
                            }
                            
                            break;
                        }
                    }
                }
            }
        }
    }
    
    private List <CheckpointProperty> findCurrentProperties(Checkpoint cp, HtmlDomProcessor.DomInformation dominfo) {
        List <CheckpointProperty> retval = new ArrayList<CheckpointProperty>();

        for (CheckpointProperty originalProperty : cp.getCheckpointProperties().getCheckpointPropertyArray()) {
            for (CheckpointProperty currentProperty : dominfo.getCheckpointProperties()) {
                if (Utils.isCheckPointPropertyMatch(currentProperty, originalProperty)) {
                    retval.add(currentProperty);
                }
            }
        }
        
        return retval;
    }
}
