/*
 * Copyright (c) 2020 François Onimus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.fonimus.ssh.shell;

import com.github.fonimus.ssh.shell.auth.SshAuthentication;
import com.github.fonimus.ssh.shell.auth.SshShellSecurityAuthenticationProvider;
import lombok.extern.slf4j.Slf4j;
import org.apache.sshd.common.Factory;
import org.apache.sshd.server.ChannelSessionAware;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.Signal;
import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.command.Command;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.Parser;
import org.jline.terminal.Attributes;
import org.jline.terminal.Size;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.springframework.boot.Banner;
import org.springframework.core.env.Environment;
import org.springframework.shell.ExitRequest;
import org.springframework.shell.Input;
import org.springframework.shell.Shell;
import org.springframework.shell.jline.InteractiveShellApplicationRunner;
import org.springframework.shell.jline.JLineShellAutoConfiguration;
import org.springframework.shell.jline.PromptProvider;
import org.springframework.shell.result.DefaultResultHandler;

import java.io.*;
import java.nio.charset.StandardCharsets;

import static com.github.fonimus.ssh.shell.SshShellCommandFactory.SSH_THREAD_CONTEXT;

/**
 * Runnable for ssh shell session
 */
@Slf4j
public class SshShellRunnable
        implements Factory<Command>, ChannelSessionAware, Runnable {

    private ChannelSession session;

    private Banner shellBanner;

    private PromptProvider promptProvider;

    private Shell shell;

    private JLineShellAutoConfiguration.CompleterAdapter completerAdapter;

    private Parser parser;

    private Environment environment;

    private File historyFile;

    private org.apache.sshd.server.Environment sshEnv;

    private boolean displayBanner;

    private SshShellCommandFactory sshShellCommandFactory;

    private InputStream is;

    private OutputStream os;

    private ExitCallback ec;

    public SshShellRunnable(ChannelSession session, Banner shellBanner, PromptProvider promptProvider, Shell shell,
                            JLineShellAutoConfiguration.CompleterAdapter completerAdapter, Parser parser,
                            Environment environment, File historyFile,
                            org.apache.sshd.server.Environment sshEnv, boolean displayBanner,
                            SshShellCommandFactory sshShellCommandFactory, InputStream is,
                            OutputStream os, ExitCallback ec) {
        this.session = session;
        this.shellBanner = shellBanner;
        this.promptProvider = promptProvider;
        this.shell = shell;
        this.completerAdapter = completerAdapter;
        this.parser = parser;
        this.environment = environment;
        this.historyFile = historyFile;
        this.sshEnv = sshEnv;
        this.displayBanner = displayBanner;
        this.sshShellCommandFactory = sshShellCommandFactory;
        this.is = is;
        this.os = os;
        this.ec = ec;
    }

    /**
     * Run ssh session
     */
    @Override
    public void run() {
        LOGGER.debug("{}: running...", session.toString());
        Size size = new Size(Integer.parseInt(sshEnv.getEnv().get("COLUMNS")), Integer.parseInt(sshEnv.getEnv().get(
                "LINES")));
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             PrintStream ps = new PrintStream(baos, true, StandardCharsets.UTF_8.name());
             Terminal terminal = TerminalBuilder.builder().system(false).size(size).type(sshEnv.getEnv().get("TERM"))
                     .streams(is, os).build()) {

            try {
                DefaultResultHandler resultHandler = new DefaultResultHandler();
                resultHandler.setTerminal(terminal);

                Attributes attr = terminal.getAttributes();
                SshShellUtils.fill(attr, sshEnv.getPtyModes());
                terminal.setAttributes(attr);

                sshEnv.addSignalListener((channel, signal) -> {
                    terminal.setSize(new Size(
                            Integer.parseInt(sshEnv.getEnv().get("COLUMNS")),
                            Integer.parseInt(sshEnv.getEnv().get("LINES"))));
                    terminal.raise(Terminal.Signal.WINCH);
                }, Signal.WINCH);

                if (displayBanner && shellBanner != null) {
                    shellBanner.printBanner(environment, this.getClass(), ps);
                }
                resultHandler.handleResult(new String(baos.toByteArray(), StandardCharsets.UTF_8));
                resultHandler.handleResult("Please type `help` to see available commands");

                LineReader reader = LineReaderBuilder.builder()
                        .terminal(terminal)
                        .appName("Spring Ssh Shell")
                        .completer(completerAdapter)
                        .highlighter((reader1, buffer) -> {
                            int l = 0;
                            String best = null;
                            for (String command : shell.listCommands().keySet()) {
                                if (buffer.startsWith(command) && command.length() > l) {
                                    l = command.length();
                                    best = command;
                                }
                            }
                            if (best != null) {
                                return new AttributedStringBuilder(buffer.length()).append(best,
                                        AttributedStyle.BOLD).append(buffer.substring(l))
                                        .toAttributedString();
                            } else {
                                return new AttributedString(buffer,
                                        AttributedStyle.DEFAULT.foreground(AttributedStyle.RED));
                            }
                        })
                        .parser(parser)
                        .build();
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

                SSH_THREAD_CONTEXT.set(new SshContext(this, terminal, reader, authentication));
                shell.run(new SshShellInputProvider(reader, promptProvider));
                LOGGER.debug("{}: closing", session.toString());
                quit(0);
            } catch (Throwable e) {
                LOGGER.error("{}: unexpected exception", session.toString(), e);
                quit(1);
            }
        } catch (IOException e) {
            LOGGER.error("Unable to open terminal", e);
            quit(1);
        }
    }

    private void quit(int exitCode) {
        if (ec != null) {
            ec.onExit(exitCode);
        }
    }

    @Override
    public void setChannelSession(ChannelSession session) {
        this.session = session;
    }

    @Override
    public Command create() {
        return sshShellCommandFactory;
    }

    static class SshShellInputProvider
            extends InteractiveShellApplicationRunner.JLineInputProvider {

        public SshShellInputProvider(LineReader lineReader, PromptProvider promptProvider) {
            super(lineReader, promptProvider);
        }

        @Override
        public Input readInput() {
            SshContext ctx = SSH_THREAD_CONTEXT.get();
            if (ctx != null) {
                ctx.setPostProcessorsList(null);
            }
            try {
                return super.readInput();
            } catch (EndOfFileException e) {
                throw new ExitRequest(1);
            }
        }
    }
}
