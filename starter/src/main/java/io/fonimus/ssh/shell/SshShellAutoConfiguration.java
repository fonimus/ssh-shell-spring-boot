package io.fonimus.ssh.shell;

import org.apache.sshd.server.SshServer;
import org.jline.terminal.Terminal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.shell.SpringShellAutoConfiguration;
import org.springframework.shell.jline.InteractiveShellApplicationRunner;
import org.springframework.shell.jline.JLineShellAutoConfiguration;
import org.springframework.shell.result.ThrowableResultHandler;

import static io.fonimus.ssh.shell.SshShellProperties.SSH_SHELL_PREFIX;

@Configuration
@ConditionalOnClass(SshServer.class)
@ConditionalOnProperty(name = SSH_SHELL_PREFIX + ".enable", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties({ SshShellProperties.class })
@AutoConfigureAfter({ JLineShellAutoConfiguration.class, SpringShellAutoConfiguration.class })
@ComponentScan(basePackages = { "io.fonimus.ssh.shell" })
public class SshShellAutoConfiguration {

	private static final ThreadLocal<Throwable> THREAD_CONTEXT = ThreadLocal.withInitial(() -> null);

	public static final String TERMINAL_DELEGATE = "terminalDelegate";

	@Autowired
	public ConfigurableEnvironment environment;

	@Bean(TERMINAL_DELEGATE)
	@Primary
	public Terminal terminal(Terminal terminal) {
		InteractiveShellApplicationRunner.disable(environment);
		return new SshShellTerminalDelegate(terminal);
	}

	@Bean
	@Primary
	public ThrowableResultHandler throwableResultHandler() {
		return new ThrowableResultHandler() {

			@Override
			protected void doHandleResult(Throwable result) {
				THREAD_CONTEXT.set(result);
				super.doHandleResult(result);
			}

			@Override
			public Throwable getLastError() {
				return THREAD_CONTEXT.get();
			}
		};
	}
}

