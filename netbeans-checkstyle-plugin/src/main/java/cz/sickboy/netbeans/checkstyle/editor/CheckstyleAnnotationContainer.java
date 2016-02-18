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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.RequestProcessor;

/**
 * The class holding the annotations for the single file object.
 *
 * @author Petr Hejl
 */
public final class CheckstyleAnnotationContainer {

    private static final Logger LOGGER = Logger.getLogger(CheckstyleAnnotationContainer.class.getName());

    private static final RequestProcessor UPDATER = new RequestProcessor(
            CheckstyleAnnotationContainer.class.getName(), 1, false, false);

    private static final Map<FileObject, CheckstyleAnnotationContainer> CONTAINERS =
            new HashMap<FileObject, CheckstyleAnnotationContainer>();

    private final FileObject fileObject;

    private final List<CheckstyleAnnotation> annotations = new ArrayList<CheckstyleAnnotation>();

    private CheckstyleAnnotationContainer(FileObject fileObject) {
        this.fileObject = fileObject;
    }

    /**
     * Returns the annotation container for the given file object. If it
     * exists returns existing one, otherwise return newly created holder.
     *
     * @param fileObject the file for which we want to get the annotation holder
     * @return the annotation container for the given file object (existing
     *           or newly created one).
     */
    public static synchronized CheckstyleAnnotationContainer getInstance(FileObject fileObject) {
        CheckstyleAnnotationContainer annotationContainer = CONTAINERS.get(fileObject);

        if (annotationContainer != null) {
            return annotationContainer;
        }

        try {
            EditorCookie.Observable editorCookie = DataObject.find(fileObject)
                    .getCookie(EditorCookie.Observable.class);

            if (editorCookie == null) {
                return null;
            }

            annotationContainer = new CheckstyleAnnotationContainer(fileObject);

            CloseHandler handler = new CloseHandler(annotationContainer, editorCookie);
            editorCookie.addPropertyChangeListener(handler);
            SwingUtilities.invokeLater(handler);

            CONTAINERS.put(fileObject, annotationContainer);

            return annotationContainer;
        } catch (DataObjectNotFoundException ex) {
            LOGGER.log(Level.WARNING, null, ex);
            return null;
        }
    }

    /**
     * Resets the factory clearing all holder and all annotations stored.
     */
    public static synchronized void reset() {
        for (CheckstyleAnnotationContainer annotationContainer : CONTAINERS.values()) {
            annotationContainer.setAnnotations(Collections.<CheckstyleAnnotation>emptyList());
        }

        CONTAINERS.clear();
    }

    /**
     * Returns the file object to which annotations belong to.
     *
     * @return the file object to which annotations belong to
     */
    public FileObject getFileObject() {
        return fileObject;
    }

    /**
     * Sets the new bunch of annotations to the file object. The old annotations
     * are removed and detached. This method is <i>thread safe</i>.
     *
     * @param newAnnotations the fresh annotations to attach
     */
    public synchronized void setAnnotations(List<CheckstyleAnnotation> newAnnotations) {
        UPDATER.post(new AnnotationUpdater(newAnnotations, annotations));
    }

    /**
     * Returns the list of annotations currently used.
     *
     * @return unmodifiable list of current used annotations
     */
    public synchronized List<CheckstyleAnnotation> getAnnotations() {
        return Collections.unmodifiableList(annotations);
    }

    private class AnnotationUpdater implements Runnable {

        private final List<CheckstyleAnnotation> annotationsToAdd;

        private final List<CheckstyleAnnotation> annotationsToRemove;

        public AnnotationUpdater(List<CheckstyleAnnotation> annotationsToAdd,
                List<CheckstyleAnnotation> annotationsToRemove) {

            this.annotationsToAdd = new ArrayList<CheckstyleAnnotation>(annotationsToAdd);
            this.annotationsToRemove = new ArrayList<CheckstyleAnnotation>(annotationsToRemove);
        }

        @Override
        public void run() {
            for (CheckstyleAnnotation annotation : annotationsToRemove) {
                annotation.documentDetach();
            }

            List<CheckstyleAnnotation> addedAnnotations =
                    new ArrayList<CheckstyleAnnotation>(annotationsToAdd.size());
            for (CheckstyleAnnotation annotation : annotationsToAdd) {
                annotation.documentAttach();
                addedAnnotations.add(annotation);
            }

            synchronized (CheckstyleAnnotationContainer.this) {
                annotations.clear();
                annotations.addAll(addedAnnotations);
            }
        }
    }

    private static class CloseHandler implements PropertyChangeListener, Runnable {

        private final CheckstyleAnnotationContainer container;

        private final EditorCookie.Observable editorCookie;

        public CloseHandler(CheckstyleAnnotationContainer container, EditorCookie.Observable editorCookie) {

            this.container = container;
            this.editorCookie = editorCookie;
        }

        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName() == null
                    || EditorCookie.Observable.PROP_OPENED_PANES.equals(evt.getPropertyName())) {
                run();
            }
        }

        public void run() {
            if (editorCookie.getOpenedPanes() == null) {
                synchronized (CheckstyleAnnotationContainer.class) {
                    CONTAINERS.remove(container.getFileObject());
                }

                container.setAnnotations(Collections.<CheckstyleAnnotation>emptyList());
                editorCookie.removePropertyChangeListener(this);
            }
        }
    }
}
