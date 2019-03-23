package com.github.fonimus.ssh.shell.commands;

import com.github.fonimus.ssh.shell.postprocess.TypePostProcessorResultHandler;
import org.jline.terminal.Terminal;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.PrintWriter;
import java.io.StringWriter;

class StacktraceCommandTest {

    @Test
    void stacktrace() {
        TypePostProcessorResultHandler.THREAD_CONTEXT.set(null);

        StacktraceCommand cmd = new StacktraceCommand();
        Terminal terminal = Mockito.mock(Terminal.class);
        Mockito.when(terminal.writer()).thenReturn(new PrintWriter(new StringWriter()));
        cmd.setTerminal(terminal);
        cmd.stacktrace();
        Mockito.verify(terminal, Mockito.never()).writer();

        TypePostProcessorResultHandler.THREAD_CONTEXT.set(new IllegalArgumentException("[TEST]"));

        cmd.stacktrace();
        Mockito.verify(terminal, Mockito.times(1)).writer();

    }
}