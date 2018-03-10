package com.github.fonimus.ssh.shell;

import org.apache.sshd.server.ExitCallback;
import org.jline.reader.LineReader;
import org.jline.terminal.Terminal;

/**
 * Ssh context to hold terminal, exit callback and thread per thread
 */
public class SshContext {

    private ExitCallback exitCallback;

    private Thread thread;

    private final LineReader lineReader;
    private Terminal terminal;

    /**
     * Constructor
     *
     * @param exitCallback ssh exit callback
     * @param thread       ssh thread session
     * @param terminal     ssh terminal
     * @param lineReader   ssh line reader
     */
    public SshContext(ExitCallback exitCallback, Thread thread, Terminal terminal, LineReader lineReader) {
        this.exitCallback = exitCallback;
        this.thread = thread;
        this.terminal = terminal;
        this.lineReader = lineReader;
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
}
