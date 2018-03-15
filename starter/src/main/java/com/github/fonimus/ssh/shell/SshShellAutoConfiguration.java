package com.github.fonimus.ssh.shell;

import org.apache.sshd.server.SshServer;
import org.jline.reader.LineReader;
import org.jline.reader.Parser;
import org.jline.terminal.Terminal;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.shell.Shell;
import org.springframework.shell.SpringShellAutoConfiguration;
import org.springframework.shell.jline.InteractiveShellApplicationRunner;
import org.springframework.shell.jline.JLineShellAutoConfiguration;
import org.springframework.shell.jline.PromptProvider;
import org.springframework.shell.result.ThrowableResultHandler;

import com.github.fonimus.ssh.shell.auth.SshShellAuthenticationProvider;
import com.github.fonimus.ssh.shell.auth.SshShellPasswordAuthenticationProvider;
import com.github.fonimus.ssh.shell.auth.SshShellSecurityAuthenticationProvider;
import com.github.fonimus.ssh.shell.handler.PrettyJsonResultHandler;

import static com.github.fonimus.ssh.shell.SshShellProperties.SSH_SHELL_PREFIX;

/**
 * <p>Ssh shell auto configuration</p>
 * <p>Can be disabled by property <b>ssh.shell.enable=false</b></p>
 */
@Configuration
@ConditionalOnClass(SshServer.class)
@ConditionalOnProperty(name = SSH_SHELL_PREFIX + ".enable", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties({ SshShellProperties.class })
@AutoConfigureAfter({ JLineShellAutoConfiguration.class, SpringShellAutoConfiguration.class })
@ComponentScan(basePackages = { "com.github.fonimus.ssh.shell" })
public class SshShellAutoConfiguration {

	public static final String TERMINAL_DELEGATE = "terminalDelegate";

	private static final ThreadLocal<Throwable> THREAD_CONTEXT = ThreadLocal.withInitial(() -> null);

	@Autowired
	public ApplicationContext context;

	@Autowired
	public ConfigurableEnvironment environment;

	@Bean
	public PrettyJsonResultHandler prettyJsonResultHandler() {
		return new PrettyJsonResultHandler();
	}

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnClass(name = "org.springframework.security.authentication.AuthenticationManager")
	@ConditionalOnProperty(value = SSH_SHELL_PREFIX + ".authentication", havingValue = "security")
	public SshShellAuthenticationProvider sshShellSecurityAuthenticationProvider(SshShellProperties properties) {
		return new SshShellSecurityAuthenticationProvider(context, properties.getAuthProviderBeanName());
	}

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnProperty(value = SSH_SHELL_PREFIX + ".authentication", havingValue = "simple", matchIfMissing = true)
	public SshShellAuthenticationProvider sshShellSimpleAuthenticationProvider(SshShellProperties properties) {
		return new SshShellPasswordAuthenticationProvider(properties.getUser(), properties.getPassword());
	}

	/**
	 * Primary terminal which delegates with right session
	 *
	 * @param terminal jline terminal
	 * @return terminal
	 */
	@Bean(TERMINAL_DELEGATE)
	@Primary
	public Terminal terminal(Terminal terminal) {
		InteractiveShellApplicationRunner.disable(environment);
		return new SshShellTerminalDelegate(terminal);
	}

	/**
	 * Primary prompt provider
	 *
	 * @param properties ssh shell properties
	 * @return prompt provider
	 */
	@Bean
	@Primary
	public PromptProvider sshPromptProvider(SshShellProperties properties) {
		return () -> new AttributedString(properties.getPrompt().getText(),
				AttributedStyle.DEFAULT.foreground(
						properties.getPrompt().getColor().toJlineAttributedStyle()));
	}

	/**
	 * <p>Primary throwable result handler (overriding spring shell ones but extending it)</p>
	 * <p>Allow to get exception per ssh session</p>
	 *
	 * @return throwable result handler
	 */
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

	/**
	 * Primary shell application runner which answers true to {@link InteractiveShellApplicationRunner#isEnabled()}
	 *
	 * @param lineReader     line reader
	 * @param promptProvider prompt provider
	 * @param parser         parser
	 * @param shell          spring shell
	 * @param environment    spring environment
	 * @return shell application runner
	 */
	@Bean
	@Primary
	public InteractiveShellApplicationRunner sshInteractiveShellApplicationRunner(LineReader lineReader,
			PromptProvider promptProvider,
			Parser parser, Shell shell,
			Environment environment) {
		return new InteractiveShellApplicationRunner(lineReader, promptProvider, parser, shell, environment) {

			@Override
			public boolean isEnabled() {
				return true;
			}

			@Override
			public void run(ApplicationArguments args) {
				// do nothing
			}
		};
	}
}

