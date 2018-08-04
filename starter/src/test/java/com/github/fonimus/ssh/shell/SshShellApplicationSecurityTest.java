package com.github.fonimus.ssh.shell;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.github.fonimus.ssh.shell.conf.SshShellSecurityConfigurationTest;

import static com.github.fonimus.ssh.shell.SshHelperTest.call;
import static com.github.fonimus.ssh.shell.SshHelperTest.verifyResponse;
import static com.github.fonimus.ssh.shell.SshHelperTest.write;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		classes = { SshShellApplicationSecurityTest.class, SshShellSecurityConfigurationTest.class },
		properties = { "ssh.shell.port=2346", "ssh.shell.password=pass", "ssh.shell.authentication=security" })
@ExtendWith(SpringExtension.class)
@SpringBootApplication
@DirtiesContext
public class SshShellApplicationSecurityTest
		extends AbstractTest {

	@Test
	void testSshCallInfoCommandAdmin() {
		call("admin", "admin", properties, (is, os) -> {
			write(os, "info");
			verifyResponse(is, "{}");
		});
	}

	@Test
	void testSshCallInfoCommandUser() {
		call("user", "password", properties, (is, os) -> {
			write(os, "health");
			verifyResponse(is, "forbidden for current user");
		});
	}
}
