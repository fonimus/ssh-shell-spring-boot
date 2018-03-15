package com.github.fonimus.ssh.shell;

import java.util.List;

import org.apache.sshd.server.ExitCallback;
import org.jline.reader.LineReader;
import org.jline.terminal.Terminal;

/**
 * Ssh context to hold terminal, exit callback and thread per thread
 */
public class SshContext {

	private final LineReader lineReader;

	private ExitCallback exitCallback;

	private Thread thread;

	private Terminal terminal;

	private List<String> authorities;

	/**
	 * Constructor
	 *
	 * @param exitCallback ssh exit callback
	 * @param thread       ssh thread session
	 * @param terminal     ssh terminal
	 * @param lineReader   ssh line reader
	 * @param authorities  (optional) spring authorities of current session
	 */
	public SshContext(ExitCallback exitCallback, Thread thread, Terminal terminal, LineReader lineReader,
			List<String> authorities) {
		this.exitCallback = exitCallback;
		this.thread = thread;
		this.terminal = terminal;
		this.lineReader = lineReader;
		this.authorities = authorities;
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

	public List<String> getAuthorities() {
		return authorities;
	}
}
