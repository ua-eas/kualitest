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
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.test.CheckpointProperty;
import org.kuali.test.FailureAction;
import org.kuali.test.KualiTestConfigurationDocument;
import org.kuali.test.Operation;
import org.kuali.test.Platform;
import org.kuali.test.TestExecutionParameter;
import org.kuali.test.handlers.parameter.ParameterHandler;
import org.kuali.test.handlers.parameter.SelectEditDocumentLookupHandler;
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
        
        TestExecutionContext tec = getTestExecutionContext();
        tec.setCurrentOperationIndex(Integer.valueOf(getOperation().getIndex()));
        tec.setCurrentTest(testWrapper);

        TestExecutionParameter tep = getOperation().getTestExecutionParameter();
        ParameterHandler ph = tec.getParameterHandler(tep.getParameterHandler());

        if (StringUtils.isNotBlank(key)) {
            long start = System.currentTimeMillis();
            while (StringUtils.isBlank(getOperation().getTestExecutionParameter().getValue()) 
                && ((System.currentTimeMillis() - start) < Constants.HTML_TEST_RETRY_TIMESPAN)
                && !tec.isHaltTest()) {
                Document doc = Utils.cleanHtml(tec.getWebClient().getCurrentWindow().getEnclosedPage().getWebResponse().getContentAsString());

                if (doc != null) {
                    if (ph instanceof SelectEditDocumentLookupHandler) {
                        tep.setValue(ph.getValue(tec, tep, doc, null));
                        tec.addTestExecutionParameter(tep);
                    } else {
                        CheckpointProperty cpmatch = null;
                        HtmlDomProcessor.DomInformation dominfo = HtmlDomProcessor.getInstance().processDom(platform, doc);

                        for (CheckpointProperty cp : dominfo.getCheckpointProperties()) {
                            String curkey = Utils.buildCheckpointPropertyKey(cp);
                            if (StringUtils.equals(key, curkey)) {
                                cpmatch = cp;
                                break;
                            }
                        }

                        // if we found a matching html element then set the parameter value
                        if (cpmatch != null) {
                            tep.setValue(ph.getValue(tec, tep, doc, cpmatch));
                            tec.addTestExecutionParameter(tep);
                        }
                    }
                }
                
                if (StringUtils.isBlank(tep.getValue())) {
                    try {
                        Thread.sleep(Constants.HTML_TEST_RETRY_SLEEP_INTERVAL);
                    } catch (InterruptedException ex) {}

                    try {
                        if (ph.isResubmitRequestAllowed()) {
                            tec.resubmitLastGetRequest();
                        }
                    }
                    
                    catch (IOException ex) {
                        throw new TestException(ex.toString(), tec.getCurrentTestOperation().getOperation(), ex);
                    }
                } else {
                    break;
                }
            }
            
            if (StringUtils.isBlank(tep.getValue())) {
                throw new TestException("failed to find value for test execution parameter[" 
                    + tep.getName() + "]", getOperation(), FailureAction.ERROR_HALT_TEST);
            } else {
                String comment = ph.getCommentText();

                if (StringUtils.isNotBlank(comment)) {
                    tec.writeCommentEntry(buildCommentOperation(tep.getName(), comment), true);
                }
            }
        }
    }
    
    private Operation buildCommentOperation(String parameterName, String comment) {
        Operation retval = Operation.Factory.newInstance();
        retval.setIndex(getTestExecutionContext().getCurrentOperationIndex());
        retval.addNewCommentOperation();
        retval.getCommentOperation().setComment("parameter[" + parameterName + "]: " + comment);
        return retval;
    }
}
