package com.github.fonimus.ssh.shell;

import org.apache.sshd.server.ExitCallback;
import org.jline.reader.LineReader;
import org.jline.terminal.Terminal;

import com.github.fonimus.ssh.shell.auth.SshAuthentication;

/**
 * Ssh context to hold terminal, exit callback and thread per thread
 */
public class SshContext {

	private final LineReader lineReader;

	private ExitCallback exitCallback;

	private Thread thread;

	private Terminal terminal;

	private SshAuthentication authentication;

	/**
	 * Constructor
	 *
	 * @param exitCallback ssh exit callback
	 * @param thread       ssh thread session
	 * @param terminal     ssh terminal
	 * @param lineReader   ssh line reader
	 * @param authentication    (optional) spring authentication objects
	 */
	public SshContext(ExitCallback exitCallback, Thread thread, Terminal terminal, LineReader lineReader,
			SshAuthentication authentication) {
		this.exitCallback = exitCallback;
		this.thread = thread;
		this.terminal = terminal;
		this.lineReader = lineReader;
		this.authentication = authentication;
	}

	public ExitCallback getExitCallback() {
		return exitCallback;
	}

	public Thread getThread() {
		return thread;
	}

	public Terminal getTerminal() {
		return terminal;
	}

	public LineReader getLineReader() {
		return lineReader;
	}

	public SshAuthentication getAuthentication() {
		return authentication;
	}
}
