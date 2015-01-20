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
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.test.CheckpointProperty;
import org.kuali.test.Parameter;
import org.kuali.test.TestHeader;
import org.kuali.test.comparators.CheckpointPropertyComparator;
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
    private Map<String, List<CheckpointProperty>> checkPointMap;
    private boolean empty = false;
    private boolean singleSelectMode;
    private BaseSetupDlg parentDialog;
    private JWebBrowser webBrowser;
    private JComboBox containers;
    private JTabbedPane tabbedPane;
    private List<ListSelectionListener> listSelectionListeners = new ArrayList <ListSelectionListener>();
    
    public HtmlCheckpointPanel (BaseSetupDlg parentDialog, JWebBrowser webBrowser, 
        final TestHeader testHeader, boolean singleSelectMode) {
        this.singleSelectMode = singleSelectMode;
        this.parentDialog = parentDialog;
        this.webBrowser = webBrowser;
        initComponents(loadDocumentInformation(testHeader));
        parentDialog.stopSpinner();
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
        TestHeader testHeader) {
        this(parentDialog, webBrowser, testHeader, false);
    }
    
    private void initComponents(HtmlDomProcessor.DomInformation dominfo) {
        if (dominfo != null) {
            setName(Constants.DEFAULT_HTML_PROPERTY_GROUP);
            checkPointMap = loadCheckpointMap(dominfo.getCheckpointProperties());

            if (LOG.isDebugEnabled()) {
                LOG.debug("labelNodes.size(): " + dominfo.getLabelMap().size());
                LOG.debug("CheckpointProperty list size: " + dominfo.getCheckpointProperties().size());
                LOG.debug("CheckpointProperty map.size: " + checkPointMap.size());
            }

            // if we have more than 1 group then we will use tabs
            if (checkPointMap.size() > 1) {
                if (isUseTabContainers()) {
                    JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
                    p.add(containers = new JComboBox(getContainerNames()));
                    add(p, BorderLayout.NORTH);
                    
                    containers.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            loadTabs();
                        }
                    });
                }
                add(tabbedPane = new JTabbedPane(), BorderLayout.CENTER);
                loadTabs();
            } else if (checkPointMap.size() == 1) {
                CheckpointTable t;
                String key = checkPointMap.keySet().iterator().next();
                
                if (singleSelectMode) {
                    t = buildParameterTableForSingleSelect(key, checkPointMap.get(key));
                } else {
                    t = buildParameterTable(key, checkPointMap.get(key));
                }
                TablePanel p = new TablePanel(t);
                t.getSelectionModel().addListSelectionListener(this);
                add(p, BorderLayout.CENTER);
            } else {
                add(new JLabel("No checkpoint properties found", JLabel.CENTER), BorderLayout.CENTER);
                empty = true;
            }
        } else {
            UIUtils.showError(parentDialog, "HTML Parse Error", "Unable to parse web browser HTML");
        }
        
        parentDialog.validate();
    }
    
    private void loadTabs() {
        tabbedPane.removeAll();
        
        List<String[]> tabNames = getTabNames();
        
        for (String s[] : tabNames) {
            if (!Constants.DEFAULT_HTML_PROPERTY_GROUP.equals(s[0])) {
                List<CheckpointProperty> props = checkPointMap.get(s[1]);

                if ((props != null) && !props.isEmpty()) {
                    CheckpointTable t = null ;
                    if (singleSelectMode) {
                        t = buildParameterTableForSingleSelect(s[1], props);
                    } else {
                        t = buildParameterTable(s[1], props);
                    }
                    t.getSelectionModel().addListSelectionListener(this);
                    tabbedPane.addTab(s[0], new TablePanel(t));
                }
            }
        }
        
        validate();
    }
    
    private String formatTabName(String input) {
        String retval = input;
        
        if (StringUtils.isNotBlank(input)) {
            int pos = input.indexOf("|");
            
            if (pos > -1) {
                retval = input.substring(pos+1);
            }
        }
        
        return retval;
    }
    
    private String[] getContainerNames() {
        Set <String> names = new HashSet<String>();
        
        for (String s : checkPointMap.keySet()) {
            int pos = s.indexOf("|");
            names.add(s.substring(0, pos));
        }

        String[] retval = names.toArray(new String[names.size()]);
        
        Arrays.sort(retval);
        
        return retval;
    }
    
    
    private List <String[]> getTabNames() {
        List <String[]> retval = new ArrayList<String[]>();
        
        if (containers != null) {
            String container = ((String)containers.getSelectedItem() + "|");
            
            for (String s : checkPointMap.keySet()) {
                if (s.startsWith(container) && !s.endsWith("|" + Constants.DEFAULT_HTML_PROPERTY_GROUP)) {
                    retval.add(new String[] {formatTabName(s), s});
                }
            }
        } else {
            for (String s : checkPointMap.keySet()) {
                retval.add(new String[] {s, s});
            }
        }
        
        Collections.sort(retval, new Comparator <String[]>() {
            @Override
            public int compare(String[] s1, String[] s2) {
                return s1[0].compareTo(s2[0]);
            }
        });
        
        return retval;
    }
    
    private boolean isUseTabContainers() {
        boolean retval = false;
        for (String s : checkPointMap.keySet()) {
            if (s.contains("|")) {
                retval = true;
                break;
            }
        }
        
        return retval;
    }
    
    private String buildTabKey(CheckpointProperty cp, boolean useTabContainer) {
        StringBuilder retval = new StringBuilder(128);
        
        if (useTabContainer) {
            if (StringUtils.isNotBlank(cp.getPropertyGroupContainer())) {
                retval.append(cp.getPropertyGroupContainer());
            } else {
                retval.append(cp.getPropertyGroup());
            }

            retval.append("|");
        }

        retval.append(cp.getPropertyGroup());

        
        return retval.toString();
    }
    
    private Map<String, List<CheckpointProperty>> loadCheckpointMap(List<CheckpointProperty> checkpointProperties) {
        Map<String, List<CheckpointProperty>> retval = new HashMap<String, List<CheckpointProperty>>();

        boolean useTabContainer = false;

        for (CheckpointProperty cp : checkpointProperties) {
            if (StringUtils.isNotBlank(cp.getPropertyGroupContainer())) {
                useTabContainer = true;
                break;
            }
        }
        
        for (CheckpointProperty cp : checkpointProperties) {
            String key = buildTabKey(cp, useTabContainer);
            
            List<CheckpointProperty> l = retval.get(key);
            if (l == null) {
                retval.put(key, l = new ArrayList<CheckpointProperty>());
            }
    
            if (Utils.isFormInputTag(cp)) {
                updatePropertyValueIfRequired(cp);
            }
            
            l.add(cp);
        }

        CheckpointPropertyComparator c = new CheckpointPropertyComparator();
        
        for (List<CheckpointProperty> cpl : retval.values()) {
            Collections.sort(cpl, c);
        }

        return retval;
    }
    
    private String getCurrentDomValueById(CheckpointProperty cp, String iframeIds, String id) {
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
        
        Parameter param = Utils.getCheckpointPropertyTagParameter(cp, Constants.HTML_TAG_ATTRIBUTE_TYPE);
        
        if ((param != null) && Constants.HTML_INPUT_ATTRIBUTE_TYPE_CHECKBOX.equals(param.getValue())) {
            s.append("').checked");
        } else {
            s.append("').value");
        }
        
        Object o = webBrowser.executeJavascriptWithResult(s.toString());
        
        if ((o != null) && StringUtils.isNotBlank(o.toString())) {
            retval = o.toString();
        }
        
        return retval;
    }
         
    private String getCurrentDomValueByName(CheckpointProperty cp, String iframeIds, String name) {
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
            
            s.append(".contentWindow.document.getElementsByName('");
        } 
        
        s.append(name);
        
        Parameter param = Utils.getCheckpointPropertyTagParameter(cp, Constants.HTML_TAG_ATTRIBUTE_TYPE);
        
        if ((param != null) && Constants.HTML_INPUT_ATTRIBUTE_TYPE_CHECKBOX.equals(param.getValue())) {
            s.append("')[0].checked");
        } else {
            s.append("')[0].value");
        }
        
        Object o = webBrowser.executeJavascriptWithResult(s.toString());
        
        if ((o != null) && StringUtils.isNotBlank(o.toString())) {
            retval = o.toString();
        }
        
        return retval;
    }
    
    private void updatePropertyValueIfRequired(CheckpointProperty cp) {
        if (StringUtils.isBlank(cp.getPropertyValue())) {
            Parameter nameParam = Utils.getCheckpointPropertyTagParameter(cp, Constants.HTML_TAG_ATTRIBUTE_ID);
            Parameter iframeIdParam = Utils.getCheckpointPropertyTagParameter(cp, Constants.IFRAME_IDS);
            String iframeIds = null;
            if (iframeIdParam != null) {
                iframeIds = iframeIdParam.getValue();
            }
            
            String currentValue = null;
            
            if (nameParam != null) {
                currentValue = getCurrentDomValueById(cp, iframeIds, nameParam.getValue());
            } else {
                nameParam = Utils.getCheckpointPropertyTagParameter(cp, Constants.HTML_TAG_ATTRIBUTE_ID);
                
                if (nameParam != null) {
                    currentValue = getCurrentDomValueByName(cp, iframeIds, nameParam.getValue());
                }
            }

            if (StringUtils.isNoneBlank(currentValue)) {
                cp.setPropertyValue(currentValue);
            }
        }
    }
    
    private CheckpointTable buildParameterTableForSingleSelect(String tabkey, List<CheckpointProperty> checkpointProperties) {
        TableConfiguration config = new TableConfiguration();
        config.setTableName("cp2-" + tabkey.toLowerCase().replace(" ", "-").replace("|", "-"));
        config.setDisplayName("Available Properties");

        
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

    private CheckpointTable buildParameterTable(String tabkey, List<CheckpointProperty> checkpointProperties) {
        TableConfiguration config = new TableConfiguration();
        config.setTableName("cp-" + tabkey.toLowerCase().replace(" ", "-").replace("|", "-"));
        config.setDisplayName("Available Properties");

        
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

        for (List <CheckpointProperty> l : checkPointMap.values()) {
            for (CheckpointProperty p : l) {
                if (p.getSelected()) {
                    retval.add(p);
                    
                    if (singleSelectMode) {
                        break;
                    }
                }
            }
        }
    
        return retval;
    }

    private CheckpointProperty getTableCheckpointProperty(int indx) {
        CheckpointProperty retval = null;
        TablePanel tp = null;
        
        if (tabbedPane != null) {
            tp = (TablePanel)tabbedPane.getSelectedComponent();
        } else {
            BorderLayout layout = (BorderLayout)getLayout();
            tp = (TablePanel)layout.getLayoutComponent(BorderLayout.CENTER);
        }

        if (tp != null) {
            retval = (CheckpointProperty)tp.getTable().getModel().getData().get(indx);
        }

        return retval;
    }

    private void clearCurrentSelection() {
        if (tabbedPane != null) {
            int curindx = tabbedPane.getSelectedIndex();
            
            for (int i = 0; i < tabbedPane.getComponentCount(); ++i) {
                if (i != curindx) {
                    TablePanel tp = (TablePanel)tabbedPane.getComponentAt(i);
                    
                    if (tp.getTable().getSelectedRowCount() > 0) {
                        tp.getTable().getSelectionModel().clearSelection();

                        List <CheckpointProperty> l = tp.getData();
                        
                        if (l != null) {
                            for (CheckpointProperty cp : l) {
                                cp.setSelected(false);
                            }
                        }
                    }
                }
            }
        }
    }
    
    @Override
    public void valueChanged(ListSelectionEvent lse) {
        if (singleSelectMode) {
            clearCurrentSelection();
            CheckpointProperty selcp = getTableCheckpointProperty(lse.getFirstIndex());
            
            if (selcp != null)  {
                selcp.setSelected(true);
            }
        }
        
        for (ListSelectionListener l : listSelectionListeners) {
            l.valueChanged(lse);
        }
    }
    
    public void addListSelectionListener(ListSelectionListener l) {
        listSelectionListeners.add(l);
    }
    
    public List <CheckpointProperty> getAllCheckpointProperties() {
        List <CheckpointProperty> retval = new ArrayList<CheckpointProperty>();
        
        for (List <CheckpointProperty> l : checkPointMap.values()) {
            retval.addAll(l);
        }
        
        return retval;
    }
}
