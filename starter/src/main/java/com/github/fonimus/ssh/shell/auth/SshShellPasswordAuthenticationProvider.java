package com.github.fonimus.ssh.shell.auth;

import org.apache.sshd.server.auth.password.PasswordChangeRequiredException;
import org.apache.sshd.server.session.ServerSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * Password implementation
 */
public class SshShellPasswordAuthenticationProvider
        implements SshShellAuthenticationProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(SshShellPasswordAuthenticationProvider.class);

    private final String user;

    private final String password;

    public SshShellPasswordAuthenticationProvider(String user, String password) {
        this.user = user;
        String pass = password;
        if (pass == null) {
            pass = UUID.randomUUID().toString();
            LOGGER.info(" --- Generating password for ssh connection: {}", pass);
        }
        this.password = pass;
    }

    @Override
    public boolean authenticate(String username, String pass,
                                ServerSession serverSession) throws PasswordChangeRequiredException {
        return username.equals(this.user) && pass.equals(this.password);
    }
}
