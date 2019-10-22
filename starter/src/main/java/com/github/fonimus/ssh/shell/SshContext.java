package com.github.fonimus.ssh.shell;

import com.github.fonimus.ssh.shell.auth.SshAuthentication;
import com.github.fonimus.ssh.shell.postprocess.PostProcessorObject;
import lombok.Getter;
import lombok.Setter;
import org.jline.reader.LineReader;
import org.jline.terminal.Terminal;

import java.util.List;

/**
 * Ssh context to hold terminal, exit callback and thread per thread
 */
@Getter
public class SshContext {

    private SshShellRunnable sshShellRunnable;

    private Terminal terminal;

    private LineReader lineReader;

    private SshAuthentication authentication;

    @Setter
    private List<PostProcessorObject> postProcessorsList;

    public SshContext() {
    }

    /**
     * Constructor
     *
     * @param sshShellRunnable ssh runnable
     * @param terminal         ssh terminal
     * @param lineReader       ssh line reader
     * @param authentication   (optional) spring authentication objects
     */
    public SshContext(SshShellRunnable sshShellRunnable, Terminal terminal, LineReader lineReader, SshAuthentication authentication) {
        this.sshShellRunnable = sshShellRunnable;
        this.terminal = terminal;
        this.lineReader = lineReader;
        this.authentication = authentication;
    }
}
