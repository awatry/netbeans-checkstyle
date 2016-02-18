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

import com.google.common.collect.ImmutableMap;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import java.util.regex.Pattern;
import junit.framework.TestCase;

/**
 *
 * @author Petr Hejl
 */
public class ConfigurationTest extends TestCase {

    private static final TestConfiguration TEST_CONFIGURATION = new TestConfiguration();

    public ConfigurationTest(String name) {
        super(name);
    }

    public void testConstructor() {
        Configuration config = new Configuration(null, null, null, null);
        assertNull(config.getSeverity());
        assertNull(config.getCheckstyleClassLoader());
        assertNull(config.getCheckstyleConfiguration());
        assertNull(config.getIgnoredPathsPattern());

        config = new Configuration(null, TEST_CONFIGURATION, null, null);
        assertNull(config.getSeverity());
        assertEquals(TEST_CONFIGURATION, config.getCheckstyleConfiguration());
        assertNull(config.getCheckstyleClassLoader());
        assertNull(config.getIgnoredPathsPattern());

        config = new Configuration(null, null, getClass().getClassLoader(), null);
        assertNull(config.getSeverity());
        assertNull(config.getCheckstyleConfiguration());
        assertEquals(getClass().getClassLoader(), config.getCheckstyleClassLoader());
        assertNull(config.getIgnoredPathsPattern());

        config = new Configuration(null, TEST_CONFIGURATION, getClass().getClassLoader(), null);
        assertNull(config.getSeverity());
        assertEquals(TEST_CONFIGURATION, config.getCheckstyleConfiguration());
        assertEquals(getClass().getClassLoader(), config.getCheckstyleClassLoader());
        assertNull(config.getIgnoredPathsPattern());

        config = new Configuration(Severity.ERROR, TEST_CONFIGURATION, getClass().getClassLoader(), null);
        assertEquals(Severity.ERROR, config.getSeverity());
        assertEquals(TEST_CONFIGURATION, config.getCheckstyleConfiguration());
        assertEquals(getClass().getClassLoader(), config.getCheckstyleClassLoader());
        assertNull(config.getIgnoredPathsPattern());

        Pattern ignored = Pattern.compile(".*");
        config = new Configuration(Severity.ERROR, TEST_CONFIGURATION, getClass().getClassLoader(), ignored);
        assertEquals(Severity.ERROR, config.getSeverity());
        assertEquals(TEST_CONFIGURATION, config.getCheckstyleConfiguration());
        assertEquals(getClass().getClassLoader(), config.getCheckstyleClassLoader());
        assertEquals(ignored, config.getIgnoredPathsPattern());
    }

    private static class TestConfiguration implements com.puppycrawl.tools.checkstyle.api.Configuration {

        public String getAttribute(String name) throws CheckstyleException {
            return null;
        }

        public String[] getAttributeNames() {
            return new String[] {};
        }

        public com.puppycrawl.tools.checkstyle.api.Configuration[] getChildren() {
            return new com.puppycrawl.tools.checkstyle.api.Configuration[] {};
        }

        public String getName() {
            return "Test"; // NOI18N
        }

        public ImmutableMap<String, String> getMessages() {
            return ImmutableMap.of();
        }
    }
}
