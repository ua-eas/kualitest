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
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.openmbean.OpenMBeanAttributeInfoSupport;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.test.JmxConnection;
import org.kuali.test.creator.TestCreator;
import org.kuali.test.ui.base.BaseSetupDlg;
import org.kuali.test.ui.base.BaseTable;
import org.kuali.test.ui.base.TableConfiguration;
import org.kuali.test.ui.components.panels.TablePanel;
import org.kuali.test.ui.components.renderers.CheckboxTableCellRenderer;
import org.kuali.test.ui.utils.UIUtils;
import org.kuali.test.utils.Utils;

/**
 *
 * @author rbtucker
 */
public class JmxDlg extends BaseSetupDlg {
    private static final Logger LOG = Logger.getLogger(JmxDlg.class);
    private JmxConnection jmx;
    private JTextField name;
    private JTextField jmxUrl;
    private JTextField username;
    private JPasswordField password;
    private JTabbedPane tabbedPane;
    
    private TabInfo[] tabInfo = {
        new TabInfo("Memory", ManagementFactory.MEMORY_MXBEAN_NAME),
        new TabInfo("Operating System", ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME),
        new TabInfo("Thread", ManagementFactory.THREAD_MXBEAN_NAME)
    };

    /**
     *
     * Creates new form PlatformDlg
     *
     * @param mainFrame
     */
    public JmxDlg(TestCreator mainFrame) {
        this(mainFrame, null);
    }

    /**
     * Creates new form JmxDlg
     *
     * @param mainFrame
     * @param jmx
     */
    public JmxDlg(TestCreator mainFrame, JmxConnection jmx) {
        super(mainFrame);
        this.jmx = jmx;
        if (jmx != null) {
            setTitle("Edit JMX connection " + jmx.getName());
            setEditmode(true);
        } else {
            setTitle("Add new JMX connection");
            this.jmx = JmxConnection.Factory.newInstance();
            this.jmx.setName("new jmx connection");
        }

        initComponents();
    }

