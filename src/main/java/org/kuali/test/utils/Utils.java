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
package org.kuali.test.utils;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpRequest;
import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.swing.tree.DefaultMutableTreeNode;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.StringEnumAbstractBase;
import org.apache.xmlbeans.XmlOptions;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Node;
import org.jsoup.safety.Whitelist;
import org.kuali.test.CheckpointType;
import org.kuali.test.ChildTagMatch;
import org.kuali.test.DatabaseConnection;
import org.kuali.test.HtmlRequestOperation;
import org.kuali.test.JmxConnection;
import org.kuali.test.KualiTestConfigurationDocument;
import org.kuali.test.KualiTestDocument;
import org.kuali.test.KualiTestDocument.KualiTest;
import org.kuali.test.ParentTagMatch;
import org.kuali.test.Platform;
import org.kuali.test.RequestHeader;
import org.kuali.test.RequestParameter;
import org.kuali.test.SuiteTest;
import org.kuali.test.TagHandler;
import org.kuali.test.TagHandlersDocument;
import org.kuali.test.TagMatchAttribute;
import org.kuali.test.TagMatchType;
import org.kuali.test.TagMatcher;
import org.kuali.test.TestHeader;
import org.kuali.test.TestOperation;
import org.kuali.test.TestOperationType;
import org.kuali.test.TestSuite;
import org.kuali.test.TestType;
import org.kuali.test.WebService;
import org.kuali.test.comparators.HtmlTagHandlerComparator;
import org.kuali.test.comparators.TagHandlerFileComparator;
import org.kuali.test.handlers.HtmlTagHandler;
import org.w3c.tidy.Tidy;

public class Utils {

    private static final Logger LOG = Logger.getLogger(Utils.class);
    public static String ENUM_CHILD_CLASS = "$Enum";

    public static Map<String, List<HtmlTagHandler>> TAG_HANDLERS = new HashMap<String, List<HtmlTagHandler>>();

    public static String[] getPlatformNames(Platform[] platforms) {
        String[] retval = new String[platforms.length];
        for (int i = 0; i < platforms.length; ++i) {
            retval[i] = platforms[i].getName();
        }
        return retval;
    }

    public static Platform findPlatform(Platform[] platforms, String name) {
        Platform retval = null;
        for (int i = 0; i < platforms.length; ++i) {
            if (StringUtils.equalsIgnoreCase(platforms[i].getName(), name)) {
                retval = platforms[i];
                break;
            }
        }
        return retval;
    }

    public static Platform getPlatformForNode(KualiTestConfigurationDocument.KualiTestConfiguration configuration,
        DefaultMutableTreeNode node) {
        Platform retval = null;

        if (node != null) {
            Object userObject = node.getUserObject();

            if (userObject != null) {
                DefaultMutableTreeNode pnode = (DefaultMutableTreeNode) node.getParent();
                if (userObject instanceof Platform) {
                    retval = (Platform) userObject;
                } else if (userObject instanceof TestSuite) {
                    TestSuite testSuite = (TestSuite) userObject;
                    retval = findPlatform(configuration, testSuite.getPlatformName());
                } else if (userObject instanceof SuiteTest) {
                    SuiteTest test = (SuiteTest) userObject;
                    retval = findPlatform(configuration, test.getTestHeader().getPlatformName());
                }
            }
        }

        return retval;
    }

    public static TestHeader findTestHeaderByName(Platform platform, String testName) {
        TestHeader retval = null;

        if ((platform != null) && (platform.getPlatformTests() != null)) {
            for (TestHeader testHeader : platform.getPlatformTests().getTestHeaderArray()) {
                if (StringUtils.equalsIgnoreCase(testName, testHeader.getTestName())) {
                    retval = testHeader;
                    break;
                }
            }
        }

        return retval;
    }

    public static Platform findPlatform(KualiTestConfigurationDocument.KualiTestConfiguration configuration,
        String platformName) {
        Platform retval = null;

        Platform[] platforms = configuration.getPlatforms().getPlatformArray();

        for (Platform platform : platforms) {
            if (StringUtils.equalsIgnoreCase(platform.getName(), platformName)) {
                retval = platform;
                break;
            }
        }

        return retval;
    }

    public static SuiteTest findSuiteTestByName(TestSuite testSuite, String testName, int testIndex) {
        SuiteTest retval = null;

        for (SuiteTest suiteTest : testSuite.getSuiteTests().getSuiteTestArray()) {
            if (StringUtils.equalsIgnoreCase(testName, suiteTest.getTestHeader().getTestName())
                && (testIndex == suiteTest.getIndex())) {
                retval = suiteTest;
                break;
            }
        }

        return retval;
    }

    public static TestSuite findTestSuite(KualiTestConfigurationDocument.KualiTestConfiguration configuration,
        String platformName, String testSuiteName) {
        TestSuite retval = null;

        Platform platform = findPlatform(configuration, platformName);

        if (platform != null) {
            TestSuite[] testSuites = platform.getTestSuites().getTestSuiteArray();

            for (TestSuite testSuite : testSuites) {
                if (StringUtils.equalsIgnoreCase(testSuite.getName(), testSuiteName)) {
                    retval = testSuite;
                    break;
                }
            }
        }

        return retval;

    }

    public static boolean removeDatabaseConnection(KualiTestConfigurationDocument.KualiTestConfiguration configuration,
        DatabaseConnection dbconn) {
        boolean retval = false;

        DatabaseConnection[] dbconns = configuration.getDatabaseConnections().getDatabaseConnectionArray();
        int indx = -1;
        for (int i = 0; i < dbconns.length; ++i) {
            if (StringUtils.equalsIgnoreCase(dbconns[i].getName(), dbconn.getName())) {
                indx = i;
                break;
            }
        }

        if (indx > -1) {
            // lets clear any usages
            Platform[] platforms = configuration.getPlatforms().getPlatformArray();

            for (Platform platform : platforms) {
                if (StringUtils.equalsIgnoreCase(platform.getDatabaseConnectionName(), dbconn.getName())) {
                    platform.setDatabaseConnectionName("");
                }
            }

            configuration.getDatabaseConnections().removeDatabaseConnection(indx);
            retval = true;
        }

        return retval;
    }

    public static boolean removeWebService(KualiTestConfigurationDocument.KualiTestConfiguration configuration,
        WebService ws) {
        boolean retval = false;

        WebService[] webServices = configuration.getWebServices().getWebServiceArray();
        int indx = -1;
        for (int i = 0; i < webServices.length; ++i) {
            if (StringUtils.equalsIgnoreCase(webServices[i].getName(), ws.getName())) {
                indx = i;
                break;
            }
        }

        if (indx > -1) {
            // lets clear any usages
            Platform[] platforms = configuration.getPlatforms().getPlatformArray();

            for (Platform platform : platforms) {
                if (StringUtils.equalsIgnoreCase(platform.getWebServiceName(), ws.getName())) {
                    platform.setWebServiceName("");
                }
            }

            configuration.getWebServices().removeWebService(indx);
            retval = true;
        }

        return retval;
    }

