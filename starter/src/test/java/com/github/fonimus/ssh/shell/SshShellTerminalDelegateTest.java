package com.github.fonimus.ssh.shell;

import org.jline.terminal.Terminal;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SshShellTerminalDelegateTest {

    public static final String NAME = "name";

    private static SshShellTerminalDelegate del;

    @BeforeAll
    static void prepare() throws Exception {
        Terminal terminal = Mockito.mock(Terminal.class);
        Mockito.when(terminal.getName()).thenReturn(NAME);
        del = new SshShellTerminalDelegate(terminal);
        assertEquals(NAME, del.getName());
    }

    @Test
    public void handle() throws Exception {
        del.handle(null, null);
    }

    @Test
    public void raise() throws Exception {
        del.raise(null);
    }

    @Test
    public void reader() throws Exception {
        del.reader();
    }

    @Test
    public void writer() throws Exception {
        del.writer();
    }

    @Test
    public void input() throws Exception {
        del.input();
    }

    @Test
    public void output() throws Exception {
        del.output();
    }

    @Test
    public void enterRawMode() throws Exception {
        del.enterRawMode();
    }

    @Test
    public void echo() throws Exception {
        del.echo();
    }

    @Test
    public void echoBis() throws Exception {
        del.echo(false);
    }

    @Test
    public void getAttributes() throws Exception {
        del.getAttributes();
    }

    @Test
    public void setAttributes() throws Exception {
        del.setAttributes(null);
    }

    @Test
    public void getSize() throws Exception {
        del.getSize();
    }

    @Test
    public void setSize() throws Exception {
        del.setSize(null);
    }

    @Test
    public void flush() throws Exception {
        del.flush();
    }

    @Test
    public void getType() throws Exception {
        del.getType();
    }

    @Test
    public void puts() throws Exception {
        del.puts(null);
    }

    @Test
    public void getBooleanCapability() throws Exception {
        del.getBooleanCapability(null);
    }

    @Test
    public void getNumericCapability() throws Exception {
        del.getNumericCapability(null);
    }

    @Test
    public void getStringCapability() throws Exception {
        del.getStringCapability(null);
    }

    @Test
    public void getCursorPosition() throws Exception {
        del.getCursorPosition(null);
    }

    @Test
    public void hasMouseSupport() throws Exception {
        del.hasMouseSupport();
    }

    @Test
    public void trackMouse() throws Exception {
        del.trackMouse(null);
    }

    @Test
    public void readMouseEvent() throws Exception {
        del.readMouseEvent();
    }

    @Test
    public void readMouseEventBis() throws Exception {
        del.readMouseEvent(null);
    }

    @Test
    public void close() throws Exception {
        del.close();
    }

}
