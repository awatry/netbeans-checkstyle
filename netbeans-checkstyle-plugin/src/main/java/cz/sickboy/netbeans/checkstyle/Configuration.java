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

import java.util.regex.Pattern;

/**
 *
 * @author Petr Hejl
 */
public final class Configuration {

    private final Severity severity;

    private final com.puppycrawl.tools.checkstyle.api.Configuration configuration;

    private final ClassLoader classLoader;

    private final Pattern ignoredPathsPattern;

    public Configuration(Severity severity,
            com.puppycrawl.tools.checkstyle.api.Configuration configuration,
            ClassLoader classLoader, Pattern ignoredPathsPattern) {
        this.severity = severity;
        this.configuration = configuration;
        this.classLoader = classLoader;
        this.ignoredPathsPattern = ignoredPathsPattern;
    }

    public com.puppycrawl.tools.checkstyle.api.Configuration getCheckstyleConfiguration() {
        return configuration;
    }

    public ClassLoader getCheckstyleClassLoader() {
        return classLoader;
    }

    public Severity getSeverity() {
        return severity;
    }

    public Pattern getIgnoredPathsPattern() {
        return ignoredPathsPattern;
    }

}
