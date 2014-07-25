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

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.CharsetUtil;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.SystemProperties;
import org.kuali.test.TestOperation;
import org.kuali.test.TestOperationType;
import org.kuali.test.ui.components.panels.WebTestPanel;
import org.kuali.test.ui.utils.UIUtils;
import org.kuali.test.utils.Constants;
import org.kuali.test.utils.Utils;
import org.littleshoot.proxy.HttpFilters;
import org.littleshoot.proxy.HttpFiltersAdapter;
import org.littleshoot.proxy.HttpFiltersSource;
import org.littleshoot.proxy.HttpFiltersSourceAdapter;
import org.littleshoot.proxy.extras.SelfSignedMitmManager;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;

/**
 *
 * @author rbtucker
 */
public class TestProxyServer {
    private static final Logger LOG = Logger.getLogger(TestProxyServer.class);
    private DefaultHttpProxyServer proxyServer;
    private List<TestOperation> testOperations = new ArrayList<TestOperation>();
    private boolean proxyServerRunning = false;
    private StringBuilder currentHtmlResponse;
    private WebTestPanel webTestPanel;
    private long lastRequestTimestamp = System.currentTimeMillis();
    private String lastRequestMessageDigest;
    /**
     *
     * @param webTestPanel
     */
    public TestProxyServer(WebTestPanel webTestPanel) {
        this.webTestPanel = webTestPanel;
        try {
            Thread.sleep(1000);
            initializeProxyServer();
        } catch (InterruptedException ex) {
            LOG.error(ex.toString(), ex);
        }
    }

    private synchronized StringBuilder getCurrentResponseBuffer() {
        if (currentHtmlResponse == null) {
            currentHtmlResponse = new StringBuilder(Constants.INITIAL_HTML_RESPONSE_BUFFER_SIZE);
        }
        
        return currentHtmlResponse;
    }
    
    private HttpFiltersSource getHttpFiltersSource() {
          return new HttpFiltersSourceAdapter() {
            @Override
            public HttpFilters filterRequest(HttpRequest originalRequest) {
                return new HttpFiltersAdapter(originalRequest) {
                    @Override
                    public HttpResponse requestPre(HttpObject httpObject) {
                        if (httpObject instanceof HttpRequest) {
                            HttpRequest request = (HttpRequest)httpObject;

                            if (isValidTestRequest(request)) {
                                int delay = (int)(System.currentTimeMillis() - lastRequestTimestamp);
                                lastRequestTimestamp = System.currentTimeMillis();
                                try {
                                    // little bit of a hack here - for some reason we are submitting 
                                    // multiple requests at times
                                    if (!isDuplicate(request)) {
                                        testOperations.add(Utils.buildTestOperation(webTestPanel.getMainframe().getConfiguration(), TestOperationType.HTTP_REQUEST, request, delay));
                                    }
                                } catch (Exception ex) {
                                    LOG.error(ex.toString(), ex);
                                    UIUtils.showError(webTestPanel, "Error", "Error handling http request - " + ex.toString());
                                }
                            }
                        }
                        return null;
                    }

                    @Override
                    public HttpResponse requestPost(HttpObject httpObject) {
                        return null;
                    }

                    @Override
                    public HttpObject responsePre(HttpObject httpObject) {
                        return httpObject;
                    }

                    @Override
                    public HttpObject responsePost(HttpObject httpObject) {
                        if (httpObject instanceof HttpContent) {
                            ByteBuf content = ((HttpContent)httpObject).content().retain();
                            if (content.isReadable()) {
                                getCurrentResponseBuffer().append(content.toString(CharsetUtil.UTF_8));
                                
                                if (httpObject instanceof LastHttpContent) {
                                    webTestPanel.setLastProxyHtmlResponse(getCurrentResponseBuffer().toString());
                                    getCurrentResponseBuffer().setLength(0);
                                }
                            }   
                            content.release();
                        }
                        return httpObject;
                    }
                };
            }

            @Override
            public int getMaximumRequestBufferSizeInBytes() {
                return Constants.MAX_REQUEST_BUFFER_SIZE;
            }

            @Override
            public int getMaximumResponseBufferSizeInBytes() {
                return Constants.MAX_RESPONSE_BUFFER_SIZE;
            }
        };
    }

    private boolean isHtmlResponse(HttpResponse response) {
        String contentType = response.headers().get(Constants.CONTENT_TYPE_KEY);
        return (StringUtils.isNotBlank(contentType) && contentType.startsWith(Constants.HTML_MIME_TYPE));
    }
    
