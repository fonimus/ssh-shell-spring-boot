package com.github.fonimus.ssh.shell;

import org.apache.sshd.server.auth.password.PasswordAuthenticator;

/**
 * Interface to implements custom authentication provider
 */
public interface SshShellAuthenticationProvider extends PasswordAuthenticator {
}
