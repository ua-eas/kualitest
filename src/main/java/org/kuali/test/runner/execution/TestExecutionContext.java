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
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.test.AutoReplaceParameter;
import org.kuali.test.Checkpoint;
import org.kuali.test.CheckpointProperty;
import org.kuali.test.CheckpointType;
import org.kuali.test.ComparisonOperator;
import org.kuali.test.KualiTestConfigurationDocument;
import org.kuali.test.KualiTestDocument.KualiTest;
import org.kuali.test.Operation;
import org.kuali.test.Platform;
import org.kuali.test.SuiteTest;
import org.kuali.test.TestExecutionParameter;
import org.kuali.test.TestHeader;
import org.kuali.test.TestOperation;
import org.kuali.test.TestOperationType;
import org.kuali.test.TestSuite;
import org.kuali.test.ValueType;
import org.kuali.test.runner.exceptions.TestException;
import org.kuali.test.runner.output.PoiHelper;
import org.kuali.test.utils.Constants;
import org.kuali.test.utils.HtmlDomProcessor;
import org.kuali.test.utils.Utils;
import org.w3c.dom.Element;

/**
 *
 * @author rbtucker
 */
public class TestExecutionContext extends Thread {
    private static final Logger LOG = Logger.getLogger(TestExecutionContext.class);
    private static final int DEFAULT_HTTP_RESPONSE_BUFFER_SIZE = 1024;
    private List <File> generatedCheckpointFiles = new ArrayList<File>();
    private File testResultsFile;

    
    private Map<String, String> autoReplaceParameterMap = new HashMap<String, String>();
    private Map<String, String> executionParameterMap = new HashMap<String, String>();
    private Map<String, List<String>> cookieMap = new HashMap<String, List<String>>();
    
    private StringBuilder lastHttpResponseData = new StringBuilder(DEFAULT_HTTP_RESPONSE_BUFFER_SIZE);
    
    private Platform platform;
    private TestSuite testSuite;
    private KualiTest kualiTest;
    private Date scheduledTime;
    private Date startTime;
    private Date endTime;
    private int testRun = 1;
    private int testRuns = 1;
    private boolean completed = false;
    
    private KualiTestConfigurationDocument.KualiTestConfiguration configuration;

    /**
     *
     */
    public TestExecutionContext() {
    }
    
    /**
     *
     * @param configuration
     * @param testSuite
     * @param scheduledTime
     * @param testRuns
     */
    public TestExecutionContext(KualiTestConfigurationDocument.KualiTestConfiguration configuration, 
        TestSuite testSuite, Date scheduledTime, int testRuns) {
        this.testSuite = testSuite;
        this.scheduledTime = scheduledTime;
        this.configuration = configuration;
        this.testRuns = testRuns;
        platform = Utils.findPlatform(configuration, testSuite.getPlatformName());
    }

    /**
     *
     * @param configuration
     * @param testSuite
     */
    public TestExecutionContext(KualiTestConfigurationDocument.KualiTestConfiguration configuration, 
        TestSuite testSuite) {
        this(configuration, testSuite, null, 1);
    }

    /**
     *
     * @param configuration
     * @param kualiTest
     * @param scheduledTime
     * @param testRuns
     */
    public TestExecutionContext(KualiTestConfigurationDocument.KualiTestConfiguration configuration, 
        KualiTest kualiTest, Date scheduledTime, int testRuns) {
        this.kualiTest = kualiTest;
        this.scheduledTime = scheduledTime;
        this.configuration = configuration;
        this.testRuns = testRuns;
        platform = Utils.findPlatform(configuration, kualiTest.getTestHeader().getPlatformName());
    }

    /**
     *
     * @param configuration
     * @param kualiTest
     */
    public TestExecutionContext(KualiTestConfigurationDocument.KualiTestConfiguration configuration, KualiTest kualiTest) {
        this(configuration, kualiTest, null, 1);
    }
    
    @Override
    public void run() {
        runTest();
    }
    
