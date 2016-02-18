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
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Petr Hejl
 */
public final class CheckerCache {

    private static final Logger LOGGER = Logger.getLogger(CheckerCache.class.getName());

    private Configuration lastConfiguration;

    private ClassLoader lastClassLoader;

    private Checker lastChecker;

    private boolean inUse;

    public Checker acquireChecker(FileObject fileObject, Configuration configuration) throws CheckstyleException {
        ClassLoader classLoader = null;
        ClassPath path = ClassPath.getClassPath(fileObject, ClassPath.EXECUTE);
        if (path != null) {
            classLoader = path.getClassLoader(true);
        }

        synchronized (this) {
            if ((lastClassLoader == classLoader || (lastClassLoader != null && lastClassLoader.equals(classLoader)))
                    && configuration.equals(lastConfiguration) && !inUse) {
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.log(Level.FINE, "Cache hit for {0}", fileObject.getNameExt());
                }
                inUse = true;
                return lastChecker;
            } else if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "Cache miss for {0}", fileObject.getNameExt());
            }
        }

        Checker freshChecker = new Checker();
        // classloader to load checks
        freshChecker.setModuleClassLoader(configuration.getCheckstyleClassLoader());
        // classloader to load classpath
        freshChecker.setClassloader(classLoader);

        freshChecker.configure(configuration.getCheckstyleConfiguration());

        synchronized (this) {
            lastConfiguration = configuration;
            lastClassLoader = classLoader;
            lastChecker = freshChecker;
            inUse = true;
        }

        return freshChecker;
    }

    public void releaseChecker(Checker checker) {
        synchronized (this) {
            if (checker.equals(lastChecker)) {
                inUse = false;
            } else {
                checker.destroy();
            }
        }
    }

    public void clear() {
        Checker current;
        synchronized (this) {
           current = lastChecker;

           lastConfiguration = null;
           lastClassLoader = null;
           lastChecker = null;
           inUse = false;
        }
        if (current != null) {
            current.destroy();
        }
    }

}
