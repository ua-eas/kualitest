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
import com.gargoylesoftware.htmlunit.IncorrectnessListener;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.util.Cookie;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import com.gargoylesoftware.htmlunit.util.WebConnectionWrapper;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.test.KualiTestDocument.KualiTest;
import org.kuali.test.TestExecutionParameter;
import org.kuali.test.TestOperation;
import org.kuali.test.utils.Constants;
import org.kuali.test.utils.Utils;


public class TestWebClient extends WebClient {
    private static final Logger LOG = Logger.getLogger(TestWebClient.class);
    private TestExecutionContext tec;
    private KualiTest currentTest;
    private int currentOperationIndex = 0;
    
    public TestWebClient(final TestExecutionContext tec) {
        super(BrowserVersion.CHROME);
        this.tec = tec;
	    getOptions().setJavaScriptEnabled(true);
	    getOptions().setThrowExceptionOnFailingStatusCode(false);
	    getOptions().setThrowExceptionOnScriptError(false);
	    getOptions().setTimeout(Constants.DEFAULT_HTTP_CONNECT_TIMEOUT);
	    getOptions().setRedirectEnabled(false);
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
                if (!request.getRequestParameters().isEmpty()) {
                    List <NameValuePair> params = getUpdatedParameterList(request.getRequestParameters());
                    request.getRequestParameters().clear();
                    request.getRequestParameters().addAll(params);
                }

                replaceJsessionId(request);
                
                return super.getResponse(request);
            }
        };

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
        
        if (currentTest != null) {
            Map<String, String> map = new HashMap<String, String>();
            
            TestOperation[] testOperations = currentTest.getOperations().getOperationArray();
            if ((currentOperationIndex-1) < testOperations.length) {
                for (int i = (currentOperationIndex-1); i >= 0; --i) {
                    if (testOperations[i].getOperation().getTestExecutionParameter() != null) {
                        TestExecutionParameter tep = testOperations[i].getOperation().getTestExecutionParameter();
                        String key = tep.getValueProperty().getPropertyValue();
                        if (StringUtils.isNotBlank(tep.getValue())) {
                            map.put(key, tep.getValue());
                        }
                    }
                }
            }
            
            for (NameValuePair nvp : work) {
                if (map.containsKey(nvp.getValue())) {
                    retval.add(new NameValuePair(nvp.getName(), map.get(nvp.getValue())));
                } else {
                    retval.add(nvp);
                }
            }
        } else {
            retval = work;
        }
        
        return retval;
    }

    private List<NameValuePair> getUpdatedParameterList(List <NameValuePair> nvplist) throws UnsupportedEncodingException {
        return replaceRequestParameterValues(decryptHttpParameters(nvplist), tec.getAutoReplaceParameterMap());
    }

    private List <NameValuePair> decryptHttpParameters(List<NameValuePair> nvplist) throws UnsupportedEncodingException {
        List <NameValuePair>  retval = new ArrayList<NameValuePair>();
        
        String epass = Utils.getEncryptionPassword(tec.getConfiguration());

        if ((nvplist != null) && !nvplist.isEmpty()) {
            for (NameValuePair nvp : nvplist) {
                if (tec.getParametersRequiringDecryption().contains(nvp.getName())) {
                    retval.add(new NameValuePair(nvp.getName(), URLDecoder.decode(Utils.decrypt(epass, nvp.getValue()), CharEncoding.UTF_8)));
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

    public KualiTest getCurrentTest() {
        return currentTest;
    }

    public void setCurrentTest(KualiTest currentTest) {
        this.currentTest = currentTest;
    }

    public int getCurrentOperationIndex() {
        return currentOperationIndex;
    }

    public void setCurrentOperationIndex(int currentOperationIndex) {
        this.currentOperationIndex = currentOperationIndex;
    }
}
