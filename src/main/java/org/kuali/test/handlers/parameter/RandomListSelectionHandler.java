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

package org.kuali.test.handlers.parameter;

import com.gargoylesoftware.htmlunit.util.NameValuePair;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.test.CheckpointProperty;
import org.kuali.test.Parameter;
import org.kuali.test.runner.execution.TestExecutionContext;
import org.kuali.test.utils.Constants;
import org.kuali.test.utils.Utils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class RandomListSelectionHandler extends AbstractParameterHandler {
    private static final Logger LOG = Logger.getLogger(RandomListSelectionHandler.class);
    
    @Override
    public String getDescription() {
        return "This handler will save the current value from the selected link and replace all instances of this value found in future test runs with a randomly selected value from the list";
    }

    private Element findTableElement(Document htmlDocument, CheckpointProperty cp) {
        Element retval = null;
        Parameter param = Utils.getCheckpointPropertyTagParameter(cp, Constants.TABLE_ID);
        
        if (param != null) {
            retval = htmlDocument.getElementById(param.getValue());
        }
        
        if (retval == null) {
            param = Utils.getCheckpointPropertyTagParameter(cp, Constants.TABLE_NAME);
        
            if (param != null) {
                NodeList l = htmlDocument.getElementsByTagName(Constants.HTML_TAG_TYPE_TABLE);
                
                for (int i = 0; i < l.getLength(); ++i) {
                    Node node = l.item(i);
                    Node att = node.getAttributes().getNamedItem(Constants.HTML_TAG_ATTRIBUTE_NAME);
                    
                    if ((att != null) && param.getValue().equals(att.getNodeValue())) {
                        retval = (Element)node;
                        break;
                    }
                }    
            }
        }
        
        return retval;
    }
    
    @Override
    public String getValue(TestExecutionContext tec, Document htmlDocument, CheckpointProperty cp, String inputValue) {
        String retval = null;

        String tagName = getTagName(cp);
        
        // if the element is in a list or a table we will randomly select a new vale
        // else we will return the input value
        if (tagName != null) {
            if (Constants.HTML_TAG_TYPE_SELECT.equalsIgnoreCase(tagName)) {
               Element sel = findElement(htmlDocument, cp, tagName);
               
               if (sel != null) {
                   NodeList l = sel.getElementsByTagName(Constants.HTML_TAG_TYPE_OPTION);
                   if (l.getLength() > 0) {
                        int selectedIndex = 0;
                        for (int i = 0; i < l.getLength(); ++i) {
                            if (inputValue.equals(getNodeValue(l.item(i)))) {
                                selectedIndex = i;
                                break;
                            }
                        }
                        retval = getNodeValue(l.item(getRandomIndex(l.getLength(), selectedIndex)));
                    }
               }
            } else if (isTableElement(cp)) {
                Element table = findTableElement(htmlDocument, cp);
                
                if (table != null) {
                    Parameter param = Utils.getCheckpointPropertyTagParameter(cp, Constants.CHILD_NODE_INDEX);
                    
                    if ((param != null) && StringUtils.isNumeric(param.getValue())) {
                        NodeList rows = table.getElementsByTagName(Constants.HTML_TAG_TYPE_TR);
                        List <String> availableData = new ArrayList<String>();
                        int colindx = Integer.parseInt(param.getValue());
                        int selectedIndex = 0;
                        for (int i = 0; i < rows.getLength(); ++i) {
                            Node row = rows.item(i);

                            int pos = 0;
                            
                            NodeList cols = row.getChildNodes();
                            
                            for (int j = 0; j < cols.getLength(); ++j) {
                                Element col = (Element)cols.item(j);
                                
                                if (Constants.HTML_TAG_TYPE_TD.equals(col.getNodeName())) {
                                    if (pos == colindx) {
                                        String data = null;

                                        // handle anchors special need to pull parameters form href
                                        Node anchor = Utils.getFirstChildNodeByNodeName(col, Constants.HTML_TAG_TYPE_ANCHOR);
                                        if (anchor != null) {
                                            Node att = anchor.getAttributes().getNamedItem(Constants.HTML_TAG_ATTRIBUTE_HREF);
                                            
                                            if (att != null) {
                                                data = getParameterValueFromHref(tec, att.getNodeValue());
                                            }
                                        } else {
                                            data = Utils.cleanDisplayText(col);
                                        }
                                        
                                        if (!inputValue.equals(data)) {
                                            availableData.add(data);
                                        } else {
                                            selectedIndex = i;
                                        }

                                        break;
                                    }
                                    pos++;
                                }
                            }
                        }
                        
                        if (!availableData.isEmpty()) {
                            retval = availableData.get(this.getRandomIndex(availableData.size(), selectedIndex));
                        }
                    }
                }
            }
        }

        if (StringUtils.isBlank(retval)) {
            retval = inputValue;
        }
        
        return retval;
    }

    private String getParameterValueFromHref(TestExecutionContext tec, String href) {
        String retval = null;
        try {
            List <NameValuePair> l = Utils.getNameValueParameterListFromUrl(href);
            
            if (l != null) {
                for (NameValuePair nvp : l) {
                    if (!tec.getRandomListAccessParameterToIgnore().contains(nvp.getName())) {
                        retval = nvp.getValue();
                        break;
                    }
                }
            }
        } 
        
        catch (UnsupportedEncodingException ex) {
            LOG.error(ex.toString(), ex);
        }
        
        return retval;
    }
    
    
    private String getNodeValue(Node node) {
        String retval = null;
        
        Node att = node.getAttributes().getNamedItem(Constants.HTML_TAG_ATTRIBUTE_VALUE);
        if (att != null) {
            retval = att.getNodeValue();
        } else {
            retval = Utils.cleanDisplayText(node);
        }
        
        return retval;
    }
    
    private int getRandomIndex(int maxValue, int excludedValue) {
        int retval = excludedValue;
        Random randomGenerator = new Random(System.currentTimeMillis());
        while (retval == excludedValue) {
            retval = randomGenerator.nextInt(maxValue-1);
        }
        
        return retval;
    }
    
    private String getTagName(CheckpointProperty cp) {
        String retval = null;
        
        Parameter name = Utils.getCheckpointPropertyTagParameter(cp, Constants.TAG_NAME);
        
        if (name != null) {
            retval = name.getValue();
        }
            
        return retval;
    }

    private String getElementId(CheckpointProperty cp) {
        String retval = null;
        
        Parameter id = Utils.getCheckpointPropertyTagParameter(cp, Constants.HTML_TAG_ATTRIBUTE_ID);
        
        if (id != null) {
            retval = id.getValue();
        }
            
        return retval;
    }
    
    private String getElementName(CheckpointProperty cp) {
        String retval = null;
        
        Parameter name = Utils.getCheckpointPropertyTagParameter(cp, Constants.HTML_TAG_ATTRIBUTE_NAME);
        
        if (name != null) {
            retval = name.getValue();
        }
            
        return retval;
    }
    
    private String[] getIframeIds(CheckpointProperty cp) {
        String[] retval = new String[0];
        
        Parameter iframeIds = Utils.getCheckpointPropertyTagParameter(cp, Constants.IFRAME_IDS);
        
        if (iframeIds != null) {
            retval = iframeIds.getValue().split(",");
        }
        
        return retval;
    }
    
    private Element findElement(Document htmlDocument, CheckpointProperty cp, String tagName) {
        Element retval = null;

        String id = getElementId(cp);
        
        if (StringUtils.isNotBlank(id)) {
            retval = htmlDocument.getElementById(id);
        }
        
        if (retval == null) {
            String name = getElementName(cp);
            
            if (StringUtils.isNotBlank(name)) {
                NodeList l = htmlDocument.getDocumentElement().getElementsByTagName(tagName);
                
                for (int i = 0; i < l.getLength(); ++i) {
                    Node node = l.item(i);
                    Node att = node.getAttributes().getNamedItem(name);
                    if (name.equals(att.getNodeValue())) {
                        retval = (Element)node;
                        break;
                    }
                }
            }
        }
        return retval;
    }
    
    private boolean isTableElement(CheckpointProperty cp) {
        return ((Utils.getCheckpointPropertyTagParameter(cp, Constants.TABLE_ID) != null) 
            || (Utils.getCheckpointPropertyTagParameter(cp, Constants.TABLE_NAME) != null));
    }
}
