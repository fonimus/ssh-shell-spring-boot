package com.github.fonimus.ssh.shell.basic;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@DirtiesContext
public class DemoApplicationWebTest {

    @Autowired
    private DemoCommand demo;

    @Test
    void testApplicationStartup() {
        assertEquals("message", demo.echo("message"));
    }
}
