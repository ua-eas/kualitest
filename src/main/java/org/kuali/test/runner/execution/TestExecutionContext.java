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

import com.gargoylesoftware.htmlunit.AlertHandler;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.IncorrectnessListener;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import javax.ws.rs.HttpMethod;
import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.test.AutoReplaceParameter;
import org.kuali.test.Checkpoint;
import org.kuali.test.CheckpointProperty;
import org.kuali.test.CheckpointType;
import org.kuali.test.ComparisonOperator;
import org.kuali.test.FailureAction;
import org.kuali.test.HtmlRequestOperation;
import org.kuali.test.KualiTestConfigurationDocument;
import org.kuali.test.KualiTestDocument;
import org.kuali.test.KualiTestDocument.KualiTest;
import org.kuali.test.Operation;
import org.kuali.test.Parameter;
import org.kuali.test.Platform;
import org.kuali.test.RequestParameter;
import org.kuali.test.SuiteTest;
import org.kuali.test.TestExecutionParameter;
import org.kuali.test.TestHeader;
import org.kuali.test.TestOperation;
import org.kuali.test.TestOperationType;
import org.kuali.test.TestSuite;
import org.kuali.test.TestType;
import org.kuali.test.ValueType;
import org.kuali.test.runner.exceptions.TestException;
import org.kuali.test.runner.output.PoiHelper;
import org.kuali.test.utils.Constants;
import org.kuali.test.utils.HtmlDomProcessor;
import org.kuali.test.utils.HtmlDomProcessor.DomInformation;
import org.kuali.test.utils.Utils;
import org.w3c.dom.Element;

/**
 *
 * @author rbtucker
 */
public class TestExecutionContext extends Thread {
    private static final Logger LOG = Logger.getLogger(TestExecutionContext.class);
    
    private List<File> generatedCheckpointFiles = new ArrayList<File>();
    private File testResultsFile;
    private int warningCount = 0;
    private int successCount = 0;
    private int errorCount = 0;

    private Map<String, String> autoReplaceParameterMap = new HashMap<String, String>();
    private Set<String> parametersRequiringDecryption = new HashSet<String>();

    private Stack<String> httpResponseStack;

    private Platform platform;
    private TestSuite testSuite;
    private KualiTest kualiTest;
    private Date scheduledTime;
    private Date startTime;
    private Date endTime;
    private int testRun = 1;
    private int testRuns = 1;
    private boolean completed = false;
    
    private WebClient webClient;
    private KualiTestConfigurationDocument.KualiTestConfiguration configuration;

    public TestExecutionContext() {
        init();
    }

    private void init() {
    }

