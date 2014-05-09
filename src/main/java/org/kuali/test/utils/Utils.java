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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.swing.tree.DefaultMutableTreeNode;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.StringEnumAbstractBase;
import org.apache.xmlbeans.XmlOptions;
import org.htmlcleaner.TagNode;
import org.kuali.test.DatabaseConnection;
import org.kuali.test.HtmlRequestOp;
import org.kuali.test.KualiTestConfigurationDocument;
import org.kuali.test.KualiTestDocument;
import org.kuali.test.KualiTestDocument.KualiTest;
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
                    retval = findPlatform(configuration, testSuite.getPlatformName());;
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
        
        op.setMethod("method");
        op.setUri("uri");
        
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
        
        if (StringUtils.isNotBlank(input)) {
            retval = StringEscapeUtils.unescapeHtml4(input).trim();  
        }
        
        return retval;
    }

    public static String formatDisplayName(String name) {
        String retval = name;
        
        if (StringUtils.isNotBlank(name)) {
            String s = name.trim();
            if (s.endsWith(":")) {
                retval = s.substring(0, s.length()-1);
            }
        }
        
        return retval;
    }
    
    public static File getTestRunnerConfigurationFile(KualiTestConfigurationDocument.KualiTestConfiguration configuration) {
        return new File(configuration.getRepositoryLocation() + "/" + Constants.TEST_RUNNER_CONFIG_FILENAME);
    }
    
    public static void initializeHtmlTagHandlers(KualiTestConfigurationDocument.KualiTestConfiguration configuration) {
        for (TagHandler th :configuration.getTagHandlers().getHandlerArray()) {
            List <HtmlTagHandler> thl = TAG_HANDLERS.get(th.getTagName());

            if (thl == null) {
                TAG_HANDLERS.put(th.getTagName(), thl = new ArrayList<HtmlTagHandler>());
            }

            try {
                HtmlTagHandler hth = (HtmlTagHandler)Class.forName(th.getHandlerClassName()).newInstance();
                hth.setTagHandler(th);
                thl.add(hth);
            } catch (Exception ex) {
                LOG.warn(ex.toString(), ex);
            }
        }
    }

    public static boolean isTagMatch(TagNode tag, TagMatchAttribute[] attributes) {
        boolean retval = true;
        
        for (TagMatchAttribute att : attributes) {
            String tagatt = tag.getAttributeByName(att.getName());

            if ((tagatt == null) || !att.getValue().equals(tagatt)) {
                retval = false;
                break;
            }
        }

        return retval;
    }
    
    public static TagNode getMatchingChild(TagMatcher tm, TagNode tag) {
        TagNode retval = null;
    
        for (TagNode child : tag.getChildTags()) {
            if (child.getName().equalsIgnoreCase(tm.getTagName())) {
                if (isTagMatch(child, tm.getMatchAttributes().getMatchAttributeArray())) {
                    retval = child;
                    break;
                }
            }
        }
        
        return retval;
    }
    
    public static TagNode getMatchingParent(TagMatcher tm, TagNode tag) {
        TagNode retval = null;
    
        TagNode parent = tag.getParent();
        
        while (parent != null) {
            if (parent.getName().equalsIgnoreCase(tm.getTagName())) {
                if (isTagMatch(parent, tm.getMatchAttributes().getMatchAttributeArray())) {
                    retval = parent;
                    break;
                }
            }
            
            parent = parent.getParent();
        }
        
        return retval;
    }

    public static TagNode getPreviousMatchingSibling(TagMatcher tm, TagNode tag) {
        TagNode retval = null;
    
        TagNode parent = tag.getParent();
        
        while (parent != null) {
            int indx = parent.getChildIndex(tag);
            
            for (int i = 0; i < indx; ++i) {
                TagNode sibling = parent.getChildTags()[i];
                if (sibling.getName().equalsIgnoreCase(tm.getTagName())) {
                    if (isTagMatch(sibling, tm.getMatchAttributes().getMatchAttributeArray())) {
                        retval = sibling;
                        break;
                    }
                }
            }
        }
        
        return retval;
    }

    public static TagNode getNextMatchingSibling(TagMatcher tm, TagNode tag) {
        TagNode retval = null;
    
        TagNode parent = tag.getParent();
        
        while (parent != null) {
            int indx = parent.getChildIndex(tag);
            
            for (int i = indx+1; i < parent.getChildTags().length; ++i) {
                TagNode sibling = parent.getChildTags()[i];
                if (sibling.getName().equalsIgnoreCase(tm.getTagName())) {
                    if (isTagMatch(sibling, tm.getMatchAttributes().getMatchAttributeArray())) {
                        retval = sibling;
                        break;
                    }
                }
            }
        }
        
        return retval;
    }
    
    public static String getLabelText(TagMatcher labelMatcher, TagNode tag) {
        String retval = "";
        TagNode labelNode = getMatchingTagNode(labelMatcher, tag);
        
        if (labelNode != null) {
            retval = tag.getAttributeByName("value");
            
            if (StringUtils.isBlank(retval)) {
                retval = cleanDisplayText(tag.getText().toString());
            }
        }
        
        return retval;
    }
    
    public static TagNode getMatchingTagNode(TagMatcher tm, TagNode tag) {
        TagNode retval = null;
        
        switch(tm.getMatchType().intValue()) {
            case TagMatchType.INT_CURRENT:
                if (isTagMatch(tag, tm.getMatchAttributes().getMatchAttributeArray())) {
                   retval = tag;
                }
                break;
            case TagMatchType.INT_CHILD:
                retval = getMatchingChild(tm, tag);
                break;
            case TagMatchType.INT_PARENT:
                retval = getMatchingParent(tm, tag);
                break;
            case TagMatchType.INT_PREVIOUS_SIBLING:
                retval = getPreviousMatchingSibling(tm, tag);
                break;
            case TagMatchType.INT_NEXT_SIBLING:
                retval = getNextMatchingSibling(tm, tag);
                break;
        }

        return retval;
    }
    
    public static HtmlTagHandler getHtmlTagHandler(TagNode tag) {
        HtmlTagHandler retval = null;
        
        List <HtmlTagHandler> thl = TAG_HANDLERS.get(tag.getName().toLowerCase());
        
        if (thl != null) {
            for (HtmlTagHandler hth : thl) {
                boolean match = true;
                TagHandler th = hth.getTagHandler();
                if (th.getTagMatchers() != null) {
                    for (TagMatcher tm : th.getTagMatchers().getTagMatcherArray()) {
                        if (getMatchingTagNode(tm, tag) == null) {
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
}
