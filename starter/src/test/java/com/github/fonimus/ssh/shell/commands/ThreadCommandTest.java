package com.github.fonimus.ssh.shell.commands;

import com.github.fonimus.ssh.shell.AbstractShellHelperTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ThreadCommandTest extends AbstractShellHelperTest {

    private ThreadCommand t;

    @BeforeEach
    void setUp() {
        t = new ThreadCommand(h);
    }

    @Test
    void threads() {
        for (ThreadCommand.ThreadColumn tc : ThreadCommand.ThreadColumn.values()) {
            assertNotNull(t.threads(ThreadCommand.ThreadAction.LIST, tc, true, null));
        }
        assertNotNull(t.threads(ThreadCommand.ThreadAction.DUMP, ThreadCommand.ThreadColumn.NAME, true, Thread.currentThread().getId()));
        assertThrows(IllegalArgumentException.class, () -> assertNotNull(t.threads(ThreadCommand.ThreadAction.DUMP, ThreadCommand.ThreadColumn.NAME, true, null)));
        assertThrows(IllegalArgumentException.class, () -> assertNotNull(t.threads(ThreadCommand.ThreadAction.DUMP, ThreadCommand.ThreadColumn.NAME, true, -1L)));
    }
}