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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Node;
import org.kuali.test.CheckpointProperty;
import org.kuali.test.Platform;
import org.kuali.test.TestExecutionParameter;
import org.kuali.test.TestHeader;
import org.kuali.test.comparators.HtmlCheckpointTabComparator;
import org.kuali.test.comparators.TestExecutionParameterComparator;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.handlers.HtmlTagHandler;
import org.kuali.test.ui.base.BasePanel;
import org.kuali.test.ui.base.BaseSetupDlg;
import org.kuali.test.ui.base.BaseTable;
import org.kuali.test.ui.base.TableConfiguration;
import static org.kuali.test.ui.components.dialogs.TestExecutionParameterDlg.LOG;
import org.kuali.test.ui.components.panels.TablePanel;
import org.kuali.test.ui.components.splash.SplashDisplay;
import org.kuali.test.utils.Constants;
import org.kuali.test.utils.Utils;


public class TestExecutionParamValueSelectDlg extends BaseSetupDlg {
    private TestExecutionParameter testExecutionParameter;
    private TestHeader testHeader;
    
    public TestExecutionParamValueSelectDlg(TestCreator mainframe, 
        JDialog parent, List <Node> labelNodes, Node rootNode, TestHeader testHeader) {
        super(mainframe, parent);
        this.testHeader = testHeader;
        initComponents(labelNodes, rootNode);
    }

    private void initComponents(final List <Node> labelNodes, final Node rootNode) {
        final BasePanel basePanel = new BasePanel(getMainframe());
       
        basePanel.setName(Constants.DEFAULT_HTML_PROPERTY_GROUP);
        
        new SplashDisplay(this, "Parsing HTML", "Parsing web page content...") {

           @Override
           protected void processCompleted() {
               basePanel.validate();
           }
            
            @Override
            protected void runProcess() {
                Map<String, String> labelMap = Utils.buildLabelMap(labelNodes);
                List<TestExecutionParameter> testExecutionParameters = new ArrayList<TestExecutionParameter>();

                Stack<String> groupStack = new Stack();
                
                groupStack.push(Constants.DEFAULT_HTML_PROPERTY_GROUP);

                processNode(groupStack, labelMap, testExecutionParameters, new HashSet<String>(), rootNode);

                Map<String, List<TestExecutionParameter>> pmap = loadParameterMap(testExecutionParameters);

                if (LOG.isDebugEnabled()) {
                    LOG.debug("labelNodes.size(): " + labelNodes.size());
                    LOG.debug("TestExecutionParameter list size: " + testExecutionParameters.size());
                    LOG.debug("TestExecutionParameter map.size: " + pmap.size());
                }

                // if we have more than 1 group then we will use tabs
                if (pmap.size() > 1) {
                    JTabbedPane tp = new JTabbedPane();
                    List<String> tabNames = new ArrayList(pmap.keySet());
                    Collections.sort(tabNames, new HtmlCheckpointTabComparator());

                    for (String s : tabNames) {
                        List<TestExecutionParameter> params = pmap.get(s);

                        if ((params != null) && !params.isEmpty()) {
                            BaseTable t = buildParameterTable(s, params, true);
                            tp.addTab(s, new TablePanel(t));
                        }
                    }

                    basePanel.add(tp, BorderLayout.CENTER);

                } else if (pmap.size() == 1) {
                    TablePanel p = new TablePanel(buildParameterTable(basePanel.getName(), pmap.get(Constants.DEFAULT_HTML_PROPERTY_GROUP), false));
                    basePanel.add(p, BorderLayout.CENTER);
                } else {
                    basePanel.add(new JLabel("No test execution parameters found", JLabel.CENTER), BorderLayout.CENTER);
                }

                getSaveButton().setEnabled(!JLabel.class.equals(basePanel.getCenterComponent().getClass()));
            }
        };
      
        addStandardButtons();
        getSaveButton().setEnabled(false);
        setDefaultBehavior();
    }
    
    @Override
    protected String getSaveText() {
        return Constants.OK_ACTION;
    }

    @Override
    protected String getDialogName() {
        return "test-execution-param-value-select";
    }

    @Override
    protected boolean save() {
        return true;
    }

    @Override
    public boolean isResizable() {
        return true;
    }

