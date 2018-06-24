package com.github.fonimus.ssh.shell;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.sshd.common.Factory;
import org.apache.sshd.server.ChannelSessionAware;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.command.Command;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.ParsedLine;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.terminal.impl.AbstractPosixTerminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.Banner;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.shell.Input;
import org.springframework.shell.Shell;
import org.springframework.shell.jline.JLineShellAutoConfiguration;
import org.springframework.shell.jline.PromptProvider;
import org.springframework.shell.result.DefaultResultHandler;
import org.springframework.stereotype.Component;

import com.github.fonimus.ssh.shell.auth.SshAuthentication;
import com.github.fonimus.ssh.shell.auth.SshShellSecurityAuthenticationProvider;

import static com.github.fonimus.ssh.shell.SshShellHistoryAutoConfiguration.HISTORY_FILE;

/**
 * Ssh shell command factory implementation
 */
@Component
public class SshShellCommandFactory
		implements Command, Factory<Command>, ChannelSessionAware, Runnable {

	public static final ThreadLocal<SshContext> SSH_THREAD_CONTEXT = ThreadLocal.withInitial(() -> null);

	private static final Logger LOGGER = LoggerFactory.getLogger(SshShellCommandFactory.class);

	private InputStream is;

	private OutputStream os;

	private ExitCallback ec;

	private Thread sshThread;

	private ChannelSession session;

	private String terminalType;

	private Banner shellBanner;

	private PromptProvider promptProvider;

	private Shell shell;

	private JLineShellAutoConfiguration.CompleterAdapter completerAdapter;

	private Environment environment;

	private File historyFile;

	/**
	 * Constructor
	 *
	 * @param banner           shell banner
	 * @param promptProvider   prompt provider
	 * @param shell            spring shell
	 * @param completerAdapter completer adapter
	 * @param environment      spring environment
	 * @param historyFile      history file location
	 */
	public SshShellCommandFactory(Banner banner, @Lazy PromptProvider promptProvider, Shell shell,
			JLineShellAutoConfiguration.CompleterAdapter completerAdapter, Environment environment,
			@Qualifier(HISTORY_FILE) File historyFile) {
		this.shellBanner = banner;
		this.promptProvider = promptProvider;
		this.shell = shell;
		this.completerAdapter = completerAdapter;
		this.environment = environment;
		this.historyFile = historyFile;
	}

	/**
	 * Start ssh session
	 *
	 * @param env ssh environment
	 */
	@Override
	public void start(org.apache.sshd.server.Environment env) {
		LOGGER.debug("start     : {}", session.toString());
		terminalType = env.getEnv().get("TERM");
		sshThread = new Thread(this, "ssh-session-" + System.nanoTime());
		sshThread.start();
	}

	/**
	 * Run ssh session
	 */
	@Override
	public void run() {
		LOGGER.debug("run         : {}", session.toString());
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
				PrintStream ps = new PrintStream(baos, true, "utf-8");
				Terminal terminal = TerminalBuilder.builder().system(false).type(terminalType).streams(is, os).build()) {
			DefaultResultHandler resultHandler = new DefaultResultHandler();
			resultHandler.setTerminal(terminal);
			shellBanner.printBanner(environment, this.getClass(), ps);
			resultHandler.handleResult(new String(baos.toByteArray(), StandardCharsets.UTF_8));
			resultHandler.handleResult("Please type `help` to see available commands");
			LineReader reader = LineReaderBuilder.builder().terminal(terminal).completer(completerAdapter).build();
			reader.setVariable(LineReader.HISTORY_FILE, historyFile.toPath());
			Object authenticationObject = session.getSession().getIoSession().getAttribute(
					SshShellSecurityAuthenticationProvider.AUTHENTICATION_ATTRIBUTE);
			SshAuthentication authentication = null;
			if (authenticationObject != null) {
				if (!(authenticationObject instanceof SshAuthentication)) {
					throw new IllegalStateException("Unknown authentication object class: " + authenticationObject.getClass().getName());
				}
				authentication = (SshAuthentication) authenticationObject;
			}
			SSH_THREAD_CONTEXT.set(new SshContext(ec, sshThread, terminal, reader, authentication));
			shell.run(() -> {
				try {
					reader.readLine(promptProvider.getPrompt().toAnsi(reader.getTerminal()));
					if (terminal instanceof AbstractPosixTerminal) {
						reader.getTerminal().writer().println();
					}
				} catch (EndOfFileException e) {
					LOGGER.debug("interrupt : {}", session.toString());
					quit(0);
					return null;
				} catch (UserInterruptException e) {
					return Input.EMPTY;
				}
				return new ParsedInput(reader.getParsedLine());
			});
			LOGGER.debug("end       : {}", session.toString());
			quit(0);
		} catch (IOException | RuntimeException e) {
			LOGGER.error("exception : {}", session.toString(), e);
			quit(1);
		}
	}

	class ParsedInput
			implements Input {

		private final ParsedLine parsedLine;

		ParsedInput(ParsedLine parsedLine) {
			this.parsedLine = parsedLine;
		}

		@Override
		public String rawText() {
			return parsedLine.line();
		}

		@Override
		public List<String> words() {
			return sanitizeInput(parsedLine.words());
		}
	}

	private static List<String> sanitizeInput(List<String> words) {
		words = words.stream()
				.map(s -> s.replaceAll("^\\n+|\\n+$", "")) // CR at beginning/end of line introduced by backslash continuation
				.map(s -> s.replaceAll("\\n+", " ")) // CR in middle of word introduced by return inside a quoted string
				.collect(Collectors.toList());
		return words;
	}

	private void quit(int exitCode) {
		SshContext ctx = SSH_THREAD_CONTEXT.get();
		if (ctx != null) {
			ctx.getExitCallback().onExit(exitCode);
		}
	}

	@Override
	public void destroy() {
		SshContext ctx = SSH_THREAD_CONTEXT.get();
		if (ctx != null) {
			ctx.getThread().interrupt();
		}
	}

	@Override
	public void setErrorStream(OutputStream errOS) {
		// not used
	}

	@Override
	public void setExitCallback(ExitCallback ec) {
		this.ec = ec;
	}

	@Override
	public void setInputStream(InputStream is) {
		this.is = is;
	}

	@Override
	public void setOutputStream(OutputStream os) {
		this.os = os;
	}

	@Override
	public void setChannelSession(ChannelSession session) {
		this.session = session;
	}

	@Override
	public Command create() {
		return this;
	}
}