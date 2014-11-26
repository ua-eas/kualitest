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

package org.kuali.test.ui.components.panels;

import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.test.CheckpointProperty;
import org.kuali.test.Parameter;
import org.kuali.test.TestHeader;
import org.kuali.test.comparators.CheckpointPropertyComparator;
import org.kuali.test.comparators.HtmlCheckpointTabComparator;
import org.kuali.test.ui.base.BasePanel;
import org.kuali.test.ui.base.BaseSetupDlg;
import org.kuali.test.ui.base.TableConfiguration;
import org.kuali.test.ui.components.dialogs.CheckpointTable;
import org.kuali.test.ui.utils.UIUtils;
import org.kuali.test.utils.Constants;
import org.kuali.test.utils.HtmlDomProcessor;
import org.kuali.test.utils.JWebBrowserDocumentGenerator;
import org.kuali.test.utils.Utils;
import org.w3c.dom.Document;


public class HtmlCheckpointPanel extends BasePanel implements ListSelectionListener {
    private static Logger LOG = Logger.getLogger(HtmlCheckpointPanel.class);
    private List <CheckpointTable> checkpointTables = new ArrayList<CheckpointTable>();
    private boolean empty = false;
    private boolean singleSelectMode;
    private BaseSetupDlg parentDialog;
    private JWebBrowser webBrowser;
    
    private List <ListSelectionListener> listeners = new ArrayList <ListSelectionListener>();