    /**
     *
     */
    public void runTest() {
        try {
            startTime= new Date();

            PoiHelper poiHelper = new PoiHelper();
            poiHelper.writeReportHeader(testSuite, kualiTest);
            poiHelper.writeColumnHeaders();

            if (testSuite != null) {
                int defaultTestWaitInterval = configuration.getDefaultTestWaitInterval();

                for (SuiteTest suiteTest : testSuite.getSuiteTests().getSuiteTestArray()) {
                    KualiTest test = Utils.findKualiTest(configuration, suiteTest.getTestHeader().getPlatformName(), suiteTest.getTestHeader().getTestName());

                    if (test != null) {

                        // add pause between tests if configured
                        if (defaultTestWaitInterval > 0) {
                            try {
                                Thread.sleep(defaultTestWaitInterval * 1000);
                            } 

                            catch (InterruptedException ex) {
                                LOG.warn(ex.toString(), ex);
                            }
                        }

                        poiHelper.writeTestHeader(test);
                        runTest(test, poiHelper);
                    }
                }
            } else if (kualiTest != null) {
                runTest(kualiTest, poiHelper);
            }

            endTime= new Date();
            testResultsFile = new File(buildTestReportFileName());

            poiHelper.writeFile(testResultsFile);
        }
        
        finally {
            cleanup();
            completed = true;
        }
    }
    
    private void cleanup() {
    }
    
    private String buildTestReportFileName() {
        StringBuilder retval = new StringBuilder(128);
        
        retval.append(configuration.getTestResultLocation());
        retval.append("/");
        if (testSuite != null) {
            retval.append(testSuite.getPlatformName());
            retval.append("/");
            retval.append(Utils.formatForFileName(testSuite.getName()));
        } else {
            retval.append(kualiTest.getTestHeader().getPlatformName());
            retval.append("/");
            retval.append(Utils.getTestFileName(kualiTest.getTestHeader()));
        }

        retval.append("-");
        retval.append(Constants.FILENAME_TIMESTAMP_FORMAT.format(startTime));
        retval.append("_");
        retval.append(testRun);
        retval.append(".xlsx");
        
        return retval.toString();
    }

    
    private void runTest(KualiTest test, PoiHelper poiHelper) {
        if (LOG.isInfoEnabled()) {
            LOG.info("--------------------------- starting test ---------------------------");
            LOG.info("platform: " + test.getTestHeader().getPlatformName());
            
            if (StringUtils.isNotBlank(test.getTestHeader().getTestSuiteName())) {
                LOG.info("test suite: " + test.getTestHeader().getTestSuiteName());
            }

            LOG.info("test: " + test.getTestHeader().getTestName());
            LOG.info("---------------------------------------------------------------------");
        }
        
        long start = System.currentTimeMillis();
        for (TestOperation op : test.getOperations().getOperationArray()) {
            // if executeTestOperation returns false we want to halt test
            if (!executeTestOperation(op, poiHelper)) {
                break;
            }
        }

        // check for max runtime exceeded
        long runtime = ((System.currentTimeMillis() - start) / 1000);
        if ((test.getTestHeader().getMaxRunTime() > 0) && (runtime > test.getTestHeader().getMaxRunTime())) {
            poiHelper.writeFailureEntry(createTestRuntimeCheckOperation(test.getTestHeader(), runtime), new Date(start), null);
        }
    }

    private TestOperation createTestRuntimeCheckOperation(TestHeader testHeader, long runtime) {
        TestOperation retval = TestOperation.Factory.newInstance();
        Operation op = Operation.Factory.newInstance();
        Checkpoint checkpoint = Checkpoint.Factory.newInstance();

        checkpoint.setName("test runtime check");
        checkpoint.setTestName(testHeader.getTestName());

        if (StringUtils.isNotBlank(testHeader.getTestSuiteName())
            && !Constants.NO_TEST_SUITE_NAME.equals(testHeader.getTestSuiteName())) {
            checkpoint.setTestSuite(testHeader.getTestSuiteName());
        }

        checkpoint.setType(CheckpointType.RUNTIME);

        checkpoint.addNewCheckpointProperties();
        CheckpointProperty cp = checkpoint.getCheckpointProperties().addNewCheckpointProperty();
        cp.setActualValue("" + runtime);
        cp.setPropertyGroup(Constants.SYSTEM_PROPERTY_GROUP);
        cp.setDisplayName("Max Runtime(sec)");
        cp.setOnFailure(testHeader.getOnRuntimeFailure());
        cp.setOperator(ComparisonOperator.LESS_THAN_OR_EQUAL);
        cp.setValueType(ValueType.INT);
        cp.setPropertyValue("" + testHeader.getMaxRunTime());
        cp.setPropertyName(Constants.MAX_RUNTIME_PROPERTY_NAME);
        retval.setOperation(op);
        retval.setOperationType(TestOperationType.CHECKPOINT);

        return retval;
    }
    
