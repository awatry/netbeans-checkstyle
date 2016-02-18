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

import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import java.io.File;
import java.util.Arrays;
import java.util.prefs.Preferences;
import junit.framework.TestCase;
import org.openide.util.NbPreferences;

/**
 *
 * @author Petr Hejl
 */
public class ConfigurationLoaderTest extends TestCase {

    public ConfigurationLoaderTest(String name) {
        super(name);
    }

    @Override
    protected void tearDown() throws Exception {
        Preferences prefs = NbPreferences.forModule(CheckstyleSettings.class);
        prefs.removeNode();

        super.tearDown();
    }

    public void testConfiguration() throws CheckstyleException {
        ConfigurationLoader loader = ConfigurationLoader.getDefault();
        assertNotNull(loader.getConfiguration().getCheckstyleConfiguration());

        com.puppycrawl.tools.checkstyle.api.Configuration config =
                loader.getConfiguration().getCheckstyleConfiguration();
        loader.reloadConfiguration();
        com.puppycrawl.tools.checkstyle.api.Configuration refreshedConfig =
                loader.getConfiguration().getCheckstyleConfiguration();
        assertNotSame(config, refreshedConfig);
        assertNotNull(refreshedConfig);
    }

    public void testClassLoader() throws CheckstyleException {
        ConfigurationLoader loader = ConfigurationLoader.getDefault();
        assertNotNull(loader.getConfiguration().getCheckstyleClassLoader());

        ClassLoader classLoader = loader.getConfiguration().getCheckstyleClassLoader();
        loader.reloadConfiguration();
        ClassLoader refreshedClassLoader = loader.getConfiguration().getCheckstyleClassLoader();
        
        assertEquals(classLoader, CheckstyleModule.class.getClassLoader());
        assertEquals(refreshedClassLoader, CheckstyleModule.class.getClassLoader());
    }

    public void testListener() throws CheckstyleException {
        ConfigurationLoader loader = ConfigurationLoader.getDefault();
        CheckstyleSettings settings = CheckstyleSettings.getDefault();

        Configuration config = loader.getConfiguration();
        assertNotNull(config);

        CheckstyleSettings.Values snapshot = new CheckstyleSettings.Values(
                Severity.ERROR, null, null, Arrays.asList(new File[]{new File("test")}), null, null);
        settings.setValues(snapshot);

        Configuration refreshedConfig = loader.getConfiguration();
        assertNotSame(config, refreshedConfig);
        assertNotNull(refreshedConfig);
    }

    public void testDefault() throws CheckstyleException {
        ConfigurationLoader loader = ConfigurationLoader.getDefault();
        CheckstyleSettings.Values snapshot = new CheckstyleSettings.Values(
                Severity.ERROR, "", null, null, null, null);

        CheckstyleSettings.getDefault().setValues(snapshot);

        assertNotNull(loader.getConfiguration());
    }
}
