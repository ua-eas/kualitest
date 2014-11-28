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

package org.kuali.test.runner.execution;

import com.gargoylesoftware.htmlunit.AlertHandler;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.IncorrectnessListener;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.StringWebResponse;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.FrameWindow;
import com.gargoylesoftware.htmlunit.html.HtmlCheckBoxInput;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlOption;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;
import com.gargoylesoftware.htmlunit.html.HtmlRadioButtonInput;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;
import com.gargoylesoftware.htmlunit.html.HtmlTextArea;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import com.gargoylesoftware.htmlunit.util.Cookie;
import com.gargoylesoftware.htmlunit.util.KeyDataPair;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import com.gargoylesoftware.htmlunit.util.WebConnectionWrapper;
import com.google.common.net.HttpHeaders;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import org.apache.commons.codec.CharEncoding;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jetty.http.HttpStatus;
import org.kuali.test.FailureAction;
import org.kuali.test.TestExecutionParameter;
import org.kuali.test.runner.exceptions.TestException;
import org.kuali.test.runner.requestprocessors.HttpRequestProcessor;
import org.kuali.test.runner.requestprocessors.HttpRequestProcessorException;
import org.kuali.test.utils.Constants;
import org.kuali.test.utils.HtmlDomProcessor;
import org.kuali.test.utils.Utils;


public class TestWebClient extends WebClient {
    private static final Logger LOG = Logger.getLogger(TestWebClient.class);
    private TestExecutionContext tec;
    private Set<String> errorIndicators = new HashSet<String>();
    private List<String> parametersToIgnore = new ArrayList<String>();
    private List<HttpRequestProcessor> preSubmitProcessors = new ArrayList<HttpRequestProcessor>();
    private List<HttpRequestProcessor> postSubmitProcessors = new ArrayList<HttpRequestProcessor>();
    private List<String> testRunUrlsToIgnore = new ArrayList<String>();
    
