package com.github.fonimus.ssh.shell.basic;

import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import com.github.fonimus.ssh.shell.PromptColor;

/**
 * Demo command for example
 */
@ShellComponent
public class DemoCommand {

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
}
