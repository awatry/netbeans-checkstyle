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

import com.puppycrawl.tools.checkstyle.api.SeverityLevel;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;
import org.openide.text.Annotation;
import org.openide.text.NbDocument;

/**
 *
 * @author Petr Hejl
 */
public final class CheckstyleAnnotation extends Annotation {

    private final StyledDocument document;

    private final Position position;

    private final String shortDescription;

    private final SeverityLevel level;

    public CheckstyleAnnotation(StyledDocument document, Position position,
            String shortDescription, SeverityLevel level) {

        this.document = document;
        this.position = position;
        this.shortDescription = shortDescription;
        this.level = level;
    }

    public String getAnnotationType() {
        return "cz-sickboy-netbeans-checkstyle-resources-checkstyle-annotation"; // NOI18N
    }

    public void documentAttach() {
        NbDocument.addAnnotation(document, position, -1, this);
    }

    public void documentDetach() {
        NbDocument.removeAnnotation(document, this);
    }

    public String getShortDescription() {
        return shortDescription;
    }

}
