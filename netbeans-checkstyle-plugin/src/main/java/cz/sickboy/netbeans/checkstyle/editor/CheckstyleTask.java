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
package cz.sickboy.netbeans.checkstyle.editor;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import cz.sickboy.netbeans.checkstyle.CancellableChecker;
import cz.sickboy.netbeans.checkstyle.CheckstyleListener;
import cz.sickboy.netbeans.checkstyle.Configuration;
import cz.sickboy.netbeans.checkstyle.ConfigurationLoader;
import cz.sickboy.netbeans.checkstyle.Severity;
import cz.sickboy.netbeans.checkstyle.error.ErrorHandler;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationInfo;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.text.NbDocument;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Petr Hejl
 */
public class CheckstyleTask implements CancellableTask<CompilationInfo>, CancellableChecker.CancellationHook {

    private static final Logger LOGGER = Logger.getLogger(CheckstyleTask.class.getName());

    private static final RequestProcessor THREAD_POOL = new RequestProcessor(CheckstyleTask.class.getName(), 10, true);

    private final FileObject fileObject;

    private Future<?> running;

    private boolean cancelled;

    public CheckstyleTask (FileObject fileObject) {
        this.fileObject = fileObject;
    }

    @Override
    public synchronized void cancel () {
        if (running != null) {
            running.cancel(true);
        }
        cancelled = true;
    }

    @Override
    public void run (CompilationInfo info) throws Exception {
        init();

        DataObject data = DataObject.find(fileObject);
        if (data == null || data.isModified()) {
            return;
        }

        EditorCookie editor = DataObject.find(fileObject).getCookie(EditorCookie.class);
        if (editor == null) {
            return;
        }

        try {
            Configuration config = ConfigurationLoader.getDefault().getConfiguration();

            final File file = FileUtil.toFile(fileObject);
            if (file == null) { // occurs for libraries for example
                return;
            }

            Pattern ignored = config.getIgnoredPathsPattern();
            if (ignored != null && ignored.matcher(file.getAbsolutePath()).matches()) {
                return;
            }

            Pattern checked = config.getCheckedPathsPattern();
            if (checked != null && !checked.matcher(file.getAbsolutePath()).matches()) {
                return;
            }


            List<CheckstyleAnnotation> results = run(fileObject, file, editor.openDocument(), config);
            if (!isCanceled()) {
                setAnnotations(fileObject, results);
            }
        } catch (CheckstyleException ex) {
            ErrorHandler.getDefault().handleError(fileObject, LOGGER, ex);
        }
    }

    synchronized void init () {
        running = null;
        cancelled = false;
    }

    @Override
    public synchronized boolean isCanceled () {
        return cancelled;
    }

    private List<CheckstyleAnnotation> run (final FileObject fileObject, final File file,
        final StyledDocument document, final Configuration config) throws CheckstyleException {

        final CollectingListener listener = new CollectingListener(config.getSeverity(),
            document);

        Future<?> future;
        synchronized (this) {
            if (isCanceled()) {
                return Collections.emptyList();
            }

            running = THREAD_POOL.submit(new Callable<Void>() {

                @Override
                public Void call () throws Exception {
                    ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
                    try {
                        Thread.currentThread().setContextClassLoader(config.getCheckstyleClassLoader());
                        CancellableChecker checker = new CancellableChecker(CheckstyleTask.this);
                        try {
                            // classloader to load checks
                            checker.setModuleClassLoader(config.getCheckstyleClassLoader());
                            // classloader to load classpath
                            ClassPath path = ClassPath.getClassPath(fileObject, ClassPath.EXECUTE);
                            if (path != null) {
                                checker.setClassloader(path.getClassLoader(true));
                            }
                            checker.configure(config.getCheckstyleConfiguration());
                            checker.addListener(listener);

                            checker.process(file);
                        } finally {
                            checker.destroy();
                        }

                        return null;
                    } finally {
                        Thread.currentThread().setContextClassLoader(originalClassLoader);
                    }
                }
            });
            future = running;
        }
        try {
            future.get();
        } catch (ExecutionException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof CheckstyleException) {
                throw (CheckstyleException) cause;
            } else {
                throw new RuntimeException(cause);
            }
        } catch (InterruptedException ex) {
            LOGGER.log(Level.FINE, null, ex);
            Thread.currentThread().interrupt();
            return Collections.emptyList();
        } catch (CancellationException ex) {
            // XXX is there a better way than catching runtime exception
            return Collections.emptyList();
        }

        return listener.getResults();
    }

    private static void setAnnotations (FileObject fileObject, List<CheckstyleAnnotation> annotations) {
        CheckstyleAnnotationContainer container = CheckstyleAnnotationContainer.getInstance(fileObject);
        if (container != null) {
            container.setAnnotations(annotations);
        } else {
            LOGGER.log(Level.INFO, "No annotation container"); // NOI18N
        }
    }

    private static Position getPosition (final StyledDocument document, final int lineNumber) {
        final AtomicReference<Position> ref = new AtomicReference<>();
        document.render(new Runnable() {

            @Override
            public void run () {
                int offset = NbDocument.findLineOffset(document, lineNumber);
                if (offset < 0 || offset >= document.getLength()) {
                    return;
                }

                try {
                    ref.set(document.createPosition(offset - NbDocument.findLineColumn(document, offset)));
                } catch (BadLocationException ex) {
                    LOGGER.log(Level.INFO, null, ex);
                }
            }
        });

        return ref.get();
    }

    private static class CollectingListener extends CheckstyleListener<CheckstyleAnnotation> {

        private final StyledDocument document;

        public CollectingListener (Severity minimalSeverity, StyledDocument document) {
            super(minimalSeverity);
            this.document = document;
        }

        @Override
        public CheckstyleAnnotation createResult (AuditEvent evt) {
            Position position = getPosition(document, evt.getLine() - 1);
            if (position != null) {
                return new CheckstyleAnnotation(document, position,
                    evt.getMessage(), evt.getSeverityLevel());
            }
            return null;
        }
    }
}
