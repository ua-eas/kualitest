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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.test.CheckpointProperty;
import org.kuali.test.ComparisonOperator;
import org.kuali.test.Platform;
import org.kuali.test.handlers.HtmlTagHandler;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

public class HtmlDomProcessor {

    private static final Logger LOG = Logger.getLogger(HtmlDomProcessor.class);
    private static HtmlDomProcessor instance;

    private HtmlDomProcessor() {
    }

    public static HtmlDomProcessor getInstance() {
        if (instance == null) {
            instance = new HtmlDomProcessor();
        }

        return instance;
    }

    public DomInformation processDom(Platform platform, String html) {
        return processDom(platform, null, html);
    }

    public DomInformation processDom(Platform platform, JWebBrowser webBrowser, String html) {
        List<Element> labelNodes = new ArrayList<Element>();
        DomInformation retval = new DomInformation(platform, Utils.buildLabelMap(labelNodes));
        retval.setCurrentNode(getHtmlRootNode(html, labelNodes, retval, webBrowser));
        processNode(retval);
        return retval;
    }

    private void processNode(DomInformation domInformation) {
        Element node = domInformation.getCurrentNode();

        HtmlTagHandler th = Utils.getHtmlTagHandler(domInformation.getPlatform().getApplication().toString(), node);

        if (th != null) {
            if (th.isContainer(node)) {
                String groupName = th.getGroupName(node);
                if (StringUtils.isNotBlank(groupName)) {
                    domInformation.getGroupStack().push(groupName);
                }

                for (Element child : Utils.getChildElements(node)) {
                    domInformation.setCurrentNode(child);
                    processNode(domInformation);
                }

                if (StringUtils.isNotBlank(groupName)) {
                    domInformation.getGroupStack().pop();
                }
            } else {
                CheckpointProperty cp = th.getCheckpointProperty(node);

                if ((cp != null) && !Utils.isNodeProcessed(domInformation.getProcessedNodes(), node)) {
                    if (StringUtils.isBlank(cp.getPropertyGroup())
                        || Constants.DEFAULT_HTML_PROPERTY_GROUP.equals(cp.getPropertyGroup())) {
                        cp.setPropertyGroup(domInformation.getGroupStack().peek());
                    }

                    cp.setPropertySection(Utils.buildCheckpointSectionName(th, node));

                    if (th.getTagHandler().getLabelMatcher() != null) {
                        cp.setDisplayName(Utils.getMatchedNodeText(th.getTagHandler().getLabelMatcher().getTagMatcherArray(), node));
                    } else if (domInformation.getLabelMap().containsKey(cp.getPropertyName())) {
                        cp.setDisplayName(Utils.trimString(domInformation.getLabelMap().get(cp.getPropertyName())));
                    }

                    if (StringUtils.isNotBlank(cp.getPropertyValue())) {
                        cp.setOperator(ComparisonOperator.EQUAL_TO);
                    }

                    if (StringUtils.isNotBlank(cp.getDisplayName()) && isValidSectionName(cp, th)) {
                        domInformation.getCheckpointProperties().add(cp);
                    }
                }
            }
        } else if (Utils.isValidContainerNode(node)) {
            for (Element child : Utils.getChildElements(node)) {
                domInformation.setCurrentNode(child);
                processNode(domInformation);
            }
        }
    }

    private boolean isValidSectionName(CheckpointProperty cp, HtmlTagHandler th) {
        boolean retval = true;

        // if section name is required then existence
        if (th.getTagHandler().getSectionNameRequired()) {
            if (StringUtils.isBlank(cp.getPropertySection())) {
                retval = false;
            } else {
                retval = StringUtils.isNotBlank(cp.getPropertySection().replaceAll(Constants.TAG_MATCH_REGEX_PATTERN, ""));
            }
        }

        // if a specified section name is required then check for match
        if (retval && StringUtils.isNotBlank(th.getTagHandler().getRequiredSectionName())) {
            if (StringUtils.isBlank(cp.getPropertySection())) {
                retval = false;
            } else {
                retval = Utils.isStringMatch(cp.getPropertySection().replaceAll(Constants.TAG_MATCH_REGEX_PATTERN, ""),
                    th.getTagHandler().getRequiredSectionName());
            }
        }

        return retval;
    }

    private Element getIframeBody(JWebBrowser webBrowser, Element iframeNode) {
        Element retval = null;

        String js = getJsIframeDataCall(iframeNode);

        Object o = webBrowser.executeJavascriptWithResult(js);

        // if we get html back then clean and get the iframe body node
        if (o != null) {
            retval = Utils.tidify(o.toString()).getDocumentElement();
        }

        return retval;
    }

