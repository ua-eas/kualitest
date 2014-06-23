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

import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.test.Checkpoint;
import org.kuali.test.CheckpointProperty;
import org.kuali.test.CheckpointType;
import org.kuali.test.TestHeader;
import org.kuali.test.comparators.CheckpointPropertyComparator;
import org.kuali.test.comparators.HtmlCheckpointTabComparator;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.ui.base.BasePanel;
import org.kuali.test.ui.base.BaseSetupDlg;
import org.kuali.test.ui.base.TableConfiguration;
import org.kuali.test.ui.components.panels.TablePanel;
import org.kuali.test.ui.utils.UIUtils;
import org.kuali.test.utils.Constants;
import org.kuali.test.utils.HtmlDomProcessor;
import org.kuali.test.utils.Utils;

/**
 *
 * @author rbtucker
 */
public class HtmlCheckPointDlg extends BaseSetupDlg {

    private static final Logger LOG = Logger.getLogger(HtmlCheckPointDlg.class);
    private final TestHeader testHeader;
    private Checkpoint checkpoint;
    private JTextField name;
    List <CheckpointTable> checkpointTables = new ArrayList<CheckpointTable>();

    /**
     *
     * @param mainFrame
     * @param testHeader
     * @param rootNode
     * @param labelNodes
     */
    public HtmlCheckPointDlg(TestCreator mainFrame, TestHeader testHeader, JWebBrowser webBrowser, String html) {
        super(mainFrame);
        this.testHeader = testHeader;

        if (checkpoint != null) {
            setTitle("Edit checkpoint " + checkpoint.getName());
            setEditmode(true);
        } else {
            setTitle("Add new checkpoint");
            checkpoint = Checkpoint.Factory.newInstance();
            checkpoint.setName("new checkpoint");
            checkpoint.setTestName(testHeader.getTestName());
            checkpoint.setType(CheckpointType.HTTP);
        }

        initComponents(webBrowser, html);
    }

    private void initComponents(JWebBrowser webBrowser, String html) {
        String[] labels = new String[]{
            "Checkpoint Name"
        };

        name = new JTextField(checkpoint.getName(), 30);
        name.setEditable(!isEditmode());

        JComponent[] components = new JComponent[]{
            name,};

        BasePanel p = new BasePanel(getMainframe());

        addStandardButtons();

        p.add(UIUtils.buildEntryPanel(labels, components), BorderLayout.NORTH);

        BasePanel propertyContainer = buildPropertyContainer(webBrowser, html);

        p.add(propertyContainer, BorderLayout.CENTER);

        getContentPane().add(p, BorderLayout.CENTER);

        setDefaultBehavior();
        setResizable(true);
    }

    private BasePanel buildPropertyContainer(final JWebBrowser webBrowser, final String html) {
        final BasePanel retval = new BasePanel(getMainframe());
        retval.setName(Constants.DEFAULT_HTML_PROPERTY_GROUP);
        HtmlDomProcessor.DomInformation dominfo 
            = HtmlDomProcessor.getInstance().processDom(Utils.findPlatform(getMainframe().getConfiguration(), 
                testHeader.getPlatformName()), webBrowser, html);

        //processNode(groupStack, labelMap, checkpointProperties, new HashSet<String>(), rootNode);

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
                        CheckpointTable t = buildParameterTable(s, props, true);
                        checkpointTables.add(t);
                        tp.addTab(s, new TablePanel(t));
                    }
                }
            }

            retval.add(tp, BorderLayout.CENTER);

        } else if (pmap.size() == 1) {
            TablePanel p = new TablePanel(buildParameterTable(retval.getName(), pmap.get(Constants.DEFAULT_HTML_PROPERTY_GROUP), false));
            retval.add(p, BorderLayout.CENTER);
        } else {
            retval.add(new JLabel("No checkpoint properties found", JLabel.CENTER), BorderLayout.CENTER);
        }

        getSaveButton().setEnabled(!JLabel.class.equals(retval.getCenterComponent().getClass()));

        return retval;
    }

    private Map<String, List<CheckpointProperty>> loadCheckpointMap(List<CheckpointProperty> checkpointProperties) {
        Map<String, List<CheckpointProperty>> retval = new HashMap<String, List<CheckpointProperty>>();

        for (CheckpointProperty cp : checkpointProperties) {
            List<CheckpointProperty> l = retval.get(cp.getPropertyGroup());

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

    private CheckpointTable buildParameterTable(String groupName, List<CheckpointProperty> checkpointProperties, boolean forTab) {
        TableConfiguration config = new TableConfiguration();
        config.setTableName("html-checkpoint-properties");
        if (!forTab) {
            config.setDisplayName("Checkpoint Properties - " + groupName);
        }

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
        return new CheckpointTable(config);
    }

    /**
     *
     * @return
     */
    @Override
    protected boolean save() {
        boolean retval = false;
        boolean oktosave = true;
        List <CheckpointProperty> selectedProperties = getSelectedProperties();
        if (StringUtils.isNotBlank(name.getText())
            && !selectedProperties.isEmpty()) {
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
            checkpoint.setName(name.getText());
            
            checkpoint.addNewCheckpointProperties().setCheckpointPropertyArray(selectedProperties.toArray(new CheckpointProperty[selectedProperties.size()]));
            
            getConfiguration().setModified(true);
            setSaved(true);
            dispose();
            retval = true;
        }

        return retval;
    }
    
    private List <CheckpointProperty> getSelectedProperties() {
        List <CheckpointProperty> retval = new ArrayList<CheckpointProperty>();
    
        for (CheckpointTable t : checkpointTables) {
            for (CheckpointProperty p : (List<CheckpointProperty>)t.getTableData()) {
                if (p.getSelected()) {
                    retval.add(p);
                }
            }
        }

        return retval;
    }

    private boolean checkpointNameExists() {
        boolean retval = false;
        String newname = name.getText();
        
        return retval;
    }

    /**
     *
     * @return
     */
    @Override
    public Object getNewRepositoryObject() {
        return checkpoint;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(800, 400);
    }

    /**
     *
     * @return
     */
    @Override
    protected String getDialogName() {
        return "html-checkpoint-entry";
    }

    @Override
    public boolean isResizable() {
        return true;
    }

}
