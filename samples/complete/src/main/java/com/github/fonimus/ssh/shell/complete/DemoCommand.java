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

package com.github.fonimus.ssh.shell.complete;

import com.github.fonimus.ssh.shell.SimpleTable;
import com.github.fonimus.ssh.shell.SshShellHelper;
import com.github.fonimus.ssh.shell.auth.SshAuthentication;
import com.github.fonimus.ssh.shell.commands.SshShellComponent;
import com.github.fonimus.ssh.shell.interactive.Interactive;
import com.github.fonimus.ssh.shell.interactive.KeyBinding;
import com.github.fonimus.ssh.shell.providers.ExtendedFileValueProvider;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.session.ServerSession;
import org.jline.terminal.Size;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.core.MethodParameter;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.shell.Availability;
import org.springframework.shell.CompletionContext;
import org.springframework.shell.CompletionProposal;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.shell.standard.ShellOption;
import org.springframework.shell.standard.ValueProviderSupport;
import org.springframework.stereotype.Component;

import java.io.File;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Demo command for example
 */
@SshShellComponent("demo-command")
public class DemoCommand extends AbstractHealthIndicator {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemoCommand.class);

    private final SshShellHelper helper;

    public DemoCommand(SshShellHelper helper) {
        this.helper = helper;
    }

    /**
     * Echo command
     *
     * @param message message to print
     * @return message
     */
    @ShellMethod("Echo command")
    public String echo(@ShellOption(valueProvider = CustomValuesProvider.class) String message) {
        return message;
    }

    /**
     * Terminal size command example
     *
     * @return size
     */
    @ShellMethod("Terminal size command")
    public Size size() {
        return helper.terminalSize();
    }

    /**
     * Progress displays command example
     *
     * @param progress current percentage
     */
    @ShellMethod("Progress command")
    public void progress(int progress) {
        helper.printSuccess(progress + "%");
        helper.print(helper.progress(progress));
    }

    /**
     * File provider command example
     *
     * @param file     file to get info from
     * @param extended extended provider file to get info from
     */
    @ShellMethod("File command")
    public void file(
            @ShellOption(defaultValue = ShellOption.NULL) File file,
            @ShellOption(valueProvider = ExtendedFileValueProvider.class, defaultValue = ShellOption.NULL) File extended) {

        info(file);
        info(extended);
    }

    private void info(File file) {
        if (file != null) {
            if (file.exists()) {
                helper.printSuccess("File exists: " + file.getAbsolutePath());
                helper.print("\nType: " + (file.isDirectory() ? "directory" : "file"));
                helper.print("Size: " + file.length());
            } else {
                helper.printError("File does not exist: " + file.getAbsolutePath());
            }
        }
    }

    /**
     * Interactive command example
     *
     * @param fullscreen fullscreen mode
     * @param delay      delay in ms
     */
    @ShellMethod("Interactive command")
    public void interactive(boolean fullscreen, @ShellOption(defaultValue = "3000") long delay) {

        KeyBinding binding = KeyBinding.builder()
                .description("K binding example")
                .key("k").input(() -> LOGGER.info("In specific action triggered by key 'k' !")).build();

        Interactive interactive = Interactive.builder().input((size, currentDelay) -> {
            LOGGER.info("In interactive command for input...");
            List<AttributedString> lines = new ArrayList<>();
            AttributedStringBuilder sb = new AttributedStringBuilder(size.getColumns());

            sb.append("\nCurrent time", AttributedStyle.BOLD).append(" : ");
            sb.append(String.format("%8tT", new Date()));

            lines.add(sb.toAttributedString());

            SecureRandom sr = new SecureRandom();
            lines.add(new AttributedStringBuilder().append(helper.progress(sr.nextInt(100)),
                    AttributedStyle.DEFAULT.foreground(sr.nextInt(6) + 1)).toAttributedString());
            lines.add(AttributedString.fromAnsi(SshShellHelper.INTERACTIVE_LONG_MESSAGE + "\n"));

            return lines;
        }).binding(binding).fullScreen(fullscreen).refreshDelay(delay).build();

        helper.interactive(interactive);
    }

    /**
     * Ex command
     *
     * @throws IllegalStateException for example
     */
    @ShellMethod("Ex command")
    public void ex() {
        throw new IllegalStateException("Test exception message");
    }

    /**
     * Interaction example command
     *
     * @return welcome message
     */
    @ShellMethod("Welcome command")
    public String welcome() {
        helper.printInfo("You are now in the welcome command");
        String name = helper.read("What's your name ?");
        return "Hello, '" + name + "' !";
    }

    /**
     * Confirmation example command
     *
     * @return welcome message
     */
    @ShellMethod("Confirmation command")
    public String conf() {
        return helper.confirm("Are you sure ?") ? "Great ! Let's do it !" : "Such a shame ...";
    }

    /**
     * Admin only example command
     *
     * @return welcome message
     */
    @ShellMethod("Admin command")
    @ShellMethodAvailability("adminAvailability")
    public String admin() {
        return "Finally an administrator !!";
    }

    /**
     * Check admin availability
     *
     * @return is admin
     */
    public Availability adminAvailability() {
        if (!helper.checkAuthorities(Collections.singletonList("ADMIN"))) {
            return Availability.unavailable("admin command is only for an admin users !");
        }
        return Availability.available();
    }

    /**
     * Authentication example command
     *
     * @return principal
     */
    @ShellMethod("Authentication command")
    public SshAuthentication authentication() {
        return helper.getAuthentication();
    }

    /**
     * Displays ssh env information
     *
     * @return table with ssh env information
     */
    @ShellMethod("Displays ssh env information")
    public String displaySshEnv() {
        Environment env = helper.getSshEnvironment();
        if (env == null) {
            return helper.getWarning("Not in a ssh session");
        }

        SimpleTable.SimpleTableBuilder builder = SimpleTable.builder().column("Property").column("Value");

        for (Map.Entry<String, String> e : env.getEnv().entrySet()) {
            builder.line(Arrays.asList(e.getKey(), e.getValue()));
        }

        return helper.renderTable(builder.build());
    }

    /**
     * Displays ssh session information
     *
     * @return table with ssh session information
     */
    @ShellMethod("Displays ssh session information")
    public String displaySshSession() {
        ServerSession session = helper.getSshSession();
        if (session == null) {
            return helper.getWarning("Not in a ssh session");
        }

        return helper.renderTable(SimpleTable.builder()
                .column("Property").column("Value")
                .line(Arrays.asList("Session id", session.getIoSession().getId()))
                .line(Arrays.asList("Local address", session.getIoSession().getLocalAddress()))
                .line(Arrays.asList("Remote address", session.getIoSession().getRemoteAddress()))
                .line(Arrays.asList("Acceptance address", session.getIoSession().getAcceptanceAddress()))
                .line(Arrays.asList("Server version", session.getServerVersion()))
                .line(Arrays.asList("Client version", session.getClientVersion()))
                .build());
    }

    /**
     * For scheduled command example
     */
    @Scheduled(initialDelay = 0, fixedDelay = 60000)
    public void logWithDelay() {
        LOGGER.info("In 'fixed-delay' scheduled task..");
    }

    /**
     * For scheduled command example
     */
    @Scheduled(initialDelay = 0, fixedRate = 60000)
    public void logWithRate() {
        LOGGER.info("In 'fixed-rate' scheduled task..");
    }

    /**
     * For scheduled command example
     */
    @Scheduled(cron = "0/60 * * * * *")
    public void logWithCron() {
        LOGGER.info("In 'cron' scheduled task..");
    }

    /**
     * For scheduled command example
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void logWithLongDuration() throws InterruptedException {
        LOGGER.info("In 'cron' scheduled task for a while..");
        Thread.sleep(100000);
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) {
        builder.up().withDetail("a-key", "a-value");
    }
}

@Component
class CustomValuesProvider
        extends ValueProviderSupport {

    private final static String[] VALUES = new String[]{
            "message1", "message2", "message3"
    };

    @Override
    public List<CompletionProposal> complete(MethodParameter parameter, CompletionContext completionContext,
                                             String[] hints) {
        return Arrays.stream(VALUES).map(CompletionProposal::new).collect(Collectors.toList());
    }
}
