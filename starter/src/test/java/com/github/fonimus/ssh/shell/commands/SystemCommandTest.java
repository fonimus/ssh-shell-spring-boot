package com.github.fonimus.ssh.shell.commands;

import com.github.fonimus.ssh.shell.SshShellHelper;
import org.jline.terminal.Size;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SystemCommandTest {

    private SystemCommand cmd;

    @BeforeEach
    void setUp() {
        SshShellHelper helper = mock(SshShellHelper.class);
        when(helper.terminalSize()).thenReturn(new Size(100, 100));
        cmd = new SystemCommand(helper);
    }

    @Test
    void jvmEnv() {
        cmd.jvmEnv(false);
        cmd.jvmEnv(true);
    }

    @Test
    void jvmProperties() {
        cmd.jvmProperties(false);
        cmd.jvmProperties(true);
    }
}