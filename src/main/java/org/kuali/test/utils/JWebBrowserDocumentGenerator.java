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
    
    private void populateIframes(JWebBrowser webBrowser, Document doc, Element element) {
        if (Constants.HTML_TAG_TYPE_IFRAME.equalsIgnoreCase(element.getTagName())) {
            String id = element.getAttribute("id");
            String name = element.getAttribute("name");
            
            if (StringUtils.isNotBlank("id")) {
                Object o = webBrowser.executeJavascriptWithResult("return document.getElementById('" + id + "').contentDocument.body.innerHTML");

                if (o != null) {
                    element.appendChild(Utils.tidify(o.toString()).getDocumentElement());
                }
            } else if (StringUtils.isNotBlank(name)) {
                Object o = webBrowser.executeJavascriptWithResult("return document.getElementsByName('" + name + "')[0].contentDocument.body.innerHTML");

                if (o != null) {
                    element.appendChild(Utils.tidify(o.toString()).getDocumentElement());
                }
            }
            
        }
        
        for (Element e : Utils.getChildElements(element)) {
            populateIframes(webBrowser, doc, e);
        }
    }
}
