package com.github.fonimus.ssh.shell;

import com.github.fonimus.ssh.shell.conf.SshShellSessionConfigurationTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {SshShellApplicationWebTest.class, SshShellSessionConfigurationTest.class},
        properties = {"ssh.shell.port=2346", "ssh.shell.password=pass"})
@ExtendWith(SpringExtension.class)
@SpringBootApplication
@DirtiesContext
public class SshShellApplicationWebTest extends AbstractCommandTest {

    @Test
    void testCommandAvailability() {
        setActuatorRole();

        super.commonCommandAvailability();

        assertTrue(cmd.httptraceAvailability().isAvailable());
        assertTrue(cmd.sessionsAvailability().isAvailable());
    }

    @Test
    void testCommandAvailabilityWithoutRole() {
        setRole("USER");

        assertAll(
                () -> assertFalse(cmd.auditAvailability().isAvailable()),
                () -> assertFalse(cmd.beansAvailability().isAvailable()),
                () -> assertFalse(cmd.conditionsAvailability().isAvailable()),
                () -> assertFalse(cmd.configpropsAvailability().isAvailable()),
                () -> assertFalse(cmd.envAvailability().isAvailable()),
                () -> assertFalse(cmd.healthAvailability().isAvailable()),
                () -> assertTrue(cmd.infoAvailability().isAvailable()),
                () -> assertFalse(cmd.loggersAvailability().isAvailable()),
                () -> assertFalse(cmd.metricsAvailability().isAvailable()),
                () -> assertFalse(cmd.mappingsAvailability().isAvailable()),
                () -> assertFalse(cmd.scheduledtasksAvailability().isAvailable()),
                () -> assertFalse(cmd.shutdownAvailability().isAvailable()),
                () -> assertFalse(cmd.threaddumpAvailability().isAvailable())
        );
    }
}
