package com.github.fonimus.ssh.shell.commands;

import com.github.fonimus.ssh.shell.AbstractShellHelperTest;
import org.jline.terminal.Size;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class ThreadCommandTest extends AbstractShellHelperTest {

    private ThreadCommand t;

    @BeforeEach
    void setUp() {
        t = new ThreadCommand(h);
    }

    @Test
    void threads() throws Exception {
        for (ThreadCommand.ThreadColumn tc : ThreadCommand.ThreadColumn.values()) {
            assertNotNull(t.threads(ThreadCommand.ThreadAction.LIST, tc, true, true, null));
        }
        assertNotNull(t.threads(ThreadCommand.ThreadAction.DUMP, ThreadCommand.ThreadColumn.NAME, true, true, Thread.currentThread().getId()));
        assertThrows(IllegalArgumentException.class, () -> assertNotNull(t.threads(ThreadCommand.ThreadAction.DUMP, ThreadCommand.ThreadColumn.NAME, true, true, null)));
        assertThrows(IllegalArgumentException.class, () -> assertNotNull(t.threads(ThreadCommand.ThreadAction.DUMP, ThreadCommand.ThreadColumn.NAME, true, true, -1L)));

        when(reader.read(100L)).thenReturn(113);
        assertEquals("", t.threads(ThreadCommand.ThreadAction.LIST, ThreadCommand.ThreadColumn.NAME, true, false, null));

        when(ter.getSize()).thenReturn(new Size(10, 10));
        assertEquals("", t.threads(ThreadCommand.ThreadAction.LIST, ThreadCommand.ThreadColumn.NAME, true, false, null));
    }
}