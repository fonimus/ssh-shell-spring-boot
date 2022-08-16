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

import com.github.fonimus.ssh.shell.auth.SshAuthentication;
import org.apache.sshd.server.Environment;
import org.jline.reader.LineReader;
import org.jline.terminal.Size;
import org.jline.terminal.Terminal;
import org.jline.utils.NonBlockingReader;
import org.junit.jupiter.api.BeforeEach;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;

import static com.github.fonimus.ssh.shell.SshShellUtilsTest.mockChannelSession;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class AbstractShellHelperTest {

    protected static SshShellHelper h;

    protected static LineReader lr;

    protected static Terminal ter;

    protected static PrintWriter writer;

    protected NonBlockingReader reader;

    @BeforeEach
    public void each() {
        h = new SshShellHelper(null);
        h.setDefaultTerminal(ter);
        h.setDefaultLineReader(lr);
        List<String> auth = Collections.singletonList("ROLE_ACTUATOR");
        lr = mock(LineReader.class);
        ter = mock(Terminal.class);
        writer = mock(PrintWriter.class);
        when(ter.writer()).thenReturn(writer);
        reader = mock(NonBlockingReader.class);
        when(ter.reader()).thenReturn(reader);
        when(lr.getTerminal()).thenReturn(ter);

        Environment sshEnv = mock(Environment.class);
        SshContext ctx = new SshContext(new SshShellRunnable(new SshShellProperties(), null,
                null, null, null, null, null, null,
                mockChannelSession(4L), sshEnv, null, null, null, null), ter, lr,
                new SshAuthentication("user", "user", null, null, auth));
        SshShellCommandFactory.SSH_THREAD_CONTEXT.set(ctx);
        when(ter.getType()).thenReturn("osx");
        when(ter.getSize()).thenReturn(new Size(123, 40));
    }
}
