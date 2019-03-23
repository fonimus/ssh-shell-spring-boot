package com.github.fonimus.ssh.shell;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.github.fonimus.ssh.shell.SshShellProperties.SSH_SHELL_PREFIX;

/**
 * Ssh shell properties (prefix : {@link SshShellProperties#SSH_SHELL_PREFIX})
 */
@Data
@ConfigurationProperties(prefix = SSH_SHELL_PREFIX)
@Validated
public class SshShellProperties {

	public static final String SSH_SHELL_PREFIX = "ssh.shell";

	public static final String SSH_SHELL_ENABLE = SSH_SHELL_PREFIX + ".enable";

	public static final String ACTUATOR_ROLE = "ACTUATOR";

    private Prompt prompt = new Prompt();

    private Actuator actuator = new Actuator();

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

	public enum AuthenticationType {
		simple, security
	}

	/**
	 * Prompt configuration
	 */
    @Data
    public static class Prompt {

		private String text = "shell>";

		private PromptColor color = PromptColor.WHITE;
	}

	/**
	 * Actuator configuration
	 */
    @Data
    public static class Actuator {

		private boolean enable = true;

		private List<String> authorizedRoles = Arrays.asList(ACTUATOR_ROLE);

		private List<String> excludes = new ArrayList<>();
	}

}
