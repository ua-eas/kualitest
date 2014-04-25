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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import javax.swing.tree.DefaultMutableTreeNode;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.test.DatabaseConnection;
import org.kuali.test.KualiTestConfigurationDocument;
import org.kuali.test.Platform;
import org.kuali.test.SuiteTest;
import org.kuali.test.TestHeader;
import org.kuali.test.TestSuite;


public class Utils {
    private static final Logger LOG = Logger.getLogger(Utils.class);
    public static String ENUM_CHILD_CLASS = "$Enum";
    
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
        List <String> retval = new ArrayList<String>();
        Field[] fields = clazz.getDeclaredFields();
        
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
                Method m = o.getClass().getMethod(buildGetMethodNameFromPropertyName(propertyName));
                retval = m.invoke(o, (Class)null);
            } catch (Exception ex) {
                LOG.warn(ex.toString());
            } 
        }

        return retval;
    }

    public static void setObjectProperty(Object o, String propertyName, Object value) {
        if (o != null) {
            try {
                Method m = o.getClass().getMethod(buildSetMethodNameFromPropertyName(propertyName));
                m.invoke(o, value);
            } catch (Exception ex) {
                LOG.warn(ex.toString());
            } 
        }
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
}
