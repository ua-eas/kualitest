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

package org.kuali.test.proxyserver;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Queue;
import javax.net.ssl.SSLEngine;
import org.apache.log4j.Logger;
import org.kuali.test.Platform;
import org.kuali.test.utils.Constants;
import org.kuali.test.utils.Utils;
import org.littleshoot.proxy.ChainedProxy;
import org.littleshoot.proxy.ChainedProxyManager;
import org.littleshoot.proxy.HttpFilters;
import org.littleshoot.proxy.HttpFiltersSourceAdapter;
import org.littleshoot.proxy.TransportProtocol;
import org.littleshoot.proxy.extras.SelfSignedSslEngineSource;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;


public class TestProxyServer {
    private static final Logger LOG = Logger.getLogger(TestProxyServer.class);
    private DefaultHttpProxyServer proxyServer;
    private Platform platform;
    private boolean running = false;
    private InetSocketAddress targetAddress;
    private InetSocketAddress localAddress;

    public TestProxyServer(Platform platform) {
        this.platform = platform;

        try {
            targetAddress = new InetSocketAddress(Utils.getHostFromUrl(platform.getWebUrl(), true), 80);
            localAddress = new InetSocketAddress(InetAddress.getLocalHost(), Constants.TEST_PROXY_SERVER_PORT);
            initializeProxyServer();
            running = true;
        }
        
        catch (Exception ex) {
            LOG.error(ex.toString(), ex);
        }
    }
    
    private HttpFilters getHttpFilters() {
        return new HttpFilters() {
            @Override
            public HttpResponse requestPre(HttpObject ho) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("requestPre:" + ho.getDecoderResult().toString());
                }
                return null;
            }

            @Override
            public HttpResponse requestPost(HttpObject ho) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("requestPost:" + ho.getDecoderResult().toString());
                }
                return null;
            }

            @Override
            public HttpObject responsePre(HttpObject ho) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("responsePre:" + ho.getDecoderResult().toString());
                }
                return ho;
            }

            @Override
            public HttpObject responsePost(HttpObject ho) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("responsePost:" + ho.getDecoderResult().toString());
                }
                
                return ho;
            }
        };
    }
    
    private HttpFiltersSourceAdapter getHttpFiltersSourceAdapter() {
        final HttpFilters filters = getHttpFilters();

        return new HttpFiltersSourceAdapter() {
            @Override
            public int getMaximumResponseBufferSizeInBytes() {
                return Constants.PROXY_RESPONSE_FILTER_MAX_SIZE;
            }

            @Override
            public int getMaximumRequestBufferSizeInBytes() {
                return Constants.PROXY_RESPONSE_FILTER_MAX_SIZE;
            }

            @Override
            public HttpFilters filterRequest(HttpRequest originalRequest, ChannelHandlerContext ctx) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("request uri: " + originalRequest.getUri());
                }
                return filters;
            }

            @Override
            public HttpFilters filterRequest(HttpRequest originalRequest) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("request uri: " + originalRequest.getUri());
                }
                return filters;
            }
        };
    }
    
    private ChainedProxy getChainedProxy() {
        return new ChainedProxy() {
            @Override
            public InetSocketAddress getChainedProxyAddress() {
                return targetAddress;
            }

            @Override
            public InetSocketAddress getLocalAddress() {
                return localAddress;
            }

            @Override
            public TransportProtocol getTransportProtocol() {
                return TransportProtocol.TCP;
            }

            @Override
            public boolean requiresEncryption() {
                return true;
            }

            @Override
            public void filterRequest(HttpObject ho) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("filterRequest: " + ho.getDecoderResult().toString());
                }
            }

            @Override
            public void connectionSucceeded() {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("proxy server connection succeeded");
                }
            }

            @Override
            public void connectionFailed(Throwable t) {
                LOG.warn(t.toString(), t);
            }

            @Override
            public void disconnected() {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("proxy server disconnected");
                }
            }

            @Override
            public SSLEngine newSslEngine() {
                return new SelfSignedSslEngineSource(true).newSslEngine();            
            }
        };
    }
    
    private ChainedProxyManager getChainProxyManager() {
        return new ChainedProxyManager() {
            @Override
            public void lookupChainedProxies(HttpRequest hr, Queue<ChainedProxy> queue) {
                queue.add(getChainedProxy());
            }
        };
    }
    
    private void initializeProxyServer() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initializing proxy server");
        }
        
        DefaultHttpProxyServer
            .bootstrap()
            .withPort(Constants.TEST_PROXY_SERVER_PORT)
            .withChainProxyManager(getChainProxyManager())
            .withFiltersSource(getHttpFiltersSourceAdapter())
            .withAllowLocalOnly(true)
            .start();
   
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("proxy server started");
        }
    }

    public void stop() {
        proxyServer.stop();
        running = false;
    }
    
    public boolean isRunning() {
        return running;
    }
}