    private void initComponents() {

        String[] labels = {
            "Name",
            "JMX URL",
            "User Name",
            "Password"
        };

        name = new JTextField(jmx.getName(), 20);
        name.setEditable(!isEditmode());

        jmxUrl = new JTextField(jmx.getJmxUrl(), 30);
        username = new JTextField(jmx.getUsername(), 20);

        String pass = "";
        if (StringUtils.isNotBlank(jmx.getPassword())) {
            try {
                pass = Utils.decrypt(getMainframe().getEncryptionPassword(), jmx.getPassword());
            } catch (UnsupportedEncodingException ex) {
                UIUtils.showError(this, "Decrypt Exception", "Password decryption failed");
            }

        }

        password = new JPasswordField(pass, 20);
        
        JComponent[] components = {name, jmxUrl, username, password};

        getContentPane().add(UIUtils.buildEntryPanel(labels, components), BorderLayout.NORTH);

        JPanel p = new JPanel(new BorderLayout());
        
        JButton b = new JButton("Refresh JMX Attributes");
        JPanel p2 = new JPanel(new FlowLayout(FlowLayout.CENTER, 1, 6));
        p2.add(b);
        p.add(p2, BorderLayout.NORTH);
        
        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadJMXData();
            }
        });
        
        
        tabbedPane = new JTabbedPane();
        for (int i = 0; i < tabInfo.length; ++i) {
            tabbedPane.addTab(tabInfo[i].getTabName(), new TablePanel(getMBeanTable(tabInfo[i].getJmxBeanName())));
        }
        
        p.add(tabbedPane, BorderLayout.CENTER);

        getContentPane().add(p, BorderLayout.CENTER);

        addStandardButtons();
        setDefaultBehavior();
    }

    /**
     *
     * @return
     */
    @Override
    protected boolean save() {
        boolean retval = false;

        try {
            boolean oktosave = true;
            if (StringUtils.isNotBlank(name.getText())
                && StringUtils.isNotBlank(jmxUrl.getText())) {

                if (!isEditmode()) {
                    if (jmxConnectionNameExists()) {
                        oktosave = false;
                        displayExistingNameAlert("JMX Connection", name.getText());
                    }
                }
            } else {
                displayRequiredFieldsMissingAlert("JMX Connection", "name, JMX url");
                oktosave = false;
            }

            if (oktosave) {
                if (!isEditmode()) {
                    if (getConfiguration().getJmxConnections() == null) {
                        getConfiguration().addNewJmxConnections();
                    }

                    jmx = getConfiguration().getJmxConnections().addNewJmxConnection();
                }

                jmx.setName(name.getText());
                jmx.setJmxUrl(jmxUrl.getText());

                if (StringUtils.isNotBlank(username.getText())) {
                    jmx.setUsername(username.getText());
                    jmx.setPassword(Utils.encrypt(getMainframe().getEncryptionPassword(), new String(password.getPassword())));
                } else {
                    jmx.setUsername("");
                    jmx.setPassword("");
                }

                setSaved(true);
                getConfiguration().setModified(true);
                dispose();
                retval = true;
            }
        } catch (Exception ex) {
            UIUtils.showError(this, "Save Error", "Error occurred while attempting to save JMX connection - " + ex.toString());
        }

        return retval;
    }

    private boolean jmxConnectionNameExists() {
        boolean retval = false;
        String newname = name.getText();

        for (String nm : getJmxConnectionNames()) {
            if (nm.equalsIgnoreCase(newname)) {
                retval = false;
                break;
            }
        }

        return retval;
    }

    /**
     *
     * @return
     */
    @Override
    public Object getNewRepositoryObject() {
        return jmx;
    }

    /**
     *
     * @return
     */
    @Override
    protected String getDialogName() {
        return "jmx-connection-setup";
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(700, 500);
    }

    private BaseTable getMBeanTable(String mxbeanName) {
        TableConfiguration config = new TableConfiguration();
        config.setTableName("jmx-performance-attributes");
        config.setDisplayName("JMX performance monitoring attributes");
        config.setColumnAlignment(new int[]{JLabel.CENTER, JLabel.LEFT, JLabel.LEFT, JLabel.LEFT});
        config.setColumnTypes(new Class[]{Boolean.class, String.class, String.class, String.class});
        config.setColumnWidths(new int[]{15, 30, 20, 150});

        config.setHeaders(new String[] {
            "Use",
            "Name",
            "Type",
            "Description"
        });

        config.setPropertyNames(new String[]{
            "selected",
            "name",
            "type",
            "description"
        });
        
        BaseTable retval = new BaseTable(config) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return (column == 0);
            }
            
        };

        retval.getColumnModel().getColumn(0).setCellRenderer(new CheckboxTableCellRenderer());
        
        return retval;
    }

    private MBeanServerConnection getMBeanConnection() throws MalformedURLException, IOException {
        MBeanServerConnection retval = null;

        if (StringUtils.isNotBlank(jmxUrl.getText())) {
            JMXServiceURL serviceUrl = new JMXServiceURL(jmxUrl.getText());

            Map map = null;

            if (StringUtils.isNotBlank(username.getText())) {
                map = new HashMap();
                map.put(JMXConnector.CREDENTIALS, new String[]{username.getText(), new String(password.getPassword())});
            }

            retval = JMXConnectorFactory.connect(serviceUrl, map).getMBeanServerConnection();
        }

        return retval;
    }

    private TablePanel getTablePanel(int indx) {
        return (TablePanel)tabbedPane.getComponentAt(indx);
    }
    
    private boolean isValidAttributeType(String type) {
        return ("int".equals(type)
            || "long".equals(type)
            || "double".equals(type));
    }
    
    public void loadJMXData() {
        try {
            MBeanServerConnection mbeanConn = getMBeanConnection();
            if (mbeanConn != null) {
                for (int i = 0; i < tabInfo.length; ++i) {
                    MBeanInfo mbeanInfo = mbeanConn.getMBeanInfo(new ObjectName(tabInfo[i].getJmxBeanName()));

                    if (mbeanInfo != null) {
                        for (MBeanAttributeInfo att : mbeanInfo.getAttributes()) {
                            AttributeWrapper aw = new AttributeWrapper(att);
                           
                            if (isValidAttributeType(aw.getType())) {
                                tabInfo[i].getAttributeInfo().add(aw);
                            }
                        }

                        getTablePanel(i).getTable().setTableData(tabInfo[i].getAttributeInfo());
                    }
                }
            }

          //   MemoryMXBean mbean = ManagementFactory.newPlatformMXBeanProxy(mbeanConn, ManagementFactory.MEMORY_MXBEAN_NAME, MemoryMXBean.class); 
        } catch (Exception ex) {
            LOG.error(ex.toString(), ex);
            UIUtils.showError(this, "JMX Error", "Error occurred during JMX connection - " + ex.toString());
        }
    }
    
    public class AttributeWrapper {
        private MBeanAttributeInfo att;
        private boolean selected = false;
        
        public AttributeWrapper (MBeanAttributeInfo att) {
            this.att = att;
        }
        
        public boolean getSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }
        
        public String getName() {
            return att.getName();
        }
        
        public String getDescription() {
            return att.getDescription();
        }
        
        public String getType() {
            String retval = att.getType();
            if (att instanceof OpenMBeanAttributeInfoSupport) {
                OpenMBeanAttributeInfoSupport ois = (OpenMBeanAttributeInfoSupport)att;
                if ("java.lang.management.MemoryUsage".equals(ois.getOpenType().getTypeName())) {
                    retval = "long";
                }
            }
            
            return retval;
        }
    }
    
    private class TabInfo {
        private String tabName;
        private String jmxBeanName;
        private List<AttributeWrapper> attributeInfo;
        
        public TabInfo(String tabName, String jmxBeanName) {
            this.tabName = tabName;
            this.jmxBeanName = jmxBeanName;
        }

        public String getTabName() {
            return tabName;
        }

        public void setTabName(String tabName) {
            this.tabName = tabName;
        }

        public String getJmxBeanName() {
            return jmxBeanName;
        }

        public void setJmxBeanName(String jmxBeanName) {
            this.jmxBeanName = jmxBeanName;
        }

        public List<AttributeWrapper> getAttributeInfo() {
            if (attributeInfo == null) {
                attributeInfo = new ArrayList<AttributeWrapper>();
            }
            return attributeInfo;
        }
    }
}
