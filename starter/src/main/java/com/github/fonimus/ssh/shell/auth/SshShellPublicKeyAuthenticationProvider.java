package com.github.fonimus.ssh.shell.auth;

import lombok.extern.slf4j.Slf4j;
import org.apache.sshd.server.config.keys.AuthorizedKeysAuthenticator;
import org.apache.sshd.server.session.ServerSession;

import java.io.File;
import java.security.PublicKey;

import static com.github.fonimus.ssh.shell.auth.SshShellAuthenticationProvider.AUTHENTICATION_ATTRIBUTE;

/**
 * Authorized keys authenticator extension to set authentication attribute
 */
@Slf4j
public class SshShellPublicKeyAuthenticationProvider
        extends AuthorizedKeysAuthenticator {

    /**
     * Default constructor
     *
     * @param publicKeysFile public keys file
     */
    public SshShellPublicKeyAuthenticationProvider(File publicKeysFile) {
        super(publicKeysFile.toPath());
    }

    @Override
    public boolean authenticate(String username, PublicKey key, ServerSession session) {
        boolean authenticated = super.authenticate(username, key, session);
        session.getIoSession().setAttribute(AUTHENTICATION_ATTRIBUTE, new SshAuthentication(username));
        return authenticated;
    }
}
