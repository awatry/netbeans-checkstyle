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
package cz.sickboy.netbeans.checkstyle;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;
import org.openide.util.NbPreferences;

/**
 * Settings for the Checkstyle plugin.
 *
 * @author Petr Hejl
 */
public final class CheckstyleSettings {

    /**
     * Marks the updated layout of properties without custom enabled switch.
     */
    static final String PROP_UPDATED =
        "cz.sickboy.netbeans.checkstyle.updated"; // NOI18N

    static final String PROP_CUSTOM_ENABLED =
        "cz.sickboy.netbeans.checkstyle.custom"; // NOI18N

    static final String PROP_SEVERITY =
        "cz.sickboy.netbeans.checkstyle.severity"; // NOI18N

    static final String PROP_CUSTOM_CONFIG_FILE =
        "cz.sickboy.netbeans.checkstyle.customFile"; // NOI18N

    static final String PROP_CUSTOM_PROPERTY_FILE =
        "cz.sickboy.netbeans.checkstyle.customPropertyFile"; // NOI18N

    static final String PROP_CUSTOM_CLASSPATH =
        "cz.sickboy.netbeans.checkstyle.customClasspath"; // NOI18N

    static final String PROP_CUSTOM_PROPERTIES =
        "cz.sickboy.netbeans.checkstyle.customProperties"; // NOI18N

    static final String PROP_IGNORED_PATHS_PATTERN =
        "cz.sickboy.netbeans.checkstyle.ignoredPathsPattern"; // NOI18N

    private static final Logger LOGGER = Logger.getLogger(CheckstyleSettings.class.getName());

    private static CheckstyleSettings instance;

    private final PropertyChangeSupport propertySupport = new PropertyChangeSupport(this);

    private final AtomicInteger listenerCount = new AtomicInteger(0);

    private CheckstyleSettings() {
        super();
    }

