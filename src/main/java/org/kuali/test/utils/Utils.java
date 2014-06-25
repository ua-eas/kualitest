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
import java.io.FileInputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.HttpURLConnection;
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
import javax.servlet.http.HttpServletResponse;
import javax.swing.tree.DefaultMutableTreeNode;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.StringEnumAbstractBase;
import org.apache.xmlbeans.XmlOptions;
import org.jasypt.util.text.BasicTextEncryptor;
import org.kuali.test.AutoReplaceParameter;
import org.kuali.test.CheckpointProperty;
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
import org.kuali.test.TagAttribute;
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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;

/**
 *
 * @author rbtucker
 */
public class Utils {
    private static final Logger LOG = Logger.getLogger(Utils.class);

    public static String ENUM_CHILD_CLASS = "$Enum";
    private static Tidy tidy;
    private static String encryptionPassword;
    
    public static Map<String, List<HtmlTagHandler>> TAG_HANDLERS = new HashMap<String, List<HtmlTagHandler>>();

    /**
     *
     * @param platforms
     * @return
     */
    public static String[] getPlatformNames(Platform[] platforms) {
        String[] retval = new String[platforms.length];
        for (int i = 0; i < platforms.length; ++i) {
            retval[i] = platforms[i].getName();
        }
        return retval;
    }

    /**
     *
     * @param platforms
     * @param name
     * @return
     */
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

    /**
     *
     * @param configuration
     * @param node
     * @return
     */
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

    /**
     *
     * @param platform
     * @param testName
     * @return
     */
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

    /**
     *
     * @param configuration
     * @param platformName
     * @return
     */
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

    /**
     *
     * @param testSuite
     * @param testName
     * @param testIndex
     * @return
     */
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

    /**
     *
     * @param configuration
     * @param platformName
     * @param testSuiteName
     * @return
     */
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

    /**
     *
     * @param configuration
     * @param dbconn
     * @return
     */
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

    /**
     *
     * @param configuration
     * @param ws
     * @return
     */
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

    /**
     *
     * @param configuration
     * @param jmx
     * @return
     */
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

    /**
     *
     * @param configuration
     * @param suiteTest
     * @return
     */
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

    /**
     *
     * @param configuration
     * @param testSuite
     * @return
     */
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

    /**
     *
     * @param configuration
     * @param node
     * @return
     */
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

    /**
     *
     * @param tests
     * @param test
     * @return
     */
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

    /**
     *
     * @param clazz
     * @return
     */
    public static String[] getXmlEnumerations(Class clazz) {
        return getXmlEnumerations(clazz, false);
    }

    /**
     *
     * @param clazz
     * @param includeEmptyItem
     * @return
     */
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

    /**
     *
     * @param url
     * @param includeProtocol
     * @return
     */
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

    /**
     *
     * @param url
     * @return
     */
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

    /**
     *
     * @param o
     * @param propertyName
     * @return
     */
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

    /**
     *
     * @param o
     * @param propertyName
     * @param value
     */
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

    /**
     *
     * @param o
     * @param propertyName
     * @return
     */
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

    /**
     *
     * @param propertyName
     * @return
     */
    public static String buildSetMethodNameFromPropertyName(String propertyName) {
        return buildMethodNameFromPropertyName("set", propertyName);
    }

    /**
     *
     * @param propertyName
     * @return
     */
    public static String buildGetMethodNameFromPropertyName(String propertyName) {
        return buildMethodNameFromPropertyName("get", propertyName);
    }

    /**
     *
     * @param prefix
     * @param propertyName
     * @return
     */
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

    /**
     *
     * @param nm
     * @return
     */
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

    /**
     *
     * @param key
     * @return
     */
    public static boolean wantHttpRequestHeader(String key) {
        return (Constants.HTTP_HEADER_ACCEPT.equals(key)
            || Constants.HTTP_HEADER_ACCEPT_ENCODING.equals(key)
            || Constants.HTTP_HEADER_CONNECTION.equals(key)
            || Constants.HTTP_HEADER_ACCEPT_LANGUAGE.equals(key)
            || Constants.HTTP_HEADER_USER_AGENT.equals(key)
            || Constants.HTTP_HEADER_HOST.equals(key)
            || Constants.HTTP_HEADER_REFERER.equals(key)
            || Constants.HTTP_HEADER_ORIGIN.equals(key));
    }
    
    /**
     *
     * @param configuration
     * @param request
     * @return
     */
    public static String buildUrlFromRequest(KualiTestConfigurationDocument.KualiTestConfiguration configuration, HttpRequest request) {
        StringBuilder retval = new StringBuilder(128);
        
        String referer = request.headers().get(Constants.HTTP_HEADER_REFERER);
        
        String s = "https://";
        if (referer != null) {
            int pos = referer.indexOf(":");
            if (pos > -1) {
                s = referer.substring(0, pos+3);
            }
        }
        
        retval.append(s);
        retval.append(request.headers().get(Constants.HTTP_HEADER_HOST));
        retval.append(processRequestData(configuration, request.getUri()));
        
        
        return retval.toString();
    }

