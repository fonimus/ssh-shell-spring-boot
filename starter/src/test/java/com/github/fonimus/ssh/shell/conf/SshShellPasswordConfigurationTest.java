package com.github.fonimus.ssh.shell.conf;

import org.springframework.context.annotation.Bean;

import com.github.fonimus.ssh.shell.auth.SshShellAuthenticationProvider;

public class SshShellPasswordConfigurationTest {

	@Bean
	public SshShellAuthenticationProvider passwordAuthenticator() {
		return (user, pass, serverSession) -> user.equals(pass);
	}

}
