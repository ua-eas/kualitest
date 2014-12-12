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

import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.WebRequest;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.test.AutoReplaceParameter;
import org.kuali.test.Checkpoint;
import org.kuali.test.CheckpointProperty;
import org.kuali.test.CheckpointType;
import org.kuali.test.ComparisonOperator;
import org.kuali.test.FailureAction;
import org.kuali.test.HtmlRequestOperation;
import org.kuali.test.KualiApplication;
import org.kuali.test.KualiTestConfigurationDocument;
import org.kuali.test.KualiTestDocument.KualiTest;
import org.kuali.test.Operation;
import org.kuali.test.Parameter;
import org.kuali.test.Platform;
import org.kuali.test.RequestHeader;
import org.kuali.test.SuiteTest;
import org.kuali.test.TestExecutionParameter;
import org.kuali.test.TestHeader;
import org.kuali.test.TestOperation;
import org.kuali.test.TestOperationType;
import org.kuali.test.TestSuite;
import org.kuali.test.TestType;
import org.kuali.test.ValueType;
import org.kuali.test.handlers.parameter.ParameterHandler;
import org.kuali.test.runner.exceptions.TestException;
import org.kuali.test.runner.output.PoiHelper;
import org.kuali.test.utils.Constants;
import org.kuali.test.utils.Utils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xhtmlrenderer.pdf.ITextRenderer;

/**
 *
 * @author rbtucker
 */
public class TestExecutionContext extends Thread {
    private static final Logger LOG = Logger.getLogger(TestExecutionContext.class);
    
    private List<File> generatedCheckpointFiles = new ArrayList<File>();
    private File testResultsFile;

    private Map<String, String> autoReplaceParameterMap = new HashMap<String, String>();
    private Set<String> randomListAccessParameterToIgnore = new HashSet<String>();
    private Set<String> parametersRequiringDecryption = new HashSet<String>();
    private List <KualiTestWrapper> completedTests = new ArrayList<KualiTestWrapper>();
    
    private Platform platform;
    private TestSuite testSuite;
    private KualiTest kualiTest;
    private Date scheduledTime;
    private Date startTime;
    private Date endTime;
    private int testRun = 1;
    private int testRuns = 1;
    private boolean completed = false;
    private boolean haltTest = false;
    private String repeatInterval;
    private int testOperationCount = 0;
    private TestOperation currentTestOperation;
    private TestWebClient webClient;
    private KualiTestConfigurationDocument.KualiTestConfiguration configuration;
    private KualiTestWrapper currentTest;
    private PoiHelper poiHelper;
    private Integer currentOperationIndex;
    
    public TestExecutionContext(KualiTestConfigurationDocument.KualiTestConfiguration configuration) {
        this.configuration = configuration;
        init();
    }

    protected void init() {
    }

    private void initializeHttpClient() {
        cleanup();
        webClient = new TestWebClient(this);
    }

    /**
     * 
     * @param configuration
     * @param testSuite
     * @param scheduledTime
     * @param testRuns
     * @param repeatInterval 
     */
    public TestExecutionContext(KualiTestConfigurationDocument.KualiTestConfiguration configuration,
        TestSuite testSuite, Date scheduledTime, int testRuns, String repeatInterval) {
        this.testSuite = testSuite;
        this.scheduledTime = scheduledTime;
        this.configuration = configuration;
        this.testRuns = testRuns;
        this.repeatInterval = repeatInterval;
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
        this(configuration, testSuite, null, 1, null);
    }

    /**
     * 
     * @param configuration
     * @param kualiTest
     * @param scheduledTime
     * @param testRuns
     * @param repeatInterval 
     */
    public TestExecutionContext(KualiTestConfigurationDocument.KualiTestConfiguration configuration,
        KualiTest kualiTest, Date scheduledTime, int testRuns, String repeatInterval) {
        this.kualiTest = kualiTest;
        this.scheduledTime = scheduledTime;
        this.configuration = configuration;
        this.testRuns = testRuns;
        this.repeatInterval = repeatInterval;
        platform = Utils.findPlatform(configuration, kualiTest.getTestHeader().getPlatformName());
        init();
    }

