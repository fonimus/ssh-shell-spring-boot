package com.github.fonimus.ssh.shell;

import org.jline.reader.LineReader;

/**
 * Utils
 */
public class SshShellUtils {

    public static final String[] DEFAULT_CONFIRM_WORDS = {"y", "yes"};

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
}
