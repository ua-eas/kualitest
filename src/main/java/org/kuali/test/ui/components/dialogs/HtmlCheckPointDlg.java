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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.kuali.test.Checkpoint;
import org.kuali.test.CheckpointProperty;
import org.kuali.test.TestHeader;
import org.kuali.test.comparators.CheckpointPropertyComparator;
import org.kuali.test.comparators.HtmlCheckpointTabComparator;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.handlers.HtmlTagHandler;
import org.kuali.test.ui.base.BasePanel;
import org.kuali.test.ui.base.BaseSetupDlg;
import org.kuali.test.ui.base.TableConfiguration;
import org.kuali.test.ui.components.panels.TablePanel;
import org.kuali.test.utils.Constants;
import org.kuali.test.utils.Utils;

/**
 *
 * @author rbtucker
 */
public class HtmlCheckPointDlg extends BaseSetupDlg {
    private static final Logger LOG = Logger.getLogger(HtmlCheckPointDlg.class);
    private TestHeader testHeader;
    private Checkpoint checkpoint;
    private JTextField name;
    
    public HtmlCheckPointDlg(TestCreator mainFrame, TestHeader testHeader, List<Node> rootNodes, List<Node>labelNodes) {
        super(mainFrame);
        this.testHeader= testHeader;
        
        if (checkpoint != null) {
            setTitle("Edit checkpoint " + checkpoint.getName());
            setEditmode(true);
        } else {
            setTitle("Add new checkpoint");
            this.checkpoint = Checkpoint.Factory.newInstance();
            this.checkpoint.setName("new checkpoint");
            this.checkpoint.setTestName(testHeader.getTestName());
        }
        
        initComponents(rootNodes, labelNodes);
    }
    
    @SuppressWarnings("unchecked")
    private void initComponents(List <Node> rootNodes, List <Node> labelNodes) {
        String[] labels = new String[] {
            "Checkpoint Name" 
        };
        
        name = new JTextField(checkpoint.getName(), 30);
        name.setEditable(!isEditmode());
        
        JComponent[] components = new JComponent[] {
            name,
        };
        
        JPanel p = new JPanel(new BorderLayout(3, 3));
        p.add(buildEntryPanel(labels, components), BorderLayout.NORTH);
        
        BasePanel propertyContainer = buildPropertyContainer(rootNodes, labelNodes);
        
        p.add(propertyContainer, BorderLayout.CENTER);
        getContentPane().add(p, BorderLayout.CENTER);

        addStandardButtons();
        
        // if propertyContainer is a JLabel then we did not find any checkpoint properties
        getSaveButton().setEnabled(!JLabel.class.equals(propertyContainer.getCenterComponent().getClass()));
        
        setDefaultBehavior();
        setResizable(true);
    }
    
    private BasePanel buildPropertyContainer(List <Node> rootNodes, List <Node> labelNodes) {
        BasePanel retval = new BasePanel(getMainframe());
        retval.setName(Constants.DEFAULT_HTML_PROPERTY_GROUP);
        
        Map <String, String> labelMap = buildLabelMap(labelNodes);
        List <CheckpointProperty> checkpointProperties = new ArrayList<CheckpointProperty>();

        for (Node node : rootNodes) {
            processNode(Constants.DEFAULT_HTML_PROPERTY_GROUP, labelMap, checkpointProperties, node);
        }

        Map <String, List <CheckpointProperty>> pmap = loadCheckpointMap(checkpointProperties);
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("rootNodes.size(): " + rootNodes.size() + ", labelNodes.size(): " + labelNodes.size());
            LOG.debug("CheckpointProperty list size: " + checkpointProperties.size());
            LOG.debug("CheckpointProperty map.size: " + pmap.size());
        }
        
        // if we have more than 1 group then we will use tabs
        if (pmap.size() > 1) {
            JTabbedPane tp = new JTabbedPane();
            List <String> tabNames = new ArrayList(pmap.keySet());
            Collections.sort(tabNames, new HtmlCheckpointTabComparator());
            
            for (String s : tabNames) {
                List <CheckpointProperty> props = pmap.get(s);
                
                if ((props != null) && !props.isEmpty()) {
                    tp.addTab(s, new TablePanel(this.buildParameterTable(s, props)));
                }
            }
            
            retval.add(tp, BorderLayout.CENTER);
            
        } else if (pmap.size() == 1) {
            TablePanel p = new TablePanel(buildParameterTable(retval.getName(), pmap.get(Constants.DEFAULT_HTML_PROPERTY_GROUP)));
            retval.add(p, BorderLayout.CENTER);
        } else {
            retval.add(new JLabel("No checkpoint properties found", JLabel.CENTER), BorderLayout.CENTER);
        }
    
