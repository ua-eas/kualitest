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
package org.kuali.test.ui.components.panels;

import chrriis.dj.nativeswing.NativeSwing;
import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;
import java.awt.BorderLayout;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import org.kuali.test.Platform;
import org.kuali.test.TestHeader;
import org.kuali.test.proxyserver.TestProxyServer;
import static org.kuali.test.ui.components.panels.BaseCreateTestPanel.LOG;
import org.kuali.test.utils.Utils;

public class WebBrowserPanel extends BaseCreateTestPanel implements ContainerListener {
    private JWebBrowser webBrowser;
    private TestProxyServer testProxyServer;
    
    public WebBrowserPanel(Platform platform, TestHeader testHeader) {
        super(platform, testHeader);

        initializeNativeBrowser();
        testProxyServer = new TestProxyServer(platform);
        addContainerListener(this);
    
        webBrowser = new JWebBrowser();
        webBrowser.setButtonBarVisible(false);
        webBrowser.setLocationBarVisible(false);
        webBrowser.setMenuBarVisible(false);
        webBrowser.navigate("http://localhost:8888/" + Utils.getContextFromUrl(getPlatform().getWebUrl()));
        add(webBrowser, BorderLayout.CENTER);
    }

    @Override
    protected void handleCreateCheckpoint() {
        if (webBrowser != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(webBrowser.getHTMLContent());
            }
        }
    }

    @Override
    protected void handleSaveTest() {
    }
    
    private void initializeNativeBrowser() {
        if (!NativeInterface.isInitialized()) {
            NativeSwing.initialize();
        }
        
        if (!NativeInterface.isOpen()) {
            NativeInterface.open();
        }
    }
    
    public void browserRemoved() {
        try {
            if (webBrowser != null) {
                webBrowser.disposeNativePeer(false);
            }
            if (NativeInterface.isOpen()) {
                NativeInterface.close();
            }

            if (testProxyServer != null) {
                testProxyServer.stop();
            }
        }
        
        catch (Exception ex) {
            LOG.warn(ex.toString());
        }
    }
    
    @Override
    public void componentAdded(ContainerEvent e) {
    }

    @Override
    public void componentRemoved(ContainerEvent e) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("component removed: " + e.getComponent().getClass().getName());
        }

        if ( e.getComponent() instanceof JWebBrowser) {
            browserRemoved();
        }
    }
 }
