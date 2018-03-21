package com.github.fonimus.ssh.shell;

import java.util.Arrays;
import java.util.List;

import org.jline.reader.LineReader;
import org.jline.terminal.impl.AbstractPosixTerminal;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

/**
 * Ssh shell helper for user interactions and authorities check
 */
public class SshShellHelper {

	public static final List<String> DEFAULT_CONFIRM_WORDS = Arrays.asList("y", "yes");

	private final List<String> confirmWords;

	public SshShellHelper() {
		this(null);
	}

	public SshShellHelper(List<String> confirmWords) {
		this.confirmWords = confirmWords != null ? confirmWords : DEFAULT_CONFIRM_WORDS;
	}

	/**
	 * @param message      confirmation message
	 * @param confirmWords (optional) confirmation words, default are {@link SshShellHelper#DEFAULT_CONFIRM_WORDS}, or configured in {@link SshShellProperties}
	 * @return whether it has been confirmed
	 */
	public boolean confirm(String message, String... confirmWords) {
		return confirm(message, false, confirmWords);
	}

	/**
	 * @param message       confirmation message
	 * @param caseSensitive should be case sensitive or not
	 * @param confirmWords  (optional) confirmation words, default are {@link SshShellHelper#DEFAULT_CONFIRM_WORDS}, or configured in {@link SshShellProperties}
	 * @return whether it has been confirmed
	 */
	public boolean confirm(String message, boolean caseSensitive, String... confirmWords) {
		String response = read(message);
		List<String> confirm = this.confirmWords;
		if (confirmWords != null && confirmWords.length > 0) {
			confirm = Arrays.asList(confirmWords);
		}
		for (String c : confirm) {
			if (caseSensitive && c.equals(response)) {
				return true;
			} else if (!caseSensitive && c.equalsIgnoreCase(response)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Print confirmation message and get response
	 *
	 * @param message message to print
	 * @return response
	 */
	public String read(String message) {
		LineReader lr = SshShellCommandFactory.SSH_THREAD_CONTEXT.get().getLineReader();
		lr.getTerminal().writer().println(message);
		lr.readLine();
		if (lr.getTerminal() instanceof AbstractPosixTerminal) {
			lr.getTerminal().writer().println();
		}
		return lr.getParsedLine().line();
	}

	/**
	 * Color message with color {@link PromptColor#GREEN}
	 *
	 * @param message message to return
	 * @return colored message
	 */
	public String getSuccess(String message) {
		return getColored(message, PromptColor.GREEN);
	}

	/**
	 * Color message with color {@link PromptColor#CYAN}
	 *
	 * @param message message to return
	 * @return colored message
	 */
	public String getInfo(String message) {
		return getColored(message, PromptColor.CYAN);
	}

	/**
	 * Color message with color {@link PromptColor#YELLOW}
	 *
	 * @param message message to return
	 * @return colored message
	 */
	public String getWarning(String message) {
		return getColored(message, PromptColor.YELLOW);
	}

	/**
	 * Color message with color {@link PromptColor#RED}
	 *
	 * @param message message to return
	 * @return colored message
	 */
	public String getError(String message) {
		return getColored(message, PromptColor.RED);
	}

	/**
	 * Color message with given color
	 *
	 * @param message message to return
	 * @param color   color to print
	 * @return colored message
	 */
	public String getColored(String message, PromptColor color) {
		return new AttributedStringBuilder().append(message, AttributedStyle.DEFAULT.foreground(color.toJlineAttributedStyle())).toAnsi();
	}

	/**
	 * Print message with color {@link PromptColor#GREEN}
	 *
	 * @param message message to print
	 */
	public void printSuccess(String message) {
		print(message, PromptColor.GREEN);
	}

	/**
	 * Print message with color {@link PromptColor#CYAN}
	 *
	 * @param message message to print
	 */
	public void printInfo(String message) {
		print(message, PromptColor.CYAN);
	}

	/**
	 * Print message with color {@link PromptColor#YELLOW}
	 *
	 * @param message message to print
	 */
	public void printWarning(String message) {
		print(message, PromptColor.YELLOW);
	}

	/**
	 * Print message with color {@link PromptColor#RED}
	 *
	 * @param message message to print
	 */
	public void printError(String message) {
		print(message, PromptColor.RED);
	}

	/**
	 * Print in the console
	 *
	 * @param message message to print
	 */
	public void print(String message) {
		print(message, null);
	}

	/**
	 * Print in the console
	 *
	 * @param message message to print
	 * @param color   (optional) prompt color
	 */
	public void print(String message, PromptColor color) {
		String toPrint = message;
		if (color != null) {
			toPrint = getColored(message, color);
		}
		SshShellCommandFactory.SSH_THREAD_CONTEXT.get().getLineReader().getTerminal().writer().println(toPrint);
	}

	/**
	 * Check that one of the roles is in current authorities
	 *
	 * @param authorizedRoles authorized roles
	 * @return true if role found in authorities
	 */
	public boolean checkAuthorities(List<String> authorizedRoles) {
		return checkAuthorities(authorizedRoles, SshShellCommandFactory.SSH_THREAD_CONTEXT.get().getAuthorities(), false);
	}

	/**
	 * Check that one of the roles is in authorities
	 *
	 * @param authorizedRoles           authorized roles
	 * @param authorities               current authorities
	 * @param authorizedIfNoAuthorities whether to return true if no authorities
	 * @return true if role found in authorities
	 */
	public boolean checkAuthorities(List<String> authorizedRoles, List<String> authorities, boolean authorizedIfNoAuthorities) {
		if (authorities == null) {
			// if authorized only -> return false
			return authorizedIfNoAuthorities;
		}
		for (String authority : authorities) {
			String check = authority;
			if (check.startsWith("ROLE_")) {
				check = check.substring(5);
			}
			if (authorizedRoles.contains(check)) {
				return true;
			}
		}

		return false;
	}

}