    private void initializeProxyServer() {
        // proxy port and host should be passed in as vm params
        String proxyHost = SystemProperties.getProperty("network.proxy_host", Constants.DEFAULT_PROXY_HOST);
        String proxyPort = SystemProperties.getProperty("network.proxy_port", Constants.DEFAULT_PROXY_PORT);
        
        if (StringUtils.isBlank(proxyHost)) {
            proxyHost = SystemProperties.getProperty("http.proxyHost", Constants.DEFAULT_PROXY_HOST);
            proxyPort = SystemProperties.getProperty("http.proxyPort", Constants.DEFAULT_PROXY_PORT);
        }
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("initializing proxy server");
            LOG.debug("proxyHost: " + proxyHost);
            LOG.debug("proxyPort: " + proxyPort);
        }

        proxyServer = (DefaultHttpProxyServer)DefaultHttpProxyServer
            .bootstrap()
            .withPort(Integer.parseInt(proxyPort))
            .withAddress(new InetSocketAddress(proxyHost, Integer.parseInt(proxyPort)))
            .withFiltersSource(getHttpFiltersSource())
            .withManInTheMiddle(new SelfSignedMitmManager())
            .withAllowLocalOnly(true)
            .start();

        proxyServerRunning = true;
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("proxy server started");
        }
    }

    /**
     *
     */
    public void stop() {
        try {
            proxyServer.stop();
            proxyServerRunning = false;
        }
        
        catch (Throwable t) {
            LOG.warn(t.toString(), t);
        }
    }

    /**
     *
     * @return
     */
    public boolean isProxyServerRunning() {
        return proxyServerRunning;
    }

    /**
     *
     * @return
     */
    public List<TestOperation> getTestOperations() {
        return testOperations;
    }
    
    /**
     *
     * @param request
     * @return
     */
    protected boolean isValidTestRequest(HttpRequest request) {
        boolean retval = false;
        
        String method = request.getMethod().toString();
        if (Constants.VALID_HTTP_REQUEST_METHOD_SET.contains(method)) {
            retval = (!isGetImageRequest(method, request.getUri())
                && !isGetJavascriptRequest(method, request.getUri())
                && !isGetCssRequest(method, request.getUri()));
        }
        
        if (retval) {
            // do not want to  run request that was an auto-redirect
            // for this check we are looking at refererer not the same as host
            String host = request.headers().get(Constants.HTTP_HEADER_HOST);
            String referer = request.headers().get(Constants.HTTP_HEADER_REFERER);

            if (StringUtils.isNotBlank(host) && StringUtils.isNotBlank(referer)) {
                try {
                    URL url = new URL(referer);
                    retval = (StringUtils.isBlank(url.getHost()) || host.equals(url.getHost()));
                } 
                catch (MalformedURLException ex) {
                    LOG.warn(ex.toString(), ex);
                }
            }
        }
        
        return retval;
    }
    
    private boolean isGetImageRequest(String method, String uri) {
        boolean retval = false;
        
        if (Constants.HTTP_REQUEST_METHOD_GET.equalsIgnoreCase(method)) {
            retval = Constants.IMAGE_SUFFIX_SET.contains(Utils.getFileSuffix(uri));
        }
        
        return retval;
    }

    private boolean isGetJavascriptRequest(String method, String uri) {
        boolean retval = false;
        
        if (Constants.HTTP_REQUEST_METHOD_GET.equalsIgnoreCase(method)) {
            retval = Constants.JAVASCRIPT_SUFFIX.equalsIgnoreCase(Utils.getFileSuffix(uri));
        }
        
        return retval;
    }

    private boolean isGetCssRequest(String method, String uri) {
        boolean retval = false;
        
        if (Constants.HTTP_REQUEST_METHOD_GET.equalsIgnoreCase(method)) {
            retval = Constants.CSS_SUFFIX.equalsIgnoreCase(Utils.getFileSuffix(uri));
        }
        
        return retval;
    }
    
    private boolean isDuplicate(HttpRequest request) throws NoSuchAlgorithmException, IOException {
        boolean retval = false;
        
        if (request instanceof FullHttpRequest) {
            FullHttpRequest frequest = (FullHttpRequest)request;
            
            StringBuilder buf = new StringBuilder(512);
            buf.append(request.getMethod());
            buf.append(".");
            buf.append(Utils.buildUrlFromRequest(webTestPanel.getMainframe().getConfiguration(), request));
            buf.append(".");
            if (frequest.content() != null) {
                byte[] b = Utils.getHttpPostContent(frequest.content());
                if (b != null) {
                    buf.append(new String(b));
                }
            }

            String s  = Utils.getMessageDigestString(buf.toString());

            if (StringUtils.isNotBlank(lastRequestMessageDigest)) {
                retval = s.equals(lastRequestMessageDigest);
            }
            
            lastRequestMessageDigest = s;
        }
        
        return retval;
    }
}