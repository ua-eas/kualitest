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

package org.kuali.test.ui.components.dialogs;

import com.gargoylesoftware.htmlunit.util.NameValuePair;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.test.HtmlRequestOperation;
import org.kuali.test.RequestParameter;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.ui.base.BaseSetupDlg;
import org.kuali.test.ui.base.BaseTable;
import org.kuali.test.ui.base.TableConfiguration;
import org.kuali.test.ui.components.panels.TablePanel;
import org.kuali.test.ui.components.labels.DataDisplayLabel;
import org.kuali.test.ui.utils.UIUtils;
import org.kuali.test.utils.Constants;
import org.kuali.test.utils.UpdateableNameValuePair;
import org.kuali.test.utils.Utils;

/**
 *
 * @author rbtucker
 */
public class HtmlRequestDetailsDlg extends BaseSetupDlg {
    private static final Logger LOG = Logger.getLogger(HtmlRequestDetailsDlg.class);
    private List <NameValuePair> urlParameters = new ArrayList<NameValuePair>();

    /**
     * 
     * @param mainFrame
     * @param parentDlg
     * @param htmlRequestOperation 
     */
    public HtmlRequestDetailsDlg(TestCreator mainFrame, JDialog parentDlg, HtmlRequestOperation htmlRequestOperation) {
        super(mainFrame, parentDlg);
        setTitle("Html Request Details");
        initComponents(htmlRequestOperation);
    }

    private void initComponents(HtmlRequestOperation htmlRequestOperation) {
        String[] labels = {
            "Method",
            "URL",
        };
        
        String url = htmlRequestOperation.getUrl();
        String urlparams = null;
        
        int pos = url.indexOf(Constants.SEPARATOR_QUESTION);
        
        if (pos > -1) {
            urlparams = url.substring(pos+1);
            url = url.substring(0, pos);
        }
        
        JComponent[] components = {
            new DataDisplayLabel(htmlRequestOperation.getMethod()), 
            new DataDisplayLabel(url)
        };

        
        getContentPane().add(UIUtils.buildEntryPanel(labels, components), BorderLayout.NORTH);
        
        if (StringUtils.isNotBlank(urlparams)) {
            try {
                List <NameValuePair> l = Utils.getNameValuePairsFromUrlEncodedParams(urlparams);
                for (NameValuePair p : l) {
                    urlParameters.add(new UpdateableNameValuePair(p));
                }
            } 
            
            catch (UnsupportedEncodingException ex) {
                LOG.warn(ex.toString(), ex);
            }
        }
        
        RequestParameter param = Utils.getContentParameter(htmlRequestOperation);
        
        if (param != null) {
            List <NameValuePair> l = null;
            
            if (Utils.isMultipart(htmlRequestOperation)) {
                l = Utils.getNameValuePairsFromMultipartParams(param.getValue());
            } else {
                try {
                    l = Utils.getNameValuePairsFromUrlEncodedParams(param.getValue());
                } catch (UnsupportedEncodingException ex) {
                    LOG.warn(ex.toString(), ex);
                }
            }
            
            if (l != null) {
                for (NameValuePair p : l) {
                    urlParameters.add(new UpdateableNameValuePair(p));
                }
            }
        }
        
        getContentPane().add(new TablePanel(getParametersTable(htmlRequestOperation)), BorderLayout.CENTER);

        addStandardButtons();
        setDefaultBehavior();
    }


    /**
     *
     * @return
     */
    @Override
    protected String getCancelText() {
        return Constants.CLOSE_ACTION;
    }
    
    private BaseTable getParametersTable(HtmlRequestOperation htmlRequestOperations) {
        TableConfiguration config = new TableConfiguration();
        config.setTableName("html-request-parameters-table");
        config.setDisplayName("Input Parameters");
        
        int[] alignment = new int[2];
        for (int i = 0; i < alignment.length; ++i) {
            alignment[i] = JLabel.LEFT;
        }
            
        Collections.sort(urlParameters, new Comparator<NameValuePair>() {
            @Override
            public int compare(NameValuePair o1, NameValuePair o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        
        config.setColumnAlignment(alignment);
        
        config.setHeaders(new String[] {
            "Name",
            "Value"
        });
        
        config.setPropertyNames(new String[] {
            "name",
            "value"
        });
            
        config.setColumnTypes(new Class[] {
            String.class,
            String.class
        });
        
        config.setColumnWidths(new int[] {
            30,
            30
        });

        config.setData(urlParameters);
        
        return new BaseTable(config) {
            @Override
             public boolean isCellEditable(int row, int column) {
                 return (column == 1);
             }

            @Override
            public void setValueAt(Object aValue, int row, int column) {
                getSaveButton().setEnabled(true);
                UpdateableNameValuePair nvp = (UpdateableNameValuePair)urlParameters.get(row);
                nvp.setValue((String)aValue);
                getModel().fireTableRowsUpdated(row, row);
            }
         };
    }
    
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(700, 400);
    }

    /**
     *
     * @return
     */
    @Override
    protected String getDialogName() {
        return "html-request-details";
    }

    /**
     *
     * @return
     */
    @Override
    protected boolean save() {
        setSaved(true);
        dispose();
        return true;
    }

    @Override
    public boolean isResizable() {
        return true;
    }

    @Override
    protected String getSaveText() {
        return Constants.UPDATE_ACTION;
    }

    @Override
    protected boolean getInitialSavedState() {
        return false;
    }

    public List<NameValuePair> getUrlParameters() {
        return urlParameters;
    }
}