    public TestWebClient(final TestExecutionContext tec) {
        super(BrowserVersion.getDefault());
        this.tec = tec;
        
        if (tec.getConfiguration().getParametersToIgnore() != null) {
            parametersToIgnore.addAll(Arrays.asList(tec.getConfiguration().getParametersToIgnore().getParameterNameArray()));
        }
        
        if (tec.getConfiguration().getErrorIndicators() != null) {
            errorIndicators.addAll(Arrays.asList(tec.getConfiguration().getErrorIndicators().getIndicatorArray()));
        }

        if (tec.getConfiguration().getTestRunUrlsToIgnore() != null) {
            testRunUrlsToIgnore.addAll(Arrays.asList(tec.getConfiguration().getTestRunUrlsToIgnore().getUrlArray()));
        }

        if (tec.getConfiguration().getHttpPreSubmitProcessors() != null) {
            for (String clazz : tec.getConfiguration().getHttpPreSubmitProcessors().getProcessorArray()) {
                try {
                    preSubmitProcessors.add((HttpRequestProcessor)Class.forName(clazz).newInstance());
                } 

                catch (Exception ex) {
                    LOG.error(ex.toString(), ex);
                }
            }
        }
        
        if (tec.getConfiguration().getHttpPostSubmitProcessors() != null) {
            for (String clazz : tec.getConfiguration().getHttpPostSubmitProcessors().getProcessorArray()) {
                try {
                    postSubmitProcessors.add((HttpRequestProcessor)Class.forName(clazz).newInstance());
                } 

                catch (Exception ex) {
                    LOG.error(ex.toString(), ex);
                }
            }
        }

        getOptions().setJavaScriptEnabled(true);
        getOptions().setThrowExceptionOnFailingStatusCode(false);
        getOptions().setThrowExceptionOnScriptError(false);
        getOptions().setTimeout(Constants.DEFAULT_HTTP_READ_TIMEOUT);
        getOptions().setRedirectEnabled(true);
        getOptions().setCssEnabled(true);
        
        // wait 30 seconds for javascript if required
        waitForBackgroundJavaScript(Constants.HTML_CLIENT_WAIT_FOR_BACKGROUND_JAVASCRIPT_TIME);
        
        setAjaxController(new NicelyResynchronizingAjaxController());
        
        setAlertHandler(new AlertHandler() {
            @Override
            public void handleAlert(Page page, String alert) {
                LOG.info(alert);
            }
        });

        setIncorrectnessListener(new IncorrectnessListener() {
            @Override
            public void notify(String msg, Object o) {
                LOG.info(msg);
            }
        });
        
        new WebConnectionWrapper(this) {
            @Override
            public WebResponse getResponse(WebRequest request) throws IOException {
                WebResponse retval = null;
                
                // if this is in the ignore list the reurn an empty response
                if (Utils.isIgnoreUrl(testRunUrlsToIgnore, request.getUrl().toExternalForm())) {
                    retval = new StringWebResponse("", request.getUrl());
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("ignored url: " + request.getUrl().toExternalForm());
                    }
                } else {
                    boolean jscall = Utils.isGetJavascriptRequest(request.getHttpMethod().toString(), request.getUrl().toExternalForm());
                    boolean csscall = Utils.isGetCssRequest(request.getHttpMethod().toString(), request.getUrl().toExternalForm());

                    if (!jscall && !csscall) {
                        if (request.getUrl().toExternalForm().contains(Constants.SEPARATOR_QUESTION)) {
                            handleUrlParameters(request);
                        }

                        replaceJsessionId(request);
                    } 

                    runPreSubmitProcessing(request);

                    Integer indx = tec.getCurrentOperationIndex();

                    WebResponse response =  super.getResponse(request);                

                    retval = runPostSubmitProcessing(request, response);

                    int status = retval.getStatusCode();

                    if (!jscall && !csscall) {
                        String results = retval.getContentAsString();
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("========================================= operation: " + indx.toString() + " =============================================");
                            LOG.debug("url=" + request.getUrl().toExternalForm());
                            LOG.debug("status=" + status);
                            LOG.debug("------------------------------------------ parameters ---------------------------------------------------------");

                            for (NameValuePair nvp : request.getRequestParameters()) {
                                LOG.debug(nvp.getName() + "=" + nvp.getValue());
                            }

                            LOG.debug("--------------------------------------------- results ---------------------------------------------------------");
                            LOG.debug(results);
                        }
                        if ((retval.getStatusCode() == HttpStatus.OK_200)
                            && retval.getContentType().startsWith(Constants.MIME_TYPE_HTML)) {
                            if (StringUtils.isNotBlank(results)) {
                                tec.getCurrentTest().pushHttpResponse(results);
                                tec.updateAutoReplaceMap(HtmlDomProcessor.getInstance().getDomDocumentElement(results));
                            }
                        } else if (!Utils.isRedirectResponse(retval.getStatusCode())) {
                            if (isErrorResult(results)) {
                                writeErrorFile(request.getUrl().toExternalForm(), indx, results);
                                tec.haltTest(new TestException("server returned error - see attached error output page", tec.getCurrentTestOperation().getOperation(), FailureAction.ERROR_HALT_TEST));
                            } else if (retval.getContentType().startsWith(Constants.MIME_TYPE_HTML)) {
                                if (tec.getConfiguration().getOutputIgnoredResults()) {
                                    TestException tex = new TestException("server returned bad status - " 
                                        + status
                                        + ", url=" 
                                        + request.getUrl().toExternalForm(), tec.getCurrentTestOperation().getOperation(), FailureAction.IGNORE);
                                    tec.writeFailureEntry(tec.getCurrentTestOperation(), new Date(), tex);
                                }
                            }
                        } else if (Utils.isRedirectResponse(retval.getStatusCode())) {
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("redirect to: " + retval.getResponseHeaderValue(HttpHeaders.LOCATION));
                            }
                        }
                    }
                }
                
