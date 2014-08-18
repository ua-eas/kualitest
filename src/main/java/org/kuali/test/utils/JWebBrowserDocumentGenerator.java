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
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

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
        
        Object o = webBrowser.executeJavascriptWithResult("return '<html>' + document.documentElement.innerHTML + '</html>';");
        if (o != null) {
            retval = Utils.tidify(o.toString());
            populateIframes(webBrowser, retval, retval.getDocumentElement());
        }
    
        return retval;
    }
    
    private void populateIframes(JWebBrowser webBrowser, Document doc, Element element) {
        if (Constants.HTML_TAG_TYPE_IFRAME.equalsIgnoreCase(element.getTagName())) {
            Element iframeBody = getIframeBody(webBrowser, element);

            if ((iframeBody != null) && !element.hasChildNodes()) {
                NodeList l = iframeBody.getElementsByTagName(Constants.HTML_TAG_TYPE_BODY);

                if (l.getLength() > 0) {
                    Element body = (Element) l.item(0);
                    l = body.getChildNodes();

                    // import all the child nodes (with deep copy) of the iframe body
                    for (int j = 0; j < l.getLength(); ++j) {
                        element.appendChild(importNode(doc, l.item(j), true));
                    }
                }
            }
        }
        
        for (Element e : Utils.getChildElements(element)) {
            populateIframes(webBrowser, doc, e);
        }
    }
    
    private Element getIframeBody(JWebBrowser webBrowser, Element iframeNode) {
        Element retval = null;

        String js = getJsIframeDataCall(iframeNode);

        Object o = webBrowser.executeJavascriptWithResult(js);

        // if we get html back then clean and get the iframe body node
        if (o != null) {
            Document doc = Utils.tidify(o.toString());
            
            if (doc != null) {
                retval = doc.getDocumentElement();
            }
        }

        return retval;
    }

    private String getJsIframeDataCall(Element iframeNode) {
        StringBuilder retval = new StringBuilder(512);
        String src = iframeNode.getAttribute(Constants.HTML_TAG_ATTRIBUTE_SRC);

        if (!src.startsWith(Constants.HTTP)) {
            String id = iframeNode.getAttribute(Constants.HTML_TAG_ATTRIBUTE_ID);
            String name = iframeNode.getAttribute(Constants.HTML_TAG_ATTRIBUTE_NAME);
            if (StringUtils.isNotBlank(id)) {
                retval.append("return document.getElementById('");
                retval.append(id);
                retval.append("')");
            } else {
                retval.append("return document.getElementsByTagName('");
                retval.append(name);
                retval.append("')[0]");
            }

            retval.append(".contentDocument.body.innerHTML;");
        } else {
            retval.append("var xmlhttp=new XMLHttpRequest();");
            retval.append("xmlhttp.open('GET','");
            retval.append(src);
            retval.append("',false);");
            retval.append("xmlhttp.send();");
            retval.append("if (xmlhttp.status==200) { return xmlhttp.responseText; } else { return null; };");
        }

        return retval.toString();
    }
    
    private Node importNode(Document d, Node n, boolean deep) {
        Node r = cloneNode(d, n);
        if (deep) {
            NodeList nl = n.getChildNodes();
            for (int i = 0; i < nl.getLength(); i++) {
                Node n1 = importNode(d, nl.item(i), deep);
                r.appendChild(n1);
            }
        }
        return r;
    }

   private Node cloneNode(Document d, Node n) {
        Node r = null;
        switch (n.getNodeType()) {
            case Node.TEXT_NODE:
                r = d.createTextNode(((Text) n).getData());
                break;
            case Node.CDATA_SECTION_NODE:
                r = d.createCDATASection(((CDATASection) n).getData());
                break;
            case Node.ELEMENT_NODE:
                r = d.createElement(((Element) n).getTagName());
                NamedNodeMap map = n.getAttributes();
                for (int i = 0; i < map.getLength(); i++) {
                    ((Element) r).setAttribute(((Attr) map.item(i)).getName(),
                        ((Attr) map.item(i)).getValue());
                }
                break;
        }
        return r;
    }
}
