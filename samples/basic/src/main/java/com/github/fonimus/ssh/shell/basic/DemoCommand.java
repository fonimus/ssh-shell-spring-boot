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

	@ShellMethod("Pojo command")
	public Pojo pojo() {
		return new Pojo("value1", "value2");
	}

	public static class Pojo {

		private String key1;

		private String key2;

		public Pojo() {
		}

		public Pojo(String key1, String key2) {
			this.key1 = key1;
			this.key2 = key2;
		}

		public String getKey1() {
			return key1;
		}

		public void setKey1(String key1) {
			this.key1 = key1;
		}

		public String getKey2() {
			return key2;
		}

		public void setKey2(String key2) {
			this.key2 = key2;
		}

		@Override
		public String toString() {
			return "Pojo{" +
					"key1='" + key1 + '\'' +
					", key2='" + key2 + '\'' +
					'}';
		}
	}
}