    public static boolean removeJmxConnection(KualiTestConfigurationDocument.KualiTestConfiguration configuration, JmxConnection jmx) {
        boolean retval = false;

        if (configuration.getJmxConnections().sizeOfJmxConnectionArray() > 0) {
            JmxConnection[] jmxConnections = configuration.getJmxConnections().getJmxConnectionArray();
            int indx = -1;
            for (int i = 0; i < jmxConnections.length; ++i) {
                if (StringUtils.equalsIgnoreCase(jmxConnections[i].getName(), jmx.getName())) {
                    indx = i;
                    break;
                }
            }

            if (indx > -1) {
                // lets clear any usages
                Platform[] platforms = configuration.getPlatforms().getPlatformArray();

                for (Platform platform : platforms) {
                    if (StringUtils.equalsIgnoreCase(platform.getJmxConnectionName(), jmx.getName())) {
                        platform.setJmxConnectionName("");
                    }
                }

                configuration.getJmxConnections().removeJmxConnection(indx);
                retval = true;
            }
        }

        return retval;
    }

    public static boolean removeSuiteTest(KualiTestConfigurationDocument.KualiTestConfiguration configuration,
        SuiteTest suiteTest) {
        boolean retval = false;

        if (suiteTest != null) {
            TestSuite testSuite = findTestSuite(configuration,
                suiteTest.getTestHeader().getPlatformName(), suiteTest.getTestHeader().getTestSuiteName());

            if (testSuite != null) {
                SuiteTest[] suiteTests = testSuite.getSuiteTests().getSuiteTestArray();
                int indx = -1;
                for (int i = 0; i < suiteTests.length; ++i) {
                    if (StringUtils.equalsIgnoreCase(suiteTests[i].getTestHeader().getTestName(),
                        suiteTest.getTestHeader().getTestName()) && (suiteTest.getIndex() == suiteTests[i].getIndex())) {
                        indx = i;
                        break;
                    }
                }

                if (indx > -1) {
                    testSuite.getSuiteTests().removeSuiteTest(indx);
                    retval = true;
                }
            }
        }

        return retval;
    }

    public static boolean removeTestSuite(KualiTestConfigurationDocument.KualiTestConfiguration configuration,
        TestSuite testSuite) {
        boolean retval = false;

        if (testSuite != null) {
            Platform platform = findPlatform(configuration, testSuite.getPlatformName());
            if (platform != null) {
                TestSuite[] testSuites = platform.getTestSuites().getTestSuiteArray();
                int indx = -1;
                for (int i = 0; i < testSuites.length; ++i) {
                    if (StringUtils.endsWithIgnoreCase(testSuites[i].getName(), testSuite.getName())) {
                        indx = i;
                        break;
                    }
                }

                if (indx > -1) {
                    platform.getTestSuites().removeTestSuite(indx);
                    retval = true;
                }
            }
        }

        return retval;
    }

    public static boolean removeRepositoryNode(KualiTestConfigurationDocument.KualiTestConfiguration configuration,
        DefaultMutableTreeNode node) {
        boolean retval = false;

        if (node != null) {
            Object userObject = node.getUserObject();

            if (userObject != null) {
                if (userObject instanceof SuiteTest) {
                    retval = removeSuiteTest(configuration, (SuiteTest) userObject);
                } else if (userObject instanceof TestSuite) {
                    retval = removeTestSuite(configuration, (TestSuite) userObject);
                } else if (userObject instanceof DatabaseConnection) {
                    retval = removeDatabaseConnection(configuration, (DatabaseConnection) userObject);
                } else if (userObject instanceof WebService) {
                    retval = removeWebService(configuration, (WebService) userObject);
                } else if (userObject instanceof JmxConnection) {
                    retval = removeJmxConnection(configuration, (JmxConnection) userObject);
                }
            }
        }

        return retval;
    }

    public static int getSuiteTestArrayIndex(SuiteTest[] tests, SuiteTest test) {
        int retval = -1;

        for (int i = 0; i < tests.length; ++i) {
            if (StringUtils.equalsIgnoreCase(tests[i].getTestHeader().getTestName(), test.getTestHeader().getTestName())
                && (tests[i].getIndex() == test.getIndex())) {
                retval = i;
                break;
            }
        }

        return retval;
    }

    public static String[] getXmlEnumerations(Class clazz) {
        return getXmlEnumerations(clazz, false);
    }

    public static String[] getXmlEnumerations(Class clazz, boolean includeEmptyItem) {
        List<String> retval = new ArrayList<String>();
        Field[] fields = clazz.getDeclaredFields();

        if (includeEmptyItem) {
            retval.add("");
        }

        for (Field field : fields) {
            // looking for XMLBean enumerated types
            if (Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers())) {
                Class c = field.getType();

                // if this is a static Enum inner class - that is what we are looking for
                if (c.getName().endsWith(ENUM_CHILD_CLASS)) {
                    try {
                        Object e = field.get(null);
                        retval.add(e.toString());
                    } catch (Exception ex) {
                        LOG.warn(ex.toString());
                    }
                }
            }
        }

