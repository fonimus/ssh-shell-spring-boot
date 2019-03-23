package com.github.fonimus.ssh.shell;

import com.github.fonimus.ssh.shell.auth.SshAuthentication;
import com.github.fonimus.ssh.shell.postprocess.PostProcessorObject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.jline.reader.LineReader;
import org.jline.terminal.Terminal;

import java.util.List;

/**
 * Ssh context to hold terminal, exit callback and thread per thread
 */
@Getter
@RequiredArgsConstructor
public class SshContext {

    private Thread thread;

    private Terminal terminal;

    private LineReader lineReader;

    private SshAuthentication authentication;

    @Setter
    private List<PostProcessorObject> postProcessorsList;

    /**
     * Constructor
     *
     * @param thread         ssh thread session
     * @param terminal       ssh terminal
     * @param lineReader     ssh line reader
     * @param authentication (optional) spring authentication objects
     */
    public SshContext(Thread thread, Terminal terminal, LineReader lineReader,
                      SshAuthentication authentication) {
        this.thread = thread;
        this.terminal = terminal;
        this.lineReader = lineReader;
        this.authentication = authentication;
    }
}
