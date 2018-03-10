package com.github.fonimus.ssh.shell.sample;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DemoCommandTest {

    private static DemoCommand cmd;

    @BeforeAll
    static void prepare() {
        cmd = new DemoCommand();
    }

    @Test
    void testCommandTest() {
        assertEquals("message", cmd.echo("message"));
    }

    @Test
    void testCommandEx() {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> cmd.ex());
        assertEquals("Test exception message", ex.getMessage());
    }

    @Test
    void testLog() {
        cmd.log();
    }
}
