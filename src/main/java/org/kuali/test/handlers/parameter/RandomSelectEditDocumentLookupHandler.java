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
import org.apache.log4j.Logger;
import org.kuali.test.CheckpointProperty;
import org.kuali.test.Parameter;
import org.kuali.test.TestExecutionParameter;
import org.kuali.test.runner.execution.TestExecutionContext;
import org.kuali.test.utils.Constants;
import org.kuali.test.utils.Utils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class RandomSelectEditDocumentLookupHandler extends SelectEditDocumentLookupHandler  {
    private static final Logger LOG = Logger.getLogger(RandomSelectEditDocumentLookupHandler.class);

    @Override
    public String getDescription() {
        return "This handler will randomly select an editable document from a lookup list";
    }
    
    @Override
    public String getValue(TestExecutionContext tec, TestExecutionParameter tep, Document htmlDocument, CheckpointProperty cp) {
        String retval = null;
        
        Element table = findLookupTable(htmlDocument);
        
        if (table != null) {
            NodeList rows = table.getElementsByTagName(Constants.HTML_TAG_TYPE_TR);
            
            if ((rows != null) && (rows.getLength() > 0)) {
                int indx = getRandomIndex(rows.getLength());

                Element selrow = (Element)rows.item(indx); 
                NodeList cols = selrow.getElementsByTagName(Constants.HTML_TAG_TYPE_TD);
                
                if ((cols != null) && (cols.getLength() > 0)) {
                    retval = getValue(tep, cp, (Element)cols.item(0));
                    setCommentText("randomly selected value " + retval + " from row " + indx);
                }
            }
        }
        
        return retval;
    }
    
    private String getValue(TestExecutionParameter tep, CheckpointProperty cp, Element columnZero) {
        String retval = null;
        
        Element anchor = getAnchor(columnZero);
        
        if (anchor != null) {
            Node att = anchor.getAttributeNode(Constants.HTML_TAG_ATTRIBUTE_HREF);
            
            if (att != null) {
                try {
                    List <NameValuePair> nvplist = Utils.getNameValuePairsFromUrlEncodedParams(att.getNodeValue());
                    
                    if ((nvplist != null) && !nvplist.isEmpty()) {
                        for (NameValuePair nvp : nvplist) {
                            if (isDocumentIdParameter(nvp)) {
                                // look at the original anchor to find the replace by value document id
                                // set this in the checkpoint property value for comparison
                                Parameter param  =  Utils.getCheckpointPropertyTagParameter(tep.getValueProperty(), Constants.ANCHOR_PARAMETERS);
                                if (param != null) {
                                    nvplist = Utils.getNameValuePairsFromUrlEncodedParams(param.getValue());

                                    if ((nvplist != null) && !nvplist.isEmpty()) {
                                        for (NameValuePair nvp2 : nvplist) {
                                            if (nvp2.getName().equalsIgnoreCase(nvp.getName())) {
                                                tep.getValueProperty().setPropertyValue(nvp2.getValue());
                                                break;
                                            }
                                        }
                                    }
                                }
                                
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

    @Override
    public boolean isReplaceByValue() {
        return true;
    }
}