    /**
     *
     * @param configuration
     * @param kualiTest
     */
    public TestExecutionContext(KualiTestConfigurationDocument.KualiTestConfiguration configuration, KualiTest kualiTest) {
        this(configuration, kualiTest, null, 1, null);
    }

    @Override
    public void run() {
        runTest();
    }

    public void runTest() {
        try {
            startTime = new Date();

            poiHelper = new PoiHelper();
            poiHelper.writeReportHeader(testSuite, kualiTest);
            poiHelper.writeColumnHeaders();

            if (testSuite != null) {
                int defaultTestWaitInterval = configuration.getDefaultTestWaitInterval();

                for (SuiteTest suiteTest : testSuite.getSuiteTests().getSuiteTestArray()) {
                    KualiTest test = Utils.findKualiTest(configuration, suiteTest.getTestHeader().getPlatformName(), suiteTest.getTestHeader().getTestName());
                    
                    if (test != null) {
                        // reinitialize new web client for each test
                        initializeHttpClient();
                        
                        // add pause between tests if configured
                        if (defaultTestWaitInterval > 0) {
                            try {
                                Thread.sleep(defaultTestWaitInterval * 1000);
                            } catch (InterruptedException ex) {
                                LOG.warn(ex.toString(), ex);
                            }
                        }

                        poiHelper.writeTestHeader(test);
                        runTest(new KualiTestWrapper(test), poiHelper);
                    }
                    
                    if (haltTest) {
                        break;
                    }
                }
            } else if (kualiTest != null) {
                runTest(new KualiTestWrapper(kualiTest), poiHelper);
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
        autoReplaceParameterMap.clear();
        
        if (webClient != null) {
            try {
                webClient.closeAllWindows();
                webClient = null;
            }
            
            catch (Exception ex) {};
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

    private void runTest(KualiTestWrapper testWrapper, PoiHelper poiHelper) {
        long start = System.currentTimeMillis();

        currentTest = testWrapper;
        
        System.out.println("starting test '" + testWrapper.getTestName() + "[" + getTestRun() + "]' for platform " + testWrapper.getPlatformName());

        // if this is a web test then initialize the client
        if (TestType.WEB.equals(testWrapper.getTestType())) {
            getWebClient();
            parametersRequiringDecryption.addAll(Arrays.asList(configuration.getParametersRequiringEncryption().getNameArray()));
        }

        
        testOperationCount = testWrapper.getOperations().length;
        
        for (TestOperation op : testWrapper.getOperations()) {
            currentTestOperation = op;
            
            // if executeTestOperation returns false we want to halt test
            if (!executeTestOperation(testWrapper, op, poiHelper)) {
                break;
            }
            
            if (haltTest) {
                break;
            }
        }
        
        // check for max runtime exceeded
        long runtime = ((System.currentTimeMillis() - start) / 1000);
        if ((testWrapper.getMaxRunTime() > 0) && (runtime > testWrapper.getMaxRunTime())) {
            if (configuration.getOutputIgnoredResults() 
                || !FailureAction.IGNORE.equals(testWrapper.getTestHeader().getOnRuntimeFailure())) {
                poiHelper.writeFailureEntry(createTestRuntimeCheckOperation(testWrapper.getTestHeader(), runtime), new Date(start), null);
            }

        }
        
        completedTests.add(testWrapper);
        
        System.out.println("test '" + testWrapper.getTestName() + "[" + getTestRun() + "]' completed. runtime=" + runtime + "sec.");
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
    private boolean executeTestOperation(KualiTestWrapper testWrapper, TestOperation op, PoiHelper poiHelper) {
        boolean retval = true;
        OperationExecution opExec = null;

        Date opStartTime = new Date();
        try {
            opExec = OperationExecutionFactory.getInstance().getOperationExecution(this, op);
            if (opExec != null) {
                try {
                    opExec.execute(configuration, platform, testWrapper);
                    
                    if (op.getOperationType().equals(TestOperationType.CHECKPOINT)) {
                        testWrapper.incrementSuccessCount();
                        poiHelper.writeSuccessEntry(op, opStartTime);
                    }
                } catch (TestException ex) {
                    throw ex;
                } catch (Exception ex) {
                    LOG.error(ex.toString(), ex);
                    Throwable t = ex.getCause();
                    if ((t != null) && (t instanceof TestException)) {
                        throw (TestException)t;
                    } else {
                        throw new TestException(ex.toString(), op.getOperation(), ex);
                    }
                }
            }
        } 
        
        catch (TestException ex) {
            if (configuration.getOutputIgnoredResults() 
                    || !FailureAction.IGNORE.equals(ex.getFailureAction())) {
                testWrapper.updateCounts(ex.getFailureAction());
                retval = poiHelper.writeFailureEntry(op, opStartTime, ex);
            }
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
            TestExecutionContext tec = new TestExecutionContext(configuration);
            tec.setStartTime(startTime);
            tec.setPlatform(platform);
            tec.setKualiTest(kualiTest);
            tec.setTestRun(i + 1);
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

    public Map<String, String> getAutoReplaceParameterMap() {
        return autoReplaceParameterMap;
    }
    
    
    public void updateAutoReplaceMap(Element element) {
        if (configuration.getAutoReplaceParameters() != null) {
            for (AutoReplaceParameter param : configuration.getAutoReplaceParameters().getAutoReplaceParameterArray()) {
                String value = Utils.findAutoReplaceParameterInDom(param, element);
                if (!autoReplaceParameterMap.containsKey(param.getParameterName()) && StringUtils.isNotBlank(value)) {
                    autoReplaceParameterMap.put(param.getParameterName(), value);
                }
            }
        }
    }

    public TestWebClient getWebClient() {
        if (webClient == null) {
            initializeHttpClient();
        }
        
        return webClient;
    }

    public Set<String> getParametersRequiringDecryption() {
        return parametersRequiringDecryption;
    }
    
    public List<KualiTestWrapper> getCompletedTests() {
        return completedTests;
    }

    public String getRepeatInterval() {
        return repeatInterval;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public TestOperation getCurrentTestOperation() {
        return currentTestOperation;
    }

    public int getTestOperationCount() {
        return testOperationCount;
    }
    
    public Map <String, TestExecutionParameter> getTestExecutionByValueParameterMap() {
        Map<String, TestExecutionParameter> retval = new HashMap<String, TestExecutionParameter>();

        for (TestOperation top : currentTest.getOperations()) {
            if (top.getOperation().getTestExecutionParameter() != null) {
                if (top.getOperation().getIndex() > currentOperationIndex.intValue()) {
                    break;
                }
                
                TestExecutionParameter tep = top.getOperation().getTestExecutionParameter();
                ParameterHandler ph = Utils.getParameterHandler(tep.getParameterHandler());
                
                if (StringUtils.isNotBlank(tep.getValue()) && ph.isReplaceByValue()) {
                    retval.put(tep.getValueProperty().getPropertyValue(), tep);
                }
            }
        }
        
        return retval;
    }
    
    public Map <String, TestExecutionParameter> getTestExecutionByElementNameParameterMap() {
        Map<String, TestExecutionParameter> retval = new HashMap<String, TestExecutionParameter>();

        for (TestOperation top : currentTest.getOperations()) {
            if (top.getOperation().getTestExecutionParameter() != null) {
                if (top.getOperation().getIndex() > currentOperationIndex.intValue()) {
                    break;
                }
                
                TestExecutionParameter tep = top.getOperation().getTestExecutionParameter();
                ParameterHandler ph = Utils.getParameterHandler(tep.getParameterHandler());

                if (StringUtils.isNotBlank(tep.getValue()) && !ph.isReplaceByValue()) {
                    if (tep.getValueProperty().getTagInformation() != null) {
                        for (Parameter p : tep.getValueProperty().getTagInformation().getParameterArray()) {
                            if (Constants.HTML_TAG_ATTRIBUTE_NAME.equals(p.getName())) {
                                retval.put(p.getValue(), tep);
                                break;
                            }
                        }
                    }
                }
            }
        }
        
        return retval;
    }

    public Map <String, TestExecutionParameter> getTestExecutionByParameterNameMap() {
        Map<String, TestExecutionParameter> retval = new HashMap<String, TestExecutionParameter>();

        for (TestOperation top : currentTest.getOperations()) {
            if (top.getOperation().getTestExecutionParameter() != null) {
                if (top.getOperation().getIndex() > currentOperationIndex.intValue()) {
                    break;
                }
                TestExecutionParameter tep = top.getOperation().getTestExecutionParameter();

                if (StringUtils.isNotBlank(tep.getValue())) {
                    retval.put(tep.getName(), tep);
                }
            }
        }
        
        return retval;
    }

    public KualiTestWrapper getCurrentTest() {
        return currentTest;
    }

    public void setCurrentTest(KualiTestWrapper currentTest) {
        this.currentTest = currentTest;
    }

    public Integer getCurrentOperationIndex() {
        return currentOperationIndex;
    }

    public void setCurrentOperationIndex(Integer currentOperationIndex) {
        this.currentOperationIndex = currentOperationIndex;
    }
    
    public boolean writeFailureEntry(TestOperation op, Date startTime, TestException ex) {
        if (poiHelper != null) {
            return poiHelper.writeFailureEntry(op, startTime, ex);
        } else {
            return false;
        }
    }

    public void writeCommentEntry(Operation op, boolean showIndex) {
        if (poiHelper != null) {
            poiHelper.writeCommentEntry(op, showIndex);
        } 
    }

    public synchronized void haltTest() {
        haltTest = true;
        poiHelper.writeFailureEntry(getCurrentTestOperation(), new Date(), 
            new TestException("Test(s) halted", getCurrentTestOperation().getOperation(), FailureAction.ERROR_HALT_TEST));
    }
    
    public synchronized void haltTest(TestException ex) {
        haltTest = true;
        getCurrentTest().incrementErrorCount();
        poiHelper.writeFailureEntry(getCurrentTestOperation(), new Date(), ex);
    }
    
    public void resubmitLastGetRequest() throws MalformedURLException, IOException {
        TestOperation[] operations = getCurrentTest().getTest().getOperations().getOperationArray();
        HtmlRequestOperation lastHtmlOp = null;
        int startpos = -1;

        // find current operation
        for (int i = 0; i < operations.length; ++i) {
            if (operations[i].getOperation().getIndex() == currentOperationIndex) {
                startpos = i;
                break;
            }
        }        

        // find previous html request if it exists
        for (int i = startpos-1; i >= 0; --i) {
            if (operations[i].getOperation().getHtmlRequestOperation() != null) {
                lastHtmlOp = operations[i].getOperation().getHtmlRequestOperation();
                break;
            }
        }
        
        // if the previous html request was a get then we will resubmit
        if ((lastHtmlOp != null) && Constants.HTTP_REQUEST_METHOD_GET.equals(lastHtmlOp.getMethod())) {
            WebRequest request = new WebRequest(new URL(lastHtmlOp.getUrl()), HttpMethod.valueOf(lastHtmlOp.getMethod()));
            if (lastHtmlOp.getRequestHeaders() != null) {
                for (RequestHeader hdr : lastHtmlOp.getRequestHeaders().getHeaderArray()) {
                    request.setAdditionalHeader(hdr.getName(), hdr.getValue());
                }
            }
            
            webClient.getPage(request);
        }
    }

    public Set<String> getRandomListAccessParameterToIgnore() {
        return randomListAccessParameterToIgnore;
    }
    
    public TestOperation getTestOperation(int i) {
        TestOperation retval = null;
        
        if ((i >= 0) && (i < getCurrentTest().getTest().getOperations().sizeOfOperationArray())) {
            retval = getCurrentTest().getTest().getOperations().getOperationArray(i);
        }
        
        return retval;
    }
    
    public KualiApplication.Enum getApplication() {
        return platform.getApplication();
    }
    
    private String getSaveScreenFileName() {
        StringBuilder retval = new StringBuilder(256);
        
        retval.append(configuration.getTestResultLocation());
        retval.append(Constants.FORWARD_SLASH);
        retval.append(platform.getName());
        retval.append(Constants.FORWARD_SLASH);
        retval.append(Constants.SCREEN_CAPTURE_DIR);
        retval.append(Constants.FORWARD_SLASH);
        retval.append(Constants.DEFAULT_DATE_FORMAT.format(new Date()));
        retval.append(Constants.FORWARD_SLASH);
        
        if (getCurrentTestOperation().getOperation().getCheckpointOperation() != null) {
            retval.append(getCurrentTestOperation().getOperation().getCheckpointOperation().getTestName().toLowerCase().replace(" ", "-"));
            retval.append("_");
            retval.append(getCurrentTestOperation().getOperation().getCheckpointOperation().getName().toLowerCase().replace(" ", "-"));
        } else {
            retval.append(getCurrentTest().getTest().getTestHeader().getTestName().toLowerCase().replace(" ", "-"));
            retval.append("-op");
            retval.append(this.getCurrentOperationIndex());
            retval.append("");
        }
        
        retval.append("_");
        retval.append(Constants.FILENAME_TIMESTAMP_FORMAT.format(new Date()));
        retval.append("_");
        retval.append(getTestRun());
        retval.append(Constants.PDF_SUFFIX);
        
        return retval.toString();
    }

    
    private String formatHtmlForPdf(String html) {
        String retval = html;
        StringBuilder buf = new StringBuilder(html.length());
        
        int pos = html.indexOf("</head>");
        
        if (pos > -1) {
            // add this css landscape to ensure page is not truncated on right
            buf.append(html.substring(0, pos));
            buf.append("<style> @page {size: landscape;} </style>");
            buf.append(html.substring(pos));
            retval = buf.toString();
        }
        
        return retval;
    }
    
    public void saveCurrentScreen(String html, boolean errorMode) {
        File f = null;
        
        if (errorMode) {
            f = new File(this.getErrorFileName(Constants.PDF_SUFFIX));
        } else {
            new File(getSaveScreenFileName());
        }

        if (!f.getParentFile().exists()) {
            f.getParentFile().mkdirs();
        }
        
        saveCurrentScreen(f, html, false);
    }

    public void saveCurrentScreen(File saveFile, String html, boolean debug) {
        if (StringUtils.isBlank(html)) {
            html = Constants.NO_HTML_FOUND;
        }
        
        Document doc = Utils.cleanHtml(formatHtmlForPdf(html), new String[] {"input.type=hidden,name=script"});

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(saveFile);
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocument(doc, platform.getWebUrl());
            renderer.layout();
            renderer.createPDF(fos);
            if (!debug) {
                getGeneratedCheckpointFiles().add(saveFile);
            }
        }

        catch (Exception ex) {
            LOG.error(ex.toString(), ex);
        }

        finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            }

            catch (Exception ex) {};
        }
    }
    
    public String getErrorFileName(String suffix) {
        StringBuilder retval = new StringBuilder(256);
        
        retval.append(getConfiguration().getTestResultLocation());
        retval.append(Constants.FORWARD_SLASH);
        retval.append(getCurrentTest().getTestHeader().getPlatformName());
        retval.append(Constants.FORWARD_SLASH);
        retval.append(Constants.SCREEN_CAPTURE_DIR);
        retval.append(Constants.FORWARD_SLASH);
        retval.append(Constants.DEFAULT_DATE_FORMAT.format(new Date()));
        retval.append(Constants.FORWARD_SLASH);
        retval.append(getCurrentTest().getTestName().toLowerCase().replace(" ", "-"));
        retval.append("_error-output_");
        retval.append(Constants.FILENAME_TIMESTAMP_FORMAT.format(new Date()));
        retval.append("_");
        retval.append(getTestRun());
        retval.append(suffix);
        
        return retval.toString();
    }
}
