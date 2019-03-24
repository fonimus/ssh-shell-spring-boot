package com.github.fonimus.ssh.shell.basic;

import com.github.fonimus.ssh.shell.PromptColor;
import com.github.fonimus.ssh.shell.SshShellHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = BasicApplication.class)
@ExtendWith(SpringExtension.class)
@DirtiesContext
public class DemoApplicationWebTest {

    @Autowired
    private DemoCommand demo;

    @Test
    void testApplicationStartup() {
        assertEquals("message", demo.echo("message", null));
        assertEquals(new SshShellHelper().getColored("message", PromptColor.CYAN),
                demo.echo("message", PromptColor.CYAN));
        assertNotNull(demo.pojo());
    }
}