    /**
     *
     * @param configuration
     * @param testop
     * @param request
     * @param redirectUrl
     */
    public static void populateHttpRequestOperation(KualiTestConfigurationDocument.KualiTestConfiguration configuration, 
        TestOperation testop, HttpRequest request) {
        if (LOG.isDebugEnabled()) {
            if (request != null) {
                LOG.debug(getHttpRequestDetails(request));
            }
        }

        HtmlRequestOperation op = testop.addNewOperation().addNewHtmlRequestOperation();
        testop.setOperationType(TestOperationType.HTTP_REQUEST);
        op.addNewRequestHeaders();
        op.addNewRequestParameters();
        if (request != null) {
            Iterator<Entry<String, String>> it = request.headers().iterator();

            while (it.hasNext()) {
                Entry<String, String> entry = it.next();
                if (wantHttpRequestHeader(entry.getKey())) {
                    RequestHeader header = op.getRequestHeaders().addNewHeader();
                    header.setName(entry.getKey());
                    header.setValue(entry.getValue());
                }
            }

            String method = request.getMethod().name();
            op.setMethod(method);
            op.setUrl(buildUrlFromRequest(configuration, request));

            // if this is a post then try to get content
            if (Constants.HTTP_REQUEST_METHOD_POST.equalsIgnoreCase(request.getMethod().name())) {
                if (request instanceof FullHttpRequest) {
                    FullHttpRequest fr = (FullHttpRequest) request;
                    if (fr.content() != null) {
                        byte[] data = getHttpPostContent(fr.content());

                        if (data != null) {
                            RequestParameter param = op.getRequestParameters().addNewParameter();
                            param.setName(Constants.PARAMETER_NAME_CONTENT);
                            param.setValue(processRequestData(configuration,new String(data)));
                        }
                    }
                }
            }
        }
    }

    /**
     *
     * @param input
     * @param parameterName
     * @param separator
     * @return
     */
    public static int[] getParameterPosition(String input, String parameterName, String separator) {
        int[] retval = null;
        
        if (StringUtils.isNotBlank(input) && StringUtils.isNotBlank(parameterName)) {
            int pos1 = input.toLowerCase().indexOf(parameterName.toLowerCase() + "=");
            
            if (pos1 > -1) {
                int pos2 = input.indexOf(separator, pos1);
                
                if (pos2 < 0) {
                    pos2 = input.length();
                }
                
                if (pos2 > pos1) {
                    retval = new int[] {pos1, pos2};
                }
            }
        }
        
        return retval;
    }

    /**
     *
     * @param input
     * @param pos
     * @return
     */
    public static String[] getParameterData(String input, int[] pos) {
        String[] retval = null;
        
        if (StringUtils.isNotBlank(input) && (pos != null)) {
            String s = input.substring(pos[0], pos[1]);

            int pos1 = s.indexOf("=");

            if (pos1 > -1) {
                retval = new String[] {s.substring(0, pos1).trim(), s.substring(pos1+1).trim()};
            }
        }
        
        return retval;
    }

    /**
     *
     * @param input
     * @param parameterPosition
     * @param parameterData
     * @return
     */
    public static String replaceParameterString(String input, int[] parameterPosition, String[] parameterData) {
        StringBuilder retval = new StringBuilder(input.length());

        retval.append(input.substring(0, parameterPosition[0]));
        retval.append(parameterData[0]);
        retval.append("=");
        retval.append(parameterData[1]);
        retval.append(input.substring(parameterPosition[1]));

        return retval.toString();
    }
    
    
    public static String removeParameterString(String input, int[] paramPosition) {
        String retval = input;
        
        if (StringUtils.isNotBlank(input)) {
            // start 1 back to get seperator
            StringBuilder buf= new StringBuilder(input.length());
            buf.append(input.substring(0, paramPosition[0] - 1));
            if (paramPosition[1] < (input.length() - 1)) {
                buf.append(paramPosition[1] + 1);
            }
            
            retval = buf.toString();
        }
        return retval;
    }
    
    /**
     *
     * @param configuration
     * @param input
     * @return
     */
    public static String processRequestData(KualiTestConfigurationDocument.KualiTestConfiguration configuration, String input) {
        String retval = input;
        
        // strip specified parameters
        for (String parameterName : configuration.getParametersToRemove().getNameArray()) {
            int[] paramPosition = getParameterPosition(retval, parameterName, Constants.SEPARATOR_AMPERSTAND);

            if (paramPosition != null) {
                retval = removeParameterString(retval, paramPosition);
            }
        }

        // encrypt specified securty parameters
        Set <String> hs = new HashSet<String>();
        for (String parameterName : configuration.getParametersRequiringEncryption().getNameArray()) {
            if (!hs.contains(parameterName)) {
                hs.add(parameterName);
                
                int[] paramPosition = getParameterPosition(retval, parameterName, Constants.SEPARATOR_AMPERSTAND);

                if (paramPosition != null) {
                    String[] parameterData = getParameterData(retval, paramPosition);

                    if (parameterData != null) {
                        parameterData[1] = encrypt(getEncryptionPassword(configuration), parameterData[1]);
                        retval = replaceParameterString(retval, paramPosition, parameterData);
                    }
                }
            }
        }
        
        return retval;
    }
    
    /**
     *
     * @param configuration
     * @param optype
     * @param inputData
     * @return
     */
    public static TestOperation buildTestOperation(KualiTestConfigurationDocument.KualiTestConfiguration configuration, 
        TestOperationType.Enum optype, Object inputData) {
        TestOperation retval = TestOperation.Factory.newInstance();

        retval.setOperationType(optype);

        if (inputData != null) {
            switch (optype.intValue()) {
                case TestOperationType.INT_HTTP_REQUEST:
                    populateHttpRequestOperation(configuration, retval, (HttpRequest)inputData);
                    break;
            }
        }

        return retval;
    }

