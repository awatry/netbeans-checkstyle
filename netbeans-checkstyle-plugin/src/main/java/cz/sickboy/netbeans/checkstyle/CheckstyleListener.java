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

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.AuditListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Petr Hejl
 */
public abstract class CheckstyleListener<T> implements AuditListener {

    private static final Logger LOGGER = Logger.getLogger(CheckstyleListener.class.getName());

    private final Severity minimalSeverity;

    /* GuardedBy("this") */
    private final List<T> results = new ArrayList<T>();

    public CheckstyleListener(Severity minimalSeverity) {
        this.minimalSeverity = minimalSeverity;
    }

    public synchronized final List<T> getResults() {
        return results;
    }

    public abstract T createResult(AuditEvent evt);

    @Override
    public final void addError(AuditEvent evt) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.log(Level.FINEST, "{0}: {1}", new Object[] {evt.getLine(), evt.getMessage()});
        }

        if (evt.getLine() <= 0) {
            return;
        }

        if (minimalSeverity.include(evt.getSeverityLevel())) {
            T result = createResult(evt);
            if (result != null) {
                synchronized (this) {
                    results.add(result);
                }
            }
        }
    }

    @Override
    public final void addException(AuditEvent evt, Throwable throwable) {
        LOGGER.log(Level.SEVERE, null, throwable);
    }

    @Override
    public final void auditFinished(AuditEvent evt) {
    }

    @Override
    public final void auditStarted(AuditEvent evt) {
    }

    @Override
    public final void fileFinished(AuditEvent evt) {
    }

    @Override
    public final void fileStarted(AuditEvent evt) {
    }
}
