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
package cz.sickboy.netbeans.checkstyle.options;

import org.netbeans.spi.options.AdvancedOption;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.NbBundle;

/**
 * Class implementing the advanced option for configuring the checkstyle plugin.
 *
 * @author Petr Hejl
 * @see OptionsPanelController
 */
public class CheckstyleOptions extends AdvancedOption {

    /**
     * Constructs the options object.
     */
    public CheckstyleOptions() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    public OptionsPanelController create() {
        return new CheckstyleOptionsController();
    }

    /**
     * {@inheritDoc}
     */
    public String getDisplayName() {
        return NbBundle.getMessage(CheckstyleOptionsPanel.class, "CheckstyleOptions.displayName");
    }

    /**
     * {@inheritDoc}
     */
    public String getTooltip() {
        return NbBundle.getMessage(CheckstyleOptionsPanel.class, "CheckstyleOptions.tooltip");
    }

}
