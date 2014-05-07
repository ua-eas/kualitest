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
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import org.apache.commons.lang3.StringUtils;
import org.kuali.test.Checkpoint;
import org.kuali.test.CheckpointProperty;
import org.kuali.test.TestHeader;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.ui.base.BaseSetupDlg;
import org.kuali.test.ui.base.TableConfiguration;
import org.kuali.test.utils.CheckpointPropertyComparator;
import org.kuali.test.utils.Constants;
import org.kuali.test.utils.HtmlCheckpointTabComparator;
import org.kuali.test.utils.Utils;

/**
 *
 * @author rbtucker
 */
public class HtmlCheckPointDlg extends BaseSetupDlg {
    private TestHeader testHeader;
    private Checkpoint checkpoint;
    private JTextField name;
    
    public HtmlCheckPointDlg(TestCreator mainFrame, TestHeader testHeader, Map <String, List<CheckpointProperty>> checkpointProperties) {
        this(mainFrame, testHeader, null, checkpointProperties);
    }
    
    public HtmlCheckPointDlg(TestCreator mainFrame, TestHeader testHeader, 
        Checkpoint checkpoint, Map <String, List<CheckpointProperty>> checkpointProperties) {
        super(mainFrame);
        this.testHeader= testHeader;
        this.checkpoint = checkpoint;
        
        if (checkpoint != null) {
            setTitle("Edit checkpoint " + checkpoint.getName());
            checkpointProperties = buildCheckpointMap(checkpoint.getCheckpointProperties().getCheckpointPropertyArray());
            setEditmode(true);
        } else {
            setTitle("Add new checkpoint");
            this.checkpoint = Checkpoint.Factory.newInstance();
            this.checkpoint.setName("new checkpoint");
            this.checkpoint.setTestName(testHeader.getTestName());
        }
        
        initComponents(checkpointProperties);
    }
    
    private Map <String, List<CheckpointProperty>> buildCheckpointMap(CheckpointProperty[] checkpointProperties) {
        Map <String, List<CheckpointProperty>>  retval = new HashMap<String, List<CheckpointProperty>> ();
        
        for (CheckpointProperty checkpointProperty : checkpointProperties) {
            List <CheckpointProperty> l = retval.get(checkpointProperty.getGroup());
            
            if (l == null) {
                retval.put(checkpointProperty.getGroup(), l = new ArrayList<CheckpointProperty>());
            }
            
            l.add(checkpointProperty);
        }
        
        CheckpointPropertyComparator comp = new CheckpointPropertyComparator();
        
        for ( List <CheckpointProperty> l : retval.values()) {
            Collections.sort(l, comp);
        }
        
        return retval;
    }

    @SuppressWarnings("unchecked")
    private void initComponents(Map <String, List<CheckpointProperty>> checkpointProperties) {
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

        List <String> tabnames = new ArrayList<String>(checkpointProperties.keySet());

        Collections.sort(tabnames, new HtmlCheckpointTabComparator());
        JTabbedPane tabbedPane = new JTabbedPane();
        
        for (String tabname : tabnames) {
            JPanel p2 = new JPanel(new BorderLayout());
            List <CheckpointProperty> l = checkpointProperties.get(tabname);
            
            if ((l != null) && !l.isEmpty()) {
                p2.add(new JScrollPane(buildParameterTable(l)), BorderLayout.CENTER);
                tabbedPane.add(Utils.buildHtmlDisplayName(Constants.GROUP, l.get(0).getGroup()), p2);
            }
        }
        
        p.add(tabbedPane, BorderLayout.CENTER);
        getContentPane().add(p, BorderLayout.CENTER);

        addStandardButtons();
        setDefaultBehavior();
        setResizable(true);
    }
    
    private CheckpointTable buildParameterTable(List <CheckpointProperty> checkpointProperties) {
        TableConfiguration config = new TableConfiguration();
        config.setTableName("html-checkpoint-properties");
        config.setDisplayName("Checkpoint Properties - " + Utils.buildHtmlDisplayName(Constants.GROUP, checkpointProperties.get(0).getGroup()));
        
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
            "subgroup",
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
