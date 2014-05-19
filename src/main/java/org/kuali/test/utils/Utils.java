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
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;
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
import org.kuali.test.HtmlRequestOp;
import org.kuali.test.KualiTestConfigurationDocument;
import org.kuali.test.KualiTestDocument;
import org.kuali.test.KualiTestDocument.KualiTest;
import org.kuali.test.ParentTagMatch;
import org.kuali.test.Platform;
import org.kuali.test.RequestHeader;
import org.kuali.test.RequestParameter;
import org.kuali.test.SuiteTest;
import org.kuali.test.TagHandler;
import org.kuali.test.TagMatchAttribute;
import org.kuali.test.TagMatchType;
import org.kuali.test.TagMatcher;
import org.kuali.test.TestHeader;
import org.kuali.test.TestOperation;
import org.kuali.test.TestOperationType;
import org.kuali.test.TestSuite;
import org.kuali.test.TestType;
import org.kuali.test.comparators.HtmlTagHandlerComparator;
import org.kuali.test.handlers.DefaultContainerTagHandler;
import org.kuali.test.handlers.HtmlTagHandler;


public class Utils {
    private static final Logger LOG = Logger.getLogger(Utils.class);
    public static String ENUM_CHILD_CLASS = "$Enum";
    
    public static Map <String, List<HtmlTagHandler>> TAG_HANDLERS = new HashMap<String, List<HtmlTagHandler>>();
    
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
                DefaultMutableTreeNode pnode = (DefaultMutableTreeNode)node.getParent();
                if (userObject instanceof Platform) {
                    retval = (Platform)userObject;
                } else if (userObject instanceof TestSuite) {
                    TestSuite testSuite = (TestSuite)userObject;
                    retval = findPlatform(configuration, testSuite.getPlatformName());
                } else if (userObject instanceof SuiteTest) {
                    SuiteTest test = (SuiteTest)userObject;
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
                    retval = removeSuiteTest(configuration, (SuiteTest)userObject);
                } else if (userObject instanceof TestSuite) {
                    retval = removeTestSuite(configuration, (TestSuite)userObject);
                } else if (userObject instanceof DatabaseConnection) {
                    retval = removeDatabaseConnection(configuration, (DatabaseConnection)userObject);
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
        List <String> retval = new ArrayList<String>();
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
                    } 

