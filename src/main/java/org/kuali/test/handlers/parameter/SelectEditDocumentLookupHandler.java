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
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.test.CheckpointProperty;
import org.kuali.test.KualiApplication;
import org.kuali.test.Parameter;
import org.kuali.test.TestOperation;
import org.kuali.test.runner.execution.TestExecutionContext;
import org.kuali.test.utils.Constants;
import org.kuali.test.utils.Utils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class SelectEditDocumentLookupHandler extends AbstractParameterHandler {
    private static final Logger LOG = Logger.getLogger(SelectEditDocumentLookupHandler.class);
    
    @Override
    public String getDescription() {
        return "This handler will select an editable document from a lookup list based on the configured link parameter";
    }
    
    private Element findRow(Element table, int columnIndex, String columnIdentifierValue) {
        Element retval = null;
        
        NodeList rows = table.getElementsByTagName(Constants.HTML_TAG_TYPE_TR);
        
        if (rows != null) {
            for (int i = 0; i < rows.getLength(); ++i) {
                Element row = (Element) rows.item(i);
                
                NodeList cols = row.getElementsByTagName(Constants.HTML_TAG_TYPE_TD);
                
                if ((cols != null) && (cols.getLength() > columnIndex)) {
                    Element column = (Element)cols.item(columnIndex);
                    
                    if (columnIdentifierValue.equals(Utils.cleanDisplayText(column))) {
                        retval = row;
                        break;
                    }
                }
            }
        }
        
        return retval;
    }
    
    @Override
    public String getValue(TestExecutionContext tec, Document htmlDocument, CheckpointProperty cp, String executionParameterName) {
        String retval = null;

        Element table = findLookupTable(htmlDocument);

        if (table != null) {
            if (StringUtils.isNotBlank(executionParameterName)) {
                String columnIdentifierValue = getColumnIdentifierValue(tec, executionParameterName);
            
                if (StringUtils.isNotBlank(columnIdentifierValue)) {
                    Parameter columnNumber = Utils.getCheckpointPropertyTagParameter(cp, Constants.COLUMN_NUMBER);
            
                    if (columnNumber != null) {
                        Element row = findRow(table, Integer.parseInt(columnNumber.getValue()), columnIdentifierValue);
                        
                        if (row != null) {
                            NodeList columns = row.getElementsByTagName(Constants.HTML_TAG_TYPE_TD);
                            
                            if ((columns != null) && (columns.getLength() > 0)) {
                                retval = getDocumentIdFromColumn((Element)columns.item(0));
                                this.setCommentText("replacing " + cp.getPropertyValue() + " with " + retval);
                            }
                        }
                    }
                }
            }
        }

        return retval;
    }
    
    private String getDocumentIdFromColumn(Element column) {
        String retval = null;
        
        Element anchor = getAnchor(column);
        
        if (anchor != null) {
            Node hrefatt = anchor.getAttributeNode(Constants.HTML_TAG_ATTRIBUTE_HREF);
            
            if (hrefatt != null) {
                try {
                    List <NameValuePair> nvplist = Utils.getNameValueParameterListFromUrl(retval);
                    
                    if (nvplist != null) {
                        for (NameValuePair nvp : nvplist) {
                            if (isDocumentIdParameter(nvp)) {
                                retval = nvp.getValue();
                                break;
                            }
                        }
                    }
                } 
                
                
                catch (UnsupportedEncodingException ex) {
                    LOG.error(ex.toString(), ex);
                }
            }
        }
        
        
        return retval;
    }
    
    private boolean isDocumentIdParameter(NameValuePair nvp) {
        boolean retval = false;
        String tst = nvp.getName().toLowerCase();
        
        if (tst.contains("doc") && tst.contains("id") && !tst.contains(".")) {
            retval = StringUtils.isNumeric(nvp.getValue());
        }
        
        return retval;
    }
    
    protected String getColumnIdentifierValue(TestExecutionContext tec, String executionParameterName) {
        String retval = null;

        for (TestOperation top : tec.getCurrentTest().getOperations()) {
            if (top.getOperation().getTestExecutionParameter() != null) {
                if (executionParameterName.equals(top.getOperation().getTestExecutionParameter().getName())) {
                    retval = top.getOperation().getTestExecutionParameter().getValue();
                    break;
                }
            }
        }
        
        return retval;
    }
    
    protected Element findLookupTable(Document htmlDocument) {
        Element retval = null;

        retval = htmlDocument.getElementById(Constants.LOOKUP_RESULTS_TABLE_ID);
        
        if (retval == null) {
            NodeList l = htmlDocument.getDocumentElement().getElementsByTagName(Constants.HTML_TAG_TYPE_TABLE);

            for (int i = 0; i < l.getLength(); ++i) {
                Node node = l.item(i);
                Node classatt = node.getAttributes().getNamedItem(Constants.HTML_TAG_ATTRIBUTE_CLASS);
                Node idatt = node.getAttributes().getNamedItem(Constants.HTML_TAG_ATTRIBUTE_ID);
                
                if ((idatt != null) && (classatt != null) 
                    && idatt.getNodeValue().equals(Constants.LOOKUP_RESULTS_TABLE_ID)
                    && classatt.getNodeValue().equals(Constants.LOOKUP_RESULTS_TABLE_CLASS)) {
                    retval = (Element)node;
                    break;
                }
            }
        }
        
        return retval;
    }

    @Override
    public boolean isValidForApplication(KualiApplication.Enum app) {
        return (KualiApplication.KFS.equals(app) || KualiApplication.KC.equals(app));
    }

    @Override
    public boolean isReplaceByValue() {
        return true;
    }
    
    protected Element getAnchor(Element columnZero) {
        Element retval = null;
        
        NodeList l = columnZero.getElementsByTagName(Constants.HTML_TAG_TYPE_ANCHOR);

        if ((l != null) && (l.getLength() > 0)) {
            for (int i = 0; i < l.getLength(); ++i) {
                Node node = l.item(i);
                String txt = Utils.cleanDisplayText(node);
                
                if (StringUtils.isNotBlank(txt)) {
                    if (txt.toLowerCase().contains(Constants.EDIT)) {
                        retval = (Element)node;
                        break;
                    }
                    
                }
            }
            
            if (retval == null) {
                retval = (Element)l.item(0);
            }
        }
        
        return retval;
    }
}
