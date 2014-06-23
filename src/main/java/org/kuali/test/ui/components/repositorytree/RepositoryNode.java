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

package org.kuali.test.ui.components.repositorytree;

import java.io.File;
import java.io.IOException;
import javax.swing.tree.DefaultMutableTreeNode;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.kuali.test.KualiTestConfigurationDocument;
import org.kuali.test.Platform;
import org.kuali.test.SuiteTest;
import org.kuali.test.TestSuite;

/**
 *
 * @author rbtucker
 */
public class RepositoryNode extends DefaultMutableTreeNode {

    /**
     *
     */
    protected static Logger LOG = Logger.getLogger(RepositoryNode.class);
    private KualiTestConfigurationDocument.KualiTestConfiguration configuration;
    
    // if platforms are passed in then this is root - handle a little differently

    /**
     *
     * @param configuration
     * @param userObject
     */
        public RepositoryNode(KualiTestConfigurationDocument.KualiTestConfiguration configuration, Object userObject) {
        super(userObject);
        this.configuration = configuration;
        
        try {
            if (isRoot()) {
                if (configuration.getPlatforms() != null) {
                    for (Platform platform : configuration.getPlatforms().getPlatformArray()) {
                        addNode(getPlatformFilePath(platform), platform, true);
                    }
                }
            } else if (userObject instanceof Platform) {
                Platform platform = (Platform)userObject;
                if (platform.getTestSuites() != null) {
                    for (TestSuite testSuite : platform.getTestSuites().getTestSuiteArray()) {
                        addNode(getTestSuiteFilePath(testSuite), testSuite, true);
                    }
                }
            } else if (userObject instanceof TestSuite) {
                TestSuite testSuite = (TestSuite)userObject;
                Platform platform = findPlatform(testSuite.getPlatformName());
                if (platform != null) {
                    if (testSuite.getSuiteTests() != null) {
                        for (SuiteTest suiteTest : testSuite.getSuiteTests().getSuiteTestArray()) {
                            File f = new File(suiteTest.getTestHeader().getTestFileName());
                            if (f.exists() && f.isFile()) {
                                add(new RepositoryNode(configuration, suiteTest));
                            }
                        }
                    }
                }
            }
        }
        
        catch (IOException ex) {
            LOG.error(ex.toString(), ex);
        }
    }

    private Platform findPlatform(String platformName) {
        Platform retval = null;
        for (Platform platform : configuration.getPlatforms().getPlatformArray()) {
            if (platform.getName().equalsIgnoreCase(platformName)) {
                retval = platform;
                break;
            }
        }
        return retval;
    }
    
    private void addNode(String filepath, Object userObject, boolean createDirIfRequired) throws IOException {
        File f = new File(filepath);
        if (!f.exists()) {
            FileUtils.forceMkdir(f);
        } 

        if (f.exists() && f.isDirectory()) {
            add(new RepositoryNode(configuration, userObject));
        }
    }
    
    private String getPlatformFilePath(Platform platform) {
        StringBuilder retval = new StringBuilder(256);
        retval.append(configuration.getRepositoryLocation());
        retval.append("/");
        retval.append(platform.getName());
        return retval.toString();
    }
    
    private String getTestSuiteFilePath(TestSuite testSuite) {
        StringBuilder retval = new StringBuilder(256);
        retval.append(configuration.getRepositoryLocation());
        retval.append("/");
        retval.append(testSuite.getPlatformName());
        retval.append("/");
        retval.append(testSuite.getName());
        return retval.toString();
    }


    @Override
    public boolean isLeaf() {
        return (getUserObject() instanceof SuiteTest);
    }

    @Override
    public boolean isRoot() {
        return(getUserObject() == null);
    }
    
    @Override
    public String toString() {
        String retval = "unknown";

        if (isRoot()) {
            File f = new File(configuration.getRepositoryLocation());
            retval = f.getName();
        } else {
            Object o = getUserObject();

            if (o != null) {
                try {
                    if (o instanceof SuiteTest) {
                        SuiteTest test = (SuiteTest)o;
                        retval = test.getTestHeader().getTestName();
                    } else {
                        retval = BeanUtils.getProperty(o, "name");
                    }
                }
                
                catch (Exception ex) {
                    LOG.warn(ex);
                }
            }
        }
        
        return retval;
    }
}
