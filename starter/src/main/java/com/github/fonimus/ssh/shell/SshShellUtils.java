package com.github.fonimus.ssh.shell;

import java.util.List;

import org.jline.reader.LineReader;

/**
 * Utils
 */
public class SshShellUtils {

	public static final String[] DEFAULT_CONFIRM_WORDS = { "y", "yes" };

	/**
	 * @param message      confirmation message
	 * @param confirmWords (optional) confirmation words, default are {@link SshShellUtils#DEFAULT_CONFIRM_WORDS}
	 * @return whether it has been confirmed
	 */
	public static boolean confirm(String message, String... confirmWords) {
		return confirm(message, false, confirmWords);
	}

	/**
	 * @param message       confirmation message
	 * @param caseSensitive should be case sensitive or not
	 * @param confirmWords  (optional) confirmation words, default are {@link SshShellUtils#DEFAULT_CONFIRM_WORDS}
	 * @return whether it has been confirmed
	 */
	public static boolean confirm(String message, boolean caseSensitive, String... confirmWords) {
		String response = read(message);
		String[] confirm = DEFAULT_CONFIRM_WORDS;
		if (confirmWords != null && confirmWords.length > 0) {
			confirm = confirmWords;
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
	public static String read(String message) {
		LineReader lr = SshShellCommandFactory.SSH_THREAD_CONTEXT.get().getLineReader();
		lr.getTerminal().writer().println(message);
		lr.readLine();
		return lr.getParsedLine().line();
	}

	/**
	 * Check that one of the roles is in current authorities
	 *
	 * @param authorizedRoles authorized roles
	 * @return true if role found in authorities
	 */
	public static boolean checkAuthorities(List<String> authorizedRoles) {
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
	public static boolean checkAuthorities(List<String> authorizedRoles, List<String> authorities, boolean authorizedIfNoAuthorities) {
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