    @Override
    public Object getNewRepositoryObject() {
        return testExecutionParameter;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(800, 400);
    }
   
    private void processNode(Stack<String> groupStack,
        Map<String, String> labelMap,
        List<TestExecutionParameter> parameters,
        Set<String> processedNodes,
        Node node) {
        Platform platform = Utils.findPlatform(getMainframe().getConfiguration(), testHeader.getPlatformName());

        HtmlTagHandler th = Utils.getHtmlTagHandler(platform.getApplication().toString(), node);

        if (LOG.isDebugEnabled()) {
            LOG.debug("incoming node: " + node.nodeName() + " - id=" + node.attr("id") + ", name=" + node.attr("name"));
            if (th == null) {
                LOG.debug("no tag handler found");
            } else {
                LOG.debug("tag handler: " + th.getClass().getName());
            }
        }

        if (th != null) {
            if (th.isContainer(node)) {
                String groupName = th.getGroupName(node);
                if (StringUtils.isNotBlank(groupName)) {
                    groupStack.push(groupName);
                }

                if (LOG.isDebugEnabled()) {
                    LOG.debug("tag handler: " + th.getClass().getName()
                        + ", group name: " + groupStack.peek());
                }

                for (Node child : node.childNodes()) {
                    processNode(groupStack, labelMap, parameters, processedNodes, child);
                }

                if (StringUtils.isNotBlank(groupName)) {
                    groupStack.pop();
                }
            } else {
                CheckpointProperty cp = th.getCheckpointProperty(node);
                
                if ((cp != null) && !Utils.isNodeProcessed(processedNodes, node)) {
                    TestExecutionParameter param = TestExecutionParameter.Factory.newInstance();
                    
                    param.setGroup(groupStack.peek());
                    param.setSection(Utils.buildCheckpointSectionName(th, node));

                    if (th.getTagHandler().getLabelMatcher() != null) {
                        cp.setDisplayName(Utils.getMatchedNodeText(th.getTagHandler().getLabelMatcher().getTagMatcherArray(), node));
                    } else if (labelMap.containsKey(cp.getPropertyName())) {
                        cp.setDisplayName(labelMap.get(cp.getPropertyName()));
                    }

                    if (StringUtils.isNotBlank(cp.getDisplayName())) {
                        param.setDisplayName(cp.getDisplayName());
                        parameters.add(param);
                    } 
                }
            }
        } else {
            if (Utils.isValidContainerNode(node)) {
                for (Node child : node.childNodes()) {
                    processNode(groupStack, labelMap, parameters, processedNodes, child);
                }
            }
        }
    }

 

    private BaseTable buildParameterTable(String groupName, List<TestExecutionParameter> parameters, boolean forTab) {
        TableConfiguration config = new TableConfiguration();
        config.setTableName("test-execution-select-parameter-value");
        if (!forTab) {
            config.setDisplayName("Available HTML Objects - " + groupName);
        }

        int[] alignment = new int[3];
        for (int i = 0; i < alignment.length; ++i) {
            alignment[i] = JLabel.LEFT;
        }

        config.setColumnAlignment(alignment);

        config.setHeaders(new String[]{
            "Section",
            "Name",
            "Current Value"
        });

        config.setPropertyNames(new String[]{
            "section",
            "displayName",
            "value"
        });

        config.setColumnTypes(new Class[]{
            String.class,
            String.class,
            String.class
        });

        config.setColumnWidths(new int[]{
            60,
            100,
            40
        });

        config.setData(parameters);
        
        return new BaseTable(config);
    }
    
    private Map<String, List<TestExecutionParameter>> loadParameterMap(List<TestExecutionParameter> testExecutionParameters) {
        Map<String, List<TestExecutionParameter>> retval = new HashMap<String, List<TestExecutionParameter>>();

        for (TestExecutionParameter param : testExecutionParameters) {
            List<TestExecutionParameter> l = retval.get(param.getGroup());

            if (l == null) {
                retval.put(param.getGroup(), l = new ArrayList<TestExecutionParameter>());
            }

            l.add(param);
        }

        TestExecutionParameterComparator c = new TestExecutionParameterComparator();
        for (List<TestExecutionParameter> params : retval.values()) {
            Collections.sort(params, c);
        }

        return retval;
    }

}
