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

import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.ui.utils.UIUtils;


public class WindowsRegistryProxyHandler {
    private static final Logger LOG = Logger.getLogger(WindowsRegistryProxyHandler.class);
    private static final String PROXY_ENABLE_QUERY = "REG QUERY \"HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings\" /v ProxyEnable";
    private static final String PROXY_SERVER_QUERY = "REG QUERY \"HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings\" /v ProxyServer";
    private static final String PROXY_ENABLE_ADD = "REG ADD \"HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings\" /v ProxyEnable /t REG_DWORD /d ? /f";
    private static final String PROXY_SERVER_ADD = "REG ADD \"HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings\" /v ProxyServer /t REG_SZ /d ? /f";
    private static final String PROXY_SERVER_DELETE = "REG DELETE \"HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings\" /v ProxyServer /f";

    private String saveProxyServer;
    private int saveProxyEnable;
    private boolean testProxySet = false;
    private TestCreator mainframe;
    
    public  WindowsRegistryProxyHandler(TestCreator mainframe, String server, String port) {
        this.mainframe = mainframe;
        if (mainframe.getConfiguration().getAutoUpdateWindowsRegistryForProxy()) {
            try {
                saveProxyServer = getCurrentProxyServer();
                saveProxyEnable = getCurrentProxyEnable();
                setProxyEnable(1);
                setProxyServer(server + ":" + port);
                testProxySet = true;
            } 

            catch (Exception ex) {
                UIUtils.showError(mainframe, "Proxy Error", "Error occurred while attempting to configure test proxy - " + ex.toString());
                LOG.error(ex.toString(), ex);
            }
        }
    }
    
    private String getCurrentProxyServer() throws Exception {
        String retval = null;
        
        Process p = Runtime.getRuntime().exec(PROXY_SERVER_QUERY);
        String results = getResults(p.getInputStream());

        if (StringUtils.isNotBlank(results) && results.contains("REG_SZ")) {
            int pos = (results.indexOf("REG_SZ") + "REG_SZ".length() + 1);
            retval = results.substring(pos).trim();
        }
        
        return retval;
    }
    
    private int getCurrentProxyEnable() throws Exception {
        int retval = 0;

        Process p = Runtime.getRuntime().exec(PROXY_ENABLE_QUERY);
        String results = getResults(p.getInputStream());
        
        if (StringUtils.isNotBlank(results) && results.contains("REG_DWORD")) {
            int pos = (results.indexOf("REG_DWORD") + "REG_DWORD".length() + 1);

            if (results.substring(pos).trim().contains("1")) {
                retval = 1;
            } 
        }
        
        return retval;
    }
    
    private void setProxyEnable(int enable) throws Exception {
        if ( Runtime.getRuntime().exec(PROXY_ENABLE_ADD.replace("?", "" + enable)).waitFor() != 0) {
            throw new Exception("add proxy enable (" + enable + ") failed");
        }
    }
    
    private void setProxyServer(String server) throws Exception {
        if (Runtime.getRuntime().exec(PROXY_SERVER_ADD.replace("?", server)).waitFor() != 0) {
            throw new Exception("add proxy server (" + server + ") failed");
        }
    }

    private String getResults(InputStream is) throws IOException {
        StringBuilder retval = new StringBuilder(256);
        
        int c = 0;
        
        while((c = is.read()) > -1) {
            retval.append((char)c);
        }
        
        return retval.toString();
    }
    
    public void resetProxy() {
        if (mainframe.getConfiguration().getAutoUpdateWindowsRegistryForProxy()) {
            if (testProxySet) {
                try {
                    if (saveProxyEnable == 0) {
                        deleteProxyServer();
                        setProxyEnable(0);
                    } else {
                        setProxyServer(saveProxyServer);
                    }
                    
                    
                    testProxySet = false;
                } 

                catch (Exception ex) {
                    UIUtils.showError(mainframe, "Proxy Error", "Error occurred reseting proxy to original values");
                    LOG.error(ex.toString(), ex);
                }
            }
        }
    }
    
    private void deleteProxyServer() throws Exception {
        if (Runtime.getRuntime().exec(PROXY_SERVER_DELETE).waitFor() != 0) {
            throw new Exception("delete proxy server failed");
        }
    }
}
