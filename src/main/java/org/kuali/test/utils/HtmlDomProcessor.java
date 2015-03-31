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
import org.kuali.test.Parameter;
import org.kuali.test.Platform;
import org.kuali.test.handlers.htmltag.HtmlTagHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

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

    public DomInformation processDom(Platform platform, Document document) {
        List<Element> labelNodes = new ArrayList<Element>();
        DomInformation retval = new DomInformation(platform, Utils.buildLabelMap(labelNodes));

        processDocument(document, labelNodes, retval);
        
        retval.setCurrentNode(document.getDocumentElement());
        
        processNode(retval);
        
        return retval;
    }

    private void processNode(DomInformation domInformation) {
        Element node = domInformation.getCurrentNode();
        
        HtmlTagHandler th = Utils.getHtmlTagHandler(domInformation.getPlatform().getApplication().toString(), node);
        if (th != null) {
            if (th.isContainer(node)) {
                String groupContainerName = th.getGroupContainerName(node);
                String groupName = th.getGroupName(node);
                
                if (StringUtils.isNotBlank(groupContainerName)) {
                    domInformation.getGroupContainerStack().push(groupContainerName);
                }

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
            
                if (StringUtils.isNotBlank(groupContainerName)) {
                    domInformation.getGroupContainerStack().pop();
                }
            } else {
                CheckpointProperty cp = th.getCheckpointProperty(node);
                if ((cp != null) && !Utils.isNodeProcessed(domInformation.getProcessedNodes(), node)) {
                    if (StringUtils.isBlank(cp.getPropertyGroupContainer()) && !domInformation.getGroupContainerStack().isEmpty()) {
                        cp.setPropertyGroupContainer(domInformation.getGroupContainerStack().peek());
                    }

                    if (StringUtils.isBlank(cp.getPropertyGroup())
                        || Constants.DEFAULT_HTML_PROPERTY_GROUP.equals(cp.getPropertyGroup())) {
                        cp.setPropertyGroup(domInformation.getGroupStack().peek());
                    }

                    cp.setPropertySection(Utils.buildCheckpointSectionName(th, node));

                    if (th.getTagHandler().getLabelMatcher() != null) {
                        String s = Utils.getMatchedNodeText(th.getTagHandler().getLabelMatcher().getTagMatcherArray(), node);
                        cp.setDisplayName(Utils.getMatchedNodeText(th.getTagHandler().getLabelMatcher().getTagMatcherArray(), node));
                    } else if (domInformation.getLabelMap().containsKey(cp.getPropertyName())) {
                        cp.setDisplayName(Utils.trimString(domInformation.getLabelMap().get(cp.getPropertyName())));
                    }

                    if (StringUtils.isNotBlank(cp.getPropertyValue())) {
                        cp.setOperator(ComparisonOperator.EQUAL_TO);
                    }

                    if (StringUtils.isNotBlank(cp.getDisplayName()) && isValidSectionName(cp, th)) {
                        cp.addNewTagInformation();

                        Parameter p = null;
                        if (Constants.HTML_TAG_TYPE_TD.equals(node.getTagName()) || Constants.HTML_TAG_TYPE_TH.equals(node.getTagName())) {
                            Element table = Utils.getParentNodeByTagName(node, Constants.HTML_TAG_TYPE_TABLE);

                            if (table != null) {
                                String tid = table.getAttribute(Constants.HTML_TAG_ATTRIBUTE_ID);
                                String tname = table.getAttribute(Constants.HTML_TAG_ATTRIBUTE_NAME);
                                
                                if (StringUtils.isNotBlank(tid)) {
                                    p = cp.getTagInformation().addNewParameter();
                                    p.setName(Constants.TABLE_ID);
                                    p.setValue(tid);
                                }

                                if (StringUtils.isNotBlank(tname)) {
                                    p = cp.getTagInformation().addNewParameter();
                                    p.setName(Constants.TABLE_NAME);
                                    p.setValue(tname);
                                }
                                
                                Element row = Utils.getParentNodeByTagName(node, Constants.HTML_TAG_TYPE_TR);
                                
                                if (row != null) {
                                    p = cp.getTagInformation().addNewParameter();
                                    p.setName(Constants.ROW_NUMBER);
                                    p.setValue("" + Utils.getChildNodeIndex(row));
                                }

                                p = cp.getTagInformation().addNewParameter();
                                p.setName(Constants.COLUMN_NUMBER);
                                p.setValue("" + Utils.getChildNodeIndex(node));
                            }
                            
                            Node anchor = Utils.getFirstChildNodeByNodeName(node, Constants.HTML_TAG_TYPE_ANCHOR);
                            
                            if (anchor != null) {
                                p = cp.getTagInformation().addNewParameter();
                                p.setName(Constants.ANCHOR_PARAMETERS);
                                p.setValue(getAnchorParameters(anchor));
                            }
                        }
                        
                        Element useNode = node;
                        
                        if (th.isInputWrapper(node)) {
                            useNode = th.getInputElement(node);
                        }

                        p = cp.getTagInformation().addNewParameter();
                        p.setName(Constants.TAG_NAME);
                        p.setValue(useNode.getTagName());
                        
                        String s = useNode.getAttribute(Constants.HTML_TAG_ATTRIBUTE_ID);
                        if (StringUtils.isNotBlank(s)) {
                            p = cp.getTagInformation().addNewParameter();
                            p.setName(Constants.HTML_TAG_ATTRIBUTE_ID);
                            p.setValue(s);
                        }
                        
                        s = useNode.getAttribute(Constants.HTML_TAG_ATTRIBUTE_NAME);
                        if (StringUtils.isNotBlank(s)) {
                            p = cp.getTagInformation().addNewParameter();
                            p.setName(Constants.HTML_TAG_ATTRIBUTE_NAME);
                            p.setValue(s);
                        }

                        s = useNode.getAttribute(Constants.HTML_TAG_ATTRIBUTE_TYPE);
                        if (StringUtils.isNotBlank(s)) {
                            p = cp.getTagInformation().addNewParameter();
                            p.setName(Constants.HTML_TAG_ATTRIBUTE_TYPE);
                            p.setValue(s);
                        }
                        
                        String iframeids = getIframeParentIds(node);
                        
                        if (StringUtils.isNotBlank(iframeids)) {
                            p = cp.getTagInformation().addNewParameter();
                            p.setName(Constants.IFRAME_IDS);
                            p.setValue(iframeids);
                        }

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
    
    private String getAnchorParameters(Node anchor) {
        String retval = "";
        
        Node att = anchor.getAttributes().getNamedItem(Constants.HTML_TAG_ATTRIBUTE_HREF);
        
        if (att != null) {
            String href = att.getNodeValue();
            
            if (StringUtils.isNotBlank(href)) {
                int pos = href.indexOf(Constants.SEPARATOR_QUESTION);
                
                if (pos > -1) {
                    retval = href.substring(pos+1);
                }
            }
        }
        
        return retval;
    }


    private String getIframeParentIds(Element node) {
        String retval = null;
        Node pnode = node.getParentNode();
        List <String> ids = new ArrayList<String>();
        
        while (pnode != null) {
            if (Constants.HTML_TAG_TYPE_IFRAME.equals(pnode.getNodeName())) {
                Node idnode = pnode.getAttributes().getNamedItem(Constants.HTML_TAG_ATTRIBUTE_ID);
                
                if (idnode != null) {
                    ids.add(idnode.getNodeValue());
                }
            }
            
            pnode = pnode.getParentNode();
        }
        
        if (!ids.isEmpty()) {
            StringBuilder s = new StringBuilder(128);
            String comma = "";
            for (int i = ids.size() - 1; i >= 0; --i) {
                s.append(comma);
                s.append(ids.get(i));
                comma = ",";
            }
            
            retval = s.toString();
        }
        
        return retval;
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

    public Element getDomDocumentElement(String html) {
        return Utils.cleanHtml(html).getDocumentElement();
    }
    
    private void processDocument(Document document, List<Element> labelNodes, DomInformation domInfo) {
        traverseNode(document.getDocumentElement(), domInfo, labelNodes);
    }

    private void traverseNode(Element parentNode, DomInformation domInfo,  List<Element> labelNodes) {
        for (Element childNode : Utils.getChildElements(parentNode)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("child node: " + childNode.getNodeName());
            }

            String nodeId = (Constants.NODE_ID + domInfo.getNextNodeId());
            childNode.setAttribute(Constants.NODE_ID, nodeId);

            if (Constants.HTML_TAG_TYPE_LABEL.equalsIgnoreCase(childNode.getNodeName())) {
                String att = childNode.getAttribute(Constants.HTML_TAG_ATTRIBUTE_FOR);

                if (StringUtils.isNotBlank(att)) {
                    labelNodes.add(childNode);
                }
            }

            traverseNode(childNode, domInfo, labelNodes);
        }
    }

    public class DomInformation {
        private Platform platform;
        private Element currentNode;
        private Set processedNodes;
        private Map<String, String> labelMap;
        private List<CheckpointProperty> checkpointProperties;
        private Stack<String> groupStack;
        private Stack<String> groupContainerStack;
        int nodeid = 1;

        public DomInformation(Platform platform, Map<String, String> labelMap) {
            this.platform = platform;
            groupStack = new Stack();
            groupContainerStack = new Stack();
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

        public Stack<String> getGroupContainerStack() {
            return groupContainerStack;
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
