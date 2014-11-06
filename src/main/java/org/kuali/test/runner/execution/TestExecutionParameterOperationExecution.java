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

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.test.CheckpointProperty;
import org.kuali.test.FailureAction;
import org.kuali.test.KualiTestConfigurationDocument;
import org.kuali.test.Operation;
import org.kuali.test.Platform;
import org.kuali.test.runner.exceptions.TestException;
import org.kuali.test.utils.Constants;
import org.kuali.test.utils.HtmlDomProcessor;
import org.kuali.test.utils.Utils;

/**
 *
 * @author rbtucker
 */
public class TestExecutionParameterOperationExecution extends AbstractOperationExecution {
    private static Logger LOG = Logger.getLogger(TestExecutionParameterOperationExecution.class);
    /**
     *
     * @param context
     * @param op
     */
    public TestExecutionParameterOperationExecution(TestExecutionContext context, Operation op) {
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
    public void execute(KualiTestConfigurationDocument.KualiTestConfiguration configuration, 
        Platform platform, KualiTestWrapper testWrapper) throws TestException {
        String key = Utils.buildCheckpointPropertyKey(getOperation().getTestExecutionParameter().getValueProperty());
        
        // try this a few time to account for asynchronous page loading
        for (int i = 0; i < Constants.HTML_TEST_RETRY_COUNT; ++ i) {
            try {
                if (StringUtils.isNotBlank(key)) {
                    for (String h : testWrapper.getHttpResponseStack()) {
                        HtmlDomProcessor.DomInformation dominfo = HtmlDomProcessor.getInstance().processDom(platform, Utils.cleanHtml(h));

                        for (CheckpointProperty cp : dominfo.getCheckpointProperties()) {
                            String curkey = Utils.buildCheckpointPropertyKey(cp);
                            if (StringUtils.equals(key, curkey)) {
                                if (StringUtils.isNotBlank(cp.getPropertyValue())) {
                                    getOperation().getTestExecutionParameter().setValue(cp.getPropertyValue().trim());
                                    break;
                                }
                            }
                        }
                    }
                    
                    if (StringUtils.isNotBlank(getOperation().getTestExecutionParameter().getValue())) {
                        break;
                    }
                }

                if (StringUtils.isBlank(getOperation().getTestExecutionParameter().getValue())) {
                    throw new TestException("failed to find test execution parameter for '" 
                        + getOperation().getTestExecutionParameter().getName() 
                        + "'", getOperation(), FailureAction.ERROR_HALT_TEST);
                }
            }
            
            catch (TestException ex) {
                if (i > Constants.HTML_TEST_RETRY_COUNT) {
                    throw ex;
                } else {
                    try {
                        Thread.sleep(Constants.HTML_TEST_RETRY_SLEEP_INTERVAL);
                    } catch (InterruptedException ex1) {
                        LOG.warn(ex1.toString(), ex1);
                    }
                }
            }
        }
    }
}
