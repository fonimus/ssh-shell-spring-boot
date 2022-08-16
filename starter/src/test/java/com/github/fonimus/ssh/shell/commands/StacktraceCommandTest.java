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
import com.github.fonimus.ssh.shell.postprocess.ExtendedResultHandlerService;
import org.jline.terminal.Terminal;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.PrintWriter;
import java.io.StringWriter;

class StacktraceCommandTest {

    @Test
    void stacktrace() {
        ExtendedResultHandlerService.THREAD_CONTEXT.set(null);

        StacktraceCommand cmd = new StacktraceCommand(new SshShellHelper(null), new SshShellProperties());
        Terminal terminal = Mockito.mock(Terminal.class);
        Mockito.when(terminal.writer()).thenReturn(new PrintWriter(new StringWriter()));
        cmd.setTerminal(terminal);
        cmd.stacktrace();
        Mockito.verify(terminal, Mockito.never()).writer();

        ExtendedResultHandlerService.THREAD_CONTEXT.set(new IllegalArgumentException("[TEST]"));

        cmd.stacktrace();
        Mockito.verify(terminal, Mockito.times(1)).writer();

    }
}
