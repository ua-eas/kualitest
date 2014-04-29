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
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserAdapter;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserWindowWillOpenEvent;
import io.netty.handler.codec.http.HttpRequest;
import java.awt.BorderLayout;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.util.List;
import org.kuali.test.Platform;
import org.kuali.test.TestHeader;
import org.kuali.test.proxyserver.TestProxyServer;
import org.kuali.test.ui.components.splash.SplashDisplay;
import org.kuali.test.ui.utils.UIUtils;

public class RootWebBrowserPanel extends BaseCreateTestPanel implements ContainerListener {
    private JWebBrowser webBrowser;
    private TestProxyServer testProxyServer;
    private TabbedTestPane tabbedPane;
    
    public RootWebBrowserPanel(TabbedTestPane tabbedPane, Platform platform, TestHeader testHeader) {
        super(platform, testHeader);
        this.tabbedPane = tabbedPane;
        getStartTest().setEnabled(false);

        new SplashDisplay(UIUtils.findWindow(tabbedPane), "Initializing Web Test", "Loading web proxy server...") {
            @Override
            protected void runProcess() {
                testProxyServer = new TestProxyServer();
                getStartTest().setEnabled(true);
            }
        };

        initializeNativeBrowser();
        initComponents();
    }

    private void initComponents() {
        addContainerListener(this);
        webBrowser = createWebBrowser();
        add(webBrowser, BorderLayout.CENTER);
    }

    private JWebBrowser createWebBrowser() {
        JWebBrowser retval = new JWebBrowser();
        retval.setButtonBarVisible(false);
        retval.setLocationBarVisible(false);
        retval.setMenuBarVisible(false);
        retval.setStatusBarVisible(true);

        retval.addWebBrowserListener(new WebBrowserAdapter() {
            @Override
            public void windowWillOpen(WebBrowserWindowWillOpenEvent e) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("in windowWillOpen()");
                }
                
                JWebBrowser wb = createWebBrowser();
                e.setNewWebBrowser(wb);
                tabbedPane.addNewBrowserPanel(wb);
            }
        });

        return retval;
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
    protected void handleCancelTest() {
        testProxyServer.getTestRequests().clear();
    }

    @Override
    protected void handleStartTest() {
        webBrowser.navigate(getPlatform().getWebUrl());
    }

    @Override
    protected void handleSaveTest() {
        List<HttpRequest> testRequests = testProxyServer.getTestRequests();
        if (LOG.isDebugEnabled()) {
            LOG.debug("num requests: " + testRequests.size());
        }
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
        } catch (Exception ex) {
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

        if (e.getComponent() instanceof JWebBrowser) {
            browserRemoved();
        }
    }
}
