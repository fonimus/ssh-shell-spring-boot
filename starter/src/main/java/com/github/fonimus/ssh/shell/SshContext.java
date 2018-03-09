package com.github.fonimus.ssh.shell;

import org.apache.sshd.server.ExitCallback;
import org.jline.terminal.Terminal;

public class SshContext {

	private Terminal terminal;
	private ExitCallback exitCallback;
	private Thread thread;

	public SshContext(Terminal terminal, ExitCallback exitCallback, Thread thread) {
		this.terminal = terminal;
		this.exitCallback = exitCallback;
		this.thread = thread;
	}

	public Terminal getTerminal() {
		return terminal;
	}

	public void setTerminal(Terminal terminal) {
		this.terminal = terminal;
	}

	public ExitCallback getExitCallback() {
		return exitCallback;
	}

	public void setExitCallback(ExitCallback exitCallback) {
		this.exitCallback = exitCallback;
	}

	public Thread getThread() {
		return thread;
	}

	public void setThread(Thread thread) {
		this.thread = thread;
	}
}
