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

package com.github.fonimus.ssh.shell;

import com.github.fonimus.ssh.shell.interactive.Interactive;
import com.github.fonimus.ssh.shell.interactive.InteractiveInput;
import com.github.fonimus.ssh.shell.interactive.KeyBinding;
import org.jline.reader.impl.completer.ArgumentCompleter;
import org.jline.terminal.Size;
import org.jline.utils.AttributedString;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class SshShellHelperTest extends AbstractShellHelperTest {

    private static final String MESSAGE = "The message";

    @Test
    void confirm() {
        setAnswer("y");
        assertTrue(h.confirm(MESSAGE));
        assertFalse(h.confirm(MESSAGE, "oui"));
        setAnswer("yes");
        assertTrue(h.confirm(MESSAGE));
        assertFalse(h.confirm(MESSAGE, "oui"));
        setAnswer("no");
        assertFalse(h.confirm(MESSAGE));
        setAnswer("nope");
        assertFalse(h.confirm(MESSAGE));
        setAnswer("y");
        setAnswer("oui");
        assertTrue(h.confirm(MESSAGE, "oui"));
        assertTrue(h.confirm(MESSAGE, "OUI"));
        assertFalse(h.confirm(MESSAGE, true, "OUI"));
    }

    private void setAnswer(String answer) {
        when(lr.getParsedLine()).thenReturn(new ArgumentCompleter.ArgumentLine(answer, 0));
    }

    @Test
    void read() {
        setAnswer(MESSAGE);
        assertEquals(MESSAGE, h.read("test"));
        verify(lr, times(2)).getTerminal();
        verify(ter, times(1)).writer();
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(writer, times(1)).println(captor.capture());
        assertEquals(1, captor.getAllValues().size());
        assertEquals("test", captor.getValue());

        reset(writer);
        assertEquals(MESSAGE, h.read());
        verify(writer, times(0)).println(anyString());
    }

    @Test
    void getSuccess() {
        assertEquals("\u001B[32m" + MESSAGE + "\u001B[0m", h.getSuccess(MESSAGE));
    }

    @Test
    void getInfo() {
        assertEquals("\u001B[36m" + MESSAGE + "\u001B[0m", h.getInfo(MESSAGE));
    }

    @Test
    void getWarning() {
        assertEquals("\u001B[33m" + MESSAGE + "\u001B[0m", h.getWarning(MESSAGE));
    }

    @Test
    void getError() {
        assertEquals("\u001B[31m" + MESSAGE + "\u001B[0m", h.getError(MESSAGE));
    }

    @Test
    void getColored() {
        assertEquals("\u001B[30m" + MESSAGE + "\u001B[0m", h.getColored(MESSAGE, PromptColor.BLACK));
    }

    @Test
    void printSuccess() {
        h.printSuccess(MESSAGE);
        verifyMessage("\u001B[32m" + MESSAGE + "\u001B[0m");
    }

    @Test
    void printInfo() {
        h.printInfo(MESSAGE);
        verifyMessage("\u001B[36m" + MESSAGE + "\u001B[0m");
    }

    @Test
    void printWarning() {
        h.printWarning(MESSAGE);
        verifyMessage("\u001B[33m" + MESSAGE + "\u001B[0m");
    }

    @Test
    void printError() {
        h.printError(MESSAGE);
        verifyMessage("\u001B[31m" + MESSAGE + "\u001B[0m");
    }

    @Test
    void print() {
        h.print(MESSAGE);
        verifyMessage(MESSAGE);
    }

    @Test
    void printColor() {
        h.print(MESSAGE, PromptColor.BLACK);
        verifyMessage("\u001B[30m" + MESSAGE + "\u001B[0m");
    }

    @Test
    void checkAuthorities() {
        assertTrue(h.checkAuthorities(Collections.singletonList("ACTUATOR")));
        assertTrue(h.checkAuthorities(Collections.singletonList("ACTUATOR")));
        assertFalse(h.checkAuthorities(Collections.singletonList("TOTO")));
        assertTrue(h.checkAuthorities(Collections.singletonList("ACTUATOR"), Collections.singletonList("ACTUATOR"),
                true));
        assertTrue(h.checkAuthorities(Collections.singletonList("ACTUATOR"), Collections.singletonList("ACTUATOR"),
                false));
        assertTrue(h.checkAuthorities(Collections.singletonList("ACTUATOR"), null, true));
        assertFalse(h.checkAuthorities(Collections.singletonList("ACTUATOR"), null, false));
    }

    @Test
    void getAuthentication() {
        assertNotNull(h.getAuthentication());
    }

    @Test
    void terminalSize() {
        assertEquals(123, h.terminalSize().getColumns());
        assertEquals(40, h.terminalSize().getRows());
    }

    @Test
    void terminalWriter() {
        assertNotNull(h.terminalWriter());
    }

    @Test
    void progress() {
        when(ter.getSize()).thenReturn(new Size(13, 40));
        assertEquals("[>          ]", h.progress(0));
        assertEquals("[>          ]", h.progress(1));
        assertEquals("[=>         ]", h.progress(10));
        assertEquals("[========>  ]", h.progress(80));
        assertEquals("[========>  ]", h.progress(40, 50));
        assertEquals("[==========>]", h.progress(100));
        assertEquals("[==========>]", h.progress(110));

        when(ter.getSize()).thenReturn(new Size(2, 40));
        assertEquals("", h.progress(40));
    }

    @Test
    void interactive() throws Exception {
        // return 'q' = 113 after third times
        when(reader.read(100L))
                // '-','+' char
                .thenReturn(43).thenReturn(45)
                // 'f' char
                .thenReturn(102)
                // 'q' char
                .thenReturn(113).thenReturn(113).thenReturn(113);

        when(ter.getSize()).thenReturn(new Size(13, 40));
        final int[] count = {0};

        KeyBinding binding = KeyBinding.builder().key("f").description("Binding Ok").input(() -> {

        }).build();
        KeyBinding failingBinding = KeyBinding.builder().key("f").description("Failing Binding").input(() -> {

        }).build();

        InteractiveInput input = (size, currentDelay) -> {
            count[0]++;
            return Collections.singletonList(AttributedString.EMPTY);
        };

        assertEquals(0, count[0]);
        h.interactive(Interactive.builder().binding(binding).binding(failingBinding).input(input).build());
        assertEquals(4, count[0]);

        h.interactive(Interactive.builder().input(input).fullScreen(false).build());

        assertEquals(5, count[0]);

        h.interactive(input);

        assertEquals(6, count[0]);
    }

    private void verifyMessage(String message) {
        verify(ter, times(1)).writer();
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(writer, times(1)).println(captor.capture());
        assertEquals(1, captor.getAllValues().size());
        assertEquals(message, captor.getValue());
    }

    @Test
    void test() {
        assertNotNull(SshShellHelper.at(1, 2));
    }

    @Test
    void table() {
        String top = "┌──────────┬──────────┬──────────┐\n";
        String headers = top +
                "│   col1   │   col2   │   col3   │\n";
        String middle = "├──────────┼──────────┼──────────┤\n";
        String body = "│line1 col1│line1 col2│line1 col3│\n" +
                "├──────────┼──────────┼──────────┤\n" +
                "│line2 col1│line2 col2│line2 col3│\n" +
                "└──────────┴──────────┴──────────┘\n";
        SimpleTable.SimpleTableBuilder builder = SimpleTable.builder()
                .column("col1")
                .column("col2")
                .column("col3")
                .line(Arrays.asList("line1 col1", "line1 col2", "line1 col3"))
                .line(Arrays.asList("line2 col1", "line2 col2", "line2 col3"));
        String table = h.renderTable(builder.build());
        assertEquals(headers + middle + body, table);
        table = h.renderTable(builder.displayHeaders(false).build());
        assertEquals(top + body, table);
    }
}