        return retval;
    }
    
    private Map<String, List <CheckpointProperty>> loadCheckpointMap(List <CheckpointProperty> checkpointProperties) {
        Map<String, List <CheckpointProperty>> retval = new HashMap<String, List <CheckpointProperty>>();
        
        for (CheckpointProperty cp : checkpointProperties) {
            List <CheckpointProperty> l = retval.get(cp.getPropertyGroup());

            if (l == null) {
                retval.put(cp.getPropertyGroup(), l = new ArrayList<CheckpointProperty>());
            }

            l.add(cp);
        }

        CheckpointPropertyComparator c = new CheckpointPropertyComparator();
        for (List<CheckpointProperty> cpl : retval.values()) {
            Collections.sort(cpl, c);
        }

        return retval;
    }
    
    private void processNode(String groupName, Map<String, String> labelMap, List <CheckpointProperty> checkpointProperties, Node node) {
        HtmlTagHandler th = Utils.getHtmlTagHandler(node);
        
        if (th != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("tag handler: " + th.getTagHandler().getHandlerClassName());
            }
            
            if (th.isContainer()) {
                for (Node child : node.childNodes()) {
                    processNode(th.getGroupName(node), labelMap, checkpointProperties, child);
                }
            } else {
                CheckpointProperty cp = th.getCheckpointProperty(node);
                cp.setPropertyGroup(groupName);
                
                if (th.getTagHandler().getLabelMatcher() != null) {
                    cp.setDisplayName(Utils.getLabelText(th.getTagHandler().getLabelMatcher(), node));
                } else if (labelMap.containsKey(cp.getPropertyName())) {
                    cp.setDisplayName(labelMap.get(cp.getPropertyName()));
                }
                
                checkpointProperties.add(cp);
            }
        } else {
            for (Node child : node.childNodes()) {
                processNode(groupName, labelMap, checkpointProperties, child);
            }
        }
    }
    
    private Map <String, String> buildLabelMap(List <Node> labelNodes) {
        Map <String, String> retval = new HashMap<String, String>();
        
        for (Node label : labelNodes) {
            String key = label.attributes().get("for");
            
            if (StringUtils.isNotBlank(key) && (label instanceof Element)) {
                retval.put(key, Utils.cleanDisplayText(((Element)label).data()));
            }
        }
        
        return retval;
    }
    
    private CheckpointTable buildParameterTable(String groupName, List <CheckpointProperty> checkpointProperties) {
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
        
        config.setHeaders(new String[] {
            "Select",
            "Section",
            "Property Name",
            "Type",
            "Operator",
            "Value",
            "On Failure"
        });
        
        config.setPropertyNames(new String[] {
            "selected",
            "propertySection",
            "displayName",
            "valueType",
            "operator",
            "propertyValue",
            "onFailure"
        });
            
        config.setColumnTypes(new Class[] {
            Boolean.class,
            String.class,
            String.class,
            String.class,
            String.class,
            String.class,
            String.class
        });
        
        config.setColumnWidths(new int[] {
            15,
            60,
            100,
            40,
            20,
            50,
            75
        });

        config.setData(checkpointProperties);
        return new CheckpointTable(config);
    }
    
    @Override
    protected boolean save() {
        boolean retval = false;
        boolean oktosave = true;
        if (StringUtils.isNotBlank(name.getText())
            && haveSelectedParameters()) {
            if (!isEditmode()) {
                if (checkpointNameExists()) {
                    oktosave = false;
                    displayExistingNameAlert("Checkpoint", name.getText());
                }
            }
        } else {
            if (StringUtils.isBlank(name.getText())) {
                displayRequiredFieldsMissingAlert("Checkpoint", "name");
            } else {
                displayRequiredFieldsMissingAlert("Checkpoint", "parameter(s) entry");
            }
            
            oktosave = false;
        }
        
        if (oktosave) {
            if (!isEditmode()) {
            }
            
            getConfiguration().setModified(true);
            setSaved(true);
            dispose();
            retval = true;
        }
        
        return retval;
    }

    private boolean haveSelectedParameters() {
        return false;
    }
    
    private boolean checkpointNameExists() {
        boolean retval = false;
        String newname = name.getText();
        return retval;
    }

    @Override
    public Object getNewRepositoryObject() {
        return checkpoint;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(800, 400);
    }

    @Override
    protected String getDialogName() {
        return "html-checkpoint-entry";
    }

    @Override
    public boolean isResizable() {
        return true;
    }
}
