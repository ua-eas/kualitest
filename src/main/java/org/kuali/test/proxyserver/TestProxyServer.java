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
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.CharsetUtil;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.fileupload.MultipartStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Consts;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.SystemProperties;
import org.kuali.test.HtmlRequestOperation;
import org.kuali.test.KualiTestConfigurationDocument;
import org.kuali.test.RequestHeader;
import org.kuali.test.RequestParameter;
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
    private List<TestOperation> testOperations = Collections.synchronizedList(new ArrayList<TestOperation>());
    private List <String> excludeParameterList = new ArrayList<String>();
    private List <String> excludeUrlList = new ArrayList<String>();

    private boolean proxyServerRunning = false;
    private StringBuilder currentHtmlResponse;
    private WebTestPanel webTestPanel;
    private long lastRequestTimestamp = System.currentTimeMillis();
    /**
     *
     * @param webTestPanel
     */
    public TestProxyServer(WebTestPanel webTestPanel) {
        this.webTestPanel = webTestPanel;
        
        if (webTestPanel.getMainframe().getConfiguration().getExcludePostParameterMatchPatterns() != null) {
            excludeParameterList.addAll(Arrays.asList(webTestPanel.getMainframe().getConfiguration().getExcludePostParameterMatchPatterns().getMatchPatternArray()));
        }
        
        if (webTestPanel.getMainframe().getConfiguration().getExcludeHttpRequestMatchPatterns() != null) {
            excludeUrlList.addAll(Arrays.asList(webTestPanel.getMainframe().getConfiguration().getExcludeHttpRequestMatchPatterns().getMatchPatternArray()));
        }

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
                                    if (!isExcludeUrl(request.getUri())) {
                                        testOperations.add(buildHttpRequestOperation(request, delay));
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
                        if (httpObject instanceof HttpRequest) {
                            HttpRequest request = (HttpRequest)httpObject;
                        }
                        
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
                                    if (Utils.isHtmlDocument(getCurrentResponseBuffer())) {
                                        webTestPanel.setLastProxyHtmlResponse(getCurrentResponseBuffer().toString());
                                    }
                                    
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
    
    /**
     *
     * @param content
     * @return
     */
    public static byte[] getHttpPostContent(ByteBuf content) {
        byte[] retval = null;
        if (content.isReadable()) {
            content.retain();
            ByteBuffer nioBuffer = content.nioBuffer();
            retval = new byte[nioBuffer.remaining()];
            nioBuffer.get(retval);
            content.release();
        }

        return retval;
    }
    
    
    /**
     * 
     * @param request
     * @return
     * @throws IOException 
     */
    public String buildFullUrl(HttpRequest request) throws IOException {
        StringBuilder retval = new StringBuilder(128);

        if (!request.getUri().startsWith(Constants.HTTP)) {
            String platformUrl = webTestPanel.getPlatform().getWebUrl();
            String host = request.headers().get(Constants.HTTP_HEADER_HOST);
            String protocol = Constants.HTTPS;
            String platformHost = null;
            if (StringUtils.isNotBlank(platformUrl)) {
                int pos = platformUrl.indexOf("://");
                if (pos > -1) {
                    protocol = platformUrl.substring(0, pos);
                } else {
                    int pos2 = platformUrl.indexOf(Constants.FORWARD_SLASH, pos+3);
                    platformHost = platformUrl.substring(pos+1, pos2);
                }
            }

            retval.append(protocol);
            retval.append("://");
            if (StringUtils.isNotBlank(host)) {
              retval.append(host);  
            } else {
              retval.append(platformHost);
            }
        }

        retval.append(request.getUri());
        
        return retval.toString();
    }

    /**
     * 
     * @param request
     * @param delay
     * @return
     * @throws IOException 
     */
    public TestOperation buildHttpRequestOperation(HttpRequest request, int delay) throws IOException {
        TestOperation retval = TestOperation.Factory.newInstance();

        HtmlRequestOperation op = retval.addNewOperation().addNewHtmlRequestOperation();
        retval.setOperationType(TestOperationType.HTTP_REQUEST);
        op.setDelay(delay);
        op.addNewRequestHeaders();
        op.addNewRequestParameters();
        if (request != null) {
            HttpHeaders headers = request.headers();
            for (String name : headers.names()) {
                if (!Constants.HTTP_REQUEST_HEADERS_TO_IGNORE.contains(name)) {
                    for (String value : headers.getAll(name)) {
                        RequestHeader header = op.getRequestHeaders().addNewHeader();
                        header.setName(name);
                        header.setValue(value);
                    }
                }
            }

            op.setMethod(request.getMethod().name());
            
            op.setUrl(buildFullUrl(request));

            // if this is a post then try to get content
            if (Constants.HTTP_REQUEST_METHOD_POST.equalsIgnoreCase(op.getMethod())) {
                if (request instanceof FullHttpRequest) {
                    FullHttpRequest fr = (FullHttpRequest) request;

                    if (fr.content() != null) {
                        byte[] data = getHttpPostContent(fr.content());

                        if (data != null) {
                            RequestParameter param = op.getRequestParameters().addNewParameter();
                            param.setName(Constants.PARAMETER_NAME_CONTENT);
                            param.setValue(processPostContent(webTestPanel.getMainframe().getConfiguration(), 
                                op, new String(data)));
                        }
                    }
                }
            }
        }
        
        return retval;
    }

    /**
     * 
     * @param configuration
     * @param reqop
     * @param input
     * @return
     * @throws IOException 
     */
  public String processPostContent(KualiTestConfigurationDocument.KualiTestConfiguration configuration, 
      HtmlRequestOperation reqop, String input) throws IOException {
        StringBuilder retval = new StringBuilder(input.length());

        
        String contentType = Utils.getRequestHeader(reqop, Constants.HTTP_RESPONSE_CONTENT_TYPE);
        
        if (StringUtils.isNotBlank(contentType)) {
            if (contentType.startsWith(Constants.MIME_TYPE_FORM_URL_ENCODED)) {
                String s = encryptFormUrlEncodedParameters(configuration, input);
                if (StringUtils.isNotBlank(s)) {
                    retval.append(s);
                } else {
                    retval.append(input);
                }
            } else if (contentType.startsWith(Constants.MIME_TYPE_MULTIPART_FORM_DATA)) {
                int pos = contentType.indexOf(Constants.MULTIPART_BOUNDARY_IDENTIFIER);

                if (pos > -1) {
                    String s = handleMultipartRequestParameters(configuration, input, contentType.substring(pos + Constants.MULTIPART_BOUNDARY_IDENTIFIER.length()), null);
                    if (StringUtils.isNotBlank(s)) {
                        retval.append(s);
                    } else {
                        retval.append(input);
                    }
                } else {
                    retval.append(input);
                }
            }
        }
        
        return retval.toString();
    }    
    
    private boolean isExcludeParameter(String input) {
        boolean retval = false;
        
        if (StringUtils.isNotBlank(input)) {
            for (String s : this.excludeParameterList) {
                if (input.contains(s)) {
                    retval = true;
                    break;
                }
            }
        }
        
        return retval;
    }


    private boolean isExcludeUrl(String input) {
        boolean retval = false;
        
        if (StringUtils.isNotBlank(input)) {
            for (String s : this.excludeUrlList) {
                if (input.contains(s)) {
                    retval = true;
                    break;
                }
            }
        }
        
        return retval;
    }

    private String encryptFormUrlEncodedParameters(KualiTestConfigurationDocument.KualiTestConfiguration configuration, 
        String parameterString) {
        StringBuilder retval = new StringBuilder(512);

        // if we have a parameter string then convert to NameValuePair list and 
        // process parameters requiring encryption
        if (StringUtils.isNotBlank(parameterString)) {
            List<NameValuePair> nvplist = URLEncodedUtils.parse(parameterString, Consts.UTF_8);

            if ((nvplist != null) && !nvplist.isEmpty()) {
                NameValuePair[] nvparray = nvplist.toArray(new NameValuePair[nvplist.size()]);

                for (String parameterName : configuration.getParametersRequiringEncryption().getNameArray()) {
                    for (int i = 0; i < nvparray.length; ++i) {
                        if (parameterName.equals(nvparray[i].getName())) {
                            nvparray[i] = new BasicNameValuePair(parameterName, Utils.encrypt(Utils.getEncryptionPassword(configuration), nvparray[i].getValue()));
                        }
                    }
                }

                nvplist = new ArrayList(Arrays.asList(nvparray));
                
                Iterator <NameValuePair> it = nvplist.iterator();
                
                while (it.hasNext()) {
                    if (this.isExcludeParameter(it.next().getName())) {
                        it.remove();
                    }
                }
                
                retval.append(URLEncodedUtils.format(Arrays.asList(nvparray), Consts.UTF_8));
            }
        }

        
        return retval.toString();
    }

    private String handleMultipartRequestParameters(KualiTestConfigurationDocument.KualiTestConfiguration configuration, 
        String input, String boundary, Map<String, String> replaceParams) throws IOException {
        StringBuilder retval = new StringBuilder(512);
        
        Set <String> hs = new HashSet<String>();
        for (String parameterName : configuration.getParametersRequiringEncryption().getNameArray()) {
            hs.add(parameterName);
        }
        
        MultipartStream multipartStream = new MultipartStream(new ByteArrayInputStream(input.getBytes()), boundary.getBytes(), 512, null);
        boolean nextPart = multipartStream.skipPreamble();
        ByteArrayOutputStream bos = new ByteArrayOutputStream(512);
        
        while (nextPart) {
            String header = multipartStream.readHeaders();
            bos.reset();
            multipartStream.readBodyData(bos);

            String name = Utils.getNameFromNameParam(header);
            String value = bos.toString();
            
            if (StringUtils.isNotBlank(name) && StringUtils.isNotBlank(value)) {
                if (!isExcludeParameter(name)) {
                    boolean senstiveParameter = false;

                    senstiveParameter = hs.contains(name);
                    retval.append(name);
                    retval.append(Constants.MULTIPART_NAME_VALUE_SEPARATOR);

                    if (senstiveParameter) {
                        retval.append(Utils.encrypt(Utils.getEncryptionPassword(configuration), value));
                    } else if ((replaceParams != null) && replaceParams.containsKey(name)) {
                        retval.append(replaceParams.get(name));
                    } else {
                        retval.append(bos.toString());
                    }
                
                    retval.append(Constants.MULTIPART_PARAMETER_SEPARATOR);
                }
            }
            
            nextPart = multipartStream.readBoundary();
        }

        return retval.toString();
    }
}