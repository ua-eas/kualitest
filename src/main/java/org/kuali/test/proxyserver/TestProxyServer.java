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

import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import org.apache.log4j.Logger;
import org.kuali.test.Platform;
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
    private Platform platform;
    private boolean running = false;
    private List<HttpRequest> testRequests = new ArrayList<HttpRequest>();

    public TestProxyServer(Platform platform) {
        this.platform = platform;

        try {
            initializeProxyServer();
            running = true;
        } catch (Exception ex) {
            LOG.error(ex.toString(), ex);
        }
    }

    private HttpFilters getHttpFilters() {
        return new HttpFilters() {
            @Override
            public HttpResponse requestPre(HttpObject ho) {
                HttpResponse retval = null;
                if (ho instanceof HttpRequest) {
                    HttpRequest httpRequest = (HttpRequest) ho;
                    testRequests.add(httpRequest);

                    if (LOG.isDebugEnabled()) {
                        LOG.debug("httpRequest uri:" + httpRequest.getUri());
                    }

                    retval = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
                }

                return retval;
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

    private HttpFiltersSource getHttpFiltersSource() {
        return new HttpFiltersSourceAdapter() {
            @Override
            public HttpFilters filterRequest(HttpRequest originalRequest) {
                return new HttpFiltersAdapter(originalRequest) {
                    @Override
                    public HttpResponse requestPre(HttpObject httpObject) {
                        HttpResponse retval = null;
                        if (httpObject instanceof HttpRequest) {
                            HttpRequest httpRequest = (HttpRequest) httpObject;
          //                  String target = Utils.getHostFromUrl(platform.getWebUrl(), true);
                            //                 String context = Utils.getContextFromUrl(httpRequest.getUri());
                            //       or.setUri(target + "/" + context);
                        }
                        return retval;
                    }

                    @Override
                    public HttpResponse requestPost(HttpObject httpObject) {
                        HttpResponse retval = null;
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException ex) {
                            LOG.info(ex.toString(), ex);
                        }

                        if (httpObject instanceof HttpRequest) {
                            HttpRequest httpRequest = (HttpRequest) httpObject;

                            if (LOG.isDebugEnabled()) {
                                LOG.debug("Http request is " + ((HttpRequest) httpObject).toString());
                                LOG.debug("Request capacity is " + ((FullHttpRequest) httpObject).content().capacity());
                            }
                        }

                        return retval;
                    }

                    @Override
                    public HttpObject responsePre(HttpObject httpObject) {
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
        if (LOG.isDebugEnabled()) {
            LOG.debug("initializing proxy server");
        }

        DefaultHttpProxyServer
            .bootstrap()
            .withPort(Constants.TEST_PROXY_SERVER_PORT)
            .withChainProxyManager(getChainProxyManager())
            .withFiltersSource(getHttpFiltersSource())
            .withManInTheMiddle(new SelfSignedMitmManager())
       //     .withAllowLocalOnly(true)
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

    public List<HttpRequest> getTestRequests() {
        return testRequests;
    }
}
