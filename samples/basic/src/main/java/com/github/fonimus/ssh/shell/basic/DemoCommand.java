package com.github.fonimus.ssh.shell.basic;

import com.github.fonimus.ssh.shell.PromptColor;
import com.github.fonimus.ssh.shell.SshShellHelper;
import com.github.fonimus.ssh.shell.auth.SshAuthentication;
import com.github.fonimus.ssh.shell.commands.SshShellComponent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

/**
 * Demo command for example
 */
@SshShellComponent
public class DemoCommand {

    private final SshShellHelper helper;

    public DemoCommand(SshShellHelper helper) {
        this.helper = helper;
    }

    /**
     * Echo command
     *
     * @param message message to print
     * @param color   color for the message
     * @return message
     */
    @ShellMethod("Echo command")
    public String echo(String message, @ShellOption(defaultValue = ShellOption.NULL) PromptColor color) {
        if (color != null) {
            return new AttributedStringBuilder().append(message, AttributedStyle.DEFAULT.foreground(color.toJlineAttributedStyle())).toAnsi();
        }
        return message;
    }

    /**
     * Pojo command
     * <p>Try the post processors like pretty, grep with it</p>
     *
     * @return pojo
     */
    @ShellMethod("Pojo command")
    public Pojo pojo() {
        return new Pojo("value1", "value2");
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

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class Pojo {

        private String key1;

        private String key2;
    }
}
