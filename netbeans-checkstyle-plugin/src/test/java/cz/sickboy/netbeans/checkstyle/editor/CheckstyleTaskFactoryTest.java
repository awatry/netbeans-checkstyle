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

import java.io.File;
import junit.framework.TestCase;
import org.netbeans.api.java.source.CancellableTask;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Petr Hejl
 */
public class CheckstyleTaskFactoryTest extends TestCase {

    public CheckstyleTaskFactoryTest(String name) {
        super(name);
    }

    public void testFactory() {
        CheckstyleTaskFactory factory = new CheckstyleTaskFactory();

        CancellableTask task1 = factory.createTask(FileUtil.toFileObject(
                FileUtil.normalizeFile(new File(System.getProperty("java.io.tmpdir")))));
        assertNotNull(task1);

        CancellableTask task2 = factory.createTask(FileUtil.toFileObject(
                FileUtil.normalizeFile(new File(System.getProperty("java.io.tmpdir")))));
        assertNotNull(task2);

        assertNotSame(task1, task2);
    }
}
