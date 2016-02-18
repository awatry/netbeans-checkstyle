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
package cz.sickboy.netbeans.checkstyle.error;

import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import cz.sickboy.netbeans.checkstyle.CheckstyleSettings;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.api.options.OptionsDisplayer;
import org.openide.awt.Notification;
import org.openide.awt.NotificationDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

/**
 *
 * @author Petr Hejl
 */
public final class ErrorHandler implements PropertyChangeListener {

    private static final Logger LOGGER = Logger.getLogger(ErrorHandler.class.getName());

    private static final Icon ICON = new ImageIcon(ImageUtilities.loadImage(
        "cz/sickboy/netbeans/checkstyle/resources/configuration-error.png")); // NOI18N

    private static final ActionListener OPTIONS_ACTION = new ActionListener() {

        @Override
        public void actionPerformed (ActionEvent e) {
            OptionsDisplayer.getDefault().open(
                "Advanced/cz-sickboy-netbeans-checkstyle-options-CheckstyleOptions"); // NOI18N
        }
    };

    private static ErrorHandler instance;

    private final List<Notification> notifications = new ArrayList<Notification>();

    private String message;

    public static synchronized ErrorHandler getDefault () {
        if (instance == null) {
            instance = new ErrorHandler();
            CheckstyleSettings settings = CheckstyleSettings.getDefault();
            settings.addPropertyChangeListener(
                WeakListeners.propertyChange(instance, settings));
        }
        return instance;
    }

    public void handleError (FileObject file, Logger logger, CheckstyleException ex) {
        Logger current = logger;
        if (current == null) {
            current = LOGGER;
        }
        current.log(Level.INFO, null, ex);

        Exception toHandle = ex;
        if ((ex.getCause() instanceof Exception) &&
            ex.getMessage() != null && ex.getMessage().startsWith("unable to parse configuration stream"))
        {
            toHandle = (Exception) ex.getCause();
        }
        synchronized (this) {
            if (message == null || !message.equals(toHandle.getMessage())) {
                message = toHandle.getMessage();
                notifications.add(NotificationDisplayer.getDefault().notify(
                    NbBundle.getMessage(ErrorHandler.class, "ErrorHandler.title"),
                    ICON, toHandle.getLocalizedMessage(), OPTIONS_ACTION, NotificationDisplayer.Priority.HIGH));
            }
        }
    }

    @Override
    public void propertyChange (PropertyChangeEvent evt) {
        List<Notification> toRemove;
        synchronized (this) {
            toRemove = new ArrayList<Notification>(notifications);
            message = null;
            notifications.clear();
        }
        for (Notification notification : toRemove) {
            notification.clear();
        }
    }

}
