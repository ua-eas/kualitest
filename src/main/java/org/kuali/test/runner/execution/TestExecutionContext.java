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
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.MessageConstraints;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.HttpConnectionFactory;
import org.apache.http.conn.ManagedHttpClientConnection;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.conn.DefaultHttpResponseParser;
import org.apache.http.impl.conn.DefaultHttpResponseParserFactory;
import org.apache.http.impl.conn.ManagedHttpClientConnectionFactory;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.conn.SystemDefaultDnsResolver;
import org.apache.http.impl.io.DefaultHttpRequestWriterFactory;
import org.apache.http.io.HttpMessageParser;
import org.apache.http.io.HttpMessageParserFactory;
import org.apache.http.io.HttpMessageWriterFactory;
import org.apache.http.io.SessionInputBuffer;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicLineParser;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.message.LineParser;
import org.apache.http.util.CharArrayBuffer;
import org.apache.log4j.Logger;
import org.kuali.test.AutoReplaceParameter;
import org.kuali.test.Checkpoint;
import org.kuali.test.CheckpointProperty;
import org.kuali.test.CheckpointType;
import org.kuali.test.ComparisonOperator;
import org.kuali.test.FailureAction;
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
    private List <File> generatedCheckpointFiles = new ArrayList<File>();
    private File testResultsFile;
    private int warningCount = 0;
    private int successCount = 0;
    private int errorCount = 0;
    
    private Map<String, String> autoReplaceParameterMap = new HashMap<String, String>();
    private Map<String, String> executionParameterMap = new HashMap<String, String>();
    
    private Stack <String> httpResponseStack;
    
    private Platform platform;
    private TestSuite testSuite;
    private KualiTest kualiTest;
    private Date scheduledTime;
    private Date startTime;
    private Date endTime;
    private int testRun = 1;
    private int testRuns = 1;
    private boolean completed = false;
    private CloseableHttpClient httpClient;
    private KualiTestConfigurationDocument.KualiTestConfiguration configuration;
    private CookieStore cookieStore;
    /**
     *
     */
    public TestExecutionContext() {
        init();
    }
    
    private void init() {
        initializeHttpClient();
        httpResponseStack = new Stack<String>();
    }
    
    private void initializeHttpClient() {
        // Use custom message parser / writer to customize the way HTTP
        // messages are parsed from and written out to the data stream.
        HttpMessageParserFactory<HttpResponse> responseParserFactory = new DefaultHttpResponseParserFactory() {

            @Override
            public HttpMessageParser<HttpResponse> create(
                SessionInputBuffer buffer, MessageConstraints constraints) {
                LineParser lineParser = new BasicLineParser() {

                    @Override
                    public Header parseHeader(final CharArrayBuffer buffer) {
                        try {
                            return super.parseHeader(buffer);
                        } 
                        
                        catch (ParseException ex) {
                            return new BasicHeader(buffer.toString(), null);
                        }
                    }

                };
                return new DefaultHttpResponseParser(
                    buffer, lineParser, DefaultHttpResponseFactory.INSTANCE, constraints) {

                    @Override
                    protected boolean reject(final CharArrayBuffer line, int count) {
                        // try to ignore all garbage preceding a status line infinitely
                        return false;
                    }

                };
            }
        };
        
        HttpMessageWriterFactory<HttpRequest> requestWriterFactory = new DefaultHttpRequestWriterFactory();

        // Use a custom connection factory to customize the process of
        // initialization of outgoing HTTP connections. Beside standard connection
        // configuration parameters HTTP connection factory can define message
        // parser / writer routines to be employed by individual connections.
        HttpConnectionFactory<HttpRoute, ManagedHttpClientConnection> connFactory = new ManagedHttpClientConnectionFactory(
                requestWriterFactory, responseParserFactory);

        // Create a registry of custom connection socket factories for supported
        // protocol schemes.
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
            .register(Constants.HTTP, PlainConnectionSocketFactory.INSTANCE)
            .register(Constants.HTTPS, new SSLConnectionSocketFactory(SSLContexts.createSystemDefault(), new AllowAllHostnameVerifier()))
            .build();

        // Create a connection manager with custom configuration.
        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(
                socketFactoryRegistry, connFactory, new SystemDefaultDnsResolver());

        // Create socket configuration
        SocketConfig socketConfig = SocketConfig.custom()
            .setTcpNoDelay(true)
            .build();

        // Configure the connection manager to use socket configuration either
        // by default or for a specific host.
        connManager.setDefaultSocketConfig(socketConfig);

        // Create message constraints
        MessageConstraints messageConstraints = MessageConstraints.custom()
            .setMaxHeaderCount(200)
            .setMaxLineLength(2000)
            .build();

        // Create connection configuration
        ConnectionConfig connectionConfig = ConnectionConfig.custom()
            .setCharset(Consts.UTF_8)
            .setMessageConstraints(messageConstraints)
            .build();
        
        // Configure the connection manager to use connection configuration either
        // by default or for a specific host.
        connManager.setDefaultConnectionConfig(connectionConfig);

        // Configure total max or per route limits for persistent connections
        // that can be kept in the pool or leased by the connection manager.
        connManager.setMaxTotal(100);
        connManager.setDefaultMaxPerRoute(10);

        // Use custom cookie store if necessary.
        cookieStore = new BasicCookieStore();

        // Use custom credentials provider if necessary.
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        
        // Create global request configuration
        RequestConfig defaultRequestConfig = RequestConfig.custom()
            .setCookieSpec(CookieSpecs.BEST_MATCH)
            .setExpectContinueEnabled(true)
            .setRedirectsEnabled(true)
            .build();

                    
        httpClient = HttpClients.custom()
            .setConnectionManager(connManager)
            .setDefaultCookieStore(cookieStore)
            .setDefaultCredentialsProvider(credentialsProvider)
            .setDefaultRequestConfig(defaultRequestConfig)
            .setRedirectStrategy(new LaxRedirectStrategy())
            .build();
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
        HttpClientUtils.closeQuietly(httpClient);
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
    public List <String> getRecentHttpResponseData() {
        List <String> retval = new ArrayList<String>();
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
    
    private String findTestExecutionParameterValue(TestExecutionParameter tep) {
        String retval = null;
        
        String key = Utils.buildCheckpointPropertyKey(tep);
        
        for (String html : getRecentHttpResponseData()) {
            if (StringUtils.isNotBlank(html)) {
                HtmlDomProcessor.DomInformation dominfo = HtmlDomProcessor.getInstance().processDom(platform, html);

                for (CheckpointProperty cp : dominfo.getCheckpointProperties()) {
                    if (key.equals(Utils.buildCheckpointPropertyKey(cp))) {
                        retval = cp.getPropertyValue();
                        break;
                    }
                }
            }
            
            if (StringUtils.isNotBlank(retval)) {
                break;
            }
        }
        
        
        return retval;

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
    
    private Cookie findJSessionIdCookie(String host) {
        Cookie retval = null;
        
        for (Cookie c : cookieStore.getCookies()) {
            if (c.getDomain().equalsIgnoreCase(host) && c.getName().equalsIgnoreCase(Constants.JSESSIONID_PARAMETER_NAME)) {
                retval = c;
                break;
            }
        }
        
        return retval;
    }
    
    private String replaceJsessionId(String input) {
        StringBuilder retval = new StringBuilder(input.length());
                
        int pos = input.toLowerCase().indexOf(Constants.JSESSIONID_PARAMETER_NAME);
        if (pos > -1) {
            try {
                Cookie cookie = findJSessionIdCookie(new URIBuilder(input).getHost());
                if (cookie != null) {
                    int pos2 = input.indexOf(Constants.SEPARATOR_QUESTION);
                    retval.append(input.subSequence(0, pos));
                    retval.append(cookie.getName());
                    retval.append("=");
                    retval.append(cookie.getValue());

                    if (pos2 > -1) {
                        retval.append(input.substring(pos2));
                    }
                } else {
                    retval.append(input);
                }
            } 

            catch (URISyntaxException ex) {
                LOG.warn(ex.toString(), ex);
            }
        } else {
            retval.append(input);
        }
        
        
        return retval.toString();
    }
    
    /**
     *
     * @param input
     * @return
     */
    public String replaceTestExecutionParameters(String input) {
        StringBuilder retval = new StringBuilder(input.length());

        String parameterString = null;

        // if this is a GET request then find the parameter string
        if (input.startsWith(Constants.HTTP_PROTOCOL) || input.startsWith(Constants.HTTPS_PROTOCOL)) {
            int pos = input.indexOf(Constants.SEPARATOR_QUESTION);
            if (pos > -1) {
                parameterString = input.substring(pos+1);
                retval.append(input.substring(0, pos+1));
            } 
        } else {
            parameterString = input;
        }

        // if we have a parameter string then convert to NameValuePair list and process
        if (StringUtils.isNotBlank(parameterString)) {
            List <NameValuePair> nvplist = URLEncodedUtils.parse(parameterString, Consts.UTF_8);

            if ((nvplist != null) && !nvplist.isEmpty()) {
                NameValuePair[] nvparray = nvplist.toArray(new NameValuePair[nvplist.size()]);
                
                // decrypt encrypted parameters
                for (String parameterName : configuration.getParametersRequiringEncryption().getNameArray()) {
                    for (int i = 0; i < nvparray.length; ++i) {
                        if (parameterName.equals(nvparray[i].getName())) {
                            nvparray[i] = new BasicNameValuePair(parameterName, Utils.decrypt(Utils.getEncryptionPassword(configuration), nvparray[i].getValue()));
                        }
                    }
                }

                for (int i = 0; i < nvparray.length; ++i) {
                    if (executionParameterMap.containsKey(nvparray[i].getName())) {
                        nvparray[i] = new BasicNameValuePair(nvparray[i].getName(), executionParameterMap.get(nvparray[i].getName()));
                    }
                }
                
                for (String parameterName : getAutoReplaceParameterMap().keySet()) {
                    for (int i = 0; i < nvparray.length; ++i) {
                        if (parameterName.equals(nvparray[i].getName())) {
                            nvparray[i] = new BasicNameValuePair(parameterName, getAutoReplaceParameterMap().get(parameterName));
                        }
                    }
/*
                    // remove the parameter if retain is false
                    AutoReplaceParameter autoReplaceParameter = Utils.findAutoReplaceParameterByName(configuration, parameterName);
                    if (autoReplaceParameter != null) {
                        if (!autoReplaceParameter.getRetain()) {
                            getAutoReplaceParameterMap().remove(parameterName);
                        }
                    }
    */
                }

                retval.append(URLEncodedUtils.format(Arrays.asList(nvparray), Consts.UTF_8));
            } else {
                retval.append(input);
            }
        } else {
            retval.append(input);
        }
        
        return replaceJsessionId(retval.toString());
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

    public CloseableHttpClient getHttpClient() {
        return httpClient;
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
            switch(failureAction.intValue()) {
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
}


