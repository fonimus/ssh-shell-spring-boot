package com.github.fonimus.ssh.shell.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * Ssh authentication
 */
@Getter
@AllArgsConstructor
public class SshAuthentication {

	private Object principal;

	private Object details;

	private Object credentials;

	private List<String> authorities;
}
