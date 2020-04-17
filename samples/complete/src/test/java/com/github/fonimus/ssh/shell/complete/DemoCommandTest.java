package com.github.fonimus.ssh.shell.complete;

import com.github.fonimus.ssh.shell.SshContext;
import com.github.fonimus.ssh.shell.SshShellCommandFactory;
import com.github.fonimus.ssh.shell.SshShellHelper;
import com.github.fonimus.ssh.shell.SshShellRunnable;
import com.github.fonimus.ssh.shell.auth.SshAuthentication;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;
import org.jline.terminal.Size;
import org.jline.terminal.Terminal;
import org.jline.utils.NonBlockingReader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DemoCommandTest {

    private static DemoCommand cmd;

    private static Terminal terminal;

    private static Size size = new Size(20, 30);

    private static LineReader lr;

    private static SshAuthentication auth;

    private static NonBlockingReader reader;

    @BeforeAll
    static void prepare() {
        cmd = new DemoCommand(new SshShellHelper());
        terminal = mock(Terminal.class);
        when(terminal.getSize()).thenReturn(size);
        PrintWriter writer = mock(PrintWriter.class);
        lr = mock(LineReader.class);
        ParsedLine line = mock(ParsedLine.class);
        when(line.line()).thenReturn("y");
        when(lr.getParsedLine()).thenReturn(line);
        when(lr.getTerminal()).thenReturn(terminal);
        when(terminal.writer()).thenReturn(writer);
        reader = mock(NonBlockingReader.class);
        when(terminal.reader()).thenReturn(reader);
        when(terminal.getType()).thenReturn("osx");
        auth = new SshAuthentication("user", null, null, null);
        SshContext ctx = new SshContext(
                new SshShellRunnable(null, null, null, null, null, null, null, null, null, false, null, null, null, null),
                terminal, lr, auth);
        SshShellCommandFactory.SSH_THREAD_CONTEXT.set(ctx);
    }

    @Test
    void testCommandEcho() {
        assertEquals("message", cmd.echo("message"));
    }

    @Test
    void testCommandEx() {
        IllegalStateException ex = Assertions.assertThrows(IllegalStateException.class, () -> cmd.ex());
        assertEquals("Test exception message", ex.getMessage());
    }

    @Test
    void testLog() {
        cmd.log();
    }

    @Test
    void size() {
        assertEquals(size, cmd.size());
    }

    @Test
    void progress() {
        cmd.progress(3);
    }

    @Test
    void auth() {
        assertEquals(auth, cmd.authentication());
    }

    @Test
    void admin() {
        cmd.admin();
    }

    @Test
    void interactive() throws Exception {
        when(reader.read(100L)).thenReturn(113);
        cmd.interactive(false, 1000);
    }

    @Test
    void conf() {
        assertEquals("Great ! Let's do it !", cmd.conf());
    }

    @Test
    void welcome() {
        assertEquals("Hello, 'y' !", cmd.welcome());
    }

    @Test
    void noAvailability() {
        assertFalse(cmd.adminAvailability().isAvailable());
    }

    @Test
    void provider() {
        assertEquals(3, new CustomValuesProvider().complete(null, null, null).size());
    }
}
