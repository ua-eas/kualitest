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

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.test.Checkpoint;
import org.kuali.test.CheckpointProperty;
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
    
    /**
     *
     * @param configuration
     * @param platform
     * @throws TestException
     */
    @Override
    public void execute(KualiTestConfigurationDocument.KualiTestConfiguration configuration, Platform platform) throws TestException {
        TestExecutionContext tec = getTestExecutionContext();

        Checkpoint cp = getOperation().getCheckpointOperation();
        List <CheckpointProperty> matchingProperties = null;
        String html = null;
        
        if (cp.getCheckpointProperties() != null) {
            for (String curhtml : tec.getRecentHttpResponseData()) {
                if (StringUtils.isNotBlank(curhtml)) {
                    HtmlDomProcessor.DomInformation dominfo = HtmlDomProcessor.getInstance().processDom(platform, curhtml);
                    matchingProperties = findCurrentProperties(cp, dominfo);
                    if (matchingProperties.size() == cp.getCheckpointProperties().sizeOfCheckpointPropertyArray()) {
                        html = curhtml;
                        break;
                    }
                }
            }
        }
            
        if (matchingProperties != null) {
            CheckpointProperty[] properties = cp.getCheckpointProperties().getCheckpointPropertyArray();
            if (matchingProperties.size() == properties.length) {
                writeHtmlIfRequired(cp, configuration, platform, html);
                boolean success = true;
                for (int i = 0; i < properties.length; ++i) {
                    if (i < matchingProperties.size()) {
                        properties[i].setActualValue(matchingProperties.get(i).getPropertyValue());

                        if (!evaluateCheckpointProperty(properties[i])) {
                            success = false;
                        }
                    }
                }

                if (!success) {
                    throw new TestException("Current web document values do not match test criteria", getOperation());
                }
            } else {
                tec.incrementErrorCount();
                throw new TestException("Expected checkpoint property count mismatch", getOperation());
            }
        } else {
            tec.incrementErrorCount();
            throw new TestException("No matching properties found", getOperation());
        }
    }
    
    private void writeHtmlIfRequired(Checkpoint cp, KualiTestConfigurationDocument.KualiTestConfiguration configuration, 
        Platform platform, String html) {
        boolean saveScreen = false;

        if (cp.getInputParameters() != null) {
            for (Parameter param : cp.getInputParameters().getParameterArray()) {
                if (Constants.SAVE_SCREEN.equalsIgnoreCase(param.getName())) {
                    saveScreen = "true".equalsIgnoreCase(param.getValue());
                    break;
                }
            }
        }

        if (saveScreen) {
            File f = new File(getSaveScreenFileName(configuration, platform));
            getTestExecutionContext().getGeneratedCheckpointFiles().add(f);
            if (!f.getParentFile().exists()) {
                f.getParentFile().mkdirs();
            }

            PrintWriter pw = null;
            try {
                pw = new PrintWriter(f);
                pw.print(html);
            }

            catch (Exception ex) {
                LOG.error(ex.toString(), ex);
            }

            finally {
                try {
                    pw.close();
                }

                catch (Exception ex) {};
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

   private String getSaveScreenFileName(KualiTestConfigurationDocument.KualiTestConfiguration configuration, 
        Platform platform) {
        StringBuilder retval = new StringBuilder(256);
        
        retval.append(configuration.getTestResultLocation());
        retval.append("/");
        retval.append(platform.getName());
        retval.append("/screen-captures/");
        retval.append(Constants.DEFAULT_DATE_FORMAT.format(new Date()));
        retval.append("/");
        retval.append(getOperation().getCheckpointOperation().getTestName());
        retval.append(getOperation().getCheckpointOperation().getName().toLowerCase().replace(" ", "-"));
        retval.append("-");
        retval.append(Constants.FILENAME_TIMESTAMP_FORMAT.format(new Date()));
        retval.append("_");
        retval.append(getTestExecutionContext().getTestRun());
        retval.append(".html");
        
        return retval.toString();
    }
}
