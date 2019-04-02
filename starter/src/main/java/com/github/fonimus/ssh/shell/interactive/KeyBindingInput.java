package com.github.fonimus.ssh.shell.interactive;

/**
 * Key binding input interface
 */
@FunctionalInterface
public interface KeyBindingInput {

    /**
     * Perform action
     */
    void action();
}
