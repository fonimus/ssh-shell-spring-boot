package com.github.fonimus.ssh.shell;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = SshShellApplicationTest.class,
        properties = {"ssh.shell.port=2345", "ssh.shell.password=pass"})
@ExtendWith(SpringExtension.class)
@SpringBootApplication
@DirtiesContext
public class SshShellApplicationTest extends AbstractCommandTest {

    @Test
    void testCommandAvailability() {
        setActuatorRole();

        super.commonCommandAvailability();

        assertFalse(cmd.httptraceAvailability().isAvailable());
    }

    @Override
    void testHttpTrace() {
        // do nothing
    }
}
