package io.fonimus.ssh.shell;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.apache.sshd.common.Factory;
import org.apache.sshd.server.ChannelSessionAware;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.channel.ChannelSession;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.Banner;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.shell.InputProvider;
import org.springframework.shell.Shell;
import org.springframework.shell.jline.InteractiveShellApplicationRunner;
import org.springframework.shell.jline.JLineShellAutoConfiguration;
import org.springframework.shell.jline.PromptProvider;
import org.springframework.shell.result.DefaultResultHandler;
import org.springframework.stereotype.Component;

@Component
public class SshShellCommandFactory
		implements Command, Factory<Command>, ChannelSessionAware, Runnable {

	private static final ThreadLocal<SshContext> THREAD_CONTEXT = ThreadLocal.withInitial(() -> null);

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

	private Terminal terminalDelegate;

	public SshShellCommandFactory(Banner shellBanner, @Lazy PromptProvider promptProvider, Shell shell,
			JLineShellAutoConfiguration.CompleterAdapter completerAdapter, Environment environment,
			@Qualifier(SshShellAutoConfiguration.TERMINAL_DELEGATE) Terminal terminalDelegate) {
		this.shellBanner = shellBanner;
		this.promptProvider = promptProvider;
		this.shell = shell;
		this.completerAdapter = completerAdapter;
		this.environment = environment;
		this.terminalDelegate = terminalDelegate;
	}

	@Override
	public void start(org.apache.sshd.server.Environment env) {
		terminalType = env.getEnv().get("TERM");
		sshThread = new Thread(this, "ssh-session-" + System.nanoTime());
		sshThread.start();
	}

	@Override
	public void run() {
		Terminal terminal;
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); PrintStream ps = new PrintStream(baos, true, "utf-8")) {
			terminal = TerminalBuilder.builder().system(false).type(terminalType).streams(is, os).build();
			DefaultResultHandler resultHandler = new DefaultResultHandler();
			resultHandler.setTerminal(terminal);
			shellBanner.printBanner(environment, this.getClass(), ps);
			resultHandler.handleResult(new String(baos.toByteArray(), StandardCharsets.UTF_8));
			resultHandler.handleResult("Please type `help` to see available commands");
			LineReader reader = LineReaderBuilder.builder().terminal(terminal).completer(completerAdapter).build();
			InputProvider inputProvider = new InteractiveShellApplicationRunner.JLineInputProvider(reader, promptProvider);
			THREAD_CONTEXT.set(new SshContext(terminal, ec, sshThread));
			if (terminalDelegate instanceof SshShellTerminalDelegate) {
				((SshShellTerminalDelegate) terminalDelegate).setDelegate(terminal);
			}
			shell.run(inputProvider);
			quit(0);
		} catch (IOException | RuntimeException e) {
			// log ex
			quit(1);
		}
	}

	private void quit(int exitCode) {
		SshContext ctx = THREAD_CONTEXT.get();
		if (ctx != null) {
			ctx.getExitCallback().onExit(exitCode);
		}
	}

	@Override
	public void destroy() {
		SshContext ctx = THREAD_CONTEXT.get();
		if (ctx != null) {
			ctx.getThread().interrupt();
		}
	}

	@Override
	public void setErrorStream(OutputStream errOS) {
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