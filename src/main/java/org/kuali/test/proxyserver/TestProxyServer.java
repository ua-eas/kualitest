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

import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Queue;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.SystemProperties;
import org.kuali.test.utils.Constants;
import org.littleshoot.proxy.ChainedProxy;
import org.littleshoot.proxy.ChainedProxyManager;
import org.littleshoot.proxy.HttpFilters;
import org.littleshoot.proxy.HttpFiltersAdapter;
import org.littleshoot.proxy.HttpFiltersSource;
import org.littleshoot.proxy.HttpFiltersSourceAdapter;
import org.littleshoot.proxy.extras.SelfSignedMitmManager;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;

public class TestProxyServer {
    private static final Logger LOG = Logger.getLogger(TestProxyServer.class);
    private DefaultHttpProxyServer proxyServer;
    private List<HttpRequest> testRequests = new ArrayList<HttpRequest>();
    private boolean proxyServerRunning = false;
    
    
    public TestProxyServer() {
        try {
            Thread.sleep(1000);
            initializeProxyServer();
        } catch (Exception ex) {
            LOG.error(ex.toString(), ex);
        }
    }

    private HttpFiltersSource getHttpFiltersSource() {
          return new HttpFiltersSourceAdapter() {
            @Override
            public HttpFilters filterRequest(HttpRequest originalRequest) {
                return new HttpFiltersAdapter(originalRequest) {
                    @Override
                    public HttpResponse requestPre(HttpObject httpObject) {
                        HttpResponse retval = null;
                        if (LOG.isTraceEnabled()) {
                            LOG.trace("requestPre: " + originalRequest.toString());
                        }
                        
                        if (isValidTestRequest(originalRequest)) {
                            testRequests.add(originalRequest);
                        }
                        
                        return retval;
                    }

                    @Override
                    public HttpResponse requestPost(HttpObject httpObject) {
                        HttpResponse retval = null;
                        if (LOG.isTraceEnabled()) {
                            LOG.trace("requestPost: " + originalRequest.toString());
                        }

                        return retval;
                    }

                    @Override
                    public HttpObject responsePre(HttpObject httpObject) {
                        if (LOG.isTraceEnabled()) {
                            LOG.trace("responsePre: " + originalRequest.toString());
                        }

                        if (httpObject instanceof HttpResponse) {
          //                  responsePreOriginalRequestMethodsSeen
            //                    .add(originalRequest.getMethod());
                        } else if (httpObject instanceof HttpContent) {
              //              responsePreBody.append(((HttpContent) httpObject)
                //                .content().toString(
                  //                  Charset.forName("UTF-8")));
                        }
                        return httpObject;
                    }

                    @Override
                    public HttpObject responsePost(HttpObject httpObject) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("responsePost: " + originalRequest.toString());
                        }
                        if (httpObject instanceof HttpResponse) {
//                            responsePostOriginalRequestMethodsSeen
  //                              .add(originalRequest.getMethod());
                        } else if (httpObject instanceof HttpContent) {
    //                        responsePostBody.append(((HttpContent) httpObject)
      //                          .content().toString(
        //                            Charset.forName("UTF-8")));
                        }
                        return httpObject;
                    }
                };
            }

            public int getMaximumRequestBufferSizeInBytes() {
                return Constants.MAX_REQUEST_BUFFER_SIZE;
            }

            public int getMaximumResponseBufferSizeInBytes() {
                return Constants.MAX_RESPONSE_BUFFER_SIZE;
            }
        };
    }

    private ChainedProxyManager getChainProxyManager() {
        return new ChainedProxyManager() {
            @Override
            public void lookupChainedProxies(HttpRequest httpRequest, Queue<ChainedProxy> chainedProxies) {
                for (ChainedProxy cp : chainedProxies) {
                }
            }

        };
    }

    private void initializeProxyServer() {
        // proxy port and host should be passed in as vm params
        String proxyHost = SystemProperties.getProperty("network.proxy_host", Constants.DEFAULT_PROXY_HOST);
        String proxyPort = SystemProperties.getProperty("network.proxy_port", Constants.DEFAULT_PROXY_PORT);
        if (LOG.isDebugEnabled()) {
            LOG.debug("initializing proxy server");
            LOG.debug("network.proxy_host: " + proxyHost);
            LOG.debug("network.proxy_port: " + proxyPort);
        }

        DefaultHttpProxyServer
            .bootstrap()
            .withPort(Integer.parseInt(proxyPort))
            .withAddress(new InetSocketAddress(proxyHost, Integer.parseInt(proxyPort)))
            .withChainProxyManager(getChainProxyManager())
            .withFiltersSource(getHttpFiltersSource())
            .withManInTheMiddle(new SelfSignedMitmManager())
            .withAllowLocalOnly(true)
            .start();

        proxyServerRunning = true;
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("proxy server started");
        }
    }

    public void stop() {
        proxyServer.stop();
        proxyServerRunning = false;
    }

    public boolean isProxyServerRunning() {
        return proxyServerRunning;
    }

    public List<HttpRequest> getTestRequests() {
        return testRequests;
    }
    
    protected boolean isValidTestRequest(HttpRequest request) {
        boolean retval = false;
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("uri: " + request.getUri());
            LOG.debug("method: " + request.getMethod().toString());
            LOG.debug("protocol version: " + request.getProtocolVersion());
            LOG.debug("headers: ");
            Iterator <Entry<String, String>> it = request.headers().iterator();
            while (it.hasNext()) {
                Entry entry = it.next();
                LOG.debug("     [" + entry.getKey() + "=" + entry.getValue() + "]");
            }
        }
        
        String method = request.getMethod().toString();
        
        if (Constants.VALID_HTTP_REQUEST_METHOD_SET.contains(method)) {
            if (!isGetImageRequest(method, request.getUri())) {
                retval = true;
            }
        }
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("isValidTestRequest: " + retval);
        }
        
        return retval;
    }
    
    private boolean isGetImageRequest(String method, String uri) {
        boolean retval = false;
        
        if (Constants.HTTP_REQUEST_METHOD_GET.equalsIgnoreCase(method)) {
            int pos = uri.lastIndexOf(".");
            if (pos > -1) {
                String s = uri.substring(pos+1).toLowerCase().trim();
                retval = Constants.IMAGE_SUFFIX_SET.contains(s);
            }
        }
        
        return retval;
    }
}
