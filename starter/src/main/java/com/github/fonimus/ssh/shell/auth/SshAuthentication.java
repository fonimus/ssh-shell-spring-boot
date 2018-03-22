package com.github.fonimus.ssh.shell.auth;

import java.util.List;

/**
 * Ssh authentication
 */
public class SshAuthentication {

	private Object principal;

	private Object details;

	private Object credentials;

	private List<String> authorities;

	public SshAuthentication(Object principal, Object details, Object credentials, List<String> authorities) {
		this.principal = principal;
		this.details = details;
		this.credentials = credentials;
		this.authorities = authorities;
	}

	public Object getPrincipal() {
		return principal;
	}

	public Object getDetails() {
		return details;
	}

	public Object getCredentials() {
		return credentials;
	}

	public List<String> getAuthorities() {
		return authorities;
	}
}
