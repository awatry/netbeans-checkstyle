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
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.prefs.Preferences;
import junit.framework.TestCase;
import org.openide.util.NbPreferences;

/**
 *
 * @author Petr Hejl
 */
public class CheckstyleSettingsTest extends TestCase {

    public CheckstyleSettingsTest(String name) {
        super(name);
    }

    @Override
    protected void tearDown() throws Exception {
        Preferences prefs = NbPreferences.forModule(CheckstyleSettings.class);
        prefs.removeNode();

        super.tearDown();
    }

    public void testCustomConfigFile() {
        CheckstyleSettings settings = CheckstyleSettings.getDefault();
        assertNull(settings.getValues().getCustomConfigFile());

        TestListener listener = new TestListener(settings,
                CheckstyleSettings.PROP_CUSTOM_CONFIG_FILE, null, "test");
        settings.addPropertyChangeListener(listener);
        try {
            setCustomConfigFile(settings, "test");
            assertEquals("test", settings.getValues().getCustomConfigFile());
        } finally {
            settings.removePropertyChangeListener(listener);
        }

        listener = new TestListener(settings,
                CheckstyleSettings.PROP_CUSTOM_CONFIG_FILE, "test", null);
        settings.addPropertyChangeListener(listener);
        try {
            setCustomConfigFile(settings, null);
            assertNull(settings.getValues().getCustomConfigFile());
        } finally {
            settings.removePropertyChangeListener(listener);
        }

        // check listener is not invoked
        setCustomConfigFile(settings, "test");
    }

    public void testCustomPropertyFile() {
        CheckstyleSettings settings = CheckstyleSettings.getDefault();
        assertNull(settings.getValues().getCustomPropertyFile());

        TestListener listener = new TestListener(settings,
                CheckstyleSettings.PROP_CUSTOM_PROPERTY_FILE, null, "test");
        settings.addPropertyChangeListener(listener);
        try {
            setCustomPropertyFile(settings, "test");
            assertEquals("test", settings.getValues().getCustomPropertyFile());
        } finally {
            settings.removePropertyChangeListener(listener);
        }

        listener = new TestListener(settings,
                CheckstyleSettings.PROP_CUSTOM_PROPERTY_FILE, "test", null);
        settings.addPropertyChangeListener(listener);
        try {
            setCustomPropertyFile(settings, null);
            assertNull(settings.getValues().getCustomPropertyFile());
        } finally {
            settings.removePropertyChangeListener(listener);
        }

        // check listener is not invoked
        setCustomPropertyFile(settings, "test");
    }

    public void testCustomClasspath() {
        List<File> classpath = new ArrayList();
        Collections.addAll(classpath, new File("file_1"), new File("file_2"));

        CheckstyleSettings settings = CheckstyleSettings.getDefault();
        assertEquals(Collections.emptyList(), settings.getValues().getCustomClasspath());

        TestListener listener = new TestListener(settings,
                CheckstyleSettings.PROP_CUSTOM_CLASSPATH, Collections.emptyList(), classpath);
        settings.addPropertyChangeListener(listener);
        try {
            setCustomClasspath(settings, classpath);
            assertEquals(classpath, settings.getValues().getCustomClasspath());
        } finally {
            settings.removePropertyChangeListener(listener);
        }

        listener = new TestListener(settings,
                CheckstyleSettings.PROP_CUSTOM_CLASSPATH, classpath, Collections.emptyList());
        settings.addPropertyChangeListener(listener);
        try {
            setCustomClasspath(settings, null);
            assertEquals(Collections.emptyList(), settings.getValues().getCustomClasspath());
        } finally {
            settings.removePropertyChangeListener(listener);
        }

        // check listener is not invoked
        setCustomClasspath(settings, classpath);
    }

    public void testCustomProperties() {
        Properties properties = new Properties();
        properties.put("prop_1", "value_1");
        properties.put("prop_2", "value_2");

        CheckstyleSettings settings = CheckstyleSettings.getDefault();
        assertEquals(new Properties(), settings.getValues().getCustomProperties());

        TestListener listener = new TestListener(settings,
                CheckstyleSettings.PROP_CUSTOM_PROPERTIES, new Properties(), properties);
        settings.addPropertyChangeListener(listener);
        try {
            setCustomProperties(settings, properties);
            assertEquals(properties, settings.getValues().getCustomProperties());
        } finally {
            settings.removePropertyChangeListener(listener);
        }

        listener = new TestListener(settings,
                CheckstyleSettings.PROP_CUSTOM_PROPERTIES, properties, new Properties());
        settings.addPropertyChangeListener(listener);
        try {
            setCustomProperties(settings, null);
            assertEquals(new Properties(), settings.getValues().getCustomProperties());
        } finally {
            settings.removePropertyChangeListener(listener);
        }

        // check listener is not invoked
        setCustomProperties(settings, properties);
    }

