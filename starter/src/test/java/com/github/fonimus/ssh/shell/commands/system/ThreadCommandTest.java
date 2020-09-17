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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

class ThreadCommandTest extends AbstractShellHelperTest {

    private ThreadCommand t;

    @BeforeEach
    void setUp() {
        t = new ThreadCommand(h, new SshShellProperties());
    }

    @Test
    void threads() throws Exception {
        for (ThreadCommand.ThreadColumn tc : ThreadCommand.ThreadColumn.values()) {
            assertNotNull(t.threads(ThreadCommand.ThreadAction.LIST, tc, true, true, null));
        }
        assertNotNull(t.threads(ThreadCommand.ThreadAction.DUMP, ThreadCommand.ThreadColumn.NAME, true, true,
                Thread.currentThread().getId()));
        assertThrows(IllegalArgumentException.class, () -> assertNotNull(t.threads(ThreadCommand.ThreadAction.DUMP,
                ThreadCommand.ThreadColumn.NAME, true, true, null)));
        assertThrows(IllegalArgumentException.class, () -> assertNotNull(t.threads(ThreadCommand.ThreadAction.DUMP,
                ThreadCommand.ThreadColumn.NAME, true, true, -1L)));

        when(reader.read(100L)).thenReturn(113);
        assertEquals("", t.threads(ThreadCommand.ThreadAction.LIST, ThreadCommand.ThreadColumn.NAME, true, false,
                null));

        when(ter.getSize()).thenReturn(new Size(10, 10));
        assertEquals("", t.threads(ThreadCommand.ThreadAction.LIST, ThreadCommand.ThreadColumn.NAME, true, false,
                null));
    }
}
