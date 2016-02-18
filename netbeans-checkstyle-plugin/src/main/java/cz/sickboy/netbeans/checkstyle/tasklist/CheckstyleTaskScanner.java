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
package cz.sickboy.netbeans.checkstyle.tasklist;

import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import cz.sickboy.netbeans.checkstyle.CheckstyleListener;
import cz.sickboy.netbeans.checkstyle.CheckstyleSettings;
import cz.sickboy.netbeans.checkstyle.Configuration;
import cz.sickboy.netbeans.checkstyle.ConfigurationLoader;
import cz.sickboy.netbeans.checkstyle.Severity;
import cz.sickboy.netbeans.checkstyle.CheckerCache;
import cz.sickboy.netbeans.checkstyle.error.ErrorHandler;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.netbeans.spi.tasklist.FileTaskScanner;
import org.netbeans.spi.tasklist.Task;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Hejl
 */
public class CheckstyleTaskScanner extends FileTaskScanner implements PropertyChangeListener {

    private static final Logger LOGGER = Logger.getLogger(CheckstyleTaskScanner.class.getName());

    private final CheckerCache checkerCache = new CheckerCache();

    private Callback callback;

    public CheckstyleTaskScanner() {
        super(NbBundle.getMessage(CheckstyleTaskScanner.class, "CheckstyleTaskScanner.label"),
                NbBundle.getMessage(CheckstyleTaskScanner.class, "CheckstyleTaskScanner.hint"),
                "Advanced/cz-sickboy-netbeans-checkstyle-options-CheckstyleOptions"); // NOI18N
    }

    public synchronized void attach(Callback callback) {
        if (this.callback == null && callback == null) {
            return;
        }

        if (this.callback == null && callback != null) {
            CheckstyleSettings.getDefault().addPropertyChangeListener(this);
        } else if (this.callback != null && callback == null) {
            CheckstyleSettings.getDefault().removePropertyChangeListener(this);
        }

        this.callback = callback;
    }

    public List<? extends Task> scan(FileObject fileObject) {
        if (fileObject == null || !"java".equalsIgnoreCase(fileObject.getExt())) { // NOI18N
            return null;
        }

        try {
            Configuration config = ConfigurationLoader.getDefault().getConfiguration();

            File file = FileUtil.toFile(fileObject);
            if (file == null) { // occurs for libraries for example
                return null;
            }

            Pattern ignored = config.getIgnoredPathsPattern();
            if (ignored != null && ignored.matcher(file.getAbsolutePath()).matches()) {
                return Collections.emptyList();
            }

            CollectingListener listener = new CollectingListener(
                    config.getSeverity(), fileObject);

            ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(config.getCheckstyleClassLoader());
                Checker checker = checkerCache.acquireChecker(fileObject, config);
                try {
                    try {
                        checker.addListener(listener);
                        checker.process(Collections.singletonList(file));
                    } finally {
                        checker.removeListener(listener);
                    }
                } finally {
                    checkerCache.releaseChecker(checker);
                }
            } finally {
                Thread.currentThread().setContextClassLoader(originalClassLoader);
            }

            return listener.getResults();
        } catch (CheckstyleException ex) {
            ErrorHandler.getDefault().handleError(fileObject, LOGGER, ex);
            return Collections.emptyList();
        }
    }

    @Override
    public void notifyFinish() {
        checkerCache.clear();
    }

    public synchronized void propertyChange(PropertyChangeEvent evt) {
        if (callback != null) {
            callback.refreshAll();
        }
    }

    private static class CollectingListener extends CheckstyleListener<Task> {

        private final FileObject file;

        public CollectingListener(Severity minimalSeverity, FileObject file) {
            super(minimalSeverity);
            this.file = file;
        }

        @Override
        public Task createResult(AuditEvent evt) {
            return Task.create(file, "cz-sickboy-netbeans-checkstyle-Task", // NOI18N
                    evt.getMessage(), evt.getLine());
        }
    }
}