    /**
     * 
     * @param op
     * @param poiHelper
     * @return true to continue test - false to halt
     */
    private boolean executeTestOperation(TestOperation op, PoiHelper poiHelper) {
        boolean retval = true;
        OperationExecution opExec = null;
        
        Date opStartTime = new Date();
        try {
            opExec = OperationExecutionFactory.getInstance().getOperationExecution(this, op);
            if (opExec != null) {
                opExec.execute(configuration, platform);
                if (op.getOperation().getCheckpointOperation() != null) {
                    poiHelper.writeSuccessEntry(op, opStartTime);
                }
            }
        } 
        
        catch (TestException ex) {
            retval = poiHelper.writeFailureEntry(op, opStartTime, ex);
        }
        
        return retval;
    }
    
    /**
     *
     * @return
     */
    public TestSuite getTestSuite() {
        return testSuite;
    }

    /**
     *
     * @param testSuite
     */
    public void setTestSuite(TestSuite testSuite) {
        this.testSuite = testSuite;
    }

    /**
     *
     * @return
     */
    public KualiTest getKualiTest() {
        return kualiTest;
    }

    /**
     *
     * @param kualiTest
     */
    public void setKualiTest(KualiTest kualiTest) {
        this.kualiTest = kualiTest;
    }

    /**
     *
     * @return
     */
    public Date getStartTime() {
        return startTime;
    }

    /**
     *
     * @param startTime
     */
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    /**
     *
     * @return
     */
    public Date getEndTime() {
        return endTime;
    }

