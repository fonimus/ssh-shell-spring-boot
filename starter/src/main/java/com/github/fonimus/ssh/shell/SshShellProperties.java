package com.github.fonimus.ssh.shell;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import static com.github.fonimus.ssh.shell.SshShellProperties.SSH_SHELL_PREFIX;

/**
 * Ssh shell properties (prefix : {@link SshShellProperties#SSH_SHELL_PREFIX})
 */
@ConfigurationProperties(prefix = SSH_SHELL_PREFIX)
@Validated
public class SshShellProperties {

	public static final String SSH_SHELL_PREFIX = "ssh.shell";

	public static final String SSH_SHELL_ENABLE = SSH_SHELL_PREFIX + ".enable";

	public static final String ACTUATOR_ROLE = "ACTUATOR";

	private final Prompt prompt = new Prompt();

	private final Actuator actuator = new Actuator();

	private boolean enable = true;

	private String host = "127.0.0.1";

	private int port = 2222;

	private String user = "user";

	private String password;

	private AuthenticationType authentication = AuthenticationType.simple;

	private String authProviderBeanName;

	private File hostKeyFile = new File(System.getProperty("java.io.tmpdir"), "hostKey.ser");

	private File historyFile = new File(System.getProperty("java.io.tmpdir"), "sshShellHistory.log");

	private List<String> confirmationWords;

	public boolean isEnable() {
		return enable;
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public AuthenticationType getAuthentication() {
		return authentication;
	}

	public void setAuthentication(AuthenticationType authentication) {
		this.authentication = authentication;
	}

	public String getAuthProviderBeanName() {
		return authProviderBeanName;
	}

	public void setAuthProviderBeanName(String authProviderBeanName) {
		this.authProviderBeanName = authProviderBeanName;
	}

	public File getHostKeyFile() {
		return hostKeyFile;
	}

	public void setHostKeyFile(File hostKeyFile) {
		this.hostKeyFile = hostKeyFile;
	}

	public File getHistoryFile() {
		return historyFile;
	}

	public void setHistoryFile(File historyFile) {
		this.historyFile = historyFile;
	}

	public List<String> getConfirmationWords() {
		return confirmationWords;
	}

	public void setConfirmationWords(List<String> confirmationWords) {
		this.confirmationWords = confirmationWords;
	}

	public Prompt getPrompt() {
		return prompt;
	}

	public Actuator getActuator() {
		return actuator;
	}

	public enum AuthenticationType {
		simple, security
	}

	/**
	 * Prompt configuration
	 */
	public static class Prompt {

		private String text = "shell>";

		private PromptColor color = PromptColor.WHITE;

		public String getText() {
			return text;
		}

		public void setText(String text) {
			this.text = text;
		}

		public PromptColor getColor() {
			return color;
		}

		public void setColor(PromptColor color) {
			this.color = color;
		}
	}

	/**
	 * Actuator configuration
	 */
	public static class Actuator {

		private boolean enable = true;

		private List<String> authorizedRoles = Arrays.asList(ACTUATOR_ROLE);

		private List<String> excludes = new ArrayList<>();

		public boolean isEnable() {
			return enable;
		}

		public void setEnable(boolean enable) {
			this.enable = enable;
		}

		public List<String> getAuthorizedRoles() {
			return authorizedRoles;
		}

		public void setAuthorizedRoles(List<String> authorizedRoles) {
			this.authorizedRoles = authorizedRoles;
		}

		public List<String> getExcludes() {
			return excludes;
		}

		public void setExcludes(List<String> excludes) {
			this.excludes = excludes;
		}
	}

}
