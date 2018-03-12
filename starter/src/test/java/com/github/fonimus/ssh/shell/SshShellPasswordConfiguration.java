package com.github.fonimus.ssh.shell;

import org.springframework.context.annotation.Bean;

public class SshShellPasswordConfiguration {

    @Bean
    public SshShellAuthenticationProvider passwordAuthenticator() {
        return (user, pass, serverSession) -> user.equals(pass);
    }

}
