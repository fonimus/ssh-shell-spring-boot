package com.github.fonimus.ssh.shell.conf;

import com.github.fonimus.ssh.shell.auth.SshShellAuthenticationProvider;
import org.springframework.context.annotation.Bean;

public class SshShellPasswordConfigurationTest {

    @Bean
    public SshShellAuthenticationProvider passwordAuthenticator() {
        return (user, pass, serverSession) -> user.equals(pass);
    }

}
