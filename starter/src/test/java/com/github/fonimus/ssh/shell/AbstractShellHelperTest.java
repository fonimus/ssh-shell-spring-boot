package com.github.fonimus.ssh.shell;

import com.github.fonimus.ssh.shell.auth.SshAuthentication;
import org.jline.reader.LineReader;
import org.jline.terminal.Size;
import org.jline.terminal.Terminal;
import org.jline.utils.NonBlockingReader;
import org.junit.jupiter.api.BeforeEach;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;

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
        h = new SshShellHelper();
        List<String> auth = Collections.singletonList("ROLE_ACTUATOR");
        lr = mock(LineReader.class);
        ter = mock(Terminal.class);
        writer = mock(PrintWriter.class);
        when(ter.writer()).thenReturn(writer);
        reader = mock(NonBlockingReader.class);
        when(ter.reader()).thenReturn(reader);
        when(lr.getTerminal()).thenReturn(ter);
        SshContext ctx = new SshContext(new SshShellRunnable(null, null, null, null, null, null, null, null, null,
                false, null, null, null, null), ter,
                lr, new SshAuthentication("user", null, null, auth));
        SshShellCommandFactory.SSH_THREAD_CONTEXT.set(ctx);
        when(ter.getType()).thenReturn("osx");
        when(ter.getSize()).thenReturn(new Size(123, 40));
    }
}
