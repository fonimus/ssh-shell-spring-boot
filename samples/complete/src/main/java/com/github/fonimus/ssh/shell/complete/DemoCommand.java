package com.github.fonimus.ssh.shell.complete;

import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.shell.Availability;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;

import com.github.fonimus.ssh.shell.SshShellHelper;
import com.github.fonimus.ssh.shell.auth.SshAuthentication;
import com.github.fonimus.ssh.shell.handler.PrettyJson;

/**
 * Demo command for example
 */
@ShellComponent
public class DemoCommand {

	private final SshShellHelper helper;

	private static final Logger LOGGER = LoggerFactory.getLogger(DemoCommand.class);

	public DemoCommand(SshShellHelper helper) {
		this.helper = helper;
	}

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
	 * Interaction example command
	 *
	 * @return welcome message
	 */
	@ShellMethod("Welcome command")
	public String welcome() {
		helper.printInfo("You are now in the welcome command");
		String name = helper.read("What's your name ?");
		return "Hello, '" + name + "' !";
	}

	/**
	 * Confirmation example command
	 *
	 * @return welcome message
	 */
	@ShellMethod("Confirmation command")
	public String conf() {
		return helper.confirm("Are you sure ?") ? "Great ! Let's do it !" : "Such a shame ...";
	}

	/**
	 * Admin only example command
	 *
	 * @return welcome message
	 */
	@ShellMethod("Admin command")
	@ShellMethodAvailability("adminAvailability")
	public String admin() {
		return "Finally an administrator !!";
	}

	public Availability adminAvailability() {
		if (!helper.checkAuthorities(Collections.singletonList("ADMIN"))) {
			return Availability.unavailable("admin command is only for an admin users !");
		}
		return Availability.available();
	}

	/**
	 * Authentication example command
	 *
	 * @return principal
	 */
	@ShellMethod("Authentication command")
	public PrettyJson<SshAuthentication> authentication() {
		return new PrettyJson<>(helper.getAuthentication());
	}

	/**
	 * For scheduled command example
	 */
	@Scheduled(initialDelay = 0, fixedDelay = 60000)
	public void log() {
		LOGGER.info("In scheduled task..");
	}
}
