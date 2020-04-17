package com.github.fonimus.ssh.shell.auth;

import lombok.extern.slf4j.Slf4j;
import org.apache.sshd.server.auth.password.PasswordChangeRequiredException;
import org.apache.sshd.server.session.ServerSession;

import java.util.UUID;

/**
 * Password implementation
 */
@Slf4j
public class SshShellPasswordAuthenticationProvider
        implements SshShellAuthenticationProvider {

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

        serverSession.getIoSession().setAttribute(AUTHENTICATION_ATTRIBUTE, new SshAuthentication(username));

        return username.equals(this.user) && pass.equals(this.password);
    }
}
