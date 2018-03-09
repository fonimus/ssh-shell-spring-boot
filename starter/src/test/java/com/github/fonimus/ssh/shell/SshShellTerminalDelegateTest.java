package com.github.fonimus.ssh.shell;

import org.jline.terminal.Terminal;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;

public class SshShellTerminalDelegateTest {

    public static final String NAME = "name";

    @Test
    public void getName() throws Exception {
        Terminal terminal = Mockito.mock(Terminal.class);
        Mockito.when(terminal.getName()).thenReturn(NAME);
        assertEquals(NAME, new SshShellTerminalDelegate(terminal).getName());
    }

}
