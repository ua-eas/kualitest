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
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.StringTokenizer;
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
import org.apache.http.client.methods.HttpPost;
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
import org.apache.http.entity.mime.MultipartEntityBuilder;
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
    private List <File> generatedCheckpointFiles = new ArrayList<File>();
    private File testResultsFile;
    private int warningCount = 0;
    private int successCount = 0;
    private int errorCount = 0;
    
    private Map<String, String> autoReplaceParameterMap = new HashMap<String, String>();
    
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
        
        decryptHttpParameters(test);
        
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
                }
                
                catch (TestException ex) {
                    throw ex;
                }

                catch (Exception ex) {
                    LOG.error(ex.toString(), ex);
                    throw new TestException(ex.toString(), op.getOperation(), ex);
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

    private String findTestExecutionParameterValue(TestExecutionParameter tep) {
        String retval = null;

        String valueKey = Utils.buildCheckpointPropertyKey(tep.getValueProperty());
        
        for (String html : getRecentHttpResponseData()) {
            if (StringUtils.isNotBlank(html)) {
                HtmlDomProcessor.DomInformation dominfo = HtmlDomProcessor.getInstance().processDom(platform, html);
                for (CheckpointProperty cp : dominfo.getCheckpointProperties()) {
                    String key = Utils.buildCheckpointPropertyKey(cp);
                    
                    if (key.equals(valueKey)) {
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
     * @param test
     * @param ep 
     */
    public void processTestExecutionParameter(KualiTest test, TestExecutionParameter ep) {
        String value = findTestExecutionParameterValue(ep);
        if (StringUtils.isNotBlank(value)) {
            TestOperation[] ops = kualiTest.getOperations().getOperationArray();
            
            int startpos = -1;
            for (int i = 0; i < ops.length; ++i) {
                if (ops[i].getOperation().getTestExecutionParameter() != null) {
                    if (ep == ops[i].getOperation().getTestExecutionParameter()) {
                        startpos = i;
                        break;
                    }
                }
            }
            
            if (startpos > -1) {
                Map <String, String> map = new HashMap<String, String>();
                for (int i = (startpos+1); i < ops.length; ++i) {
                    if (ops[i].getOperation().getHtmlRequestOperation() != null) {
                        replaceUrlFormEncodedTestExecutionParams(ops[i].getOperation().getHtmlRequestOperation(), ep);
                        replaceMultiPartTestExecutionParams(ops[i].getOperation().getHtmlRequestOperation(), ep);
                    } else if (ops[i].getOperation().getCheckpointOperation() != null) {
                        if (CheckpointType.SQL.equals(ops[i].getOperation().getCheckpointOperation().getType())) {
                            Checkpoint cp = ops[i].getOperation().getCheckpointOperation();

                            Parameter param = Utils.getCheckpointParameter(cp, Constants.SQL_QUERY);
                            param.setValue(param.getValue().replace("${" + ep.getName() + "}", ep.getValueProperty().getActualValue()));
                        }
                    }
                }
            }
        }
    }
    
    public void processAutoReplaceParameters(KualiTest test, HtmlRequestOperation reqop) {
        if (autoReplaceParameterMap.size() > 0) {
            TestOperation[] ops = kualiTest.getOperations().getOperationArray();
            for (int i = 0; i < ops.length; ++i) {
                if (ops[i].getOperation().getHtmlRequestOperation() != null) {
                    replaceUrlFormEncodedParams(ops[i].getOperation().getHtmlRequestOperation(), autoReplaceParameterMap);
                    replaceMultiPartParams(ops[i].getOperation().getHtmlRequestOperation(), autoReplaceParameterMap);
                } 
            }
            
            autoReplaceParameterMap.clear();
        }
    }

    
    private void replaceMultiPartTestExecutionParams(HtmlRequestOperation op, TestExecutionParameter ep) {
        if (Utils.isMultipart(op)) {
            RequestParameter param = Utils.getContentParameter(op);
            
            if (param != null) {
                String params = param.getValue();
                StringBuilder buf = new StringBuilder(params.length());

                StringTokenizer st1 = new StringTokenizer(params, Constants.MULTIPART_PARAMETER_SEPARATOR);

                String seperator = "";

                while (st1.hasMoreElements()) {
                    StringTokenizer st2 = new StringTokenizer(st1.nextToken(), Constants.MULTIPART_NAME_VALUE_SEPARATOR);
                    
                    if (st2.countTokens() == 2) {
                        String name = st2.nextToken();
                        String value = st2.nextToken();

                        if (value.equals(ep.getValueProperty().getPropertyValue())) {
                            value = ep.getValueProperty().getActualValue();
                        }

                        buf.append(seperator);
                        buf.append(name);
                        buf.append(Constants.MULTIPART_NAME_VALUE_SEPARATOR);
                        buf.append(value);
                        seperator = Constants.MULTIPART_PARAMETER_SEPARATOR;
                    }
                }
                
                param.setValue(buf.toString());
            }
        }
    }
    
    private void replaceUrlFormEncodedTestExecutionParams(HtmlRequestOperation op, TestExecutionParameter ep) {
        String params = Utils.getParamsFromUrl(op.getUrl());
                        
                                // if we have a parameter string then convert to NameValuePair list and process
        if (StringUtils.isNotBlank(params)) {
            List <NameValuePair> nvplist = URLEncodedUtils.parse(params, Consts.UTF_8);

            if ((nvplist != null) && !nvplist.isEmpty()) {
                NameValuePair[] nvparray = nvplist.toArray(new NameValuePair[nvplist.size()]);

                for (int i = 0; i < nvparray.length; ++i) {
                    if (nvparray[i].getValue().equals(ep.getValueProperty().getPropertyValue())) {
                        nvparray[i] = new BasicNameValuePair(nvparray[i].getName(), ep.getValueProperty().getActualValue());
                    }
                }
            
                int pos = op.getUrl().indexOf(Constants.SEPARATOR_QUESTION);

                if (pos > -1) {
                    StringBuilder buf = new StringBuilder(op.getUrl().length());
                    buf.append(op.getUrl().substring(0, pos));
                    buf.append(Constants.SEPARATOR_QUESTION);
                    buf.append(URLEncodedUtils.format(Arrays.asList(nvparray), Consts.UTF_8));
                    op.setUrl(buf.toString());
                }
            }
            
            if (Utils.isUrlFormEncoded(op)) {
                RequestParameter param = Utils.getContentParameter(op);
                if (param != null) {
                    nvplist = URLEncodedUtils.parse(param.getValue(), Consts.UTF_8);
                    NameValuePair[] nvparray = nvplist.toArray(new NameValuePair[nvplist.size()]);

                    for (int i = 0; i < nvparray.length; ++i) {
                        if (nvparray[i].getValue().equals(ep.getValueProperty().getPropertyValue())) {
                            nvparray[i] = new BasicNameValuePair(nvparray[i].getName(), ep.getValueProperty().getActualValue());
                        }
                    }

                    param.setValue(URLEncodedUtils.format(Arrays.asList(nvparray), Consts.UTF_8));
                }
            }
        }
    }
    
    private void replaceMultiPartParams(HtmlRequestOperation op, Map<String, String> paramMap) {
        if (Utils.isMultipart(op)) {
            RequestParameter param = Utils.getContentParameter(op);
            
            if (param != null) {
                String params = param.getValue();
                StringBuilder buf = new StringBuilder(params.length());

                StringTokenizer st1 = new StringTokenizer(params, Constants.MULTIPART_PARAMETER_SEPARATOR);

                String seperator = "";

                while (st1.hasMoreElements()) {
                    StringTokenizer st2 = new StringTokenizer(st1.nextToken(), Constants.MULTIPART_NAME_VALUE_SEPARATOR);
                    
                    if (st2.countTokens() == 2) {
                        String name = st2.nextToken();
                        String value = st2.nextToken();

                        String replacement = paramMap.get(name);
                        
                        if (StringUtils.isNotBlank(replacement)) {
                            value = replacement;
                        }

                        buf.append(seperator);
                        buf.append(name);
                        buf.append(Constants.MULTIPART_NAME_VALUE_SEPARATOR);
                        buf.append(value);
                        seperator = Constants.MULTIPART_PARAMETER_SEPARATOR;
                    }
                }
                
                param.setValue(buf.toString());
            }
        }
    }
    
    private void replaceUrlFormEncodedParams(HtmlRequestOperation op, Map<String, String> paramMap) {
        String params = Utils.getParamsFromUrl(op.getUrl());
                        
                                // if we have a parameter string then convert to NameValuePair list and process
        if (StringUtils.isNotBlank(params)) {
            List <NameValuePair> nvplist = URLEncodedUtils.parse(params, Consts.UTF_8);

            if ((nvplist != null) && !nvplist.isEmpty()) {
                NameValuePair[] nvparray = nvplist.toArray(new NameValuePair[nvplist.size()]);

                for (int i = 0; i < nvparray.length; ++i) {
                    String replacement = paramMap.get(nvparray[i].getName());
                    
                    if (StringUtils.isNotBlank(replacement)) {
                        nvparray[i] = new BasicNameValuePair(nvparray[i].getName(), replacement);
                    }
                }
            
                int pos = op.getUrl().indexOf(Constants.SEPARATOR_QUESTION);

                if (pos > -1) {
                    StringBuilder buf = new StringBuilder(op.getUrl().length());
                    buf.append(op.getUrl().substring(0, pos));
                    buf.append(Constants.SEPARATOR_QUESTION);
                    buf.append(URLEncodedUtils.format(Arrays.asList(nvparray), Consts.UTF_8));
                    op.setUrl(buf.toString());
                }
            }     
            if (Utils.isUrlFormEncoded(op)) {
                RequestParameter param = Utils.getContentParameter(op);
                if (param != null) {
                    nvplist = URLEncodedUtils.parse(param.getValue(), Consts.UTF_8);
                    NameValuePair[] nvparray = nvplist.toArray(new NameValuePair[nvplist.size()]);

                    for (int i = 0; i < nvparray.length; ++i) {
                        String replacement = paramMap.get(nvparray[i].getName());

                        if (StringUtils.isNotBlank(replacement)) {
                            nvparray[i] = new BasicNameValuePair(nvparray[i].getName(), replacement);
                        }
                    }

                    param.setValue(URLEncodedUtils.format(Arrays.asList(nvparray), Consts.UTF_8));
                }
            }
        }
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
     * @param test 
     */
    public void decryptHttpParameters(KualiTest test) {
        
        String epass = Utils.getEncryptionPassword(configuration);
        if (configuration.getParametersRequiringEncryption() != null) {
            Set<String> hs = new HashSet<String>(Arrays.asList(configuration.getParametersRequiringEncryption().getNameArray()));
            
            for (TestOperation op : test.getOperations().getOperationArray()) {
                if (op.getOperation().getHtmlRequestOperation() != null) {
                    HtmlRequestOperation reqop = op.getOperation().getHtmlRequestOperation();
                    String params = Utils.getParamsFromUrl(reqop.getUrl());
                    List <NameValuePair> nvplist = URLEncodedUtils.parse(params, Consts.UTF_8);

                    if ((nvplist != null) && !nvplist.isEmpty()) {
                        NameValuePair[] nvparray = nvplist.toArray(new NameValuePair[nvplist.size()]);

                        // decrypt encrypted parameters
                        for (int i = 0; i < nvparray.length; ++i) {
                            if (hs.contains(nvparray[i].getName())) {
                                nvparray[i] = new BasicNameValuePair(nvparray[i].getName(), Utils.decrypt(Utils.getEncryptionPassword(configuration), nvparray[i].getValue()));
                            }
                        }
                        
                        int pos = reqop.getUrl().indexOf(Constants.SEPARATOR_QUESTION);
                        
                        StringBuilder buf = new StringBuilder(reqop.getUrl().length());
                        buf.append(reqop.getUrl().substring(0, pos));
                        buf.append(Constants.SEPARATOR_QUESTION);
                        buf.append(URLEncodedUtils.format(Arrays.asList(nvparray), Consts.UTF_8));

                        reqop.setUrl(replaceJsessionId(buf.toString()));
                    }
                    
                    RequestParameter param = Utils.getContentParameter(reqop);
                        
                    if (param != null) {
                        if (Utils.isMultipart(reqop)) {
                            String seperator = "";
                            StringBuilder buf = new StringBuilder(param.getValue().length());
                            StringTokenizer st1 = new StringTokenizer(param.getValue(), Constants.MULTIPART_PARAMETER_SEPARATOR);
                            while (st1.hasMoreTokens()) {
                                StringTokenizer st2 = new StringTokenizer(st1.nextToken(), Constants.MULTIPART_NAME_VALUE_SEPARATOR);
                            
                                if (st2.countTokens() == 2) {
                                    String name = st2.nextToken();
                                    String value = st2.nextToken();

                                    if (hs.contains(name)) {
                                        value = Utils.decrypt(Utils.getEncryptionPassword(configuration), value);
                                    }

                                    buf.append(seperator);
                                    buf.append(name);
                                    buf.append(Constants.MULTIPART_NAME_VALUE_SEPARATOR);
                                    buf.append(value);
                                    seperator = Constants.MULTIPART_PARAMETER_SEPARATOR;
                                }
                            }
                            
                            param.setValue(buf.toString());
                        } else if (Utils.isUrlFormEncoded(reqop)) {
                            nvplist = URLEncodedUtils.parse(param.getValue(), Consts.UTF_8);

                            if ((nvplist != null) && !nvplist.isEmpty()) {
                                NameValuePair[] nvparray = nvplist.toArray(new NameValuePair[nvplist.size()]);

                                // decrypt encrypted parameters
                                for (int i = 0; i < nvparray.length; ++i) {
                                    if (hs.contains(nvparray[i].getName())) {
                                        nvparray[i] = new BasicNameValuePair(nvparray[i].getName(), Utils.decrypt(Utils.getEncryptionPassword(configuration), nvparray[i].getValue()));
                                    }
                                }

                                int pos = reqop.getUrl().indexOf(Constants.SEPARATOR_QUESTION);
                                param.setValue(URLEncodedUtils.format(Arrays.asList(nvparray), Consts.UTF_8));
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 
     * @param postRequest
     * @param reqop
     * @param input
     * @throws IOException 
     */
    public void addMultiPartParameters(HttpPost postRequest, HtmlRequestOperation reqop, String input) throws IOException {
        Set <String> hs = new HashSet<String>();
        hs.addAll(Arrays.asList(configuration.getParametersRequiringEncryption().getNameArray()));

        Map<String, String> paramMap = new HashMap<String, String>();

        StringTokenizer st1 = new StringTokenizer(input, Constants.MULTIPART_PARAMETER_SEPARATOR);
        MultipartEntityBuilder reqEntity = MultipartEntityBuilder.create();
        
        while (st1.hasMoreTokens()) {
            StringTokenizer st2 = new StringTokenizer(st1.nextToken(), Constants.MULTIPART_NAME_VALUE_SEPARATOR);

            if (st2.countTokens() == 2) {
                String name = st2.nextToken();
                String value = st2.nextToken();
                
                if (hs.contains(name)) {
                    value = Utils.decrypt(Utils.getEncryptionPassword(configuration), value);
                } else if (paramMap.containsKey(name)) {
                    value = paramMap.get(name);
                }
                
                reqEntity.addTextBody(name, value);
            }
        }
        
        postRequest.setEntity(reqEntity.build());
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

    public void updateTestExecutionParameters(String html) {
        Map <String, CheckpointProperty> map = new HashMap<String, CheckpointProperty>();
        
        for (TestOperation op : kualiTest.getOperations().getOperationArray()) {
            if (op.getOperation().getTestExecutionParameter() != null) {
                CheckpointProperty cp = op.getOperation().getTestExecutionParameter().getValueProperty();
                String key = Utils.buildCheckpointPropertyKey(cp);
                map.put(key, cp);
            }
        }
        
        DomInformation dominfo = HtmlDomProcessor.getInstance().processDom(platform, html);
        
        for (CheckpointProperty cp : dominfo.getCheckpointProperties()) {
            String key = Utils.buildCheckpointPropertyKey(cp);
            CheckpointProperty tepcp = map.get(key);
            if (tepcp != null) {
                if (StringUtils.isNotBlank(cp.getPropertyValue())) {
                    tepcp.setActualValue(cp.getPropertyValue().trim());
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

    public Stack<String> getHttpResponseStack() {
        return httpResponseStack;
    }

    public CookieStore getCookieStore() {
        return cookieStore;
    }
}


