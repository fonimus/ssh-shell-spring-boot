package com.github.fonimus.ssh.shell.sample;

import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

@ShellComponent
public class DemoCommand {

	@ShellMethod("Test command")
	public String test(String text, String tutu) {
		return "You said: " + text + " : " + tutu;
	}

	@ShellMethod("Test command ex")
	public String testex() {
		throw new IllegalStateException("Test exception message");
	}
}
