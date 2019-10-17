package com.github.fonimus.ssh.shell;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.github.fonimus.ssh.shell.conf.SshShellPasswordConfigurationTest;

import static com.github.fonimus.ssh.shell.SshHelperTest.call;
import static com.github.fonimus.ssh.shell.SshHelperTest.verifyResponse;
import static com.github.fonimus.ssh.shell.SshHelperTest.write;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
		classes = { SshShellApplicationCustomAuthenticatorTest.class, SshShellPasswordConfigurationTest.class },
		properties = { "ssh.shell.port=2349", "management.endpoints.web.exposure.include=*" }
)
@ExtendWith(SpringExtension.class)
@SpringBootApplication
@DirtiesContext
public class SshShellApplicationCustomAuthenticatorTest
		extends AbstractTest {

	@Test
	void testSshCallInfoCommand() {
		call("user", "user", properties, (is, os) -> {
			write(os, "info");
			verifyResponse(is, "{}");
		});
	}

	@Test
	void testSshCallInfoCommandOtherUser() {
		call("myself", "myself", properties, (is, os) -> {
			write(os, "info");
			verifyResponse(is, "{}");
		});
	}

}
