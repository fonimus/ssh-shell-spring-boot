package com.github.fonimus.ssh.shell;

import com.github.fonimus.ssh.shell.conf.SshShellPasswordConfigurationTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static com.github.fonimus.ssh.shell.SshHelperTest.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
        classes = {SshShellApplicationCustomAuthenticatorTest.class, SshShellPasswordConfigurationTest.class},
        properties = {"ssh.shell.port=2349"})
@ExtendWith(SpringExtension.class)
@SpringBootApplication
@DirtiesContext
public class SshShellApplicationCustomAuthenticatorTest extends AbstractTest {

    @Test
    void testSshCallInfoCommand() {
        call("user", "user", properties, (is, os) -> {
            write(os, "info");
            verifyResponse(is, "{ }");
        });
    }

    @Test
    void testSshCallInfoCommandOtherUser() {
        call("myself", "myself", properties, (is, os) -> {
            write(os, "info");
            verifyResponse(is, "{ }");
        });
    }

}