    public static synchronized CheckstyleSettings getDefault() {
        if (instance == null) {
            instance = new CheckstyleSettings();
        }
        return instance;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.addPropertyChangeListener(listener);
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.log(Level.FINER, "Listener count: {0}", listenerCount.incrementAndGet());
        }
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.removePropertyChangeListener(listener);
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.log(Level.FINER, "Listener count: {0}", listenerCount.decrementAndGet());
        }
    }

    public void setValues(Values values) {
        List<PropertyChangeEvent> events = new ArrayList<PropertyChangeEvent>();

        synchronized (this) {
            updateProperties();
            setCustomSeverity(values.getCustomSeverity(), events);
            setCustomConfigFile(values.getCustomConfigFile(), events);
            setCustomPropertyFile(values.getCustomPropertyFile(), events);
            setCustomClasspath(values.getCustomClasspath(), events);
            setCustomProperties(values.getCustomProperties(), events);
            setIgnoredPathsPattern(values.getIgnoredPathsPattern(), events);
        }

        for (PropertyChangeEvent event : events) {
            propertySupport.firePropertyChange(event);
        }
    }

    public Values getValues() {
        synchronized (this) {
            updateProperties();
            return new Values(getCustomSeverity(), getCustomConfigFile(),
                    getCustomPropertyFile(), getCustomClasspath(),
                    getCustomProperties(), getIgnoredPathsPattern());
        }
    }

    private synchronized void updateProperties() {
        // we intentionally do not fire events as there is no real change
        Preferences prefs = getPreferences();
        if (prefs.getBoolean(PROP_UPDATED, false)) {
            return;
        }

        if (!prefs.getBoolean(PROP_CUSTOM_ENABLED, false)) {
            prefs.remove(PROP_CUSTOM_CONFIG_FILE);
            prefs.remove(PROP_CUSTOM_PROPERTY_FILE);
            prefs.remove(PROP_CUSTOM_PROPERTIES);
            prefs.remove(PROP_CUSTOM_CLASSPATH);
        }

        prefs.remove(PROP_CUSTOM_ENABLED);
        prefs.putBoolean(PROP_UPDATED, true);
    }

    private void setCustomSeverity(Severity severity, List<PropertyChangeEvent> events) {
        Severity oldValue;
        synchronized (this) {
            oldValue = getCustomSeverity();
            getPreferences().put(PROP_SEVERITY, severity.name());
        }

        if (oldValue != severity) {
            events.add(new PropertyChangeEvent(this, PROP_SEVERITY, oldValue, severity));
        }
    }

    private synchronized Severity getCustomSeverity() {
        return Severity.valueOf(
                getPreferences().get(PROP_SEVERITY, Severity.IGNORE.name()));
    }

    private void setCustomConfigFile(String location, List<PropertyChangeEvent> events) {
        String oldValue;
        synchronized (this) {
            oldValue = getCustomConfigFile();
            if (location == null) {
                getPreferences().remove(PROP_CUSTOM_CONFIG_FILE);
            } else {
                getPreferences().put(PROP_CUSTOM_CONFIG_FILE, location);
            }
        }

        if (oldValue != location && (oldValue == null || !oldValue.equals(location))) {
            events.add(new PropertyChangeEvent(this, PROP_CUSTOM_CONFIG_FILE, oldValue, location));
        }
    }

    private synchronized String getCustomConfigFile() {
        return trimToNull(getPreferences().get(PROP_CUSTOM_CONFIG_FILE, null));
    }

    private void setCustomPropertyFile(String location, List<PropertyChangeEvent> events) {
        String oldValue;
        synchronized (this) {
            oldValue = getCustomPropertyFile();
            if (location == null) {
                getPreferences().remove(PROP_CUSTOM_PROPERTY_FILE);
            } else {
                getPreferences().put(PROP_CUSTOM_PROPERTY_FILE, location);
            }
        }

        if (oldValue != location && (oldValue == null || !oldValue.equals(location))) {
            events.add(new PropertyChangeEvent(this, PROP_CUSTOM_PROPERTY_FILE, oldValue, location));
        }
    }

    private synchronized String getCustomPropertyFile() {
        return trimToNull(getPreferences().get(PROP_CUSTOM_PROPERTY_FILE, null));
    }

    private void setCustomClasspath(List<File> classpath, List<PropertyChangeEvent> events) {
        String value = null;
        List<File> copied = Collections.emptyList();

        if (classpath != null && !classpath.isEmpty()) {
            copied = new ArrayList<File>(classpath);
            StringBuilder builder = new StringBuilder();

            for (File f : copied) {
                if (builder.length() > 0) {
                    builder.append(File.pathSeparatorChar);
                }
                builder.append(f);
            }
            value = builder.toString();
        }

        List<File> oldValue;
        synchronized (this) {
            oldValue = getCustomClasspath();
            if (value == null) {
                getPreferences().remove(PROP_CUSTOM_CLASSPATH);
            } else {
                getPreferences().put(PROP_CUSTOM_CLASSPATH, value);
            }
        }

        if (!isEqual(oldValue, copied)) {
            events.add(new PropertyChangeEvent(this, PROP_CUSTOM_CLASSPATH, oldValue, copied));
        }
    }

    private List<File> getCustomClasspath() {
        String value;
        synchronized (this) {
            value = getPreferences().get(PROP_CUSTOM_CLASSPATH, null);
        }
        if (value == null || "".equals(value.trim())) { // NOI18N
            return Collections.emptyList();
        }
        List<File> files = new ArrayList<File>();
        for (String file : value.split(Pattern.quote(File.pathSeparator))) {
            files.add(new File(file));
        }
        return files;
    }

    private void setCustomProperties(Properties properties, List<PropertyChangeEvent> events) {
        String value = null;
        Properties copied = new Properties();

        if (properties != null) {
            copied.putAll(properties);

            StringBuilder builder = new StringBuilder();

            for (Enumeration e = copied.propertyNames(); e.hasMoreElements();) {
                String propKey = e.nextElement().toString();
                String propValue = copied.getProperty(propKey);
                if (propValue != null) {
                    builder.append(propKey).append("=").append(propValue); // NOI18N
                }
                if (builder.length() > 0) {
                    builder.append("\n"); // NOI18N
                }
            }
            value = builder.toString();
        }

        Properties oldValue;
        synchronized (this) {
            oldValue = getCustomProperties();
            if (value == null) {
                getPreferences().remove(PROP_CUSTOM_PROPERTIES);
            } else {
                getPreferences().put(PROP_CUSTOM_PROPERTIES, value);
            }
        }

        if (!isEqual(oldValue, copied)) {
            events.add(new PropertyChangeEvent(this, PROP_CUSTOM_PROPERTIES, oldValue, copied));
        }
    }

    private Properties getCustomProperties() {
        String value;
        synchronized (this) {
            value = getPreferences().get(PROP_CUSTOM_PROPERTIES, null);
        }
        if (value == null || "".equals(value.trim())) { // NOI18N
            return new Properties();
        }
        Properties properties = new Properties();
        for (String pairString : value.split(Pattern.quote("\n"))) { // NOI18N
            String[] pair = pairString.split("="); // NOI18N
            if (pair.length == 2) {
                properties.setProperty(pair[0], pair[1]);
            }
        }
        return properties;
    }

    private void setIgnoredPathsPattern(String pattern, List<PropertyChangeEvent> events) {
        String oldValue;
        synchronized (this) {
            oldValue = getIgnoredPathsPattern();
            if (pattern == null) {
                getPreferences().remove(PROP_IGNORED_PATHS_PATTERN);
            } else {
                getPreferences().put(PROP_IGNORED_PATHS_PATTERN, pattern);
            }
        }

        if (oldValue != pattern && (oldValue == null || !oldValue.equals(pattern))) {
            events.add(new PropertyChangeEvent(this, PROP_IGNORED_PATHS_PATTERN, oldValue, pattern));
        }
    }

    private synchronized String getIgnoredPathsPattern() {
        return trimToNull(getPreferences().get(PROP_IGNORED_PATHS_PATTERN, null));
    }

    private static boolean isEqual(List<File> oldClasspath, List<File> newClasspath) {
        if (oldClasspath == newClasspath) {
            return true;
        }
        if (oldClasspath == null || newClasspath == null) {
            return false;
        }
        if (oldClasspath.size() != newClasspath.size()) {
            return false;
        }

        for (int i = 0; i < oldClasspath.size(); i++) {
            File oldValue = oldClasspath.get(i);
            File newValue = newClasspath.get(i);
            if (oldValue != newValue && (oldValue == null || !oldValue.equals(newValue))) {
                return false;
            }
        }
        return true;
    }

    private static boolean isEqual(Properties oldProperties, Properties newProperties) {
        if (oldProperties == newProperties) {
            return true;
        }
        if (oldProperties == null || newProperties == null) {
            return false;
        }
        if (oldProperties.size() != newProperties.size()) {
            return false;
        }

        for (Map.Entry<Object, Object> entry : oldProperties.entrySet()) {
            Object oldValue = entry.getValue();
            Object newValue = newProperties.get(entry.getKey());
            if (oldValue != newValue && (oldValue == null || !oldValue.equals(newValue))) {
                return false;
            }
        }
        return true;
    }

    private Preferences getPreferences() {
        return NbPreferences.forModule(CheckstyleSettings.class);
    }

    private static String trimToNull(String value) {
        if (value == null || value.trim().equals("")) {
            return null;
        }
        return value;
    }

    public static final class Values {

        private final Severity customSeverity;

        private final String customConfigFile;

        private final String customPropertyFile;

        private final List<File> customClasspath;

        private final Properties customProperties;

        private final String ignoredPathsPattern;

        public Values(Severity customSeverity, String customConfigFile,
                String customPropetyFile, List<File> customClasspath,
                Properties customProperties, String ignoredPathsPattern) {

            this.customSeverity = customSeverity;
            this.customConfigFile = customConfigFile;
            this.customPropertyFile = customPropetyFile;

            this.customClasspath = new ArrayList<File>();
            if (customClasspath != null) {
                this.customClasspath.addAll(customClasspath);
            }

            this.customProperties = new Properties();
            if (customProperties != null) {
                this.customProperties.putAll(customProperties);
            }
            this.ignoredPathsPattern = ignoredPathsPattern;
        }

        public Severity getCustomSeverity() {
            return customSeverity;
        }

        public List<File> getCustomClasspath() {
            return customClasspath;
        }

        public String getCustomConfigFile() {
            return customConfigFile;
        }

        public Properties getCustomProperties() {
            return customProperties;
        }

        public String getCustomPropertyFile() {
            return customPropertyFile;
        }

        public String getIgnoredPathsPattern() {
            return ignoredPathsPattern;
        }
    }
}
