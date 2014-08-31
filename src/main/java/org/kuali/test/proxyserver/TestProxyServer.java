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

import chrriis.dj.nativeswing.NativeSwing;
import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import org.apache.commons.collections.map.IdentityMap;
import org.apache.commons.fileupload.MultipartStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.log4j.Logger;
import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.http.HttpStatus;
import org.kuali.test.HtmlRequestOperation;
import org.kuali.test.KualiTestConfigurationDocument;
import org.kuali.test.Operation;
import org.kuali.test.RequestHeader;
import org.kuali.test.RequestParameter;
import org.kuali.test.TestOperation;
import org.kuali.test.TestOperationType;
import org.kuali.test.ui.components.panels.WebTestPanel;
import org.kuali.test.ui.utils.UIUtils;
import org.kuali.test.utils.Constants;
import org.kuali.test.utils.Utils;
import static org.kuali.test.utils.Utils.isStringMatch;
import org.kuali.test.utils.WindowsRegistryProxyHandler;
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
    private boolean proxyServerRunning = false;
    private WebTestPanel webTestPanel;
    private long lastRequestTimestamp = System.currentTimeMillis();
    private Stack <Integer> httpStatus = new Stack<Integer>();
    private List <String> urlsToIgnore = new ArrayList<String>();
    private Set <String> hostsRequiringHttps = new HashSet<String>();
    private WindowsRegistryProxyHandler windowsProxyHandler = null;
    private String previousGetRequest;

    private List<TestOperation> testOperations = Collections.synchronizedList(new ArrayList<TestOperation>() {
        @Override
        public boolean add(TestOperation op) {
            boolean retval = super.add(op);
            op.getOperation().setIndex(size());
            return retval;
        }
    });
    
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

    private HttpFiltersSource getHttpFiltersSource() {
        return new HttpFiltersSourceAdapter() {
            Map <HttpRequest, HttpResponse> imap = new IdentityMap();
            
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
                                    testOperations.add(buildHttpRequestOperation(request, delay));
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
                        if (httpObject instanceof HttpResponse) {
                            HttpResponse response = (HttpResponse)httpObject;
                            if (Utils.isRedirectResponse(response.getStatus().code()) 
                                || (response.getStatus().code() == HttpStatus.OK_200)
                                || Utils.isRedirectResponse(response.getStatus().code())) {
                                imap.put(originalRequest, response);
                                httpStatus.push(Integer.valueOf(response.getStatus().code()));
                            }
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
        String proxyHost = System.getProperty("network.proxy_host", Constants.DEFAULT_PROXY_HOST);
        String proxyPort = System.getProperty("network.proxy_port", Constants.DEFAULT_PROXY_PORT);
        
        if (StringUtils.isBlank(proxyHost)) {
            proxyHost = System.getProperty("http.proxyHost", Constants.DEFAULT_PROXY_HOST);
            proxyPort = System.getProperty("http.proxyPort", Constants.DEFAULT_PROXY_PORT);
        }
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("initializing proxy server");
            LOG.debug("proxyHost: " + proxyHost);
            LOG.debug("proxyPort: " + proxyPort);
        }
        
        if (SystemUtils.IS_OS_WINDOWS) {
            if (windowsProxyHandler != null) {
                windowsProxyHandler.resetProxy();
            }

            windowsProxyHandler = new WindowsRegistryProxyHandler(webTestPanel.getMainframe(), proxyHost, proxyPort);
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
        
        if (webTestPanel.getMainframe().getConfiguration().getUrlsToIgnore() != null) {
            urlsToIgnore.addAll(Arrays.asList(webTestPanel.getMainframe().getConfiguration().getUrlsToIgnore().getUrlArray()));
        }
        
        if (webTestPanel.getMainframe().getConfiguration().getHostsRequiringHttps() != null) {
            hostsRequiringHttps.addAll(Arrays.asList(webTestPanel.getMainframe().getConfiguration().getHostsRequiringHttps().getHostArray()));
        }
        
        if (!NativeInterface.isInitialized()) {
            NativeSwing.initialize();
        }

        if (!NativeInterface.isOpen()) {
            try {
                NativeInterface.open();
            }
            
            catch (Exception ex) {
                LOG.error(ex.toString(), ex);
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("proxy server started");
        }
    }

    /**
     *
     */
    public void stop() {
       try {
            if (NativeInterface.isOpen()) {
                NativeInterface.close();
            }
        } catch (Exception ex) {
            LOG.warn(ex.toString());
        }

        try {
            proxyServer.stop();
            proxyServerRunning = false;
            if (SystemUtils.IS_OS_WINDOWS) {
                if (windowsProxyHandler != null) {
                    windowsProxyHandler.resetProxy();
                }
            }
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
        try {
            String method = request.getMethod().toString();
            if (Constants.VALID_HTTP_REQUEST_METHOD_SET.contains(method)) {
                if (!Utils.isGetImageRequest(method, request.getUri())
                    && !Utils.isGetJavascriptRequest(method, request.getUri())
                    && !Utils.isGetCssRequest(method, request.getUri())
                    && !isDuplicateGetRequest(request)) {
                    int status = HttpStatus.OK_200;
                    
                    if (!httpStatus.isEmpty()) {
                        status = httpStatus.pop().intValue();
                    }
                    
                    retval = (status == HttpStatus.OK_200);
                }

                if (retval) {
                    retval = isIgnoreUrl(request.getUri());
                }
            }
        }
        
        catch (IOException ex) {
            LOG.error(ex.toString(), ex);
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
            String host = request.headers().get(HttpHeaders.HOST);
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

            String myhost = platformHost;

            if (StringUtils.isNotBlank(host)) {
              myhost = host;  
            }
            
            if (hostsRequiringHttps.contains(myhost)) {
                protocol = Constants.HTTPS;
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
        
        Operation myop = retval.addNewOperation();

        HtmlRequestOperation op = myop.addNewHtmlRequestOperation();

        retval.setOperationType(TestOperationType.HTTP_REQUEST);
        op.setDelay(delay);
        op.addNewRequestHeaders();
        op.addNewRequestParameters();
        String multipartBoundary = null;
        
        if (request != null) {
            io.netty.handler.codec.http.HttpHeaders headers = request.headers();
            for (String name : headers.names()) {
                String value = request.headers().get(name);
                if (!Constants.HTTP_REQUEST_HEADERS_TO_IGNORE.contains(name)) {
                    RequestHeader header = op.getRequestHeaders().addNewHeader();
                    header.setName(name);
                    
                    if (HttpHeaders.CONTENT_TYPE.equals(name)) {
                        if (Utils.isMultipart(value)) {
                            multipartBoundary = Utils.getMultipartBoundary(value);
                            value = Utils.stripMultipartBoundary(value);
                        }
                    }
                    
                    header.setValue(value);
                }
            }

            op.setMethod(request.getMethod().name());
            
            String url = buildFullUrl(request);
            op.setUrl(url);

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
                                op, new String(data), multipartBoundary));
                        }
                    }
                }
            } else {
                previousGetRequest = url;
            }
        }
        
        return retval;
    }

    private boolean isDuplicateGetRequest(HttpRequest request) throws IOException {
        boolean retval = false;
        if (Constants.HTTP_REQUEST_METHOD_GET.equals(request.getMethod().name())) {
            retval = buildFullUrl(request).equals(previousGetRequest);
        }
        return retval;
    }

    /**
     * 
     * @param configuration
     * @param reqop
     * @param input
     * @param multipartBoundary
     * @return
     * @throws IOException 
     */
    public String processPostContent(KualiTestConfigurationDocument.KualiTestConfiguration configuration, 
      HtmlRequestOperation reqop, String input, String multipartBoundary) throws IOException {
        StringBuilder retval = new StringBuilder(input.length());

        String contentType = Utils.getRequestHeader(reqop, Constants.HTTP_RESPONSE_CONTENT_TYPE);
        
        if (StringUtils.isNotBlank(contentType)) {
            if (Utils.isUrlFormEncoded(contentType)) {
                String s = encryptFormUrlEncodedParameters(configuration, input);
                if (StringUtils.isNotBlank(s)) {
                    retval.append(s);
                } else {
                    retval.append(input);
                }
            } else if (Utils.isMultipart(contentType)) {
                String s = handleMultipartRequestParameters(configuration, input, multipartBoundary, null);
                if (StringUtils.isNotBlank(s)) {
                    retval.append(s);
                } else {
                    retval.append(input);
                }
            }
        }
        
        return retval.toString();
    }    
    
    private String encryptFormUrlEncodedParameters(KualiTestConfigurationDocument.KualiTestConfiguration configuration, 
        String parameterString) throws UnsupportedEncodingException {
        StringBuilder retval = new StringBuilder(512);

        // if we have a parameter string then convert to NameValuePair list and 
        // process parameters requiring encryption
        if (StringUtils.isNotBlank(parameterString)) {
            List<NameValuePair> nvplist = Utils.getNameValuePairsFromUrlEncodedParams(parameterString);

            if ((nvplist != null) && !nvplist.isEmpty()) {
                NameValuePair[] nvparray = nvplist.toArray(new NameValuePair[nvplist.size()]);

                for (String parameterName : configuration.getParametersRequiringEncryption().getNameArray()) {
                    for (int i = 0; i < nvparray.length; ++i) {
                        if (parameterName.equals(nvparray[i].getName())) {
                            nvparray[i] = new NameValuePair(parameterName, Utils.encrypt(Utils.getEncryptionPassword(configuration), nvparray[i].getValue()));
                        }
                    }
                }
                
                retval.append(Utils.buildUrlEncodedParameterString(nvparray));
            }
        }

        
        return retval.toString();
    }

    private String handleMultipartRequestParameters(KualiTestConfigurationDocument.KualiTestConfiguration configuration, 
        String input, String boundary, Map<String, String> replaceParams) throws IOException {
        StringBuilder retval = new StringBuilder(512);
        
        Set <String> hs = new HashSet<String>(Arrays.asList(configuration.getParametersRequiringEncryption().getNameArray()));
        MultipartStream multipartStream = new MultipartStream(new ByteArrayInputStream(input.getBytes()), boundary.getBytes(), 512, null);
        boolean nextPart = multipartStream.skipPreamble();
        ByteArrayOutputStream bos = new ByteArrayOutputStream(512);
        
        String nameValueSeparator = "";
        while (nextPart) {
            MultiPartHeader mph = new MultiPartHeader(multipartStream.readHeaders());
            bos.reset();
            multipartStream.readBodyData(bos);
            
            String name = mph.getParameter("name");
            if (StringUtils.isBlank(mph.getContentType()) && StringUtils.isNotBlank(name)) {
                String value = bos.toString();

                if (StringUtils.isNotBlank(name)) {
                    if (StringUtils.isBlank(value)) {
                        value = "";
                    }

                    boolean senstiveParameter = hs.contains(name);
                    retval.append(nameValueSeparator);

                    retval.append(name);
                    retval.append(Constants.MULTIPART_NAME_VALUE_SEPARATOR);

                    if (senstiveParameter) {
                        retval.append(Utils.encrypt(Utils.getEncryptionPassword(configuration), value));
                    } else if ((replaceParams != null) && replaceParams.containsKey(name)) {
                        retval.append(replaceParams.get(name));
                    } else {
                        retval.append(value);
                    }

                    nameValueSeparator = Constants.MULTIPART_PARAMETER_SEPARATOR;
                }
            }
            
            nextPart = multipartStream.readBoundary();
        }

        return retval.toString();
    }

    private boolean isIgnoreUrl(String url) {
        boolean retval = false;
        
        if (StringUtils.isNotBlank(url)) {
            for (String compareString : urlsToIgnore) {
                if (isStringMatch(compareString, url)) {
                    retval = true;
                    break;
                }
            }
        }
        
        return retval;
    }

}