package com.github.fonimus.ssh.shell.interactive;

import org.jline.terminal.Size;
import org.jline.utils.AttributedString;

import java.util.List;

/**
 * Interface to give to interactive command to provide lines
 */
@FunctionalInterface
public interface InteractiveInput {

    /**
     * Get lines to write
     *
     * @param size         terminal size
     * @param currentDelay current refresh delay
     * @return lines
     */
    List<AttributedString> getLines(Size size, long currentDelay);
}
