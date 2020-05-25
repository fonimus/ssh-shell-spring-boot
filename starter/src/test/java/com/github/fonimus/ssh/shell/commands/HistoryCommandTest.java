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

package com.github.fonimus.ssh.shell.commands;

import com.github.fonimus.ssh.shell.SshShellHelper;
import org.jline.reader.impl.history.DefaultHistory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HistoryCommandTest {

    private HistoryCommand cmd;

    @BeforeEach
    void setUp() {
        SshShellHelper helper = mock(SshShellHelper.class);
        when(helper.getHistory()).thenReturn(new DefaultHistory());
        cmd = new HistoryCommand(helper);
    }

    @Test
    void testGet() throws Exception {
        List<String> lines = cmd.history(null);
        assertNotNull(lines);
        assertEquals(0, lines.size());
    }

    @Test
    void testWrite() throws Exception {
        File file = new File("target/test-write-history.txt");
        List<String> lines = cmd.history(file);
        assertNotNull(lines);
        assertEquals(1, lines.size());
        assertTrue(lines.get(0).startsWith("Wrote 0 entries to"));
    }
}
