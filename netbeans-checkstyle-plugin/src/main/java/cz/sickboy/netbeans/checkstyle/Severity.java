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

import com.puppycrawl.tools.checkstyle.api.SeverityLevel;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Hejl
 */
public enum Severity {

    ERROR {

        @Override
        public String toString() {
            return NbBundle.getMessage(Severity.class,
                    "CheckstyleSeverity.error.label");
        }

        @Override
        public boolean include(SeverityLevel level) {
            return level != SeverityLevel.IGNORE && level != SeverityLevel.INFO
                    && level != SeverityLevel.WARNING;
        }
    },

    WARNING {

        @Override
        public String toString() {
            return NbBundle.getMessage(Severity.class,
                    "CheckstyleSeverity.warning.label");
        }

        @Override
        public boolean include(SeverityLevel level) {
            return level != SeverityLevel.IGNORE && level != SeverityLevel.INFO;
        }
    },

    INFO {

        @Override
        public String toString() {
            return NbBundle.getMessage(Severity.class,
                    "CheckstyleSeverity.info.label");
        }

        @Override
        public boolean include(SeverityLevel level) {
            return level != SeverityLevel.IGNORE;
        }
    },

    IGNORE {

        @Override
        public String toString() {
            return NbBundle.getMessage(Severity.class,
                    "CheckstyleSeverity.ignore.label");
        }

        @Override
        public boolean include(SeverityLevel level) {
            return true;
        }
    };

    public abstract boolean include(SeverityLevel level);
}
