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
import org.jline.reader.History;
import org.jline.reader.impl.history.DefaultHistory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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
        History history = new DefaultHistory();
        history.add("cmd1");
        history.add("cmd2");
        when(helper.getHistory()).thenReturn(history);
        when(helper.isLocalPrompt()).thenReturn(false);
        shared = new HistoryCommand(new SshShellProperties(), helper);
        SshShellProperties properties = new SshShellProperties();
        properties.setSharedHistory(false);
        notShared = new HistoryCommand(properties, helper);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testGet(boolean displayArray) throws Exception {
        testGet(shared, displayArray);
        testGet(notShared, displayArray);
    }

    @SuppressWarnings("unchecked")
    private void testGet(HistoryCommand cmd, boolean displayArray) throws Exception {
        if (!displayArray) {
            String result = (String) cmd.history(null, false);
            assertEquals("cmd1\ncmd2\n", result);
        } else {
            List<String> lines = (List<String>) cmd.history(null, true);
            assertNotNull(lines);
            assertEquals(2, lines.size());
            assertEquals("cmd1", lines.get(0));
            assertEquals("cmd2", lines.get(1));
        }
    }

    @Test
    void testWrite() throws Exception {
        testWrite(shared, "target/test-write-history.txt");
        testWrite(notShared, "target/test-write-history-shared.txt");
    }

    private void testWrite(HistoryCommand cmd, String fileName) throws IOException {
        File file = new File(fileName);
        String lines = (String) cmd.history(file, false);
        assertNotNull(lines);
        assertTrue(lines.startsWith("Wrote 2 entries to"));
    }
}
