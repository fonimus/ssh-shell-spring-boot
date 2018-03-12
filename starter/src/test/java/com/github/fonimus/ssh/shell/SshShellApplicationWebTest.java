package com.github.fonimus.ssh.shell;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {SshShellApplicationWebTest.class, SshShellSessionConfigurationTest.class},
        properties = {"ssh.shell.port=2346", "ssh.shell.password=pass"})
@ExtendWith(SpringExtension.class)
@SpringBootApplication
@DirtiesContext
public class SshShellApplicationWebTest extends AbstractCommandTest {

    @Test
    void testCommandAvailability() {
        super.commonCommandAvailability();
        assertTrue(cmd.httptraceAvailability().isAvailable());
        assertTrue(cmd.sessionsAvailability().isAvailable());
    }
}
