/*
 * Checkstyle Beans: A NetBeans checkstyle integration plugin.
 * Copyright (C) 2007-1013  Petr Hejl
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package cz.sickboy.netbeans.checkstyle.options;

import cz.sickboy.netbeans.checkstyle.CheckstyleSettings;
import cz.sickboy.netbeans.checkstyle.Severity;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.JTextComponent;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.execution.NbClassPath;
import org.openide.explorer.propertysheet.PropertyPanel;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.PropertySupport;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbCollections;

/**
 * Class implementing the controller for the options dialog. Presents the
 * {@link CheckstyleOptionsPanel} as the configuration component.
 *
 * @author Petr Hejl
 */
public class CheckstyleOptionsController extends OptionsPanelController
        implements ActionListener {

    private static final FileFilter CONFIG_FILE_FILTER = new ConfigFileFilter();

    private static final FileFilter PROPERTY_FILE_FILTER = new PropertyFileFilter();

    private CheckstyleOptionsPanel panel;

    private volatile boolean initialized;

    private List<File> classpath = new ArrayList<File>();

    private Properties properties = new Properties();

    /**
     * Constructs the controller.
     */
    public CheckstyleOptionsController() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update() {
        CheckstyleSettings.Values values = CheckstyleSettings.getDefault().getValues();

        // TODO remove UI from controller
        panel.severityComboBox.setSelectedItem(values.getCustomSeverity());
        panel.configFileLocationField.setText(values.getCustomConfigFile());
        panel.propertyFileLocationField.setText(values.getCustomPropertyFile());

        classpath = new ArrayList<File>(values.getCustomClasspath());
        panel.classpathPanel.removeAll();
        PropertyPanel classpathPanel = new PropertyPanel(
                new ClasspathProperty(classpath), PropertyPanel.PREF_CUSTOM_EDITOR);
        panel.classpathPanel.add(classpathPanel);
        panel.classpathLabel.setLabelFor(classpathPanel);

        properties = new Properties();
        properties.putAll(values.getCustomProperties());
        panel.propertiesPanel.removeAll();
        PropertyPanel propertiesPanel = new PropertyPanel(
                new PropertiesProperty(properties), PropertyPanel.PREF_CUSTOM_EDITOR);
        panel.propertiesPanel.add(propertiesPanel);
        panel.propertiesLabel.setLabelFor(propertiesPanel);

        panel.ignoredPathsArea.setText(values.getIgnoredPathsPattern());

        initialized = true;
    }

    /**
     * {@inheritDoc}
     */
    public void applyChanges() {
        if (!initialized) {
            return;
        }

        CheckstyleSettings.Values values = new CheckstyleSettings.Values(
                (Severity) panel.severityComboBox.getSelectedItem(), panel.configFileLocationField.getText(),
                panel.propertyFileLocationField.getText(), classpath, properties, panel.ignoredPathsArea.getText());

        CheckstyleSettings.getDefault().setValues(values);
    }

    /**
     * {@inheritDoc}
     */
    public void cancel() {
        // nothing to do for now
    }

    /**
     * {@inheritDoc}
     */
    public boolean isChanged() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isValid() {
        return true;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns the {@link CheckstyleOptionsPanel}.
     */
    public JComponent getComponent(Lookup lookup) {
        if (panel == null) {
            panel = new CheckstyleOptionsPanel();
            panel.configFileBrowseButton.addActionListener(this);
            panel.propertyFileBrowseButton.addActionListener(this);
        }
        return panel;
    }

    /**
     * {@inheritDoc}
     */
    public HelpCtx getHelpCtx() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        // nothing to do for now
    }

    /**
     * {@inheritDoc}
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        // nothing to do for now
    }

    /**
     * {@inheritDoc}
     * <p>
     * Handles the action events coming from the panel.
     */
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == panel.configFileBrowseButton) {

            openFileDialog(NbBundle.getMessage(CheckstyleOptionsController.class,
                    "CheckstyleOptionsController.openConfigFileDialog"),
                    CONFIG_FILE_FILTER, panel.configFileLocationField);
        } else if (e.getSource() == panel.propertyFileBrowseButton) {

            openFileDialog(NbBundle.getMessage(CheckstyleOptionsController.class,
                    "CheckstyleOptionsController.openPropertyFileDialog"),
                    PROPERTY_FILE_FILTER, panel.propertyFileLocationField);
        }
    }

    private void openFileDialog(String title, FileFilter filter, JTextComponent component) {
        File oldFile = FileUtil.normalizeFile(new File(component.getText()));

        JFileChooser fileChooser = new JFileChooser(oldFile);
        fileChooser.setDialogTitle(title);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.addChoosableFileFilter(filter);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        int ret = fileChooser.showOpenDialog(SwingUtilities.getWindowAncestor(panel));
        if (ret == JFileChooser.APPROVE_OPTION) {
            File f = fileChooser.getSelectedFile();
            if (f != null) {
                component.setText(f.getAbsolutePath());
            }
        }
    }

    private static class ConfigFileFilter extends FileFilter {

        public boolean accept(File f) {
            return f.isDirectory() || f.getName().endsWith(".xml"); // NOI18N
        }

        public String getDescription() {
            return NbBundle.getMessage(CheckstyleOptionsController.class,
                    "CheckstyleOptionsController.xmlFileFilter");
            }
    }

    private static class PropertyFileFilter extends FileFilter {

        public boolean accept(File f) {
            return f.isDirectory() || f.getName().endsWith(".properties"); // NOI18N
        }

        public String getDescription() {
            return NbBundle.getMessage(CheckstyleOptionsController.class,
                    "CheckstyleOptionsController.propertyFileFilter");
            }
    }

    private static class ClasspathProperty extends PropertySupport.ReadWrite<NbClassPath> {

        private List<File> classpath;

        public ClasspathProperty(List<File> classpath) {
            super("classpath", NbClassPath.class, null, null); // NOI18N
            this.classpath = classpath;
        }

        public NbClassPath getValue() {
            return new NbClassPath(classpath.toArray(new File[classpath.size()]));
        }

        public void setValue(NbClassPath val)  {
            String cp = val.getClassPath();
            if (cp.startsWith("\"") && cp.endsWith("\"")) { // NOI18N
                cp = cp.substring(1, cp.length() - 1);
            }
            classpath.clear();
            for (String f : cp.split(Pattern.quote(File.pathSeparator))) {
                classpath.add(new File(f));
            }
        }
    }

    private static class PropertiesProperty extends PropertySupport.ReadWrite<Properties> {

        private Properties properties;

        public PropertiesProperty(Properties properties) {
            super("properties", Properties.class, null, null); // NOI18N
            this.properties = properties;
        }

        public Properties getValue() {
            Properties p = new Properties();
            p.putAll(properties);
            return p;
        }

        public void setValue(Properties val) {
            properties.clear();
            properties.putAll(NbCollections.checkedMapByCopy(val, String.class, String.class, true));
        }
    }
}