    /**
     *
     * @param input
     * @return
     */
    public static String formatForFileName(String input) {
        String retval = null;
        if (StringUtils.isNotBlank(input)) {
            retval = input.toLowerCase().replace(' ', '-');
        }
        return retval;
    }

    /**
     *
     * @param header
     * @return
     */
    public static String getTestFileName(TestHeader header) {
        return formatForFileName(header.getTestName());
    }

    /**
     *
     * @param repositoryLocation
     * @param header
     * @return
     */
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

    /**
     *
     * @return
     */
    public static XmlOptions getSaveXmlOptions() {
        XmlOptions retval = new XmlOptions();
        retval.setSavePrettyPrint();
        retval.setSavePrettyPrintIndent(3);
        return retval;
    }

    /**
     *
     * @param request
     * @return
     */
    public static String getHttpRequestDetails(HttpRequest request) {
        StringBuilder retval = new StringBuilder(512);
        retval.append("******************** http request *******************\r\n");
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

        retval.append("******************************************************\r\n");
        return retval.toString();
    }

    /**
     *
     * @param content
     * @return
     */
    public static byte[] getHttpPostContent(ByteBuf content) {
        byte[] retval = null;
        if (content.isReadable()) {
            ByteBuffer nioBuffer = content.nioBuffer();
            retval = new byte[nioBuffer.remaining()];
            nioBuffer.get(retval);
        }

        return retval;
    }

    /**
     *
     * @param configuration
     * @param platformName
     * @param testName
     * @return
     */
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

    /**
     *
     * @param node
     * @return
     */
    public static boolean isTextNode(Node node) {
        return ((node != null) && (node.getNodeType() == Node.TEXT_NODE));
    }
    
    /**
     *
     * @param node
     * @param buf
     */
    public static void getCleanedText(Node node, StringBuilder buf) {
        NodeList children = node.getChildNodes();
        
        for (int i = 0; i < children.getLength(); ++i) {
            Node child = children.item(i);
            
            if (isTextNode(child)) {
                buf.append(child.getNodeValue());
            } else {
                getCleanedText(child, buf);
            }
        }
    }
    
    /**
     *
     * @param in
     * @return
     */
    public static String trimString(String in) {
        if (StringUtils.isNotBlank(in)) {
            return in.trim().replaceAll("[^\\x00-\\x7F]", "");
        } else {
            return "";
        }
    }
    
    /**
     *
     * @param node
     * @return
     */
    public static String cleanDisplayText(Node node) {
        StringBuilder buf = new StringBuilder(128);
        getCleanedText(node, buf);
        return Utils.trimString(StringEscapeUtils.unescapeHtml4(buf.toString()));
    }

    /**
     *
     * @param configuration
     * @return
     */
    public static File getTestRunnerConfigurationFile(KualiTestConfigurationDocument.KualiTestConfiguration configuration) {
        return new File(configuration.getRepositoryLocation() + "/" + Constants.TEST_RUNNER_CONFIG_FILENAME);
    }

    /**
     *
     * @param configuration
     */
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