    public Element getDomDocumentElement(String html) {
        return Utils.tidify(html).getDocumentElement();
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

    private Element getHtmlRootNode(String html, List<Element> labelNodes, DomInformation domInfo, JWebBrowser webBrowser) {
        Document doc = Utils.tidify(html);
        traverseNode(doc, doc.getDocumentElement(), domInfo, webBrowser, labelNodes);

        if (LOG.isDebugEnabled()) {
            Utils.printDom(doc);
        }

        return doc.getDocumentElement();
    }

    private void traverseNode(Document doc, Element parentNode, DomInformation domInfo, JWebBrowser webBrowser, List<Element> labelNodes) {
        NodeList children = parentNode.getChildNodes();
        
        for (int i = 0; i < children.getLength() ; ++i) {
            Node node = children.item(i);
            
            if (node instanceof Element) {
                Element childNode = (Element)node;
                if (LOG.isDebugEnabled()) {
                    LOG.debug("child node: " + childNode.getNodeName());
                }

                String nodeId = (Constants.NODE_ID + domInfo.getNextNodeId());
                childNode.setAttribute(Constants.NODE_ID, nodeId);

                // if this tag is an iframe we will load by javascript call
                // we will replace the iframe with a div and the contents of the
                // iframe body tag
                if (Constants.HTML_TAG_TYPE_IFRAME.equalsIgnoreCase(childNode.getNodeName())) {
                    if (webBrowser != null) {
                        Element iframeBody = getIframeBody(webBrowser, childNode);

                        if (iframeBody != null) {
                            // create the div
                            Element div = doc.createElement(Constants.HTML_TAG_TYPE_DIV);
                            div.setAttribute(Constants.NODE_ID, nodeId);
                            
                            // replace the iframe with the div in the dom
                            childNode.getParentNode().replaceChild(div, childNode);       
                            
                            // get the iframe body tag
                            NodeList l = iframeBody.getElementsByTagName(Constants.HTML_TAG_TYPE_BODY);

                            if (l.getLength() > 0) {
                                Element body = (Element) l.item(0);
                                l = body.getChildNodes();
                                
                                // import all the child nodes (with deep copy) of the iframe body
                                for (int j = 0; j < l.getLength(); ++j) {
                                    div.appendChild(importNode(doc, l.item(j), true));
                                }
                            }
                            
                            childNode = div;
                        }
                    }
                } else if (Constants.HTML_TAG_TYPE_LABEL.equalsIgnoreCase(childNode.getNodeName())) {
                    String att = childNode.getAttribute(Constants.HTML_TAG_ATTRIBUTE_FOR);

                    if (StringUtils.isNotBlank(att)) {
                        labelNodes.add(childNode);
                    }
                }

                traverseNode(doc, childNode, domInfo, webBrowser, labelNodes);
            }
        }
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

    public class DomInformation {

        private Platform platform;
        private Element currentNode;
        private Set processedNodes;
        private Map<String, String> labelMap;
        private List<CheckpointProperty> checkpointProperties;
        private Stack<String> groupStack;
        int nodeid = 1;

        public DomInformation(Platform platform, Map<String, String> labelMap) {
            this.platform = platform;
            groupStack = new Stack();
            groupStack.push(Constants.DEFAULT_HTML_PROPERTY_GROUP);
            this.labelMap = labelMap;
            checkpointProperties = new ArrayList<CheckpointProperty>();
            processedNodes = new HashSet<String>();
        }

        public Map<String, String> getLabelMap() {
            return labelMap;
        }

        public void setLabelMap(Map<String, String> labelMap) {
            this.labelMap = labelMap;
        }

        public List<CheckpointProperty> getCheckpointProperties() {
            return checkpointProperties;
        }

        public void setCheckpointProperties(List<CheckpointProperty> checkpointProperties) {
            this.checkpointProperties = checkpointProperties;
        }

        public Stack<String> getGroupStack() {
            return groupStack;
        }

        public void setGroupStack(Stack<String> groupStack) {
            this.groupStack = groupStack;
        }

        public Platform getPlatform() {
            return platform;
        }

        public void setPlatform(Platform platform) {
            this.platform = platform;
        }

        public Element getCurrentNode() {
            return currentNode;
        }

        public void setCurrentNode(Element currentNode) {
            this.currentNode = currentNode;
        }

        public Set getProcessedNodes() {
            return processedNodes;
        }

        public void setProcessedNodes(Set processedNodes) {
            this.processedNodes = processedNodes;
        }

        public String getNextNodeId() {
            return ("" + (nodeid++));
        }
    }
}
