package com.github.fonimus.ssh.shell.sample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

/**
 * Demo command for example
 */
@ShellComponent
public class DemoCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemoCommand.class);

    /**
     * Echo command
     *
     * @param message message to print
     * @return message
     */
    @ShellMethod("Echo command")
    public String echo(String message) {
        return message;
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
     * For scheduled command example
     */
    @Scheduled(initialDelay = 0, fixedDelay = 60000)
    public void log() {
        LOGGER.info("In scheduled task..");
    }
}
