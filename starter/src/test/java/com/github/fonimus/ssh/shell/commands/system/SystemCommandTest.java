/*
 * Copyright (c) 2020 François Onimus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.fonimus.ssh.shell.commands.system;

import com.github.fonimus.ssh.shell.AbstractShellHelperTest;
import com.github.fonimus.ssh.shell.SshShellProperties;
import org.jline.terminal.Size;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class SystemCommandTest extends AbstractShellHelperTest {

    private SystemCommand command;

    @BeforeEach
    void setUp() {
        command = new SystemCommand(h, new SshShellProperties());
    }

    @Test
    void jvmEnv() {
        command.jvmEnv(false);
        command.jvmEnv(true);
    }

    @Test
    void jvmProperties() {
        command.jvmProperties(false);
        command.jvmProperties(true);
    }

    @Test
    void threads() throws Exception {
        for (SystemCommand.ThreadColumn tc : SystemCommand.ThreadColumn.values()) {
            assertNotNull(command.threads(SystemCommand.ThreadAction.list, tc, true, true, null));
        }
        // Hint: We have to override the PrintWriterAccess otherwise Thread.printStackTrace() will fail with:
        // java.lang.NullPointerException: Cannot enter synchronized block because "lock" is null
        writer = new PrintWriter(PrintWriter.nullWriter()); //Hint: mock(PrintWriter.class); fails in Throwable.printStackTrace()
        when(ter.writer()).thenReturn(writer);
        assertNotNull(command.threads(SystemCommand.ThreadAction.dump, SystemCommand.ThreadColumn.name, true, true,
                Thread.currentThread().getId()));
        assertThrows(IllegalArgumentException.class,
                () -> assertNotNull(command.threads(SystemCommand.ThreadAction.dump,
                        SystemCommand.ThreadColumn.name, true, true, null)));
        assertThrows(IllegalArgumentException.class,
                () -> assertNotNull(command.threads(SystemCommand.ThreadAction.dump,
                        SystemCommand.ThreadColumn.name, true, true, -1L)));

        when(reader.read(100L)).thenReturn(113);
        assertEquals("", command.threads(SystemCommand.ThreadAction.list, SystemCommand.ThreadColumn.name, true, false,
                null));

        when(ter.getSize()).thenReturn(new Size(10, 10));
        assertEquals("", command.threads(SystemCommand.ThreadAction.list, SystemCommand.ThreadColumn.name, true, false,
                null));
    }
}
