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
package org.kuali.test.utils;

import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class JWebBrowserDocumentGenerator {
    private static final Logger LOG = Logger.getLogger(JWebBrowserDocumentGenerator.class);
    private static JWebBrowserDocumentGenerator instance;

    private JWebBrowserDocumentGenerator() {
    }

    public static JWebBrowserDocumentGenerator getInstance() {
        if (instance == null) {
            instance = new JWebBrowserDocumentGenerator();
        }

        return instance;
    }

    public Document generate(JWebBrowser webBrowser) {
        return getHtmlDocument(webBrowser);
    }

    private Document getHtmlDocument(JWebBrowser webBrowser) {
        Document retval = null;
        Object o = webBrowser.executeJavascriptWithResult("return document.documentElement.innerHTML;");
        if (o != null) {
            retval = Utils.tidify(o.toString());
            populateIframes(webBrowser, retval, retval.getDocumentElement());
        }
    
        return retval;
    }
    
    private String getIframeContentCall(Element element, String id, String name) {
        StringBuilder retval = new StringBuilder(256);
        
        retval.append("return ");
        
        Element pnode = getIframeParent(element);
        
        if (pnode == null) {
            if (StringUtils.isNotBlank(id)) {
                retval.append("document.getElementById('");
                retval.append(id);
                retval.append("')");
            } else if (StringUtils.isNotBlank(name)) {
                retval.append("document.getElementsByTagName('");
                retval.append(id);
                retval.append("')[0]");
            }
            retval.append(".contentDocument.body.innerHTML;");
        } else {
            String pid = pnode.getAttribute("id");

            if (StringUtils.isNotBlank(pid)) {
                retval.append("document.getElementById('");
                retval.append(pid);
                retval.append("').");
                
                if (StringUtils.isNotBlank(id)) {
                    retval.append("contentDocument.getElementById('");
                    retval.append(id);
                    retval.append("').contentDocument.body.innerHTML;");
                } else if (StringUtils.isNotBlank(name)) {
                    retval.append("contentDocument.getElementsByTagName('");
                    retval.append(id);
                    retval.append("')[0].contentDocument.body.innerHTML;");
                }
            } else if (StringUtils.isNotBlank(name)) {
                retval.append("document.getElementsByTagName('");
                retval.append(id);
                retval.append("')[0].");
                if (StringUtils.isNotBlank(id)) {
                    retval.append("contentDocument.getElementById('");
                    retval.append(id);
                    retval.append("').contentDocument.body.innerHTML;");
                } else if (StringUtils.isNotBlank(name)) {
                    retval.append("contentDocument.getElementsByTagName('");
                    retval.append(id);
                    retval.append("')[0].contentDocument.body.innerHTML;");
                }
            }
        }
        
        return retval.toString();
    }
    
    private Element getIframeParent(Element element) {
        Element retval = null;
        
        Element pnode = (Element)element.getParentNode();
        
        while (pnode != null) {
            if (Constants.HTML_TAG_TYPE_IFRAME.equalsIgnoreCase(pnode.getTagName())) {
                retval = pnode;
                break;
            }
            
            if (pnode.getParentNode() instanceof Element) {
                pnode = (Element)pnode.getParentNode();
            } else {
                break;
            }
        }
        
        return retval;
    }
    
    private void populateIframes(JWebBrowser webBrowser, Document doc, Element element) {
        if (Constants.HTML_TAG_TYPE_IFRAME.equalsIgnoreCase(element.getTagName()) && !element.hasChildNodes()) {
            String id = element.getAttribute("id");
            String name = element.getAttribute("name");

            String iframeCall = getIframeContentCall(element, id, name);
            
            if (StringUtils.isNotBlank(iframeCall)) {
                Object o = webBrowser.executeJavascriptWithResult(iframeCall);
                if (o != null) {
                    if (o.toString().contains("<html>")) {
                        element.appendChild(Utils.tidify(o.toString()).getDocumentElement());
                    } else {
                        element.appendChild(Utils.tidify("<html>" + o.toString() + "</html>").getDocumentElement());
                    }
                }
            }
        }
        
        for (Element e : Utils.getChildElements(element)) {
            populateIframes(webBrowser, doc, e);
        }
    }
}