        return retval.toArray(new String[retval.size()]);
    }

    public static String getHostFromUrl(String url, boolean includeProtocol) {
        String retval = url;

        if (url != null) {
            int pos1 = url.indexOf("//");
            int pos2 = url.indexOf("/", pos1 + 3);

            if ((pos1 > -1) && (pos2 > -1) && (pos2 > pos1)) {

                if (includeProtocol) {
                    retval = retval.substring(0, pos2);
                } else {
                    retval = retval.substring(pos1 + 2, pos2);
                }
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("host: " + retval);
        }

        return retval;
    }

    public static String getContextFromUrl(String url) {
        String retval = null;

        if (url != null) {
            String host = getHostFromUrl(url, false);

            if (host != null) {
                int pos = (url.indexOf(host) + host.length() + 1);

                if (pos > -1) {
                    retval = url.substring(pos);
                }
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("context: " + retval);
        }

        return retval;
    }

    public static Object getObjectProperty(Object o, String propertyName) {
        Object retval = null;

        if (o != null) {
            try {
                int pos = propertyName.indexOf(".");
                Method m = null;

                if (pos > -1) {
                    m = o.getClass().getMethod(buildGetMethodNameFromPropertyName(propertyName.substring(0, pos)));
                } else {
                    m = o.getClass().getMethod(buildGetMethodNameFromPropertyName(propertyName));
                }
                retval = m.invoke(o);

                if ((retval != null) && (pos > -1)) {
                    retval = getObjectProperty(retval, propertyName.substring(pos + 1));
                }
            } catch (Exception ex) {
                LOG.warn("object: " + o.getClass().getName() + "." + propertyName + ", error: " + ex.toString());
            }
        }

        return retval;
    }

    public static void setObjectProperty(Object o, String propertyName, Object value) {
        if (o != null) {
            try {
                int pos = propertyName.indexOf(".");
                if (pos > -1) {
                    String s = propertyName.substring(pos + 1);
                    setObjectProperty(getObjectProperty(o, s), s, value);
                } else {
                    Class argumentType = getPropertyClass(o, propertyName);
                    Method m = o.getClass().getMethod(buildSetMethodNameFromPropertyName(propertyName), argumentType);

                    // this is a hack to handle the XMLBeans Enums 
                    if (value != null) {
                        if (!argumentType.isAssignableFrom(value.getClass())) {
                            if (StringEnumAbstractBase.class.isAssignableFrom(argumentType)) {
                                Method m2 = argumentType.getMethod("forString", String.class);
                                value = m2.invoke(null, value);
                            }
                        }
                    }

                    m.invoke(o, value);
                }

            } catch (Exception ex) {
                LOG.warn("object: " + o.getClass().getName() + "." + propertyName + ", error: " + ex.toString());
            }
        }
    }

    public static Class getPropertyClass(Object o, String propertyName) {
        Class retval = null;
        try {
            Method m = o.getClass().getMethod(buildGetMethodNameFromPropertyName(propertyName));

            retval = m.getReturnType();
        } catch (Exception ex) {
            LOG.warn(ex.toString());
        }

        return retval;
    }

    public static String buildSetMethodNameFromPropertyName(String propertyName) {
        return buildMethodNameFromPropertyName("set", propertyName);
    }

    public static String buildGetMethodNameFromPropertyName(String propertyName) {
        return buildMethodNameFromPropertyName("get", propertyName);
    }

    public static String buildMethodNameFromPropertyName(String prefix, String propertyName) {
        String retval = null;
        if (StringUtils.isNoneBlank(prefix) && StringUtils.isNotBlank(propertyName)) {
            StringBuilder buf = new StringBuilder(32);
            buf.append(prefix);
            buf.append(Character.toUpperCase(propertyName.charAt(0)));
            buf.append(propertyName.substring(1));
            retval = buf.toString();
        }

        return retval;
    }

    public static String getFileSuffix(String nm) {
        String retval = null;

        if (StringUtils.isNotBlank(nm)) {
            int pos = nm.lastIndexOf(".");
            if (pos > -1) {
                retval = nm.substring(pos + 1).toLowerCase().trim();
            }
        }

        return retval;
    }

    public static boolean wantHttpRequestHeader(String key) {
        return false;
    }

    public static void populateHttpRequestOperation(TestOperation testop, HttpRequest request) {
        if (LOG.isDebugEnabled()) {
            LOG.debug(getHttpRequestDetails(request));
        }

        HtmlRequestOperation op = testop.addNewOperation().addNewHtmlRequestOperation();
        op.addNewRequestHeaders();
        op.addNewRequestParameters();
        boolean ispost = false;

        Iterator<Entry<String, String>> it = request.headers().iterator();

        while (it.hasNext()) {
            Entry<String, String> entry = it.next();
            if (LOG.isDebugEnabled()) {
                LOG.debug(entry.getKey() + "=" + entry.getValue());
            }

            if (wantHttpRequestHeader(entry.getKey())) {
                RequestHeader header = op.getRequestHeaders().addNewHeader();
                header.setName(entry.getKey());
                header.setValue(entry.getValue());
            }
        }

        String method = request.getMethod().name();

        op.setMethod(method);
        op.setUri(request.getUri());

        // if this is a post then try to get content
        if (Constants.HTTP_REQUEST_METHOD_POST.equalsIgnoreCase(request.getMethod().name())) {
            if (request instanceof FullHttpRequest) {
                FullHttpRequest fr = (FullHttpRequest) request;
                if (fr.content() != null) {
                    byte[] data = getHttpPostContent(fr.content());

                    if (data != null) {
                        RequestParameter param = op.getRequestParameters().addNewParameter();
                        param.setName("content");
                        param.setValue(new String(data));
                    }
                }
            }
        }
    }

    public static TestOperation buildTestOperation(TestOperationType.Enum optype, Object inputData) {
        TestOperation retval = TestOperation.Factory.newInstance();

        retval.setOperationType(optype);

        if (inputData != null) {
            switch (optype.intValue()) {
                case TestOperationType.INT_HTTP_REQUEST:
                    populateHttpRequestOperation(retval, (HttpRequest) inputData);
                    break;
            }
        }

        return retval;
    }

    public static String formatForFileName(String input) {
        String retval = null;
        if (StringUtils.isNotBlank(input)) {
            retval = input.toLowerCase().replace(' ', '-');
        }
        return retval;
    }

    public static String getTestFileName(TestHeader header) {
        return formatForFileName(header.getTestName());
    }

    public static File buildTestFile(String repositoryLocation, TestHeader header) {
        StringBuilder nm = new StringBuilder(256);
        nm.append(repositoryLocation);
        nm.append("/");
        nm.append(header.getPlatformName());
        nm.append("/tests/");
        nm.append(getTestFileName(header));
        nm.append(".xml");

        return new File(nm.toString());
    }

    public static XmlOptions getSaveXmlOptions() {
        XmlOptions retval = new XmlOptions();
        retval.setSavePrettyPrint();
        retval.setSavePrettyPrintIndent(3);
        return retval;
    }

    public static String getHttpRequestDetails(HttpRequest request) {
        StringBuilder retval = new StringBuilder(512);
        retval.append("uri: ");
        retval.append(request.getUri());
        retval.append("\r\n");
        retval.append("method: ");
        retval.append(request.getMethod().toString());
        retval.append("\r\n");
        retval.append("protocol version: ");
        retval.append(request.getProtocolVersion());
        retval.append("\r\n");
        retval.append("------------ headers -----------\r\n");
        Iterator<Entry<String, String>> it = request.headers().iterator();
        while (it.hasNext()) {
            Entry entry = it.next();
            retval.append(entry.getKey());
            retval.append("=");
            retval.append(entry.getValue());
            retval.append("\r\n");
        }

        retval.append("--------------------------------\r\n");

        if (request instanceof DefaultFullHttpRequest) {
            DefaultFullHttpRequest fr = (DefaultFullHttpRequest) request;
            if (fr.content() != null) {
                byte[] data = getHttpPostContent(fr.content());

                if (data != null) {
                    retval.append("------------ content -----------\r\n");
                    retval.append(new String(getHttpPostContent(fr.content())));
                    retval.append("--------------------------------\r\n");
                }
            }
        }

        return retval.toString();
    }

    public static byte[] getHttpPostContent(ByteBuf content) {
        byte[] retval = null;
        if (content.isReadable()) {
            ByteBuffer nioBuffer = content.nioBuffer();
            retval = new byte[nioBuffer.remaining()];
            nioBuffer.get(retval);
        }

        return retval;
    }

    public static KualiTest findKualiTest(KualiTestConfigurationDocument.KualiTestConfiguration configuration,
        String platformName, String testName) {
        KualiTest retval = null;

        Platform platform = findPlatform(configuration, platformName);

        if (platform != null) {
            for (TestHeader th : platform.getPlatformTests().getTestHeaderArray()) {
                if (StringUtils.equalsIgnoreCase(testName, th.getTestName())) {
                    File f = buildTestFile(configuration.getRepositoryLocation(), th);

                    if ((f.exists() && f.isFile())) {
                        KualiTestDocument doc;
                        try {
                            doc = KualiTestDocument.Factory.parse(f);
                            retval = doc.getKualiTest();
                            retval.setTestHeader(th);
                        } catch (Exception ex) {
                            LOG.error(ex.toString(), ex);
                        }

                        break;
                    }
                }
            }
        }

        return retval;
    }

    public static String cleanDisplayText(String input) {
        String retval = "";

        if (LOG.isDebugEnabled()) {
            LOG.debug("cleanDisplayText: input=" + input);
        }

        if (StringUtils.isNotBlank(input)) {
            retval = Jsoup.clean(input, Whitelist.none()).replace("&nbsp;", " ").trim();
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("cleanDisplayText: retval=" + retval);
        }

        return retval;
    }

    public static File getTestRunnerConfigurationFile(KualiTestConfigurationDocument.KualiTestConfiguration configuration) {
        return new File(configuration.getRepositoryLocation() + "/" + Constants.TEST_RUNNER_CONFIG_FILENAME);
    }

    public static void initializeHtmlTagHandlers(KualiTestConfigurationDocument.KualiTestConfiguration configuration) {
        File handlerDir = new File(configuration.getTagHandlersLocation());

        if (handlerDir.exists() && handlerDir.isDirectory()) {
            File[] files = handlerDir.listFiles(new XMLFileFilter());

            Arrays.sort(files, new TagHandlerFileComparator());

            for (File f : files) {
                try {
                    TagHandlersDocument doc = TagHandlersDocument.Factory.parse(f);

                    if (doc != null) {
                        for (TagHandler th : doc.getTagHandlers().getHandlerArray()) {
                            String key = null;
                            if (StringUtils.isBlank(th.getApplication())) {
                                key = ("all." + th.getTagName());
                            } else {
                                key = (th.getApplication() + "." + th.getTagName());
                            }

                            List<HtmlTagHandler> thl = TAG_HANDLERS.get(key);

                            if (thl == null) {
                                TAG_HANDLERS.put(key, thl = new ArrayList<HtmlTagHandler>());
                            }

                            try {
                                HtmlTagHandler hth = (HtmlTagHandler) Class.forName(th.getHandlerClassName()).newInstance();
                                hth.setTagHandler(th);
                                thl.add(hth);
                            } catch (Exception ex) {
                                LOG.warn(ex.toString(), ex);
                            }
                        }
                    }
                } catch (Exception ex) {
                    LOG.error("Error loading tag handler file", ex);
                }
            }
        }

        // sort the handler so we hit most contrained matching test first the 
        // fall through to more generic tests
        for (List<HtmlTagHandler> l : TAG_HANDLERS.values()) {
            Collections.sort(l, new HtmlTagHandlerComparator());
        }
    }

    public static boolean isTagMatch(Node node, TagMatcher tm) {
        boolean retval = true;

        if ((tm.getMatchAttributes() != null) && (tm.getMatchAttributes().sizeOfMatchAttributeArray() > 0)) {
            for (TagMatchAttribute att : tm.getMatchAttributes().getMatchAttributeArray()) {
                if (!node.hasAttr(att.getName())) {
                    retval = false;
                    break;
                } else {
                    if (att.getValue().contains("|")) {
                        // can use "|" to seperate "or" values for matching
                        StringTokenizer st = new StringTokenizer(att.getValue(), "|");

                        boolean foundit = false;
                        while (st.hasMoreTokens()) {
                            String token = st.nextToken();

                            if (token.equalsIgnoreCase(node.attr(att.getName()))) {
                                foundit = true;
                                break;
                            }
                        }

                        if (!foundit) {
                            retval = false;
                            break;
                        }
                    } else if (att.getValue().contains("*")) {
                        String nodeData = node.attr(att.getName());
                        String attData = att.getValue();

                        int pos = attData.indexOf("*");
                        if (StringUtils.isBlank(nodeData)) {
                            retval = false;
                        } else {
                            if (pos == 0) {
                                if (!nodeData.endsWith(attData.substring(1))) {
                                    retval = false;
                                }
                            } else if (pos == (attData.length() - 1)) {
                                if (!nodeData.startsWith(attData.substring(0, pos))) {
                                    retval = false;
                                }
                            } else {
                                String s1 = attData.substring(0, pos);
                                String s2 = attData.substring(pos + 1);

                                if (!nodeData.startsWith(s1) || !nodeData.endsWith(s2)) {
                                    retval = false;
                                }
                            }
                        }

                        if (!retval) {
                            break;
                        }
                    } else {
                        String val = att.getValue();
                        if (!val.equalsIgnoreCase(node.attr(att.getName()))) {
                            retval = false;
                            break;
                        }
                    }
                }
            }
        }

        // if we have a match and a child tag matcher - check the children
        if (retval) {
            if (isChildTagMatchFailure(node, tm.getChildTagMatch())) {
                retval = false;
            }
        }

        if (retval) {
            if (isParentTagMatchFailure(node, tm.getParentTagMatch())) {
                retval = false;
            }
        }

        return retval;
    }

    public static boolean isChildTagMatchFailure(Node node, ChildTagMatch childTagMatch) {
        boolean retval = false;

        if ((childTagMatch != null) && (node != null)) {
            if (node.childNodeSize() > 0) {
                boolean foundone = false;
                Set<String> childTagNames = new HashSet<String>();

                StringTokenizer st = new StringTokenizer(childTagMatch.getChildTagName(), "|");

                while (st.hasMoreTokens()) {
                    childTagNames.add(st.nextToken());
                }

                for (Node child : node.childNodes()) {
                    if (childTagNames.contains(child.nodeName())) {
                        foundone = true;
                        if (childTagMatch.getMatchAttributes() != null) {
                            if (childTagMatch.getMatchAttributes().sizeOfMatchAttributeArray() > 0) {
                                for (TagMatchAttribute att : childTagMatch.getMatchAttributes().getMatchAttributeArray()) {
                                    if ((att != null) && StringUtils.isNotBlank(att.getName())) {
                                        String childAttr = child.attr(att.getName());
                                        if (StringUtils.isBlank(childAttr)) {
                                            retval = true;
                                        } else {
                                            int pos = att.getValue().indexOf('*');

                                            if (pos > -1) {
                                                if (pos == 0) {
                                                    retval = !childAttr.endsWith(att.getValue().substring(1));
                                                } else {
                                                    String s1 = att.getValue().substring(0, pos);
                                                    String s2 = att.getValue().substring(pos + 1);

                                                    retval = (!childAttr.startsWith(s1) || !childAttr.endsWith(s2));
                                                }
                                            } else {
                                                retval = !childAttr.equalsIgnoreCase(att.getValue());
                                            }
                                        }
                                        break;
                                    } else {
                                        retval = true;
                                        break;
                                    }
                                }
                                // if retval is false then we found a match so break
                                if (!retval) {
                                    break;
                                }
                            }
                        } else {
                            break;
                        }
                    }
                }

                if (!foundone) {
                    retval = true;
                }
            } else {
                retval = true;
            }
        }

        return retval;
    }

    public static boolean isParentTagMatchFailure(Node node, ParentTagMatch parentTagMatch) {
        boolean retval = false;

        if ((parentTagMatch != null) && (node != null)) {
            Set<String> parentTagNames = new HashSet<String>();

            StringTokenizer st = new StringTokenizer(parentTagMatch.getParentTagName(), "|");

            while (st.hasMoreTokens()) {
                parentTagNames.add(st.nextToken());
            }
            Node validParent = null;
            Node parent = node.parentNode();

            // if we are looking for table and this is a tbody ten move up 1 level
            if (Constants.HTML_TAG_TYPE_TABLE.equalsIgnoreCase(parentTagMatch.getParentTagName())
                && Constants.HTML_TAG_TYPE_TBODY.equalsIgnoreCase(parent.nodeName())) {
                parent = parent.parentNode();
            }

            if (parent != null) {
                if (parentTagNames.contains(parent.nodeName())) {
                    if (parentTagMatch.getMatchAttributes() != null) {
                        if (parentTagMatch.getMatchAttributes().sizeOfMatchAttributeArray() > 0) {
                            boolean ok = true;
                            for (TagMatchAttribute att : parentTagMatch.getMatchAttributes().getMatchAttributeArray()) {
                                String parentAttr = parent.attr(att.getName());
                                if (StringUtils.isNotBlank(parentAttr)) {
                                    int pos = att.getValue().indexOf('*');

                                    if (pos > -1) {
                                        if (pos == 0) {
                                            ok = parentAttr.endsWith(att.getValue().substring(1));
                                        } else {
                                            String s1 = att.getValue().substring(0, pos);
                                            String s2 = att.getValue().substring(pos + 1);

                                            ok = (parentAttr.startsWith(s1) && parentAttr.endsWith(s2));
                                        }
                                    } else {
                                        ok = parentAttr.equalsIgnoreCase(att.getValue());
                                    }
                                } else {
                                    ok = false;
                                }

                                if (!ok) {
                                    break;
                                }
                            }

                            if (ok) {
                                validParent = parent;
                            }
                        }
                    } else {
                        validParent = parent;
                    }
                }
            }

            retval = (validParent == null);
        }

        return retval;
    }

    public static Node getMatchingChild(TagMatcher tm, Node node) {
        Node retval = null;

        if (node.childNodeSize() > 0) {
            retval = getMatchingSibling(tm, node.childNode(0));
        }

        return retval;
    }

    public static Node getMatchingParent(TagMatcher tm, Node node) {
        Node retval = null;

        Node parent = node.parentNode();
        int cnt = 0;
        int totalCnt = Integer.MAX_VALUE;

        boolean limited = false;
        String sdef = tm.getSearchDefinition();
        if (StringUtils.isNotBlank(sdef)) {
            if (StringUtils.isNumeric(sdef)) {
                totalCnt = Integer.parseInt(sdef);
                limited = true;
            }
        }

        while ((cnt < totalCnt) && (parent != null)) {
            if (parent.nodeName().equalsIgnoreCase(tm.getTagName())) {
                cnt++;
                TagMatchAttribute[] attrs = null;
                if (tm.getMatchAttributes() != null) {
                    attrs = tm.getMatchAttributes().getMatchAttributeArray();
                }

                if ((!limited || (cnt == totalCnt)) && isTagMatch(parent, tm)) {
                    retval = parent;
                    break;
                }
            }

            parent = parent.parentNode();
        }

        return retval;
    }

    public static int getSiblingNodeSearchDirection(String searchDefinition) {
        int retval = Constants.SIBLING_NODE_SEARCH_DIRECTION_INVALID;

        switch (searchDefinition.charAt(0)) {
            case '-':
                retval = Constants.SIBLING_NODE_SEARCH_DIRECTION_PREVIOUS;
                break;
            case '+':
                retval = Constants.SIBLING_NODE_SEARCH_DIRECTION_NEXT;
                break;
            case '*':
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                retval = Constants.SIBLING_NODE_SEARCH_DIRECTION_ABSOLUTE;
                break;

        }

        return retval;
    }

    public static Node getMatchingSibling(TagMatcher tm, Node node) {
        Node retval = null;

        String searchDefinition = tm.getSearchDefinition();

        // default to search all children forward
        if (StringUtils.isBlank(searchDefinition)) {
            searchDefinition = "+*";
        }

        int startIndex = node.siblingIndex();
        int cnt = 0;

        switch (getSiblingNodeSearchDirection(searchDefinition)) {
            case Constants.SIBLING_NODE_SEARCH_DIRECTION_PREVIOUS:
                if (startIndex > -1) {
                    int targetCnt = Integer.MAX_VALUE;
                    boolean limited = true;
                    // if we have an * then loop through all siblings
                    if (!searchDefinition.substring(1).equals("*")) {
                        targetCnt = Integer.parseInt(searchDefinition.substring(1));
                    } else {
                        limited = false;
                    }

                    Node prev = node.previousSibling();

                    while ((prev != null) && (cnt < targetCnt)) {
                        if (prev.nodeName().equalsIgnoreCase(tm.getTagName())) {
                            cnt++;
                            if ((!limited || (cnt == targetCnt)) && isTagMatch(prev, tm)) {
                                retval = prev;
                                break;
                            }
                        }

                        prev = prev.previousSibling();
                    }
                }
                break;
            case Constants.SIBLING_NODE_SEARCH_DIRECTION_NEXT:
                if (startIndex > -1) {
                    int targetCnt = Integer.MAX_VALUE;

                    boolean limited = true;
                    // if we have an * then loop through all siblins
                    if (!searchDefinition.substring(1).equals("*")) {
                        if (StringUtils.isBlank(searchDefinition.substring(1))) {
                            targetCnt = 1;
                        } else {
                            targetCnt = Integer.parseInt(searchDefinition.substring(1));
                        }
                    } else {
                        limited = false;
                    }

                    Node next = node.nextSibling();

                    while ((next != null) && (cnt < targetCnt)) {
                        if (next.nodeName().equalsIgnoreCase(tm.getTagName())) {
                            cnt++;
                            if ((!limited || (cnt == targetCnt)) && isTagMatch(next, tm)) {
                                retval = next;
                                break;
                            }
                        }

                        next = next.nextSibling();
                    }
                }
                break;
            case Constants.SIBLING_NODE_SEARCH_DIRECTION_ABSOLUTE:
                if (startIndex > -1) {
                    int targetCnt = Integer.MAX_VALUE;

                    boolean limited = true;
                    // if we have an * then loop through all chiildren
                    if (!searchDefinition.equals("*")) {
                        targetCnt = Integer.parseInt(searchDefinition);
                    } else {
                        limited = false;
                    }

                    Node child = node.parentNode().childNode(0);

                    while ((child != null) && (cnt < targetCnt)) {
                        if (child.nodeName().equalsIgnoreCase(tm.getTagName())) {
                            cnt++;
                            if ((!limited || (cnt == targetCnt))
                                && isTagMatch(child, tm)) {
                                retval = child;
                                break;
                            }
                        }

                        child = child.nextSibling();
                    }
                }
                break;
        }

        return retval;
    }

    public static int getSiblingIndexByTagType(Node node) {
        int retval = 0;

        Node parent = node.parentNode();

        for (Node sibling : parent.childNodes()) {
            if (sibling.siblingIndex() == node.siblingIndex()) {
                break;
            }

            if (sibling.nodeName().equals(node.nodeName())) {
                retval++;
            }
        }

        return retval;
    }

    public static String getMatchedNodeText(TagMatcher[] tagMatchers, Node node) {
        String retval = null;

        if ((tagMatchers != null) && (tagMatchers.length > 0)) {
            Node curnode = node;

            for (int i = 0; i < tagMatchers.length; ++i) {
                TagMatcher tm = tagMatchers[i];
                String sdef = tm.getSearchDefinition();

                // "I" is a key to match to same sibling index as parent
                // used for table column header matching. Will need to clone
                // original matcher in this case
                if (StringUtils.isNotBlank(sdef) && sdef.startsWith(Constants.NODE_INDEX_MATCHER_CODE)) {
                    tm = (TagMatcher) tm.copy();
                    if (sdef.length() > 1) {
                        tm.setSearchDefinition("" + (getSiblingIndexByTagType(node) + Integer.parseInt(sdef.substring(1))));
                    } else {
                        tm.setSearchDefinition("" + (getSiblingIndexByTagType(node) + 1));
                    }
                }

                curnode = Utils.getMatchingTagNode(tm, curnode);
            }

            if (curnode != null) {
                retval = Utils.cleanDisplayText(curnode.toString());
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("getMatchedNodeText: tag=" + node.nodeName() + ", text=" + retval);
            }
        }

        return retval;
    }

    public static Node getMatchingTagNode(TagMatcher tm, Node node) {
        Node retval = null;

        if ((tm != null) && (node != null)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("match type: " + tm.getMatchType() + ", tag: " + node.nodeName());
            }

            switch (tm.getMatchType().intValue()) {
                case TagMatchType.INT_CHILD:
                    retval = getMatchingChild(tm, node);
                    break;
                case TagMatchType.INT_PARENT:
                    retval = getMatchingParent(tm, node);
                    break;
                case TagMatchType.INT_SIBLING:
                    retval = getMatchingSibling(tm, node);
                    break;
                case TagMatchType.INT_CURRENT:
                    if (isTagMatch(node, tm)) {
                        retval = node;
                    }
                    break;
            }
        }

        return retval;
    }

    public static HtmlTagHandler getHtmlTagHandler(String app, Node node) {
        HtmlTagHandler retval = null;

        if (LOG.isDebugEnabled()) {
            LOG.debug("getHtmlTagHandler - " + node.nodeName());
        }

        List<HtmlTagHandler> handlerList = new ArrayList<HtmlTagHandler>();

        // add the application specific handlers first
        List<HtmlTagHandler> thl = TAG_HANDLERS.get(app + "." + node.nodeName().trim().toLowerCase());

        if (thl != null) {
            handlerList.addAll(thl);
        }

        // add the general handlers
        thl = TAG_HANDLERS.get("all." + node.nodeName().trim().toLowerCase());

        if (thl != null) {
            handlerList.addAll(thl);
        }

        if (!handlerList.isEmpty()) {
            for (HtmlTagHandler hth : handlerList) {
                boolean match = true;
                TagHandler th = hth.getTagHandler();
                if (th.getTagMatchers() != null) {
                    for (TagMatcher tm : th.getTagMatchers().getTagMatcherArray()) {
                        if (getMatchingTagNode(tm, node) == null) {
                            match = false;
                            break;
                        }
                    }
                }

                if (match) {
                    retval = hth;
                    break;
                }
            }
        }

        return retval;
    }

    public static boolean isHtmlContainer(Node node) {
        return Constants.DEFAULT_HTML_CONTAINER_TAGS.contains(node.nodeName().toLowerCase().trim());
    }

    public static String[] getValidTestTypesForPlatform(Platform platform) {
        List<String> retval = new ArrayList<String>();

        if (platform != null) {
            String[] testTypes = Utils.getXmlEnumerations(TestType.class);

            for (String testType : testTypes) {
                if (TestType.DATABASE.toString().equals(testType)) {
                    if (StringUtils.isNotBlank(platform.getDatabaseConnectionName())) {
                        retval.add(testType);
                    }
                } else if (TestType.WEB_SERVICE.toString().equals(testType)) {
                    if (StringUtils.isNotBlank(platform.getWebServiceName())) {
                        retval.add(testType);
                    }
                } else {
                    retval.add(testType);
                }
            }
        }

        return retval.toArray(new String[retval.size()]);
    }

    public static String[] getValidCheckpointTypesForPlatform(TestType.Enum testType, Platform platform) {
        List<String> retval = new ArrayList<String>();
        String[] checkpointTypes = Utils.getXmlEnumerations(CheckpointType.class);

        for (String checkpointType : checkpointTypes) {
            if (checkpointType.equals(CheckpointType.HTTP.toString())) {
                if (TestType.WEB.equals(testType)) {
                    retval.add(checkpointType);
                }
            } else {
                if (CheckpointType.SQL.toString().equals(checkpointType)) {
                    if (StringUtils.isNotBlank(platform.getDatabaseConnectionName())) {
                        retval.add(checkpointType);
                    }
                } else if (CheckpointType.WEB_SERVICE.toString().equals(checkpointType)) {
                    if (StringUtils.isNotBlank(platform.getWebServiceName())) {
                        retval.add(checkpointType);
                    }
                } else if (CheckpointType.MEMORY.toString().equals(checkpointType)) {
                    if (StringUtils.isNotBlank(platform.getJmxConnectionName())) {
                        retval.add(checkpointType);
                    }
                } else {
                    retval.add(checkpointType);
                }
            }
        }

        return retval.toArray(new String[retval.size()]);
    }

    public static String buildCheckpointSectionName(HtmlTagHandler th, Node node) {
        StringBuilder retval = new StringBuilder(128);

        String subSectionName = th.getSubSectionName(node);
        String sectionName = th.getSectionName(node);
        String subSectionAdditional = th.getSubSectionAdditional(node);

        retval.append("<html><span style='white-space: nowrap;'>");

        boolean haveSection = StringUtils.isNotBlank(sectionName);
        if (haveSection) {
            String s = sectionName;
            if (StringUtils.isNotBlank(subSectionName)) {
                s = (sectionName + ": ");
            }

            retval.append(buildHtmlStyle(Constants.HTML_BOLD_BLUE_STYLE, s));
        }

        if (StringUtils.isNotBlank(subSectionName)) {
            retval.append(subSectionName);
        }

        if (StringUtils.isNotBlank(subSectionAdditional)) {
            retval.append(" - ");
            retval.append(subSectionAdditional);
        }

        retval.append("</span></html>");
        return retval.toString();
    }

    public static boolean isValidContainerNode(Node node) {
        boolean retval = true;

        if (node.childNodeSize() > 0) {
            if (node.childNodeSize() == 1) {
                retval = !Constants.HTML_TEXT_NODE_NAME.equals(node.childNode(0).nodeName());
            }
        } else {
            retval = false;
        }

        return retval;
    }

    public static DatabaseConnection findDatabaseConnectionByName(KualiTestConfigurationDocument.KualiTestConfiguration configuration, String dbname) {
        DatabaseConnection retval = null;

        if ((configuration != null) && StringUtils.isNotBlank(dbname)) {
            if ((configuration.getDatabaseConnections() != null)
                && (configuration.getDatabaseConnections().sizeOfDatabaseConnectionArray() > 0)) {
                for (DatabaseConnection db : configuration.getDatabaseConnections().getDatabaseConnectionArray()) {
                    if (dbname.equalsIgnoreCase(db.getName())) {
                        retval = db;
                        break;
                    }
                }
            }
        }

        return retval;
    }

    public static WebService findWebServiceByName(KualiTestConfigurationDocument.KualiTestConfiguration configuration, String wsname) {
        WebService retval = null;

        if ((configuration != null) && StringUtils.isNotBlank(wsname)) {
            if ((configuration.getWebServices() != null)
                && (configuration.getWebServices().sizeOfWebServiceArray() > 0)) {
                for (WebService ws : configuration.getWebServices().getWebServiceArray()) {
                    if (wsname.equalsIgnoreCase(ws.getName())) {
                        retval = ws;
                        break;
                    }
                }
            }
        }

        return retval;
    }

    @SuppressWarnings("empty-statement")
    public static void closeDatabaseResources(Connection conn, Statement stmt, ResultSet res) {
        try {
            if (res != null) {
                res.close();
            }
        } catch (SQLException ex) {
        };
        try {
            if (stmt != null) {
                stmt.close();
            }
        } catch (SQLException ex) {
        };
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException ex) {
        };
    }

    public static Connection getDatabaseConnection(KualiTestConfigurationDocument.KualiTestConfiguration configuration,
        DatabaseConnection dbconn) throws ClassNotFoundException, SQLException {
        Class.forName(dbconn.getJdbcDriver());
        Connection retval = DriverManager.getConnection(dbconn.getJdbcUrl(), dbconn.getUsername(), dbconn.getPassword());
        retval.setReadOnly(true);
        return retval;
    }

    public static String getJdbcTypeName(int jdbcType, int decimalDigits) {
        String retval = Constants.DATA_TYPE_OTHER;

        if (isStringJdbcType(jdbcType)) {
            retval = Constants.DATA_TYPE_STRING;
        } else if (isIntegerJdbcType(jdbcType, decimalDigits)) {
            retval = Constants.DATA_TYPE_INT;
        } else if (isDateJdbcType(jdbcType)) {
            retval = Constants.DATA_TYPE_DATE;
        } else if (isTimestampJdbcType(jdbcType)) {
            retval = Constants.DATA_TYPE_TIMESTAMP;
        } else if (isFloatJdbcType(jdbcType, decimalDigits)) {
            retval = Constants.DATA_TYPE_FLOAT;
        }
        return retval;
    }

    public static String cleanTableDisplayName(String input) {
        String retval = input;

        if (StringUtils.isNotBlank(input)) {
            if (input.endsWith("Impl")) {
                retval = input.substring(0, input.length() - 4);
            }
        }

        return retval;
    }

    public static boolean isIntegerJdbcType(int jdbcType, int decimalDigits) {
        boolean retval = false;

        if (isNumericJdbcType(jdbcType)) {
            retval = (decimalDigits == 0);
        }

        return retval;
    }

    public static boolean isFloatJdbcType(int jdbcType, int decimalDigits) {
        boolean retval = false;

        if (isNumericJdbcType(jdbcType)) {
            retval = (decimalDigits > 0);
        }

        return retval;
    }

    public static boolean isStringJdbcType(int jdbcType) {
        boolean retval = false;

        switch (jdbcType) {
            case java.sql.Types.LONGNVARCHAR:
            case java.sql.Types.LONGVARCHAR:
            case java.sql.Types.NCHAR:
            case java.sql.Types.NCLOB:
            case java.sql.Types.NVARCHAR:
            case java.sql.Types.VARCHAR:
            case java.sql.Types.SQLXML:
                retval = true;
                break;
        }

        return retval;
    }

    public static boolean isTimestampJdbcType(int jdbcType) {
        return (jdbcType == java.sql.Types.TIMESTAMP);
    }

    public static boolean isDateJdbcType(int jdbcType) {
        return (jdbcType == java.sql.Types.DATE);
    }

    public static boolean isTimeJdbcType(int jdbcType) {
        return (jdbcType == java.sql.Types.TIME);
    }

    public static boolean isNumericJdbcType(int jdbcType) {
        boolean retval = false;

        switch (jdbcType) {
            case java.sql.Types.BIGINT:
            case java.sql.Types.INTEGER:
            case java.sql.Types.SMALLINT:
            case java.sql.Types.TINYINT:
            case java.sql.Types.DECIMAL:
            case java.sql.Types.NUMERIC:
                retval = true;
                break;
        }

        return retval;
    }

    public static boolean isDateTimeJdbcType(int jdbcType) {
        return (isDateJdbcType(jdbcType) || isTimestampJdbcType(jdbcType) || isTimeJdbcType(jdbcType));
    }

    public static List<String> getAggregateFunctionsForType(int jdbcType) {
        List<String> retval = new ArrayList<String>();

        for (String s : Constants.AGGREGATE_FUNCTIONS) {
            if (StringUtils.isBlank(s)
                || isNumericJdbcType(jdbcType)
                || Constants.COUNT.equals(s)) {
                retval.add(s);
            } else if (isDateTimeJdbcType(jdbcType)) {
                if (!Constants.SUM.equals(s) && !Constants.AVG.equals(s)) {
                    retval.add(s);
                }
            }
        }

        return retval;
    }

    public static String buildHtmlStyle(String style, String data) {
        return style.replace("^", data);
    }

    public static String getLabelDataDisplay(String input) {
        StringBuilder retval = new StringBuilder(128);
        if (StringUtils.isNotBlank(input)) {
            retval.append("<html><span style='font-weight: normal;'>");
            retval.append(input);
            retval.append("</span></html>");
        } else {
            retval.append("");
        }

        return retval.toString();
    }

    public static Calendar truncate(Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return cal;
    }

    public static String buildCsvLine(List<String> columns) {
        StringBuilder retval = new StringBuilder(256);
        String comma = "";

        for (String s : columns) {
            retval.append(comma);
            retval.append("\"");

            if (StringUtils.isNotBlank(s)) {
                retval.append(s);
            }

            retval.append("\"");

            comma = ",";
        }

        return retval.toString();
    }

    public static JmxConnection findJmxConnection(KualiTestConfigurationDocument.KualiTestConfiguration configuration, String jmxName) {
        JmxConnection retval = null;

        if (configuration.getJmxConnections() != null) {
            for (JmxConnection jmx : configuration.getJmxConnections().getJmxConnectionArray()) {
                if (jmx.getName().equals(jmxName)) {
                    retval = jmx;
                    break;
                }
            }
        }

        return retval;
    }

    public static String[] getEmailToAddresses(KualiTestConfigurationDocument.KualiTestConfiguration configuration,
        TestSuite testSuite, TestHeader testHeader) {
        Set<String> retval = new HashSet<String>();
        Platform platform = null;

        if (testSuite != null) {
            if (StringUtils.isNotBlank(testSuite.getEmailAddresses())) {
                StringTokenizer st = new StringTokenizer(testSuite.getEmailAddresses(), ",");
                while (st.hasMoreTokens()) {
                    retval.add(st.nextToken().trim());
                }

                platform = findPlatform(configuration, testSuite.getPlatformName());
            }

        } else if (testHeader != null) {
            platform = findPlatform(configuration, testHeader.getPlatformName());
        }

        if ((platform != null) && StringUtils.isNotBlank(platform.getEmailAddresses())) {
            StringTokenizer st = new StringTokenizer(platform.getEmailAddresses(), ",");
            while (st.hasMoreTokens()) {
                retval.add(st.nextToken().trim());
            }
        }

        if (StringUtils.isNotBlank(configuration.getEmailSetup().getToAddresses())) {
            StringTokenizer st = new StringTokenizer(configuration.getEmailSetup().getToAddresses(), ",");
            while (st.hasMoreTokens()) {
                retval.add(st.nextToken().trim());
            }
        }

        return retval.toArray(new String[retval.size()]);
    }

    public static void sendMail(KualiTestConfigurationDocument.KualiTestConfiguration configuration,
        TestSuite testSuite, TestHeader testHeader, List<File> testResults) {

        if (StringUtils.isNotBlank(configuration.getEmailSetup().getFromAddress())
            && StringUtils.isNotBlank(configuration.getEmailSetup().getMailHost())) {

            String[] toAddresses = getEmailToAddresses(configuration, testSuite, testHeader);

            if (toAddresses.length > 0) {
                Properties props = new Properties();
                props.put("mail.smtp.host", configuration.getEmailSetup().getMailHost());
                Session session = Session.getDefaultInstance(props, null);

                try {
                    Message msg = new MimeMessage(session);
                    msg.setFrom(new InternetAddress(configuration.getEmailSetup().getFromAddress()));

                    for (String recipient : toAddresses) {
                        msg.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
                    }

                    StringBuilder subject = new StringBuilder(configuration.getEmailSetup().getSubject());
                    subject.append(" - Platform: ");
                    if (testSuite != null) {
                        subject.append(testSuite.getPlatformName());
                        subject.append(", TestSuite: ");
                        subject.append(testSuite.getName());
                    } else {
                        subject.append(testHeader.getPlatformName());
                        subject.append(", Test: ");
                        subject.append(testHeader.getTestName());
                    }

                    msg.setSubject(subject.toString());

                    StringBuilder msgtxt = new StringBuilder(256);
                    msgtxt.append("Please see test output in the following attached files:\n");

                    for (File f : testResults) {
                        msgtxt.append(f.getName());
                        msgtxt.append("\n");
                    }

                    // create and fill the first message part
                    MimeBodyPart mbp1 = new MimeBodyPart();
                    mbp1.setText(msgtxt.toString());

                    // create the Multipart and add its parts to it
                    Multipart mp = new MimeMultipart();
                    mp.addBodyPart(mbp1);

                    for (File f : testResults) {
                        if (f.exists() && f.isFile()) {
                            // create the second message part
                            MimeBodyPart mbp2 = new MimeBodyPart();

                            // attach the file to the message
                            mbp2.setDataHandler(new DataHandler(new FileDataSource(f)));
                            mbp2.setFileName(f.getName());
                            mp.addBodyPart(mbp2);
                        }
                    }

                    // add the Multipart to the message
                    msg.setContent(mp);

                    // set the Date: header
                    msg.setSentDate(new Date());

                    Transport.send(msg);
                } catch (MessagingException ex) {
                    LOG.warn(ex.toString(), ex);
                }
            }
        }
    }

    public static Map<String, String> buildLabelMap(List<Node> labelNodes) {
        Map<String, String> retval = new HashMap<String, String>();

        for (Node label : labelNodes) {
            String key = label.attr("for");

            if (StringUtils.isNotBlank(key)) {
                retval.put(key, cleanDisplayText(label.toString()));
            }
        }

        return retval;
    }

    public static boolean isRadioOrCheckboxInput(Node node) {
        boolean retval = false;

        if (Constants.HTML_TAG_TYPE_INPUT.equalsIgnoreCase(node.nodeName())) {
            String type = node.attr(Constants.HTML_TAG_ATTRIBUTE_TYPE);

            retval = (Constants.HTML_INPUT_ATTRIBUTE_TYPE_RADIO.equalsIgnoreCase(type)
                || Constants.HTML_INPUT_ATTRIBUTE_TYPE_CHECKBOX.equalsIgnoreCase(type));
        }

        return retval;
    }

    public static boolean isNodeProcessed(Set processedNodes, Node node) {
        boolean retval = false;

        if (isRadioOrCheckboxInput(node)) {
            retval = processedNodes.contains(node.attr(Constants.HTML_TAG_ATTRIBUTE_NAME));

            if (!retval) {
                processedNodes.add(node.attr(Constants.HTML_TAG_ATTRIBUTE_NAME));
            }
        } else {
            retval = processedNodes.contains(node.attr(Constants.NODE_ID));

            if (!retval) {
                processedNodes.add(node.attr(Constants.NODE_ID));
            }
        }

        return retval;
    }

    public static String[] loadPlatformNames(KualiTestConfigurationDocument.KualiTestConfiguration configuration) {
        List<String> retval = new ArrayList<String>();

        if (configuration.getPlatforms() != null) {
            for (Platform platform : configuration.getPlatforms().getPlatformArray()) {
                retval.add(platform.getName());
            }
        }

        return retval.toArray(new String[retval.size()]);
    }

    public static Node findFirstChildNode(Node parent, String nodeName) {
        Node retval = null;
        
        for (Node child : parent.childNodes()) {
            if (nodeName.equalsIgnoreCase(child.nodeName())) {
                retval = child;
                break;
            }
        }
        
        return retval;
    }

     public static List <Node> findChildNodes(Node parent, String nodeName) {
        List <Node> retval = new ArrayList<Node>();
        
        for (Node child : parent.childNodes()) {
            if (nodeName.equalsIgnoreCase(child.nodeName())) {
                retval.add(child);
            }
        }
        
        return retval;
    }
    
     public static Node findChildNode(Node curnode, String nodeName, String attributeName, String attributeValue) {
       Node retval = null;
       
       if (curnode != null) {
           if (curnode.nodeName().equals(nodeName) && attributeValue.equals(curnode.attr(attributeName))) {
               retval = curnode;
           } else {
               for (Node child : curnode.childNodes()) {
                   retval = findChildNode(child, nodeName, attributeName, attributeValue);
                   if (retval != null) {
                       break;
                   }
               }
           }
       }
       
       return retval;
    }


     public static Node findFirstParentNode(Node curnode, String nodeName) {
       Node retval = null;
       
       if (curnode != null) {
           Node parent = curnode.parent();
           
           while (parent != null) {
               if (parent.nodeName().equals(nodeName)) {
                   retval = parent;
                   break;
               }
               parent = parent.parent();
           }
       }
       
       return retval;
    }

     public static Node findFirstParentNode(Node curnode, String nodeName, String attributeName, String attributeValue) {
        Node retval = null;  
        Node parent = curnode.parent();

        while (parent != null) {
            if (parent.nodeName().equals(nodeName)) {
                 if (attributeValue.equalsIgnoreCase(parent.attr(attributeName))) {
                     retval = parent;
                     break;
                 }
            }
            parent = parent.parent();
        }
       
       return retval;
    }

     public static Node findPreviousSiblingNode(Node curnode, String nodeName) {
       Node retval = null;
       
       if (curnode != null) {
           Node sibling = curnode.previousSibling();
           
           while (sibling != null) {
               if (sibling.nodeName().equals(nodeName)) {
                   retval = sibling;
                   break;
               }
               sibling = sibling.previousSibling();
           }
       }
       
       return retval;
    }

    public static Node findNextSiblingNode(Node curnode, String nodeName) {
       Node retval = null;
       
       if (curnode != null) {
           Node sibling = curnode.nextSibling();
           
           while (sibling != null) {
               if (sibling.nodeName().equals(nodeName)) {
                   retval = sibling;
                   break;
               }
               sibling = sibling.nextSibling();
           }
       }
       
       return retval;
    }


    public static boolean containsChildNode(Node parent, String nodeName) {
        boolean retval = false;
        
        for (Node child : parent.childNodes()) {
            if (nodeName.equalsIgnoreCase(child.nodeName())) {
                retval = true;
                break;
            }
        }
        
        return retval;
    }
    
    
      public static String tidify(String input) {
        Tidy tidy = new Tidy();
        tidy.setMakeClean(true);
        tidy.setXHTML(true);
        tidy.setHideComments(true);
        tidy.setDropEmptyParas(true);
        tidy.setDropFontTags(true);
        
        StringWriter writer = new StringWriter();
        tidy.parse(new StringReader(input), writer);
        return writer.getBuffer().toString();
    }
}
