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
import com.github.fonimus.ssh.shell.SshShellProperties;
import org.jline.reader.impl.history.DefaultHistory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HistoryCommandTest {

    private HistoryCommand shared;

    private HistoryCommand notShared;

    @BeforeEach
    void setUp() {
        SshShellHelper helper = mock(SshShellHelper.class);
        when(helper.getHistory()).thenReturn(new DefaultHistory());
        when(helper.isLocalPrompt()).thenReturn(false);
        shared = new HistoryCommand(new SshShellProperties(), helper, new DefaultHistory());
        SshShellProperties properties = new SshShellProperties();
        properties.setSharedHistory(false);
        notShared = new HistoryCommand(properties, helper, new DefaultHistory());
    }

    @Test
    void testGet() throws Exception {
        testGet(shared);
        testGet(notShared);
    }

    private void testGet(HistoryCommand cmd) throws Exception {
        List<String> lines = cmd.history(null);
        assertNotNull(lines);
        assertEquals(0, lines.size());
    }

    @Test
    void testWrite() throws Exception {
        testWrite(shared, "target/test-write-history.txt");
        testWrite(notShared, "target/test-write-history-shared.txt");
    }

    private void testWrite(HistoryCommand cmd, String fileName) throws IOException {
        File file = new File(fileName);
        List<String> lines = cmd.history(file);
        assertNotNull(lines);
        assertEquals(1, lines.size());
        assertTrue(lines.get(0).startsWith("Wrote 0 entries to"));
    }
}