    public HtmlCheckpointPanel (final BaseSetupDlg parentDialog, final JWebBrowser webBrowser, 
        final TestHeader testHeader, boolean singleSelectMode) {
        this.singleSelectMode = singleSelectMode;
        this.parentDialog = parentDialog;
        this.webBrowser = webBrowser;
        parentDialog.getMainframe().startSpinner("Loading available checkpoint properties...");
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                initComponents(loadDocumentInformation(testHeader));
                parentDialog.getMainframe().stopSpinner();
            }
        });
    }

    private HtmlDomProcessor.DomInformation loadDocumentInformation(TestHeader testHeader) {
        HtmlDomProcessor.DomInformation retval = null;
        Document doc = JWebBrowserDocumentGenerator.getInstance().generate(webBrowser);
        if (doc != null) {
            retval = HtmlDomProcessor.getInstance().processDom(Utils.findPlatform(parentDialog.getMainframe().getConfiguration(), testHeader.getPlatformName()), doc);
        }
        
        return retval;
    }
    public HtmlCheckpointPanel (BaseSetupDlg parentDialog, JWebBrowser webBrowser, 
        TestHeader testHeader, String html) {
        this(parentDialog, webBrowser, testHeader, false);
    }
    
    private void initComponents(HtmlDomProcessor.DomInformation dominfo) {
        if (dominfo != null) {
            setName(Constants.DEFAULT_HTML_PROPERTY_GROUP);
        
            Map<String, List<CheckpointProperty>> pmap = loadCheckpointMap(dominfo.getCheckpointProperties());

            if (LOG.isDebugEnabled()) {
                LOG.debug("labelNodes.size(): " + dominfo.getLabelMap().size());
                LOG.debug("CheckpointProperty list size: " + dominfo.getCheckpointProperties().size());
                LOG.debug("CheckpointProperty map.size: " + pmap.size());
            }

            // if we have more than 1 group then we will use tabs
            if (pmap.size() > 1) {
                JTabbedPane tp = new JTabbedPane();
                List<String> tabNames = new ArrayList(pmap.keySet());
                Collections.sort(tabNames, new HtmlCheckpointTabComparator());

                for (String s : tabNames) {
                    if (!Constants.DEFAULT_HTML_PROPERTY_GROUP.equals(s)) {
                        List<CheckpointProperty> props = pmap.get(s);

                        if ((props != null) && !props.isEmpty()) {
                            CheckpointTable t = null ;
                            if (singleSelectMode) {
                                t = buildParameterTableForSingleSelect(s, props);
                            } else {
                                t = buildParameterTable(s, props);
                            }
                            checkpointTables.add(t);
                            t.getSelectionModel().addListSelectionListener(this);
                            tp.addTab(s, new TablePanel(t));
                        }
                    }
                }

                add(tp, BorderLayout.CENTER);
            } else if (pmap.size() == 1) {
                CheckpointTable t;
                if (singleSelectMode) {
                    t = buildParameterTableForSingleSelect(getName(), pmap.get(Constants.DEFAULT_HTML_PROPERTY_GROUP));
                } else {
                    t = buildParameterTable(getName(), pmap.get(Constants.DEFAULT_HTML_PROPERTY_GROUP));
                }
                TablePanel p = new TablePanel(t);
                checkpointTables.add(t);
                add(p, BorderLayout.CENTER);
                t.getSelectionModel().addListSelectionListener(this);
            } else {
                add(new JLabel("No checkpoint properties found", JLabel.CENTER), BorderLayout.CENTER);
                empty = true;
            }
        } else {
            UIUtils.showError(parentDialog, "HTML Parse Error", "Unable to parse web browser HTML");
        }
    }

    private Map<String, List<CheckpointProperty>> loadCheckpointMap(List<CheckpointProperty> checkpointProperties) {
        Map<String, List<CheckpointProperty>> retval = new HashMap<String, List<CheckpointProperty>>();

        for (CheckpointProperty cp : checkpointProperties) {
            List<CheckpointProperty> l = retval.get(cp.getPropertyGroup());

            if (l == null) {
                retval.put(cp.getPropertyGroup(), l = new ArrayList<CheckpointProperty>());
            }
    
            updatePropertyValue(cp);
            
            if (!singleSelectMode || StringUtils.isNotBlank(cp.getPropertyValue())) {
                l.add(cp);
            }
        }

        CheckpointPropertyComparator c = new CheckpointPropertyComparator();
        
        // if singleSelectMode load only properties with values
        for (List<CheckpointProperty> cpl : retval.values()) {
            Collections.sort(cpl, c);
        }

        return retval;
    }
    
    private String getCurrentDomValueById(String iframeIds, String id) {
        String retval = null;
        StringBuilder s = new StringBuilder(256);
        
        s.append("return document.getElementById('");
        if (StringUtils.isNotBlank(iframeIds)) {
            StringTokenizer st = new StringTokenizer(iframeIds, ",");
            s.append(st.nextToken());
            s.append("')");
            while (st.hasMoreTokens()) {
                s.append(".contentWindow.document.getElementById('");
                s.append(st.nextToken());
                s.append("')");
            }
            
            s.append(".contentWindow.document.getElementById('");
        } 
        
        s.append(id);
        s.append("').value");
        
        Object o = webBrowser.executeJavascriptWithResult(s.toString());
        
        if ((o != null) && StringUtils.isNotBlank(o.toString())) {
            retval = o.toString();
        }
        
        return retval;
    }
         
    private String getCurrentDomValueByName(String iframeIds, String name) {
        String retval = null;
        StringBuilder s = new StringBuilder(256);
        
        s.append("return document.getElementsById('");
        if (StringUtils.isNotBlank(iframeIds)) {
            StringTokenizer st = new StringTokenizer(iframeIds, ",");
            s.append(st.nextToken());
            s.append("')");
            while (st.hasMoreTokens()) {
                s.append(".contentWindow.document.getElementById('");
                s.append(st.nextToken());
                s.append("')");
            }
            
            s.append(".contentWindow.document.getElementsByName('");
        } 
        
        s.append(name);
        s.append("')[0].value");
        
        Object o = webBrowser.executeJavascriptWithResult(s.toString());
        
        if ((o != null) && StringUtils.isNotBlank(o.toString())) {
            retval = o.toString();
        }
        
        return retval;
    }
    
    private void updatePropertyValue(CheckpointProperty cp) {
        if (StringUtils.isBlank(cp.getPropertyValue())) {
            Parameter nameParam = Utils.getCheckpointPropertyTagParameter(cp, Constants.HTML_TAG_ATTRIBUTE_ID);
            Parameter iframeIdParam = Utils.getCheckpointPropertyTagParameter(cp, "iframe-ids");
            String iframeIds = null;
            if (iframeIdParam != null) {
                iframeIds = iframeIdParam.getValue();
            }
            
            String currentValue = null;
            
            if (nameParam != null) {
                currentValue = getCurrentDomValueById(iframeIds, nameParam.getValue());
            } else {
                nameParam = Utils.getCheckpointPropertyTagParameter(cp, Constants.HTML_TAG_ATTRIBUTE_ID);
                
                if (nameParam != null) {
                    currentValue = getCurrentDomValueByName(iframeIds, nameParam.getValue());
                }
            }

            if (StringUtils.isNoneBlank(currentValue)) {
                cp.setPropertyValue(currentValue);
            }
        }
    }
    
    private CheckpointTable buildParameterTableForSingleSelect(String groupName, List<CheckpointProperty> checkpointProperties) {
        TableConfiguration config = new TableConfiguration();
        config.setTableName("html-checkpoint-properties2");
        config.setDisplayName("Available Properties - " + groupName);

        
        int[] alignment = new int[4];
        for (int i = 0; i < alignment.length; ++i) {
            alignment[i] = JLabel.LEFT;
        }

        config.setColumnAlignment(alignment);

        config.setHeaders(new String[]{
            "Section",
            "Property Name",
            "Display Name",
            "Value",
        });

        config.setPropertyNames(new String[]{
            "propertySection",
            "propertyName",
            "displayName",
            "propertyValue",
        });

        config.setColumnTypes(new Class[]{
            String.class,
            String.class,
            String.class,
            String.class
        });

        config.setColumnWidths(new int[]{
            70,
            100,
            100,
            50,
        });

        config.setData(checkpointProperties);
        return new CheckpointTable(config, singleSelectMode);
    }

    private CheckpointTable buildParameterTable(String groupName, List<CheckpointProperty> checkpointProperties) {
        TableConfiguration config = new TableConfiguration();
        config.setTableName("html-checkpoint-properties");
        config.setDisplayName("Checkpoint Properties - " + groupName);

        
        int[] alignment = new int[7];
        for (int i = 0; i < alignment.length; ++i) {
            if (i == 0) {
                alignment[i] = JLabel.CENTER;
            } else {
                alignment[i] = JLabel.LEFT;
            }
        }

        config.setColumnAlignment(alignment);

        config.setHeaders(new String[]{
            "Use",
            "Section",
            "Property Name",
            "Type",
            "Operator",
            "Value",
            "On Failure"
        });

        config.setPropertyNames(new String[]{
            "selected",
            "propertySection",
            "displayName",
            "valueType",
            "operator",
            "propertyValue",
            "onFailure"
        });

        config.setColumnTypes(new Class[]{
            Boolean.class,
            String.class,
            String.class,
            String.class,
            String.class,
            String.class,
            String.class
        });

        config.setColumnWidths(new int[]{
            15,
            60,
            100,
            40,
            20,
            50,
            75
        });

        config.setData(checkpointProperties);
        return new CheckpointTable(config, singleSelectMode);
    }


    public boolean isEmpty() {
        return empty;
    }

    public List <CheckpointProperty> getSelectedProperties() {
        List <CheckpointProperty> retval = new ArrayList<CheckpointProperty>();

        for (CheckpointTable t : checkpointTables) {
            if (singleSelectMode) {
                if (t.getSelectedRow() > -1) {
                    retval.add((CheckpointProperty)t.getRowData(t.getSelectedRow()));
                    break;
                }

            } else {
                for (CheckpointProperty p : (List<CheckpointProperty>)t.getTableData()) {
                    if (p.getSelected()) {
                        retval.add(p);
                    }
                }
            }
        }
    
        return retval;
    }

    @Override
    public void valueChanged(ListSelectionEvent lse) {
        if (singleSelectMode) {
            for (CheckpointTable t : checkpointTables) {
                if (t.getSelectionModel() != lse.getSource()) {
                    t.clearSelection();
                }
            }
        }
        
        for (ListSelectionListener l : listeners) {
            l.valueChanged(lse);
        }
    }
    
    public void addListSelectionListener(ListSelectionListener l) {
        listeners.add(l);
    }
}
