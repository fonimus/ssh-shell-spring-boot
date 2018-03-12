package com.github.fonimus.ssh.shell;

import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.springframework.context.annotation.Bean;

public class SshShellPasswordConfiguration {

    @Bean
    public PasswordAuthenticator passwordAuthenticator() {
        return (user, pass, serverSession) -> user.equals(pass);
    }

}