                return retval;
            }
        };
    }
    
    private void runPreSubmitProcessing(WebRequest request) {
        // allow custom requst pocessing if desired
        for (HttpRequestProcessor p : preSubmitProcessors) {
            try {
                p.preProcess(this, tec, request);
            } 

            catch (HttpRequestProcessorException ex) {
                LOG.error(ex.toString(), ex);
            }
        }
    }

    private WebResponse runPostSubmitProcessing(WebRequest request, WebResponse response) {
        WebResponse retval = response;
        // allow custom requst pocessing if desired
        for (HttpRequestProcessor p : postSubmitProcessors) {
            try {
                retval = p.postProcess(this, tec, request, response);
            } 

            catch (HttpRequestProcessorException ex) {
                LOG.error(ex.toString(), ex);
            }
        }
        
        return retval;
    }

    @Override
    public Page getPage(WebRequest request) throws IOException, FailingHttpStatusCodeException {
        Page retval = super.getPage(request); 
        
        if (retval instanceof HtmlPage) {
            for (FrameWindow frame : ((HtmlPage)retval).getFrames()) {
                tec.getCurrentTest().pushHttpResponse(Utils.printElement(frame.getEnclosingPage().getDocumentElement()));
            }
        }
        
        return retval;
    }

    public String getUpdatedUrlParameters(String input) throws UnsupportedEncodingException {
        StringBuilder retval = new StringBuilder(512);
        
        if (StringUtils.isNotBlank(input)) {
            // hack to handle urls with multiple question marks in the parameter list
            int pos = input.indexOf(Constants.SEPARATOR_QUESTION);
            if (pos > -1) {
                retval.append(input.substring(0, pos+1));
                String params = Utils.getParametersFromUrl(input);

                StringBuilder buf = new StringBuilder(params.length());
                
                // handle parameter list that contains question marks
                if (params.contains(Constants.SEPARATOR_QUESTION)) {
                    StringTokenizer st = new StringTokenizer(params, Constants.SEPARATOR_QUESTION);
                    String separator = "";

                    while (st.hasMoreTokens()) {
                        String token = st.nextToken();
                        buf.append(separator);
                        List <NameValuePair> npvlist = Utils.getNameValuePairsFromUrlEncodedParams(token);
                        if ((npvlist != null) && !npvlist.isEmpty()) {
                            npvlist = getUpdatedParameterList(npvlist);
                            buf.append(Utils.buildUrlEncodedParameterString(npvlist));
                            separator = Constants.SEPARATOR_QUESTION;
                        }
                    }
                } else {
                    List <NameValuePair> npvlist = Utils.getNameValuePairsFromUrlEncodedParams(params);
                    if ((npvlist != null) && !npvlist.isEmpty()) {
                        npvlist = getUpdatedParameterList(npvlist);
                        buf.append(Utils.buildUrlEncodedParameterString(npvlist));
                    }
                }
                
                retval.append(buf.toString());
            } else {
                retval.append(input);
            }
        }
        
        return retval.toString();
    }

    private void handleUrlParameters(WebRequest request) throws UnsupportedEncodingException, MalformedURLException {
        String s = request.getUrl().toExternalForm();
        
        String updatedUrl = getUpdatedUrlParameters(request.getUrl().toExternalForm());
        if (StringUtils.isNotBlank(updatedUrl)) {
            request.setUrl(new URL(updatedUrl));
        }
    }
    
    private List  <NameValuePair> replaceRequestParameterValues(List  <NameValuePair> nvplist, Map<String, String> paramMap) throws UnsupportedEncodingException  {
        List  <NameValuePair> retval = new ArrayList<NameValuePair>();
        List  <NameValuePair> work = new ArrayList<NameValuePair>();
        
        if ((nvplist != null) && !nvplist.isEmpty()) {
            for (NameValuePair nvp : nvplist) {
                String replacement = paramMap.get(nvp.getName());
                
                if (StringUtils.isNotBlank(replacement)) {
                    work.add(new NameValuePair(nvp.getName(), replacement));
                } else {
                    work.add(nvp);
                }
            }
        }
        
        if (tec.getCurrentTest() != null) {
            Map<String, TestExecutionParameter> byValueMap = tec.getTestExecutionByValueParameterMap();
            Map<String, TestExecutionParameter> byElementNameMap = tec.getTestExecutionByElementNameParameterMap();

            for (NameValuePair nvp : work) {
                TestExecutionParameter tep1 = byValueMap.get(nvp.getValue());
                TestExecutionParameter tep2 = byElementNameMap.get(nvp.getName());
                if (tep1 != null) {
                    retval.add(new NameValuePair(nvp.getName(), tep1.getValue()));
                } else if (tep2 != null) {
                    retval.add(new NameValuePair(nvp.getName(), tep2.getValue()));
                } else if (isFileAttachment(nvp.getName())) {
                    StringTokenizer st = new StringTokenizer(nvp.getValue(), Constants.SEPARATOR_COLON);
                    String mimeType = st.nextToken();
                    String fname = st.nextToken();
                    retval.add(new KeyDataPair("file", new File(fname), mimeType, CharEncoding.UTF_8));
                } else {
                    retval.add(nvp);
                }
            }
        } else {
            retval = work;
        }
        
        return retval;
    }
    
    private boolean isFileAttachment(String nm) {
        return nm.contains(Constants.FILE_ATTACHMENT_MARKER);
    }
    
    public List<NameValuePair> getUpdatedParameterList(List <NameValuePair> nvplist) throws UnsupportedEncodingException {
        Iterator <NameValuePair> it = nvplist.iterator();
        while (it.hasNext()) {
            if (isIgnoreParameter(it.next().getName())) {
                it.remove();
            }
        }
        
        return replaceRequestParameterValues(decryptHttpParameters(nvplist), tec.getAutoReplaceParameterMap());
    }

    private boolean isIgnoreParameter(String name) {
        boolean retval = false;
        
        if (StringUtils.isNotBlank(name)) {
            for (String compareString : parametersToIgnore) {
                if (Utils.isStringMatch(compareString, name)) {
                    retval = true;
                    break;
                }
            }
        }
        
        return retval;
    }
    
    private List <NameValuePair> decryptHttpParameters(List<NameValuePair> nvplist) throws UnsupportedEncodingException {
        List <NameValuePair>  retval = new ArrayList<NameValuePair>();
        
        String epass = Utils.getEncryptionPassword(tec.getConfiguration());

        if ((nvplist != null) && !nvplist.isEmpty()) {
            for (NameValuePair nvp : nvplist) {
                if (tec.getParametersRequiringDecryption().contains(nvp.getName())) {
                    retval.add(new NameValuePair(nvp.getName(), Utils.decrypt(epass, nvp.getValue())));
                } else {
                    retval.add(nvp);
                }
            }
        }
        
        return retval;
    }

    private void replaceJsessionId(WebRequest request) throws MalformedURLException {
        String urlString = request.getUrl().toExternalForm();

        int pos = urlString.toLowerCase().indexOf(Constants.JSESSIONID_PARAMETER_NAME);
        if (pos > -1) {
            Cookie cookie = findJSessionIdCookie(request.getUrl());
            if (cookie != null) {
                StringBuilder buf = new StringBuilder(urlString.length());
                int pos2 = urlString.indexOf(Constants.SEPARATOR_QUESTION);
                buf.append(urlString.subSequence(0, pos));
                buf.append(Constants.JSESSIONID_PARAMETER_NAME);
                buf.append(Constants.SEPARATOR_EQUALS);
                buf.append(cookie.getValue());

                if (pos2 > -1) {
                    buf.append(urlString.substring(pos2));
                }

                request.setUrl(new URL(buf.toString()));
            }
        } 
    }

    private Cookie findJSessionIdCookie(URL url) {
        Cookie retval = null;
            
        for (Cookie c : getCookies(url)) {
            if (c.getName().equalsIgnoreCase(Constants.JSESSIONID_PARAMETER_NAME)) {
                retval = c;
                break;
            }
        }

        return retval;
    }

    public Set <Cookie> getCookies() {
        return getCookieManager().getCookies();
    }

    private void writeErrorFile(String url, int indx, String results) {
        File f = new File(getErrorFileName());

        if (!f.getParentFile().exists()) {
            f.getParentFile().mkdirs();
        }

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(f);
            String s = "test operation=" + indx;
            fos.write(s.getBytes());
            s = "\r\nurl=" + url;
            fos.write(s.getBytes());
            fos.write("\r\n----------------------------------------------------------------------------------------\r\n".getBytes());
            fos.write(results.getBytes());
            tec.getGeneratedCheckpointFiles().add(f);
        }

        catch (Exception ex) {
            LOG.error(ex.toString(), ex);
        }

        finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            }

            catch (Exception ex) {};
        }
    }

   private String getErrorFileName() {
        StringBuilder retval = new StringBuilder(256);
        
        retval.append(tec.getConfiguration().getTestResultLocation());
        retval.append(Constants.FORWARD_SLASH);
        retval.append(tec.getCurrentTest().getTestHeader().getPlatformName());
        retval.append(Constants.FORWARD_SLASH);
        retval.append(Constants.SCREEN_CAPTURE_DIR);
        retval.append(Constants.FORWARD_SLASH);
        retval.append(Constants.DEFAULT_DATE_FORMAT.format(new Date()));
        retval.append(Constants.FORWARD_SLASH);
        retval.append(tec.getCurrentTest().getTestName().toLowerCase().replace(" ", "-"));
        retval.append("_error-output_");
        retval.append(Constants.FILENAME_TIMESTAMP_FORMAT.format(new Date()));
        retval.append("_");
        retval.append(tec.getTestRun());
        retval.append(Constants.TXT_SUFFIX);
        
        return retval.toString();
    }
   
    private boolean isErrorResult(String input) {
        boolean retval = false;
        for (String s : errorIndicators) {
            if (input.contains(s)) {
                retval = true;
                break;
            }
        }
        
        return retval;
    }
    
    public boolean isPagePopulated(List <NameValuePair> nvplist) throws IOException {
        boolean retval = true;
        Page page = getCurrentWindow().getEnclosedPage();
        for (NameValuePair nvp : nvplist) {
            if (findHtmlElementByName(page, Utils.stripXY(nvp.getName())) == null) {
                retval = false;
                break;
            }
        }
        
        return retval;
    }

    public HtmlElement findHtmlElementByName(String elementName) {
        return findHtmlElementByName(getCurrentWindow().getEnclosedPage(), elementName);
    }

    public HtmlElement findHtmlElementByName(Page page, String elementName) {
        HtmlElement retval = null;
        
        if (page.isHtmlPage()) {
            try {
                retval = ((HtmlPage)page).getElementByName(elementName);
            }

            catch (ElementNotFoundException ex) {};
            
            if (retval == null) {
                for (FrameWindow window : ((HtmlPage)page).getFrames()) {
                    Page pg = window.getFrameElement().getEnclosedPage();
                    if (pg.isHtmlPage()) {
                        try {
                            retval = ((HtmlPage)pg).getElementByName(elementName);
                        }
                        
                        catch (ElementNotFoundException ex) {};
                        
                        if (retval == null) {
                            retval = findHtmlElementByName(pg, elementName);
                        }
                    }
                    
                    if (retval != null) {
                        break;
                    }
                }
            }
        }
        
        return retval;
    }

    public HtmlElement findFormSubmitElement(List <NameValuePair> nvplist) throws IOException {
        HtmlElement retval = null;
        
        if (nvplist != null) {
            Page pg = getCurrentWindow().getEnclosedPage();
            for (NameValuePair nvp : nvplist) {
                HtmlElement element = findHtmlElementByName(pg, Utils.stripXY(nvp.getName()));
                if (element != null) {
                    if (Constants.HTML_TAG_TYPE_INPUT.equals(element.getTagName())) {
                        if (Utils.isSubmitInputType(element.getAttribute(Constants.HTML_TAG_ATTRIBUTE_TYPE))) {
                            retval = element;
                            break;
                        }
                    }
                }
            }
        }
        
        return retval;
    }
    
    public void populateFormElements(TestExecutionContext tec, List <NameValuePair> nvplist) throws UnsupportedEncodingException {
        HtmlPage page = (HtmlPage)tec.getWebClient().getCurrentWindow().getEnclosedPage();
        for (NameValuePair nvp : nvplist) {
            try {
                HtmlElement e = findHtmlElementByName(page, nvp.getName());
                populateFormElement(page, e, nvp.getName(), nvp.getValue());
            }

            catch (ElementNotFoundException ex) {};
        }
    }

    private void populateFormElement(HtmlPage page, HtmlElement e, String name, String value) {
        if (e instanceof HtmlTextInput) {
            HtmlTextInput ti = (HtmlTextInput)e;
            ti.setText(value);
        } else if (e instanceof HtmlPasswordInput) {
            HtmlPasswordInput pi = (HtmlPasswordInput)e;
            pi.setText(value);
        } else if (e instanceof HtmlRadioButtonInput) {
            for (DomElement de : page.getElementsByName(name)) {
                HtmlRadioButtonInput rbi = (HtmlRadioButtonInput)de;
                if (rbi.getValueAttribute().equals(value)) {
                    rbi.setChecked(true);
                    break;
                }
            }
        } else if (e instanceof HtmlCheckBoxInput) {
            for (DomElement de : page.getElementsByName(name)) {
                HtmlCheckBoxInput cbi = (HtmlCheckBoxInput)de;
                if (cbi.getValueAttribute().equals(name)) {
                    cbi.setChecked(true);
                    break;
                }
            }
        } else if (e instanceof HtmlSelect) {
            for (DomElement de : page.getElementsByName(name)) {
                HtmlSelect sel = (HtmlSelect)de;
                HtmlOption option = sel.getOptionByValue(value);

                if (option != null) {
                    sel.setSelectedAttribute(option, true);
                    break;
                }
            }
        } else if (e instanceof HtmlTextArea) {
            HtmlTextArea ta = (HtmlTextArea)e;
            ta.setText(value);
        }
    }
}