    /**
     *
     * @param node
     * @param tm
     * @return
     */
    public static boolean isTagMatch(Element node, TagMatcher tm) {
        boolean retval = true;
        if ((tm.getMatchAttributes() != null) && (tm.getMatchAttributes().sizeOfMatchAttributeArray() > 0)) {
            for (TagMatchAttribute att : tm.getMatchAttributes().getMatchAttributeArray()) {
                NamedNodeMap m = node.getAttributes();

                if (StringUtils.isBlank(node.getAttribute(att.getName()))) {
                    retval = false;
                    break;
                } else {
                    if (att.getValue().contains("|")) {
                        // can use "|" to seperate "or" values for matching
                        StringTokenizer st = new StringTokenizer(att.getValue(), "|");

                        boolean foundit = false;
                        while (st.hasMoreTokens()) {
                            String token = st.nextToken();

                            if (token.equalsIgnoreCase(node.getAttribute(att.getName()))) {
                                foundit = true;
                                break;
                            }
                        }

                        if (!foundit) {
                            retval = false;
                            break;
                        }
                    } else if (att.getValue().contains("*")) {
                        String nodeData = node.getAttribute(att.getName());
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
                        if (!val.equalsIgnoreCase(node.getAttribute(att.getName()))) {
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

    /**
     *
     * @param node
     * @return
     */
    public static List <Element> getChildElements(Element node) {
        List <Element> retval = new ArrayList<Element>();
        
        Node child = node.getFirstChild();
        
        while (child != null) {
            if (child instanceof Element) {
                retval.add((Element)child);
            }
            
            child = child.getNextSibling();
        }
        
        return retval;
    }
    
    /**
     *
     * @param node
     * @param childTagMatch
     * @return
     */
    public static boolean isChildTagMatchFailure(Element node, ChildTagMatch childTagMatch) {
        boolean retval = false;

        if ((childTagMatch != null) && (node != null)) {
            if (node.hasChildNodes()) {
                boolean foundone = false;
                Set<String> childTagNames = new HashSet<String>();

                StringTokenizer st = new StringTokenizer(childTagMatch.getChildTagName(), "|");

                while (st.hasMoreTokens()) {
                    childTagNames.add(st.nextToken());
                }

                for (Element child : getChildElements(node)) {
                    if (childTagNames.contains(child.getTagName())) {
                        foundone = true;
                        if (childTagMatch.getMatchAttributes() != null) {
                            if (childTagMatch.getMatchAttributes().sizeOfMatchAttributeArray() > 0) {
                                for (TagMatchAttribute att : childTagMatch.getMatchAttributes().getMatchAttributeArray()) {
                                    if ((att != null) && StringUtils.isNotBlank(att.getName())) {
                                        String childAttr = child.getAttribute(att.getName());
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

    /**
     *
     * @param node
     * @param parentTagMatch
     * @return
     */
    public static boolean isParentTagMatchFailure(Element node, ParentTagMatch parentTagMatch) {
        boolean retval = false;

        if ((parentTagMatch != null) && (node != null)) {
            Set<String> parentTagNames = new HashSet<String>();

            StringTokenizer st = new StringTokenizer(parentTagMatch.getParentTagName(), "|");

            while (st.hasMoreTokens()) {
                parentTagNames.add(st.nextToken());
            }
            
            Element validParent = null;
            Node parent = node.getParentNode();

            // if we are looking for table and this is a tbody ten move up 1 level
            if (Constants.HTML_TAG_TYPE_TABLE.equalsIgnoreCase(parentTagMatch.getParentTagName())
                && Constants.HTML_TAG_TYPE_TBODY.equalsIgnoreCase(parent.getNodeName())) {
                parent = parent.getParentNode();
            }

            if (isElement(parent)) {
                if (parentTagNames.contains(parent.getNodeName())) {
                    if (parentTagMatch.getMatchAttributes() != null) {
                        if (parentTagMatch.getMatchAttributes().sizeOfMatchAttributeArray() > 0) {
                            boolean ok = true;
                            for (TagMatchAttribute att : parentTagMatch.getMatchAttributes().getMatchAttributeArray()) {
                                String parentAttr = ((Element)parent).getAttribute(att.getName());
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
                                validParent = (Element)parent;
                            }
                        }
                    } else {
                        validParent = (Element)parent;
                    }
                }
            }

            retval = (validParent == null);
        }

        return retval;
    }

    /**
     *
     * @param tm
     * @param node
     * @return
     */
    public static Element getMatchingChild(TagMatcher tm, Element node) {
        Element retval = null;

        List <Element> l = getChildElements(node);
        if (!l.isEmpty()) {
            retval = getMatchingSibling(tm, l.get(0));
        }

        return retval;
    }

    /**
     *
     * @param tm
     * @param node
     * @return
     */
    public static Element getMatchingParent(TagMatcher tm, Element node) {
        Element retval = null;

        Node parent = node.getParentNode();
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

        while ((cnt < totalCnt) && isElement(parent)) {
            if (parent.getNodeName().equalsIgnoreCase(tm.getTagName())) {
                cnt++;
                TagMatchAttribute[] attrs = null;
                if (tm.getMatchAttributes() != null) {
                    attrs = tm.getMatchAttributes().getMatchAttributeArray();
                }

                if ((!limited || (cnt == totalCnt)) && isTagMatch((Element)parent, tm)) {
                    retval = (Element)parent;
                    break;
                }
            }

            parent = parent.getParentNode();
        }

        return retval;
    }

    /**
     *
     * @param searchDefinition
     * @return
     */
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

    /**
     *
     * @param node
     * @return
     */
    public static Element getPreviousSiblingElement(Node node) {
        Element retval = null;
        
        Node sibling = node.getPreviousSibling();
        
        while ((sibling != null) && !isElement(sibling)) {
            sibling = sibling.getPreviousSibling();
        }
        
        if (isElement(sibling)) {
            retval = (Element)sibling;
        }
        
        return retval;
    }
    
    /**
     *
     * @param node
     * @return
     */
    public static Element getNextSiblingElement(Node node) {
        Element retval = null;
        
        Node sibling = node.getNextSibling();
        
        while ((sibling != null) && !isElement(sibling)) {
            sibling = sibling.getNextSibling();
        }
        
        if (isElement(sibling)) {
            retval = (Element)sibling;
        }
        
        return retval;
    }

    /**
     *
     * @param node
     * @return
     */
    public static int getChildNodeIndex(Element node) {
        int retval = 0;

        Element curnode = getPreviousSiblingElement(node);
        
        while(curnode != null) {
            retval++;
            curnode = getPreviousSiblingElement(curnode);
        }
        
        return retval;
    }
    
    /**
     *
     * @param tm
     * @param node
     * @return
     */
    public static Element getMatchingSibling(TagMatcher tm, Element node) {
        Element retval = null;

        String searchDefinition = tm.getSearchDefinition();

        // default to search all children forward
        if (StringUtils.isBlank(searchDefinition)) {
            searchDefinition = "+*";
        }

        int startIndex = getChildNodeIndex(node);
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

                    Element prev = getPreviousSiblingElement(node);

                    while ((prev != null) && (cnt < targetCnt)) {
                        if (prev.getNodeName().equalsIgnoreCase(tm.getTagName())) {
                            cnt++;
                            if ((!limited || (cnt == targetCnt)) && isTagMatch(prev, tm)) {
                                retval = prev;
                                break;
                            }
                        }

                        prev = getPreviousSiblingElement(prev);
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

                    Element next = getNextSiblingElement(node);

                    while ((next != null) && (cnt < targetCnt)) {
                        if (next.getNodeName().equalsIgnoreCase(tm.getTagName())) {
                            cnt++;
                            if ((!limited || (cnt == targetCnt)) && isTagMatch(next, tm)) {
                                retval = next;
                                break;
                            }
                        }

                        next = getNextSiblingElement(next);
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


                    for (Element child : getChildElements((Element)node.getParentNode())) {
                        if (child.getNodeName().equalsIgnoreCase(tm.getTagName())) {
                            cnt++;
                            if ((!limited || (cnt == targetCnt))
                                && isTagMatch(child, tm)) {
                                retval = child;
                                break;
                            }
                        }
                    }
                }
                break;
        }

        return retval;
    }

    /**
     *
     * @param node
     * @return
     */
    public static int getSiblingIndex(Element node) {
        int retval = -1;

        Element parent = (Element)node.getParentNode();

        List <Element> elements = getChildElements(parent);
        
        int indx = 0;
        for (Node curnode : getChildElements(parent)) {
            if (node == curnode) {
                retval = indx;
                break;
            }
            
            indx++;
        }
        
        return retval;
    }

    /**
     *
     * @param node
     * @return
     */
    public static int getSiblingIndexByTagType(Element node) {
        int retval = 0;

        Element parent = (Element)node.getParentNode();

        for (Element sibling : getChildElements(parent)) {
            if (getChildNodeIndex(sibling) == getChildNodeIndex(node)) {
                break;
            }

            if (sibling.getNodeName().equals(node.getNodeName())) {
                retval++;
            }
        }

        return retval;
    }

    /**
     *
     * @param tagMatchers
     * @param node
     * @return
     */
    public static String getMatchedNodeText(TagMatcher[] tagMatchers, Element node) {
        String retval = null;

        if ((tagMatchers != null) && (tagMatchers.length > 0)) {
            Element curnode = node;

            for (int i = 0; i < tagMatchers.length; ++i) {
                TagMatcher tm = tagMatchers[i];
                String sdef = tm.getSearchDefinition();

                // "I" is a key to match to same sibling index as parent
                // used for table column header matching. Will need to clone
                // original matcher in this case
                if (StringUtils.isNotBlank(sdef) && sdef.startsWith(Constants.NODE_INDEX_MATCHER_CODE)) {
                    tm = (TagMatcher) tm.copy();
                    if (sdef.length() > 1) {
                        tm.setSearchDefinition("" + (getSiblingIndex(node) + Integer.parseInt(sdef.substring(1))));
                    } else {
                        tm.setSearchDefinition("" + (getSiblingIndex(node) + 1));
                    }
                }

                curnode = Utils.getMatchingTagNode(tm, curnode);
            }

            if (curnode != null) {
                retval = Utils.cleanDisplayText(curnode);
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("getMatchedNodeText: tag=" + node.getNodeName() + ", text=" + retval);
            }
        }

        return retval;
    }

    /**
     *
     * @param tm
     * @param node
     * @return
     */
    public static Element getMatchingTagNode(TagMatcher tm, Element node) {
        Element retval = null;

        if ((tm != null) && (node != null)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("match type: " + tm.getMatchType() + ", tag: " + node.getNodeName());
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

    /**
     *
     * @param app
     * @param node
     * @return
     */
    public static HtmlTagHandler getHtmlTagHandler(String app, Element node) {
        HtmlTagHandler retval = null;

        if (LOG.isDebugEnabled()) {
            LOG.debug("getHtmlTagHandler - " + node.getNodeName());
        }
        
        List<HtmlTagHandler> handlerList = new ArrayList<HtmlTagHandler>();

        // add the application specific handlers first
        List<HtmlTagHandler> thl = TAG_HANDLERS.get(app + "." + node.getNodeName().trim().toLowerCase());

        if (thl != null) {
            handlerList.addAll(thl);
        }

        // add the general handlers
        thl = TAG_HANDLERS.get("all." + node.getNodeName().trim().toLowerCase());

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

    /**
     *
     * @param node
     * @return
     */
    public static boolean isHtmlContainer(Element node) {
        return Constants.DEFAULT_HTML_CONTAINER_TAGS.contains(node.getNodeName().toLowerCase().trim());
    }

    /**
     *
     * @param platform
     * @return
     */
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

    /**
     *
     * @param testType
     * @param platform
     * @return
     */
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

    /**
     *
     * @param th
     * @param node
     * @return
     */
    public static String buildCheckpointSectionName(HtmlTagHandler th, Element node) {
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

    /**
     *
     * @param node
     * @return
     */
    public static boolean isValidContainerNode(Element node) {
        return !getChildElements(node).isEmpty();
    }

    /**
     *
     * @param configuration
     * @param dbname
     * @return
     */
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

    /**
     *
     * @param configuration
     * @param wsname
     * @return
     */
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

    /**
     *
     * @param conn
     * @param stmt
     * @param res
     */
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

    /**
     *
     * @param password
     * @param dbconn
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public static Connection getDatabaseConnection(String password, DatabaseConnection dbconn) throws ClassNotFoundException, SQLException {
        Class.forName(dbconn.getJdbcDriver());
        Connection retval = DriverManager.getConnection(dbconn.getJdbcUrl(), dbconn.getUsername(), Utils.decrypt(password, dbconn.getPassword()));
        retval.setReadOnly(true);
        return retval;
    }

    /**
     *
     * @param jdbcType
     * @param decimalDigits
     * @return
     */
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

    /**
     *
     * @param input
     * @return
     */
    public static String cleanTableDisplayName(String input) {
        String retval = input;

        if (StringUtils.isNotBlank(input)) {
            if (input.endsWith("Impl")) {
                retval = input.substring(0, input.length() - 4);
            }
        }

        return retval;
    }

    /**
     *
     * @param jdbcType
     * @param decimalDigits
     * @return
     */
    public static boolean isIntegerJdbcType(int jdbcType, int decimalDigits) {
        boolean retval = false;

        if (isNumericJdbcType(jdbcType)) {
            retval = (decimalDigits == 0);
        }

        return retval;
    }

    /**
     *
     * @param jdbcType
     * @param decimalDigits
     * @return
     */
    public static boolean isFloatJdbcType(int jdbcType, int decimalDigits) {
        boolean retval = false;

        if (isNumericJdbcType(jdbcType)) {
            retval = (decimalDigits > 0);
        }

        return retval;
    }

    /**
     *
     * @param jdbcType
     * @return
     */
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

    /**
     *
     * @param jdbcType
     * @return
     */
    public static boolean isTimestampJdbcType(int jdbcType) {
        return (jdbcType == java.sql.Types.TIMESTAMP);
    }

    /**
     *
     * @param jdbcType
     * @return
     */
    public static boolean isDateJdbcType(int jdbcType) {
        return (jdbcType == java.sql.Types.DATE);
    }

    /**
     *
     * @param jdbcType
     * @return
     */
    public static boolean isTimeJdbcType(int jdbcType) {
        return (jdbcType == java.sql.Types.TIME);
    }

    /**
     *
     * @param jdbcType
     * @return
     */
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

    /**
     *
     * @param jdbcType
     * @return
     */
    public static boolean isDateTimeJdbcType(int jdbcType) {
        return (isDateJdbcType(jdbcType) || isTimestampJdbcType(jdbcType) || isTimeJdbcType(jdbcType));
    }

    /**
     *
     * @param jdbcType
     * @return
     */
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

    /**
     *
     * @param style
     * @param data
     * @return
     */
    public static String buildHtmlStyle(String style, String data) {
        return style.replace("^", data);
    }

    /**
     *
     * @param input
     * @return
     */
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

    /**
     *
     * @param cal
     * @return
     */
    public static Calendar truncate(Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return cal;
    }

    /**
     *
     * @param columns
     * @return
     */
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

    /**
     *
     * @param configuration
     * @param jmxName
     * @return
     */
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

    /**
     *
     * @param configuration
     * @param testSuite
     * @param testHeader
     * @return
     */
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

    /**
     *
     * @param configuration
     * @param testSuite
     * @param testHeader
     * @param testResults
     */
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

    /**
     *
     * @param labelNodes
     * @return
     */
    public static Map<String, String> buildLabelMap(List<Element> labelNodes) {
        Map<String, String> retval = new HashMap<String, String>();

        for (Element label : labelNodes) {
            String key = label.getAttribute("for");

            if (StringUtils.isNotBlank(key)) {
                retval.put(key, cleanDisplayText(label));
            }
        }

        return retval;
    }

    /**
     *
     * @param node
     * @return
     */
    public static boolean isRadioOrCheckboxInput(Element node) {
        boolean retval = false;

        if (Constants.HTML_TAG_TYPE_INPUT.equalsIgnoreCase(node.getNodeName())) {
            String type = node.getAttribute(Constants.HTML_TAG_ATTRIBUTE_TYPE);

            retval = (Constants.HTML_INPUT_ATTRIBUTE_TYPE_RADIO.equalsIgnoreCase(type)
                || Constants.HTML_INPUT_ATTRIBUTE_TYPE_CHECKBOX.equalsIgnoreCase(type));
        }

        return retval;
    }

    /**
     *
     * @param processedNodes
     * @param node
     * @return
     */
    public static boolean isNodeProcessed(Set processedNodes, Element node) {
        boolean retval = false;

        if (isRadioOrCheckboxInput(node)) {
            retval = processedNodes.contains(node.getAttribute(Constants.HTML_TAG_ATTRIBUTE_NAME));

            if (!retval) {
                processedNodes.add(node.getAttribute(Constants.HTML_TAG_ATTRIBUTE_NAME));
            }
        } else {
            retval = processedNodes.contains(node.getAttribute(Constants.NODE_ID));

            if (!retval) {
                processedNodes.add(node.getAttribute(Constants.NODE_ID));
            }
        }

        return retval;
    }

    /**
     *
     * @param configuration
     * @return
     */
    public static String[] loadPlatformNames(KualiTestConfigurationDocument.KualiTestConfiguration configuration) {
        List<String> retval = new ArrayList<String>();

        if (configuration.getPlatforms() != null) {
            for (Platform platform : configuration.getPlatforms().getPlatformArray()) {
                retval.add(platform.getName());
            }
        }

        return retval.toArray(new String[retval.size()]);
    }

    /**
     *
     * @param parent
     * @param nodeName
     * @return
     */
    public static Element findFirstChildNode(Element parent, String nodeName) {
        Element retval = null;
        
        for (Element child : getChildElements(parent)) {
            if (nodeName.equalsIgnoreCase(child.getNodeName())) {
                retval = child;
                break;
            }
        }
        
        return retval;
    }

    /**
     *
     * @param parent
     * @param nodeName
     * @return
     */
    public static List <Element> findChildNodes(Element parent, String nodeName) {
        List <Element> retval = new ArrayList<Element>();
        
        for (Element child : getChildElements(parent)) {
            if (nodeName.equalsIgnoreCase(child.getNodeName())) {
                retval.add(child);
            }
        }
        
        return retval;
    }
    
    /**
     *
     * @param curnode
     * @param nodeName
     * @param attributeName
     * @param attributeValue
     * @return
     */
    public static Element findChildNode(Element curnode, String nodeName, String attributeName, String attributeValue) {
       Element retval = null;
       
       if (curnode != null) {
           if (curnode.getNodeName().equals(nodeName) && attributeValue.equals(curnode.getAttribute(attributeName))) {
               retval = curnode;
           } else {
               for (Element child : getChildElements(curnode)) {
                   retval = findChildNode(child, nodeName, attributeName, attributeValue);
                   if (retval != null) {
                       break;
                   }
               }
           }
       }
       
       return retval;
    }

    /**
     *
     * @param curnode
     * @param nodeName
     * @return
     */
    public static Element findFirstParentNode(Element curnode, String nodeName) {
       Element retval = null;
       
       if (curnode != null) {
           Element parent = (Element)curnode.getParentNode();
           
           while (parent != null) {
               if (parent.getNodeName().equals(nodeName)) {
                   retval = parent;
                   break;
               }
               parent = (Element)parent.getParentNode();
           }
       }
       
       return retval;
    }

    /**
     *
     * @param curnode
     * @param nodeName
     * @param attributeName
     * @param attributeValue
     * @return
     */
    public static Element findFirstParentNode(Element curnode, String nodeName, String attributeName, String attributeValue) {
        Element retval = null;  
        Element parent = (Element)curnode.getParentNode();

        while (parent != null) {
            if (parent.getNodeName().equals(nodeName)) {
                 if (attributeValue.equalsIgnoreCase(parent.getAttribute(attributeName))) {
                     retval = parent;
                     break;
                 }
            }
            parent = (Element)parent.getParentNode();
        }
       
       return retval;
    }

    /**
     *
     * @param curnode
     * @param nodeName
     * @return
     */
    public static Element findPreviousSiblingNode(Element curnode, String nodeName) {
       Element retval = null;
       
       if (curnode != null) {
           Element sibling = getPreviousSiblingElement(curnode);
           
           while (sibling != null) {
               if (sibling.getNodeName().equals(nodeName)) {
                   retval = sibling;
                   break;
               }
               sibling = getPreviousSiblingElement(sibling);
           }
       }
       
       return retval;
    }

    /**
     *
     * @param curnode
     * @param nodeName
     * @return
     */
    public static Element findNextSiblingNode(Element curnode, String nodeName) {
       Element retval = null;
       
       if (curnode != null) {
           Element sibling = getNextSiblingElement(curnode);
           
           while (sibling != null) {
               if (sibling.getNodeName().equals(nodeName)) {
                   retval = sibling;
                   break;
               }
               sibling = getNextSiblingElement(sibling);
           }
       }
       
       return retval;
    }

    /**
     *
     * @param parent
     * @param nodeName
     * @return
     */
    public static boolean containsChildNode(Element parent, String nodeName) {
        return (parent.getElementsByTagName(nodeName).getLength() > 0);
    }

    /**
     *
     * @return
     */
    public static Tidy getTidy() {
        if (tidy == null) {
            tidy = new Tidy();
            tidy.setMakeClean(true);
            tidy.setXHTML(true);
            tidy.setShowWarnings(false);
            tidy.setHideComments(true);
            tidy.setQuiet(true);
        }
        
        return tidy;
        
    }

    /**
     *
     * @param doc
     * @param tagnames
     */
    public static void removeTagsFromDocument(Document doc, String[] tagnames) {
        List <Node> removeList = new ArrayList<Node>();
        for (String tag : tagnames) {
            // if this tag has a '.' in the name the attribute(s) are specified
            // in the form '<tagname>.<attname>=<attvalue>,<attname>=<attvalue>...'
            int pos = tag.indexOf(".");
            String tagname = tag;
            Map <String, String> attmap = null;
            if (pos > -1) {
                tagname = tag.substring(0, pos);
                attmap = new HashMap<String, String>();
                StringTokenizer st = new StringTokenizer(tag.substring(pos+1), ",");
                
                while (st.hasMoreTokens()) {
                    String s = st.nextToken();
                    pos = s.indexOf("=");
                    if (pos > -1) {
                        attmap.put(s.substring(0, pos), s.substring(pos+1));
                    }
                }
            }
            
            NodeList l= doc.getDocumentElement().getElementsByTagName(tagname);
            for (int i = 0; i < l.getLength(); ++i) {
                Element item = (Element)l.item(i);
                
                // if attribute is specified check the node for matching attribute
                if (attmap != null) {
                    boolean canRemove = (attmap.size() > 0);

                    for (String key : attmap.keySet()) {
                        String checkValue = attmap.get(key);
                        String val = item.getAttribute(key);
                    
                        if (StringUtils.isNotBlank(val)) {
                            if (!val.equalsIgnoreCase(checkValue)) {
                                canRemove = false;
                                break;
                            }
                        }
                        
                        if (canRemove) {
                            removeList.add(item);
                        }
                    }
                } else {
                    removeList.add(item);
                }
            }
        }

        for (Node node : removeList) {
            // this check required because we may have already removed the parent
            if (node.getParentNode() != null) {
                node.getParentNode().removeChild(node);
            }
        }
    }
    
    /**
     *
     * @param input
     * @return
     */
    public static Document tidify(String input) {
        Document retval = getTidy().parseDOM(new StringReader(input), new StringWriter());
        
        // remove tags we do not want
        removeTagsFromDocument(retval, Constants.DEFAULT_UNNECCESSARY_TAGS);

        if (LOG.isDebugEnabled()) {
            System.out.println("====================================================================");
            getTidy().pprint(retval, System.out);
            System.out.println("====================================================================");
        }
        
        return retval;
    }

    /**
     *
     * @param node
     * @return
     */
    public static boolean isElement(Node node) {
        return ((node != null) && (node.getNodeType() == Node.ELEMENT_NODE));
    }
    
    /**
     *
     * @param element
     * @return
     */
    public static List <Element> getSiblingElements(Element element) {
        List <Element> retval = new ArrayList<Element>();
        
        if (isElement(element.getParentNode())) {
            retval = getChildElements((Element)element.getParentNode());
            
            if (retval != null) {
                Iterator <Element> it = retval.iterator();
                
                while (it.hasNext()) {
                    if (it.next() == element) {
                        it.remove();
                        break;
                    }
                }
            }
        }
        
        return retval;
    }
    
    /**
     *
     * @param password
     * @param input
     * @return
     */
    public static String encrypt(String password, String input) {
        String retval = input;
        
        if (StringUtils.isNotBlank(password) && StringUtils.isNotBlank(input)) {
            try {
                // use StrongTextEncryptor with JCE installed for more secutity
                BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
                textEncryptor.setPassword(password);
                retval = textEncryptor.encrypt(input);
            }

            catch (Exception ex) {
                LOG.warn(ex.toString(), ex);
            }
        }
        
        return retval;
    }
    
    /**
     *
     * @param password
     * @param input
     * @return
     */
    public static String decrypt(String password, String input) {
        String retval = input;
        
        if (StringUtils.isNotBlank(password) && StringUtils.isNotBlank(input)) {
            try {
                // use StrongTextEncryptor with JCE installed for more secutity
                BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
                textEncryptor.setPassword(password);
                retval =  textEncryptor.decrypt(input);
            }

            catch (Exception ex) {
                LOG.warn(ex.toString(), ex);
            }
        }
        
        return retval;
    }
    
    /**
     *
     * @param status
     * @return
     */
    public static boolean isHttpRedirect(int status) {
        return ((status == HttpURLConnection.HTTP_MOVED_TEMP)
            || (status == HttpURLConnection.HTTP_MOVED_PERM)
            || (status == HttpURLConnection.HTTP_SEE_OTHER));
    }
    
    /**
     *
     * @param configuration
     * @return
     */
    public static String getEncryptionPassword(KualiTestConfigurationDocument.KualiTestConfiguration configuration) {
        if (encryptionPassword == null) {
            String pass = Constants.DEFAULT_ENCRYPTION_PASSWORD;
            
            if (StringUtils.isNotBlank(configuration.getEncryptionPasswordFile())) {
                File f = new File(configuration.getEncryptionPasswordFile());
                if (f.exists() && f.isFile()) {
                    FileInputStream fis = null;
        
                    try {
                        fis = new FileInputStream(f);
                        byte[] buf = new byte[(int)f.length()];
                        fis.read(buf);
                        pass = new String(buf);
                    }
                    
                    catch (Exception ex) {
                    }
                }
            }
            
            encryptionPassword = Base64.encodeBase64String(pass.getBytes());
            
        }
        
        return encryptionPassword;
    }

    public static boolean isCheckPointPropertyMatch(CheckpointProperty cp1, CheckpointProperty cp2) {
        boolean retval = false;
        if ((cp1 != null) && (cp2 != null)) {
            String key1 = buildCheckpointPropertyKey(cp1);
            String key2 = buildCheckpointPropertyKey(cp2);
            retval = key1.equals(key2);
        }
        
        return retval;
    }
    
    public static String buildCheckpointPropertyKey(CheckpointProperty cp) {
        StringBuilder retval = new StringBuilder(128);
        
        retval.append(cp.getPropertyGroup());
        retval.append(".");
        retval.append(cp.getPropertySection());
        retval.append(".");
        retval.append(cp.getPropertySubSection());
        retval.append(".");
        retval.append(cp.getPropertyName());
        
        return retval.toString();
    }

    public static boolean isAutoReplaceParameterMatch(AutoReplaceParameter param, Node node) {
        boolean retval = false;
        
        if ((node != null) && (node.getNodeType() == Node.ELEMENT_NODE)) {
            Element e = (Element)node;
            if (param.getTagName().equals(e.getTagName())) {
                if (param.getParameterName().equals(e.getAttribute("name"))) {
                    retval = true;
                    if ((param.getTagAttributes() != null) && (param.getTagAttributes().sizeOfAttributeArray() > 0)) {
                        for (TagAttribute att : param.getTagAttributes().getAttributeArray()) {
                            if (!att.getValue().equals(e.getAttribute(att.getName()))) {
                                retval = false;
                                break;
                            }
                        }
                    }
                }
            }
        }
        
        return retval;
    }
    
    public static String getNodeValue(Node node) {
        String retval = null;
        
        if (Constants.HTML_TAG_TYPE_INPUT.equalsIgnoreCase(node.getNodeName())) {
            Element e = (Element)node;
            retval = e.getAttribute(Constants.HTML_TAG_ATTRIBUTE_VALUE);
        }
        
        return retval;
    }
    
    public static String findAutoReplaceParameterInDom(AutoReplaceParameter param, Node node) {
        String retval = null;
         
        if (isAutoReplaceParameterMatch(param, node)) {
            retval = getNodeValue(node);
        } else {
            NodeList children = node.getChildNodes();
            
            for (int i = 0; (retval == null) && (i < children.getLength()); ++i) {
                retval = findAutoReplaceParameterInDom(param, children.item(i));
            }
        }
        
        return retval;
    }
    
    
    public static boolean isRedirectResponse(int status) {
        return ((status == HttpServletResponse.SC_MOVED_TEMPORARILY)
            || (status == HttpServletResponse.SC_MOVED_PERMANENTLY)
            || (status == HttpServletResponse.SC_SEE_OTHER));
            
    }
}        
