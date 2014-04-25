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

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.kuali.test.Platform;
import org.kuali.test.utils.Constants;
import org.kuali.test.utils.Utils;
import org.littleshoot.proxy.ChainProxyManager;
import org.littleshoot.proxy.DefaultHttpFilter;
import org.littleshoot.proxy.DefaultHttpProxyServer;
import org.littleshoot.proxy.HandshakeHandler;
import org.littleshoot.proxy.HandshakeHandlerFactory;
import org.littleshoot.proxy.HttpFilter;
import org.littleshoot.proxy.HttpRequestFilter;
import org.littleshoot.proxy.HttpRequestMatcher;
import org.littleshoot.proxy.HttpResponseFilter;
import org.littleshoot.proxy.HttpResponseFilters;


public class TestProxyServer {
    private static final Logger LOG = Logger.getLogger(TestProxyServer.class);
    private DefaultHttpProxyServer proxyServer;
    private Platform platform;

    public TestProxyServer(Platform platform) {
        this.platform = platform;
        
        try {
            initializeProxyServer();
        }
        
        catch (Exception ex) {
            LOG.error(ex);
        }
    }
    
    private HttpResponseFilter getResponseFilter() {
        HttpResponseFilter retval = new HttpResponseFilter() {
            @Override
            public HttpResponse filterResponse(HttpRequest request, HttpResponse response) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Http Response Filter");
                    LOG.debug("-----------------------------------------");
                    LOG.debug("status: " + response.getStatus());
                }
                
                return response;
            }
        };

        return retval;
    }

    private HttpRequestMatcher getRequestMatcher() {
        HttpRequestMatcher retval = new HttpRequestMatcher() {
            public boolean shouldFilterResponses(HttpRequest httpRequest) {
                return ((httpRequest != null) && (httpRequest.getUri().contains("kfs-dev")));
            }

            @Override
            public boolean filterResponses(HttpRequest hr) {
                return true;
            }
        };
        
        return retval;
    }

    private HttpFilter getHttpFilter() {
        HttpFilter retval = new DefaultHttpFilter(getResponseFilter(), getRequestMatcher()) {
            @Override
            public int getMaxResponseSize() {
                return Constants.PROXY_RESPONSE_FILTER_MAX_SIZE;
            }

            @Override
            public boolean filterResponses(HttpRequest hr) {
                return true;
            }
        };
        
        return retval;
    }
    
    private HttpResponseFilters getResponseFilters() {
        final HttpFilter httpFilter = getHttpFilter();
        HttpResponseFilters retval =  new HttpResponseFilters() {
            @Override
            public HttpFilter getFilter(String string) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("getFilter(): " + string);
                }
                
                return httpFilter;
            }
        };

        return retval;
    }

    private HttpRequestFilter getRequestFilter() {
        HttpRequestFilter retval = new HttpRequestFilter() {
            @Override
            public void filter(final HttpRequest request) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Http Request Filter");
                    LOG.debug("-----------------------------------------");
                    LOG.debug("uri: " + request.getUri());
                }
            }
        };

        return retval;
    }

    private ChainProxyManager getChainProxyManager() {
        ChainProxyManager retval = new ChainProxyManager() {
            @Override
            public String getChainProxy(HttpRequest hr) {
                return Utils.getHostFromUrl(platform.getWebUrl(), true);
            }

            @Override
            public void onCommunicationError(String error) {
                LOG.warn(error);
            }
        };
        
        return retval;
    }
    
    private HandshakeHandlerFactory getHandshakeHandlerFactory() {
        HandshakeHandlerFactory retval = new HandshakeHandlerFactory() {
            @Override
            public HandshakeHandler newHandshakeHandler() {
                return new HandshakeHandler() {

                    @Override
                    public ChannelHandler getChannelHandler() {
                        return new SimpleChannelHandler();
                    }

                    @Override
                    public String getId() {
                        return "100";
                    }
                };
            }
        };
            
            return retval;
    }
    
    private void initializeProxyServer() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initializing proxy server");
        }
    /*
        proxyServer = new DefaultHttpProxyServer(
            Constants.TEST_PROXY_SERVER_PORT,
            getResponseFilters(), 
            getChainProxyManager(), 
            getHandshakeHandlerFactory(), 
            getRequestFilter());
    */
        proxyServer = new DefaultHttpProxyServer(
            Constants.TEST_PROXY_SERVER_PORT,
            getRequestFilter(),
            getResponseFilters());

        proxyServer.start(true, true);
                
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("proxy server started");
        }
    }

    public void stop() {
        proxyServer.stop();
    }
}
