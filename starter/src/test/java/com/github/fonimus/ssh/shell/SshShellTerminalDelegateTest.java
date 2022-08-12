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

import org.jline.terminal.Terminal;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static com.github.fonimus.ssh.shell.SshShellCommandFactory.SSH_THREAD_CONTEXT;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SshShellTerminalDelegateTest {

    private static SshShellTerminalDelegate del;

    @BeforeAll
    static void prepare() {
        Terminal terminal = Mockito.mock(Terminal.class);
        del = new SshShellTerminalDelegate(terminal);
    }

    @BeforeEach
    void setUp() {
        SSH_THREAD_CONTEXT.remove();
    }

    @Test
    void init() {
        assertThrows(IllegalStateException.class, () -> new SshShellTerminalDelegate(null).getName());
    }

    @Test
    public void getName() {
        del.getName();
    }

    @Test
    public void handle() {
        del.handle(null, null);
    }

    @Test
    public void raise() {
        del.raise(null);
    }

    @Test
    public void reader() {
        del.reader();
    }

    @Test
    public void writer() {
        del.writer();
    }

    @Test
    public void input() {
        del.input();
    }

    @Test
    public void output() {
        del.output();
    }

    @Test
    public void enterRawMode() {
        del.enterRawMode();
    }

    @Test
    public void echo() {
        del.echo();
    }

    @Test
    public void echoBis() {
        del.echo(false);
    }

    @Test
    public void getAttributes() {
        del.getAttributes();
    }

    @Test
    public void setAttributes() {
        del.setAttributes(null);
    }

    @Test
    public void getSize() {
        del.getSize();
    }

    @Test
    public void setSize() {
        del.setSize(null);
    }

    @Test
    public void flush() {
        del.flush();
    }

    @Test
    public void getType() {
        del.getType();
    }

    @Test
    public void puts() {
        del.puts(null);
    }

    @Test
    public void getBooleanCapability() {
        del.getBooleanCapability(null);
    }

    @Test
    public void getNumericCapability() {
        del.getNumericCapability(null);
    }

    @Test
    public void getStringCapability() {
        del.getStringCapability(null);
    }

    @Test
    public void getCursorPosition() {
        del.getCursorPosition(null);
    }

    @Test
    public void hasMouseSupport() {
        del.hasMouseSupport();
    }

    @Test
    public void trackMouse() {
        del.trackMouse(null);
    }

    @Test
    public void readMouseEvent() {
        del.readMouseEvent();
    }

    @Test
    public void readMouseEventBis() {
        del.readMouseEvent(null);
    }

    @Test
    public void close() throws Exception {
        del.close();
    }

}
