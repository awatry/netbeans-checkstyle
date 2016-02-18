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

import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.PropertiesExpander;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.WeakListeners;
import org.xml.sax.InputSource;

/**
 *
 * @author Petr Hejl
 */
public final class ConfigurationLoader implements PropertyChangeListener {

    private static final Logger LOGGER = Logger.getLogger(ConfigurationLoader.class.getName());

    private static final String DEFAULT_CONFIGURATION_RESOURCE =
            "cz/sickboy/netbeans/checkstyle/resources/sun_checks.xml"; // NOI18N

    private static final ScheduledExecutorService EXECUTOR = Executors.newScheduledThreadPool(0);

    private static final long RELOAD_DELAY_MILLIS = 300;

    private static ConfigurationLoader instance;

    private Configuration configuration;

    private CheckstyleException exception;

    private Future<?> reloadTask;

    private ConfigurationLoader() {
        super();
    }

    public static synchronized ConfigurationLoader getDefault() {
        if (instance == null) {
            instance = new ConfigurationLoader();
            CheckstyleSettings settings = CheckstyleSettings.getDefault();
            settings.addPropertyChangeListener(
                    WeakListeners.propertyChange(instance, settings));

            // prepare the configuration
            instance.reloadConfiguration();
        }
        return instance;
    }

    public synchronized Configuration getConfiguration() throws CheckstyleException {
        if (exception != null) {
            throw exception;
        }
        if (configuration == null) {
            reloadConfiguration();
        }
        return configuration;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        synchronized (this) {
            configuration = null;
            exception = null;

            if (reloadTask != null) {
                reloadTask.cancel(false);
            }

            reloadTask = EXECUTOR.schedule(new Runnable() {
                public void run() {
                    reloadConfiguration();
                }
            }, RELOAD_DELAY_MILLIS, TimeUnit.MILLISECONDS);
        }
    }

    synchronized void reloadConfiguration() {
        configuration = null;
        exception = null;

        try {
            CheckstyleSettings.Values values = CheckstyleSettings.getDefault().getValues();

            Properties properties = (values.getCustomPropertyFile() != null)
                    ? loadProperties(values.getCustomPropertyFile(), System.getProperties())
                    : System.getProperties();

            Properties fresh = new Properties();
            // doing this because of https://github.com/checkstyle/checkstyle/commit/7d513f0
            for (Enumeration e = properties.propertyNames(); e.hasMoreElements();) {
                String name = (String) e.nextElement();
                fresh.put(name, properties.getProperty(name));
            }
            fresh.putAll(values.getCustomProperties());

            Pattern ignoredPathsPattern = null;
            String patternValue = values.getIgnoredPathsPattern();
            if (patternValue != null) {
                try {
                    ignoredPathsPattern = Pattern.compile(patternValue);
                } catch (PatternSyntaxException ex) {
                    LOGGER.log(Level.INFO, null, ex);
                }
            }

            configuration = new Configuration(values.getCustomSeverity(),
                    loadConfiguration(values.getCustomConfigFile(), fresh),
                    createClassLoader(values.getCustomClasspath()), ignoredPathsPattern);
        } catch (CheckstyleException ex) {
            exception = ex;
            LOGGER.log(Level.INFO, null, ex);
        }
    }

    private com.puppycrawl.tools.checkstyle.api.Configuration loadConfiguration(
            String configurationFile, Properties properties) throws CheckstyleException {

        if (configurationFile != null) {
            return com.puppycrawl.tools.checkstyle.ConfigurationLoader.loadConfiguration(
                    configurationFile, new PropertiesExpander(properties), false);
        }

        InputStream is = getClass().getClassLoader()
                .getResourceAsStream(DEFAULT_CONFIGURATION_RESOURCE);

        try {
            return com.puppycrawl.tools.checkstyle.ConfigurationLoader.loadConfiguration(
                    new InputSource(is), new PropertiesExpander(System.getProperties()), false);
        } finally {
            try {
                is.close();
            } catch (IOException ex) {
                LOGGER.log(Level.CONFIG, "Could not close configuration file {0}", configurationFile);
            }
        }
    }

    private static ClassLoader createClassLoader(List<File> classpath) {
        if (classpath.isEmpty()) {
            return Checker.class.getClassLoader();
        }

        List<URL> urls = new ArrayList<URL>(classpath.size());
        for (File file : classpath) {
            FileObject fileObject = FileUtil.toFileObject(FileUtil.normalizeFile(file));
            if (fileObject != null) {
                urls.add(URLMapper.findURL(fileObject, URLMapper.EXTERNAL));
            }
        }
        return new URLClassLoader(urls.toArray(new URL[urls.size()]),
                Checker.class.getClassLoader());
    }

    private static Properties loadProperties(String propertyFile, Properties defaultProperties) {
        Properties properties = new Properties(defaultProperties);
        try {
            InputStream is = new BufferedInputStream(new FileInputStream(propertyFile));
            try {
                properties.load(is);
            } finally {
                try {
                    is.close();
                } catch (IOException ex) {
                    LOGGER.log(Level.CONFIG, "Could not close property file {0}", propertyFile);
                }
            }
        } catch (IOException ex) {
            LOGGER.log(Level.CONFIG, "Could not read property file {0}", propertyFile);
        }
        return properties;
    }
}
