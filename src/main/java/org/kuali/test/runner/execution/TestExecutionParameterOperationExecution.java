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
import org.kuali.test.TestExecutionParameter;
import org.kuali.test.handlers.parameter.ExecutionContextParameterHandler;
import org.kuali.test.handlers.parameter.ParameterHandler;
import org.kuali.test.runner.exceptions.TestException;
import org.kuali.test.utils.Constants;
import org.kuali.test.utils.HtmlDomProcessor;
import org.kuali.test.utils.Utils;
import org.w3c.dom.Document;

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
        
        long start = System.currentTimeMillis();
        
        TestExecutionContext tec = getTestExecutionContext();
        tec.setCurrentOperationIndex(Integer.valueOf(getOperation().getIndex()));
        tec.setCurrentTest(testWrapper);

        TestException lastTestException = null;
        CheckpointProperty cpmatch = null;
        
        // if the source page was loaded from a get request try reload a few times if 
        // required to handle asynchronous processing that may affect parameter fields
        while ((System.currentTimeMillis() - start) < Constants.HTML_TEST_RETRY_TIMESPAN) {
            if (StringUtils.isNotBlank(key)) {
                Document doc = null;
                for (String h : testWrapper.getHttpResponseStack()) {
                    doc = Utils.cleanHtml(h);
                    HtmlDomProcessor.DomInformation dominfo = HtmlDomProcessor.getInstance().processDom(platform, doc);

                    for (CheckpointProperty cp : dominfo.getCheckpointProperties()) {
                        String curkey = Utils.buildCheckpointPropertyKey(cp);
                        if (StringUtils.equals(key, curkey)) {
                            if (StringUtils.isNotBlank(cp.getPropertyValue())) {
                                cpmatch = cp;
                                break;
                            }
                        }
                    }
                }

                // if we found a matching html element then set the parameter value
                if (cpmatch != null) {
                    TestExecutionParameter tep = getOperation().getTestExecutionParameter();
                    ParameterHandler ph = Utils.getParameterHandler(tep.getParameterHandler());
                    
                    if (ph instanceof ExecutionContextParameterHandler) {
                        tep.setValue(ph.getValue(tec, doc, cpmatch, tep.getAdditionalInfo()));
                    } else {
                        tep.setValue(ph.getValue(tec, doc, cpmatch, cpmatch.getPropertyValue().trim()));
                    }
                    
                    lastTestException = null;
                    String comment = ph.getCommentText();
                    
                    if (StringUtils.isNotBlank(comment)) {
                        tec.writeCommentEntry(buildCommentOperation(tep.getName(), comment));
                    }
                    
                    break;
                }
            }

            if (StringUtils.isBlank(getOperation().getTestExecutionParameter().getValue())) {
                lastTestException = new TestException("failed to find test execution parameter for '" 
                    + getOperation().getTestExecutionParameter().getName() 
                    + "'", getOperation(), FailureAction.ERROR_HALT_TEST);
            }
            
            try {
                Thread.sleep(Constants.HTML_TEST_RETRY_SLEEP_INTERVAL);
            } 
            
            catch (InterruptedException ex) {};
                
            try {
                tec.resubmitLastGetRequest();
            }
            
            catch (Exception ex) {
                lastTestException = new TestException(ex.toString(), getOperation(), ex);
                break;
            }
        }
        
        if (lastTestException != null) {
            throw lastTestException;
        }
    }
    
    private Operation buildCommentOperation(String parameterName, String comment) {
        Operation retval = Operation.Factory.newInstance();
        retval.addNewCommentOperation();
        retval.getCommentOperation().setComment("parameter [" + parameterName + "]: " + comment);
        return retval;
    }
}
