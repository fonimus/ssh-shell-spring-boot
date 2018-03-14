package com.github.fonimus.ssh.shell.basic;

import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

/**
 * Demo command for example
 */
@ShellComponent
public class DemoCommand {

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
}
