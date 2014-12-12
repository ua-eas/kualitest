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
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.kuali.test.Checkpoint;
import org.kuali.test.CheckpointProperty;
import org.kuali.test.FailureAction;
import org.kuali.test.KualiTestConfigurationDocument;
import org.kuali.test.Operation;
import org.kuali.test.Parameter;
import org.kuali.test.Platform;
import org.kuali.test.runner.exceptions.TestException;
import org.kuali.test.utils.Constants;
import org.kuali.test.utils.HtmlDomProcessor;
import org.kuali.test.utils.Utils;

/**
 *
 * @author rbtucker
 */
public class HttpCheckpointOperationExecution extends AbstractOperationExecution {
    private static Logger LOG = Logger.getLogger(HttpCheckpointOperationExecution.class);
    
    /**
     *
     * @param context
     * @param op
     */
    public HttpCheckpointOperationExecution(TestExecutionContext context, Operation op) {
        super(context, op);
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

   /**
    * 
    * @param configuration
    * @param platform
    * @param testWrapper
    * @throws TestException 
    */
    @Override
    public void execute(KualiTestConfigurationDocument.KualiTestConfiguration configuration, Platform platform, 
        KualiTestWrapper testWrapper) throws TestException {
        TestExecutionContext tec = getTestExecutionContext();
        String html = null;

        tec.setCurrentOperationIndex(Integer.valueOf(getOperation().getIndex()));
        tec.setCurrentTest(testWrapper);
        
        try {
            Checkpoint cp = getOperation().getCheckpointOperation();
            CheckpointProperty[] properties = cp.getCheckpointProperties().getCheckpointPropertyArray();
            List <CheckpointProperty> matchingProperties = null;
            if (cp.getCheckpointProperties() != null) {
                long start = System.currentTimeMillis();
        
                while ((System.currentTimeMillis() - start) < Constants.HTML_TEST_RETRY_TIMESPAN) {
                    html = tec.getWebClient().getCurrentWindow().getEnclosedPage().getWebResponse().getContentAsString().trim();
                    matchingProperties = findCurrentProperties(cp, HtmlDomProcessor.getInstance().processDom(platform, Utils.cleanHtml(html)));

                    if (matchingProperties.size() == properties.length) {
                        break;
                    } else {
                        try {
                            Thread.sleep(Constants.HTML_TEST_RETRY_SLEEP_INTERVAL);
                        } catch (InterruptedException ex) {}

                        tec.resubmitLastGetRequest();
                    }
                }
            }
        
            if ((matchingProperties != null) && !matchingProperties.isEmpty()) {
                if (matchingProperties.size() == properties.length) {
                    boolean success = true;

                    FailureAction.Enum failureAction = FailureAction.IGNORE;

                    for (int j = 0; j < properties.length; ++j) {
                        if (j < matchingProperties.size()) {
                            properties[j].setActualValue(matchingProperties.get(j).getPropertyValue());
                            if (!evaluateCheckpointProperty(testWrapper, properties[j])) {
                                success = false;

                                // use the most severe action in the group
                                if (properties[j].getOnFailure().intValue() > failureAction.intValue()) {
                                    failureAction = properties[j].getOnFailure();
                                }
                            }
                        }
                    }

                    if (!success) {
                        throw new TestException("Current web document values do not match test criteria", getOperation(), failureAction);
                    }
                } else {
                    throw new TestException("Expected checkpoint property count mismatch: expected " 
                        + properties.length 
                        + " found " 
                        + matchingProperties.size(), getOperation(), FailureAction.ERROR_HALT_TEST);
                }
            } else {
                throw new TestException("No matching properties found", getOperation(), FailureAction.ERROR_HALT_TEST);
            }
        }
        
        catch (IOException ex) {
            throw new TestException(ex.toString(), tec.getCurrentTestOperation().getOperation(), ex);
        }
        
        finally {
            if (isSaveScreen()) {
                getTestExecutionContext().saveCurrentScreen(html, false);
            }
        }
    }

    private boolean isSaveScreen() {
       Parameter param = Utils.getCheckpointParameter(getOperation().getCheckpointOperation(), Constants.SAVE_SCREEN);
       return ((param != null) && Constants.TRUE.equals(param.getValue()));
    }
}
