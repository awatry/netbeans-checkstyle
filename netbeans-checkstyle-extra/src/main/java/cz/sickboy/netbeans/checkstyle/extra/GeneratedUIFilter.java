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
package cz.sickboy.netbeans.checkstyle.extra;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.AutomaticBean;
import com.puppycrawl.tools.checkstyle.api.FileContents;
import com.puppycrawl.tools.checkstyle.api.Filter;
import com.puppycrawl.tools.checkstyle.api.TextBlock;
import com.puppycrawl.tools.checkstyle.checks.FileContentsHolder;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Petr.Hejl
 */
public class GeneratedUIFilter extends AutomaticBean implements Filter {

    private static final Pattern BLOCK_ON_PATTERN =
            Pattern.compile("GEN-END\\:"); // NOI18N

    private static final Pattern BLOCK_OFF_PATTERN =
            Pattern.compile("GEN-BEGIN\\:"); // NOI18N

    private static final Pattern[] ONE_LINE_PATTERNS =
            new Pattern[] {Pattern.compile("GEN-FIRST\\:"), Pattern.compile("GEN-LAST\\:")}; // NOI18N

    private final List<SectionTag> sectionTags = new ArrayList<SectionTag>();

    private final Set<Integer> ignoredLines = new HashSet<Integer>();

    /**
     * References the current FileContents for this filter.
     * Since this is a weak reference to the FileContents, the FileContents
     * can be reclaimed as soon as the strong references in TreeWalker
     * and FileContentsHolder are reassigned to the next FileContents,
     * at which time filtering for the current FileContents is finished.
     */
    private WeakReference<FileContents> fileContentsReference = new WeakReference<FileContents>(null);

    public GeneratedUIFilter() {
        super();
    }

    public FileContents getFileContents() {
        return fileContentsReference.get();
    }

    public void setFileContents(FileContents aFileContents) {
        fileContentsReference = new WeakReference<FileContents>(aFileContents);
    }

    public boolean accept(AuditEvent aEvent) {
        if (aEvent.getLocalizedMessage() == null) {
            return true; // special event
        }

        FileContents currentContents = FileContentsHolder.getContents();
        if (currentContents == null) {
            return true;
        }

        if (getFileContents() != currentContents) {
            setFileContents(currentContents);
            tagComments();
        }

        if (ignoredLines.contains(aEvent.getLine())) {
            return false;
        }

        SectionTag matchTag = findPreceedingTag(aEvent);
        if (matchTag != null && !matchTag.isBegin()) {
            return false;
        }
        return true;
    }

    private SectionTag findPreceedingTag(AuditEvent aEvent) {
        SectionTag result = null;
        for (SectionTag tag : sectionTags) {
            if ((tag.getLine() > aEvent.getLine())
                    || (tag.getLine() == aEvent.getLine() && tag.isBegin())) {
                break;
            }
            result = tag;
        }
        return result;
    }

    private void tagComments() {
        sectionTags.clear();
        Collection comments = getFileContents().getCppComments().values();

        for (Iterator iter = comments.iterator(); iter.hasNext();) {
            TextBlock comment = (TextBlock) iter.next();
            String[] text = comment.getText();
            for (int i = 0, startLineNo = comment.getStartLineNo(); i < text.length; i++) {
                tagCommentLine(text[i], startLineNo + i);
            }
        }

        Collections.sort(sectionTags);
    }

    private void tagCommentLine(String commentText, int lineNumber) {
        Matcher offMatcher = BLOCK_OFF_PATTERN.matcher(commentText);
        if (offMatcher.find()) {
            sectionTags.add(new SectionTag(lineNumber, false));
            return;
        }
        Matcher onMatcher = BLOCK_ON_PATTERN.matcher(commentText);
        if (onMatcher.find()) {
            sectionTags.add(new SectionTag(lineNumber, true));
            return;
        }

        for (Pattern oneLinePattern : ONE_LINE_PATTERNS) {
            Matcher matcher = oneLinePattern.matcher(commentText);
            if (matcher.find()) {
                ignoredLines.add(lineNumber);
                break;
            }
        }
    }

    private static class SectionTag implements Comparable<SectionTag> {

        private final int line;

        private final boolean begin;

        public SectionTag(int line, boolean begin) {
            this.line = line;
            this.begin = begin;
        }

        public int getLine() {
            return line;
        }

        public boolean isBegin() {
            return begin;
        }

        public int compareTo(SectionTag other) {
            return line - other.getLine();
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof SectionTag)) {
                return false;
            }
            return line == ((SectionTag) obj).getLine();
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 59 * hash + this.line;
            return hash;
        }

    }
}