    private void initializeHttpClient() {
        webClient = new WebClient(BrowserVersion.CHROME);
	    webClient.getOptions().setJavaScriptEnabled(true);
	    webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
	    webClient.getOptions().setThrowExceptionOnScriptError(false);
	    webClient.getOptions().setTimeout(Constants.DEFAULT_HTTP_CONNECT_TIMEOUT);
	    webClient.getOptions().setRedirectEnabled(true);
        webClient.setAjaxController(new NicelyResynchronizingAjaxController());

        webClient.setAlertHandler(new AlertHandler() {
            @Override
            public void handleAlert(Page page, String alert) {
                LOG.info(alert);
            }
        });

        webClient.setIncorrectnessListener(new IncorrectnessListener() {
            @Override
            public void notify(String msg, Object o) {
                LOG.info(msg);
            }
        });
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
        init();
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
        init();
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

    @SuppressWarnings("SleepWhileInLoop")
    public void runTest() {
        try {
            startTime = new Date();

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
                            } catch (InterruptedException ex) {
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

            endTime = new Date();
            testResultsFile = new File(buildTestReportFileName());

            poiHelper.writeFile(testResultsFile);
        } finally {
            cleanup();
            completed = true;
        }
    }

    private void cleanup() {
        if (webClient != null) {
            webClient.closeAllWindows();
        }
    }

    private String buildTestReportFileName() {
        StringBuilder retval = new StringBuilder(128);

        retval.append(configuration.getTestResultLocation());
        retval.append(Constants.FORWARD_SLASH);
        if (testSuite != null) {
            retval.append(testSuite.getPlatformName());
            retval.append(Constants.FORWARD_SLASH);
            retval.append(Utils.formatForFileName(testSuite.getName()));
        } else {
            retval.append(kualiTest.getTestHeader().getPlatformName());
            retval.append(Constants.FORWARD_SLASH);
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

        // if this is a web test then initialize the client
        if (TestType.WEB.equals(test.getTestHeader().getTestType())) {
            getWebClient();
            parametersRequiringDecryption.addAll(Arrays.asList(configuration.getParametersRequiringEncryption().getNameArray()));
        }

        for (TestOperation op : test.getOperations().getOperationArray()) {
            // if executeTestOperation returns false we want to halt test
            if (!executeTestOperation(test, op, poiHelper)) {
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
    private boolean executeTestOperation(KualiTestDocument.KualiTest test, TestOperation op, PoiHelper poiHelper) {
        boolean retval = true;
        OperationExecution opExec = null;

        Date opStartTime = new Date();
        try {
            opExec = OperationExecutionFactory.getInstance().getOperationExecution(test, this, op);
            if (opExec != null) {
                try {
                    opExec.execute(configuration, platform, test);
                    if (op.getOperation().getCheckpointOperation() != null) {
                        incrementSuccessCount();
                        poiHelper.writeSuccessEntry(op, opStartTime);
                    }
                } catch (TestException ex) {
                    throw ex;
                } catch (Exception ex) {
                    LOG.error(ex.toString(), ex);
                    throw new TestException(ex.toString(), op.getOperation(), ex);
                }
            }
        } catch (TestException ex) {
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
     * @param html
     */
    public void pushHttpResponse(String html) {
        httpResponseStack.push(html);
        if (httpResponseStack.size() > Constants.LAST_RESPONSE_STACK_SIZE) {
            httpResponseStack.remove(0);
        }
    }

    /**
     *
     * @return
     */
    public List<String> getRecentHttpResponseData() {
        List<String> retval = new ArrayList<String>();
        while (!httpResponseStack.empty()) {
            retval.add(httpResponseStack.pop());
        }

        return retval;
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
        List<TestExecutionContext> retval = new ArrayList<TestExecutionContext>();;
        retval.add(this);

        for (int i = 1; i < testRuns; ++i) {
            TestExecutionContext tec = new TestExecutionContext();
            tec.setStartTime(startTime);
            tec.setPlatform(platform);
            tec.setKualiTest(kualiTest);
            tec.setTestRun(i + 1);
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

    public List  <NameValuePair> replaceRequestParameterValues(List  <NameValuePair> nvplist, Map<String, String> paramMap) throws UnsupportedEncodingException  {
        List  <NameValuePair> retval = new ArrayList<NameValuePair>();
        
        if ((nvplist != null) && !nvplist.isEmpty()) {
            for (NameValuePair nvp : nvplist) {
                String replacement  = paramMap.get(nvp.getName());

                if (StringUtils.isNotBlank(replacement)) {
                    retval.add(new NameValuePair(nvp.getName(), replacement));
                } else {
                    retval.add(nvp);
                }
            }
        }
        
        return retval;
    }

    /**
     * 
     * @param nvplist
     * @return
     * @throws UnsupportedEncodingException 
     */
    public List <NameValuePair> decryptHttpParameters(List<NameValuePair> nvplist) throws UnsupportedEncodingException {
        List <NameValuePair>  retval = new ArrayList<NameValuePair>();
        String epass = Utils.getEncryptionPassword(configuration);

        if ((nvplist != null) && !nvplist.isEmpty()) {
            for (NameValuePair nvp : nvplist) {
                if (parametersRequiringDecryption.contains(nvp.getName())) {
                    retval.add(new NameValuePair(nvp.getName(), URLDecoder.decode(Utils.decrypt(epass, nvp.getValue()), CharEncoding.UTF_8)));
                } else {
                    retval.add(nvp);
                }
            }
        }
        
        return retval;
    }

    

    public Map<String, String> getAutoReplaceParameterMap() {
        return autoReplaceParameterMap;
    }

    public void updateAutoReplaceMap() {
        if ((configuration.getAutoReplaceParameters() != null) && !httpResponseStack.empty()) {
            Element element = HtmlDomProcessor.getInstance().getDomDocumentElement(httpResponseStack.peek());
            for (AutoReplaceParameter param : configuration.getAutoReplaceParameters().getAutoReplaceParameterArray()) {
                String value = Utils.findAutoReplaceParameterInDom(param, element);
                if (!autoReplaceParameterMap.containsKey(param.getParameterName()) && StringUtils.isNotBlank(value)) {
                    autoReplaceParameterMap.put(param.getParameterName(), value);
                }
            }
        }
    }

    public synchronized void updateTestExecutionParameters(KualiTest test, HtmlRequestOperation curop, String html) throws UnsupportedEncodingException {
        Map<String, TestExecutionParameter> map = new HashMap<String, TestExecutionParameter>();

        List<HtmlRequestOperation> hreqops = new ArrayList<HtmlRequestOperation>();
        List<Checkpoint> sqlops = new ArrayList<Checkpoint>();

        boolean foundit = false;
        for (TestOperation op : test.getOperations().getOperationArray()) {
            if ((op.getOperation().getHtmlRequestOperation() != null)
                && (curop == op.getOperation().getHtmlRequestOperation())) {
                foundit = true;
            }

            if (foundit) {
                if (op.getOperation().getHtmlRequestOperation() != null) {
                    hreqops.add(op.getOperation().getHtmlRequestOperation());
                } else if (CheckpointType.SQL.equals(op.getOperation().getCheckpointOperation().getType())) {
                    sqlops.add(op.getOperation().getCheckpointOperation());
                }
            }

            if (op.getOperation().getTestExecutionParameter() != null) {
                CheckpointProperty cp = op.getOperation().getTestExecutionParameter().getValueProperty();
                String key = Utils.buildCheckpointPropertyKey(cp);
                map.put(key, op.getOperation().getTestExecutionParameter());
            }
        }

        DomInformation dominfo = HtmlDomProcessor.getInstance().processDom(platform, html);

        for (CheckpointProperty cp : dominfo.getCheckpointProperties()) {
            String key = Utils.buildCheckpointPropertyKey(cp);
            TestExecutionParameter tep = map.get(key);
            if (tep != null) {
                if (StringUtils.isNotBlank(cp.getPropertyValue())) {
                    tep.setValue(cp.getPropertyValue().trim());

                    for (HtmlRequestOperation op : hreqops) {
                        updateTestExecutionParameters(op, tep);
                    }

                    for (Checkpoint sqlcp : sqlops) {
                        Parameter param = Utils.getCheckpointParameter(sqlcp, Constants.SQL_QUERY);
                        param.setValue(param.getValue().replace("${" + tep.getName() + "}", tep.getValue()));
                    }
                }
            }
        }
    }

    private void updateTestExecutionParameters(HtmlRequestOperation op, TestExecutionParameter tep) {
        String[] urlparts = Utils.getUrlParts(op.getUrl());
        StringBuilder url = new StringBuilder(512);
        url.append(urlparts[0]);

        List <NameValuePair> worklist = new ArrayList<NameValuePair>();
        
        if (StringUtils.isNotBlank(urlparts[1])) {
            List <NameValuePair> nvplist = Utils.getNameValuePairsFromUrlEncodedParams(urlparts[1]);

            for (NameValuePair nvp : nvplist) {
                if (StringUtils.isNotBlank(nvp.getValue())) {
                    if (nvp.getValue().trim().equals(tep.getValueProperty().getPropertyValue().trim())) {
                        worklist.add(new NameValuePair(nvp.getName(), tep.getValue()));
                    } else {
                        worklist.add(nvp);
                    }
                } else {
                    worklist.add(nvp);
                }
            }
            
            url.append(Utils.buildUrlEncodedParameterString(nvplist));
        }

        op.setUrl(url.toString());
        
        if (HttpMethod.POST.equalsIgnoreCase(op.getMethod())) {
            worklist.clear();
            RequestParameter param = Utils.getContentParameter(op);
            
            if (param != null) {
                if (Utils.isUrlFormEncoded(op)) {
                    List <NameValuePair> nvplist = Utils.getNameValuePairsFromUrlEncodedParams(Utils.getContentParameterFromRequestOperation(op));

                    for (NameValuePair nvp : nvplist) {
                        if (StringUtils.isNotBlank(nvp.getValue())) {
                            if (nvp.getValue().trim().equals(tep.getValueProperty().getPropertyValue().trim())) {
                                worklist.add(new NameValuePair(nvp.getName(), tep.getValue()));
                            } else {
                                worklist.add(nvp);
                            }
                        } else {
                            worklist.add(nvp);
                        }
                    }
                    
                    param.setValue(Utils.buildUrlEncodedParameterString(nvplist));
                } else if (Utils.isMultipart(op)) {
                    List <NameValuePair> nvplist = Utils.getNameValuePairsFromMultipartParams(Utils.getContentParameterFromRequestOperation(op));

                    for (NameValuePair nvp : nvplist) {
                        if (StringUtils.isNotBlank(nvp.getValue())) {
                            if (nvp.getValue().trim().equals(tep.getValueProperty().getPropertyValue().trim())) {
                                worklist.add(new NameValuePair(nvp.getName(), tep.getValue()));
                            } else {
                                worklist.add(nvp);
                            }
                        } else {
                            worklist.add(nvp);
                        }
                    }
                    
                    param.setValue(Utils.buildMultipartParameterString(nvplist));
                }
            }
        }
    }
    
    public WebClient getWebClient() {
        if (webClient == null) {
            initializeHttpClient();
            httpResponseStack = new Stack<String>();
        }
        
        return webClient;
    }
    
    

    public void incrementErrorCount() {
        errorCount++;
    }

    public void incrementWarningCount() {
        warningCount++;
    }

    public void incrementSuccessCount() {
        successCount++;
    }

    public void updateCounts(FailureAction.Enum failureAction) {
        if (failureAction != null) {
            switch (failureAction.intValue()) {
                case FailureAction.INT_ERROR_CONTINUE:
                case FailureAction.INT_ERROR_HALT_TEST:
                    incrementErrorCount();
                    break;
                case FailureAction.INT_WARNING:
                    incrementWarningCount();
                    break;
            }
        } else {
            incrementSuccessCount();
        }
    }

    public int getWarningCount() {
        return warningCount;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public Stack<String> getHttpResponseStack() {
        return httpResponseStack;
    }

    public Set<String> getParametersRequiringDecryption() {
        return parametersRequiringDecryption;
    }
}