    /**
     *
     * @param endTime
     */
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }
    
    /**
     *
     */
    public final void startTest() {
        start();
    }

    /**
     *
     * @return
     */
    public Date getScheduledTime() {
        return scheduledTime;
    }

    /**
     *
     * @param scheduledTime
     */
    public void setScheduledTime(Date scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    /**
     *
     * @return
     */
    public boolean isCompleted() {
        return completed;
    }
    
    /**
     *
     */
    public void clearLastHttpResponse() {
        if (lastHttpResponseData != null) {
            lastHttpResponseData.setLength(0);
        }
    }

    /**
     *
     * @return
     */
    public StringBuilder getLastHttpResponseData() {
        if (lastHttpResponseData == null) {
            lastHttpResponseData= new StringBuilder(DEFAULT_HTTP_RESPONSE_BUFFER_SIZE);
        }
        
        return lastHttpResponseData;
    }

    /**
     *
     * @return
     */
    public int getTestRun() {
        return testRun;
    }

    /**
     *
     * @return
     */
    public List<TestExecutionContext> getTestInstances() {
        List <TestExecutionContext> retval = new ArrayList<TestExecutionContext>();;
        retval.add(this);
        
        for (int i = 1; i < testRuns; ++i) {
            TestExecutionContext tec = new TestExecutionContext();
            tec.setStartTime(startTime);
            tec.setPlatform(platform);
            tec.setKualiTest(kualiTest);
            tec.setTestRun(i+1);
            tec.setConfiguration(configuration);
            retval.add(tec);
        }
        
        return retval;
    }

    /**
     *
     * @param platform
     */
    public void setPlatform(Platform platform) {
        this.platform = platform;
    }

    /**
     *
     * @param testRun
     */
    public void setTestRun(int testRun) {
        this.testRun = testRun;
    }

    /**
     *
     * @param configuration
     */
    public void setConfiguration(KualiTestConfigurationDocument.KualiTestConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     *
     * @return
     */
    public List<File> getGeneratedCheckpointFiles() {
        return generatedCheckpointFiles;
    }

    /**
     *
     * @return
     */
    public File getTestResultsFile() {
        return testResultsFile;
    }

    /**
     *
     * @return
     */
    public Platform getPlatform() {
        return platform;
    }

    /**
     *
     * @return
     */
    public int getTestRuns() {
        return testRuns;
    }

    /**
     *
     * @return
     */
    public KualiTestConfigurationDocument.KualiTestConfiguration getConfiguration() {
        return configuration;
    }

    /**
     *
     * @return
     */
    public Map<String, String> getExecutionParameterMap() {
        return executionParameterMap;
    }
    
    private String findTestExecutionParameterValue(TestExecutionParameter ep) {
        return null;
    }
    
    /**
     *
     * @param ep
     */
    public void processTestExecutionParameter(TestExecutionParameter ep) {
        if (!ep.getRemove()) {
            String value = findTestExecutionParameterValue(ep);

            if (StringUtils.isNotBlank(value)) {
               executionParameterMap.put(ep.getName(), value);
            }
        } else {
            executionParameterMap.remove(ep.getName());
        }
    }
    
    /**
     *
     * @param parameterName
     * @return
     */
    public String getTestExecutionParameterValue(String parameterName) {
        return executionParameterMap.get(parameterName);
    }
    
    /**
     *
     * @param input
     * @return
     */
    public String replaceTestExecutionParameters(String input) {
        String retval = input;
        
        // decrypt encrypted parameters
        for (String parameterName : configuration.getParametersRequiringEncryption().getNameArray()) {
            int[] parameterPosition = Utils.getParameterPosition(retval, parameterName, Constants.SEPARATOR_AMPERSTAND);
            
            if (parameterPosition != null) {
                String[] parameterData = Utils.getParameterData(retval, parameterPosition);
                
                if (parameterData != null) {
                    parameterData[1] = Utils.decrypt(Utils.getEncryptionPassword(configuration), parameterData[1]);
                
                    retval = Utils.replaceParameterString(retval, parameterPosition, parameterData);
                }
            }
        }
        
        
        for (String parameterName : getAutoReplaceParameterMap().keySet()) {
            int[] parameterPosition = Utils.getParameterPosition(retval, parameterName, Constants.SEPARATOR_AMPERSTAND);
            
            if (parameterPosition != null) {
                String[] parameterData = Utils.getParameterData(retval, parameterPosition);
                
                if (parameterData != null) {
                    parameterData[1] = getAutoReplaceParameterMap().get(parameterName);
                    retval = Utils.replaceParameterString(retval, parameterPosition, parameterData);
                }
            }
        }
        
        return retval;
    }
    
    /**
     *
     * @param cookie
     */
    public void addCookie(String cookie) {
        int[] parameterPosition = Utils.getParameterPosition(cookie, Constants.PARAMETER_NAME_PATH, Constants.SEPARATOR_SEMICOLON);
        
        if (parameterPosition != null) {
            String[] parameterData = Utils.getParameterData(cookie, parameterPosition);

            if (parameterData != null) {
                List <String> l = cookieMap.get(parameterData[1]);

                if (l == null) {
                    cookieMap.put(parameterData[1], l = new ArrayList<String>());
                }

                l.add(cookie);
            }
        }
    }
    
    /**
     *
     * @param path
     * @return
     */
    public List <String> getCookies(String path) {
        List <String> retval = new ArrayList<String>();
        try {
            String uri = new URL(path).toURI().getPath();
            for (String key : cookieMap.keySet()) {
                if (uri.startsWith(key)) {
                    retval.addAll(cookieMap.get(key));
                }
            }
        } 
        
        catch (MalformedURLException ex) {
            LOG.warn(ex.toString(), ex);
        } catch (URISyntaxException ex) {
            LOG.warn(ex.toString(), ex);
        }
        
        return retval;
    }

    public Map<String, String> getAutoReplaceParameterMap() {
        return autoReplaceParameterMap;
    }
    
    
    public void updateAutoReplaceMap() {
        if (configuration.getAutoReplaceParameters() != null) {
            Element element = HtmlDomProcessor.getInstance().getDomDocumentElement(getLastHttpResponseData().toString());
            
            Map <String, AutoReplaceParameter> map = new HashMap<String, AutoReplaceParameter>();

            for (AutoReplaceParameter param : configuration.getAutoReplaceParameters().getAutoReplaceParameterArray()) {
                // remove non-retained values
                if (!param.getRetain()) {
                    autoReplaceParameterMap.remove(param.getParameterName());
                }
                
                String value = Utils.findAutoReplaceParameterInDom(param, element);
                
                if (StringUtils.isNotBlank(value)) {
                    autoReplaceParameterMap.put(param.getParameterName(), value);
                }
            }
        }
    }
}


