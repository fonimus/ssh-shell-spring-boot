package com.github.fonimus.ssh.shell;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
        classes = SshShellApplicationExclusionsTest.class, properties = {"ssh.shell.port=2344", "ssh.shell.actuator" +
        ".excludes[0]=info", "ssh.shell.actuator.excludes[1]=beans"})
@ExtendWith(SpringExtension.class)
@SpringBootApplication
@DirtiesContext
public class SshShellApplicationExclusionsTest extends AbstractTest {

    @Test
    void testCommandAvailability() {
        assertTrue(cmd.auditAvailability().isAvailable());
        assertFalse(cmd.infoAvailability().isAvailable());
        assertFalse(cmd.beansAvailability().isAvailable());
        assertTrue(cmd.configpropsAvailability().isAvailable());
    }
}
