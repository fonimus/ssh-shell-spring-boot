package com.github.fonimus.ssh.shell;

import org.apache.sshd.server.ExitCallback;

/**
 * Ssh context to hold terminal, exit callback and thread per thread
 */
public class SshContext {

    private ExitCallback exitCallback;

    private Thread thread;

    /**
     * Constructor
     *
     * @param exitCallback ssh exit callback
     * @param thread       ssh thread session
     */
    public SshContext(ExitCallback exitCallback, Thread thread) {
        this.exitCallback = exitCallback;
        this.thread = thread;
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