    public void testIgnoredPathsPattern() {
        CheckstyleSettings settings = CheckstyleSettings.getDefault();
        assertNull(settings.getValues().getIgnoredPathsPattern());

        TestListener listener = new TestListener(settings,
                CheckstyleSettings.PROP_IGNORED_PATHS_PATTERN, null, "test");
        settings.addPropertyChangeListener(listener);
        try {
            setIgnoredPathsPattern(settings, "test");
            assertEquals("test", settings.getValues().getIgnoredPathsPattern());
        } finally {
            settings.removePropertyChangeListener(listener);
        }

        listener = new TestListener(settings,
                CheckstyleSettings.PROP_IGNORED_PATHS_PATTERN, "test", null);
        settings.addPropertyChangeListener(listener);
        try {
            setIgnoredPathsPattern(settings, null);
            assertNull(settings.getValues().getIgnoredPathsPattern());
        } finally {
            settings.removePropertyChangeListener(listener);
        }

        // check listener is not invoked
        setIgnoredPathsPattern(settings, "test");
    }

    private static void setCustomConfigFile(CheckstyleSettings settings, String file) {
        CheckstyleSettings.Values values = settings.getValues();
        values = new CheckstyleSettings.Values(Severity.IGNORE,
                file, values.getCustomPropertyFile(),
                values.getCustomClasspath(), values.getCustomProperties(), values.getIgnoredPathsPattern());
        settings.setValues(values);
    }

    private static void setCustomPropertyFile(CheckstyleSettings settings, String file) {
        CheckstyleSettings.Values values = settings.getValues();
        values = new CheckstyleSettings.Values(Severity.IGNORE,
                values.getCustomConfigFile(), file,
                values.getCustomClasspath(), values.getCustomProperties(), values.getIgnoredPathsPattern());
        settings.setValues(values);
    }

    private static void setCustomClasspath(CheckstyleSettings settings, List<File> classpath) {
        CheckstyleSettings.Values values = settings.getValues();
        values = new CheckstyleSettings.Values(Severity.IGNORE,
                values.getCustomConfigFile(), values.getCustomPropertyFile(),
                classpath, values.getCustomProperties(), values.getIgnoredPathsPattern());
        settings.setValues(values);
    }

    private static void setCustomProperties(CheckstyleSettings settings, Properties properties) {
        CheckstyleSettings.Values values = settings.getValues();
        values = new CheckstyleSettings.Values(Severity.IGNORE,
                values.getCustomConfigFile(), values.getCustomPropertyFile(),
                values.getCustomClasspath(), properties, values.getIgnoredPathsPattern());
        settings.setValues(values);
    }

    private static void setIgnoredPathsPattern(CheckstyleSettings settings, String pattern) {
        CheckstyleSettings.Values values = settings.getValues();
        values = new CheckstyleSettings.Values(Severity.IGNORE,
                values.getCustomConfigFile(), values.getCustomPropertyFile(),
                values.getCustomClasspath(), values.getCustomProperties(), pattern);
        settings.setValues(values);
    }

    private static void assertEquals(List expected, List value) {
        assertEquals(expected.size(), value.size());

        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), value.get(i));
        }
    }

    private static void assertEquals(Properties expected, Properties value) {
        assertEquals(expected.size(), value.size());

        for (Map.Entry<Object, Object> entry : expected.entrySet()) {
            assertEquals(entry.getValue(), value.get(entry.getKey()));
        }
    }

    private static class TestListener implements PropertyChangeListener {

        private final Object source;

        private final String propertyName;

        private final Object oldValue;

        private final Object newValue;

        private boolean received;

        public TestListener(Object source, String propertyName, Object oldValue, Object newValue) {
            this.source = source;
            this.propertyName = propertyName;
            this.oldValue = oldValue;
            this.newValue = newValue;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            assertFalse(received);
            received = true;

            assertEquals(source, evt.getSource());
            assertEquals(propertyName, evt.getPropertyName());

            if (oldValue instanceof List && evt.getOldValue() instanceof List) {
                assertEquals((List) oldValue, (List) evt.getOldValue());
            } else {
                assertEquals(oldValue, evt.getOldValue());
            }
            if (newValue instanceof List && evt.getNewValue() instanceof List) {
                assertEquals((List) newValue, (List) evt.getNewValue());
            } else {
                assertEquals(newValue, evt.getNewValue());
            }
        }

    }
}
