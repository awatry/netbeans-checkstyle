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
import com.puppycrawl.tools.checkstyle.api.Configuration;
import com.puppycrawl.tools.checkstyle.api.Context;
import com.puppycrawl.tools.checkstyle.api.FileSetCheck;
import com.puppycrawl.tools.checkstyle.api.FileText;
import com.puppycrawl.tools.checkstyle.api.LocalizedMessage;
import com.puppycrawl.tools.checkstyle.api.MessageDispatcher;
import cz.sickboy.netbeans.checkstyle.editor.CheckstyleTask;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * The checkstyle checker that calls for the cancel status of the {@link CheckstyleTask} and cancel itself if the task
 * is cancelled.
 *
 * @author Petr Hejl
 * @see CheckstyleTask
 */
public class CancellableChecker extends Checker {

    private static final SortedSet<LocalizedMessage> EMPTY_SET = new TreeSet<LocalizedMessage>() {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean add(LocalizedMessage e) {
            throw new UnsupportedOperationException("Read only set");
        }

        @Override
        public boolean addAll(Collection<? extends LocalizedMessage> c) {
            throw new UnsupportedOperationException("Read only set");
        }
    };

    private final CancellationHook hook;

    /**
     * Contructs the checker that won't do any checks whenewer the task has cancelled status set to <code>true</code>.
     *
     * @param hook the task that will be consulted for the cancellation
     * @throws CheckstyleException if any problem with initialization occurs
     */
    public CancellableChecker(CancellationHook hook) throws CheckstyleException {
        this.hook = hook;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addFileSetCheck(FileSetCheck fileSetCheck) {
        super.addFileSetCheck(new CancellableFileSetCheck(fileSetCheck, hook));
    }

    /**
     * Checks the given file.
     *
     * @param file file to check
     * @see Checker#process(File[])
     */
    public void process(File file) throws CheckstyleException {
        process(Collections.singletonList(file));
    }

    /**
     * Interface that defines a way to check if the checker has been canceled.
     */
    public static interface CancellationHook {

        /**
         * Has the action been canceled.
         *
         * @return Whether the check has been canceled.
         */
        boolean isCanceled();

    }

    /**
     * A FileSetCheck that can be interrupted.
     */
    private static class CancellableFileSetCheck implements FileSetCheck {

        private final FileSetCheck check;

        private final CancellationHook hook;

        public CancellableFileSetCheck(FileSetCheck check, CancellationHook hook) {
            this.check = check;
            this.hook = hook;
        }

        @Override
        public void contextualize(Context context) throws CheckstyleException {
            check.contextualize(context);
        }

        @Override
        public void configure(Configuration configuration) throws CheckstyleException {
            check.configure(configuration);
        }

        @Override
        public void setMessageDispatcher(MessageDispatcher dispatcher) {
            check.setMessageDispatcher(dispatcher);
        }

        @Override
        public SortedSet<LocalizedMessage> process(File file, FileText fileText) throws CheckstyleException {
            if (hook.isCanceled()) {
                return EMPTY_SET;
            }
            return check.process(file, fileText);
        }

        @Override
        public void init() {
            check.init();
        }

        @Override
        public void finishProcessing() {
            check.finishProcessing();
        }

        @Override
        public void destroy() {
            check.destroy();
        }

        @Override
        public void beginProcessing(String charset) {
            check.beginProcessing(charset);
        }
    }
}