                    catch (Exception ex) {
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
            int pos2 = url.indexOf("/", pos1+ 3);
            
            if ((pos1 > -1) && (pos2 > -1) && (pos2 > pos1)) {
                
                if (includeProtocol) {
                    retval = retval.substring(0, pos2);
                } else {
                    retval = retval.substring(pos1+2, pos2);
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
                    retval = getObjectProperty(retval, propertyName.substring(pos+1));
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
                   String s = propertyName.substring(pos+1);
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
        }
        
        catch (Exception ex) {
            LOG.warn(ex.toString());
        }
        
        return retval;
    }
    
    
    public static  String buildSetMethodNameFromPropertyName(String propertyName) {
        return buildMethodNameFromPropertyName("set", propertyName);
    }

    public static  String buildGetMethodNameFromPropertyName(String propertyName) {
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
                retval = nm.substring(pos+1).toLowerCase().trim();
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

        HtmlRequestOp op = testop.addNewOperation().addNewHtmlRequestOperation();
        op.addNewRequestHeaders();
        op.addNewRequestParameters();
        boolean ispost = false;
        
        Iterator <Entry<String, String>> it = request.headers().iterator();
        
        while (it.hasNext()) {
            Entry <String, String> entry = it.next();
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
                FullHttpRequest fr = (FullHttpRequest)request;
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
            switch(optype.intValue()) {
                case TestOperationType.INT_HTTP_REQUEST:
                    populateHttpRequestOperation(retval, (HttpRequest)inputData);
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
        Iterator <Entry<String, String>> it = request.headers().iterator();
        while (it.hasNext()) {
            Entry entry = it.next();
            retval.append(entry.getKey());
            retval.append("=");
            retval.append(entry.getValue());
            retval.append("\r\n");
        }

        retval.append("--------------------------------\r\n");
        
        if (request instanceof DefaultFullHttpRequest) {
            DefaultFullHttpRequest fr = (DefaultFullHttpRequest)request;
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
                        } 
                        
                        catch (Exception ex) {
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
        for (TagHandler th :configuration.getTagHandlers().getHandlerArray()) {
            String key = null;
            if (StringUtils.isBlank(th.getApplication())) {
                key = ("all." + th.getTagName());
            } else {
                key = (th.getApplication() + "." + th.getTagName());
            }

            List <HtmlTagHandler> thl =  TAG_HANDLERS.get(key);
                 
            if (thl == null) {
                TAG_HANDLERS.put(key, thl = new ArrayList<HtmlTagHandler>());
            }

            try {
                HtmlTagHandler hth = (HtmlTagHandler)Class.forName(th.getHandlerClassName()).newInstance();
                hth.setTagHandler(th);
                thl.add(hth);
            } catch (Exception ex) {
                LOG.warn(ex.toString(), ex);
            }
        }
        
        // sort the handler so we hit most contrained matching test first the 
        // fall through to more generic tests
        for (List <HtmlTagHandler> l : TAG_HANDLERS.values()) {
            Collections.sort(l, new HtmlTagHandlerComparator());
        }
    }

    public static boolean isTagMatch(Node node, ParentTagMatch parentTagMatch, ChildTagMatch childTagMatch, TagMatchAttribute[] attributes) {
        boolean retval = true;
        
        if ((attributes != null) && (attributes.length > 0)) {
            for (TagMatchAttribute att : attributes) {
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
                                String s2 = attData.substring(pos+1);

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
            if (isChildTagMatchFailure(node, childTagMatch)) {
                retval = false;
            }
        }   
        
        if (retval) {
            if (isParentTagMatchFailure(node, parentTagMatch)) {
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
                Set <String> childTagNames = new HashSet<String>();

                
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
                                }
                                
                                // if retval is null then we found a match so break
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
            Set <String> parentTagNames = new HashSet<String>();

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
                if ((!limited || (cnt == totalCnt)) && isTagMatch(parent, tm.getParentTagMatch(), tm.getChildTagMatch(), tm.getMatchAttributes().getMatchAttributeArray())) {
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
        
        if (StringUtils.isNotBlank(searchDefinition)) {
            switch(searchDefinition.charAt(0)) {
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
        }
        
        return retval;
    }
    
    public static Node getMatchingSibling(TagMatcher tm, Node node) {
        Node retval = null;

        String searchDefinition = tm.getSearchDefinition();
        int startIndex = node.siblingIndex();
        int cnt = 0;
        
        switch(getSiblingNodeSearchDirection(searchDefinition)) {
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
                            if ((!limited || (cnt == targetCnt)) && isTagMatch(prev, tm.getParentTagMatch(), tm.getChildTagMatch(), tm.getMatchAttributes().getMatchAttributeArray())) {
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
                        targetCnt = Integer.parseInt(searchDefinition.substring(1));
                    } else {
                        limited = false;
                    }

                    
                    Node next = node.nextSibling();
                    
                    while ((next != null) && (cnt < targetCnt)) {
                        if (next.nodeName().equalsIgnoreCase(tm.getTagName())) {
                            cnt++;
                            if ((!limited || (cnt == targetCnt)) && isTagMatch(next, tm.getParentTagMatch(), tm.getChildTagMatch(), tm.getMatchAttributes().getMatchAttributeArray())) {
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
                                && isTagMatch(child, tm.getParentTagMatch(), tm.getChildTagMatch(), tm.getMatchAttributes().getMatchAttributeArray())) {
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
                    tm = (TagMatcher)tm.copy();
                    if (sdef.length() > 1) {
                        tm.setSearchDefinition("" + (getSiblingIndexByTagType(node) + Integer.parseInt(sdef.substring(1))));
                    } else {
                        tm.setSearchDefinition("" + (getSiblingIndexByTagType(node)+1));
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
            switch(tm.getMatchType().intValue()) {
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
                    if (isTagMatch(node, tm.getParentTagMatch(), tm.getChildTagMatch(), tm.getMatchAttributes().getMatchAttributeArray())) {
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
        
        List <HtmlTagHandler> handlerList = new ArrayList<HtmlTagHandler>();
        
        // add the application specific handlers first
        List <HtmlTagHandler> thl = TAG_HANDLERS.get(app + "." + node.nodeName().trim().toLowerCase());
        
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
        
        if (retval == null) {
            if (isHtmlContainer(node)) {
                retval = new DefaultContainerTagHandler();
            }
        }
        
        return retval;
    }
    
    public static boolean isHtmlContainer(Node node) {
        return Constants.DEFAULT_HTML_CONTAINER_TAGS.contains(node.nodeName().toLowerCase().trim());
    }
    
    public static String[] getValidTestTypesForPlatform(Platform platform) {
        List <String> retval = new ArrayList<String>();
        String[] testTypes = Utils.getXmlEnumerations(TestType.class);

        for (String testType : testTypes) {
            if (TestType.DATABASE.toString().equals(testType)) {
                if (StringUtils.isNotBlank(platform.getDatabaseConnectionName())) {
                    retval.add(testType);
                }
            } else if (TestType.WEB_SERVICE.toString().equals(testType)) {
                if (StringUtils.isNotBlank(platform.getWebServiceUrl())) {
                    retval.add(testType);
                }
            } else {
                retval.add(testType);
            }
        }
        
        return retval.toArray(new String[retval.size()]);
    }

    public static String[] getValidCheckpointTypesForPlatform(Platform platform) {
        List <String> retval = new ArrayList<String>();
        String[] checkpointTypes = Utils.getXmlEnumerations(CheckpointType.class);

        for (String checkpointType : checkpointTypes) {
            if (CheckpointType.SQL.toString().equals(checkpointType)) {
                if (StringUtils.isNotBlank(platform.getDatabaseConnectionName())) {
                    retval.add(checkpointType);
                }
            } else if (CheckpointType.WEB_SERVICE.toString().equals(checkpointType)) {
                if (StringUtils.isNotBlank(platform.getWebServiceUrl())) {
                    retval.add(checkpointType);
                }
            } else {
                retval.add(checkpointType);
            }
        }
        
        return retval.toArray(new String[retval.size()]);
    }

    public static String buildCheckpointSectionName(HtmlTagHandler th, Node node) {
        StringBuilder retval = new StringBuilder(128);
        
        String subSectionName = th.getSubSectionName(node);
        String sectionName = th.getSectionName(node);
        String subSectionAdditional = th.getSubSectionAdditional(node);

        retval.append("<html>");
        
        boolean haveSection = StringUtils.isNotBlank(sectionName);
        if (haveSection) {
            retval.append("<span style='color: #000099; font-weight: 700; white-space: nowrap;'>");
            retval.append(sectionName);
            retval.append("");
        }
        
        if (StringUtils.isNotBlank(subSectionName)) {
            if (haveSection) { 
                retval.append(": </span>");
            }
            retval.append(subSectionName);
        } else if (haveSection) {
           retval.append("</span>");
        }

        if (StringUtils.isNotBlank(subSectionAdditional)) {
            retval.append(" - ");
            retval.append(subSectionAdditional);
        }
        
        retval.append("</html>");
        return retval.toString();
    }
    
    public static boolean isValidContainerNode(Node node) {
        boolean retval = true;
        
        if (node.childNodeSize() == 1) {
            if (Constants.HTML_TEXT_NODE_NAME.equals(node.childNode(0).nodeName())) {
                retval = false;
            }
        }
        
        return retval;
    }
}
