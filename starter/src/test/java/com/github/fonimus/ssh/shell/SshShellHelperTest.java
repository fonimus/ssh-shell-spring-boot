/*
 * Copyright (c) Worldline 2018.
 */

package com.github.fonimus.ssh.shell;

import java.io.PrintWriter;
import java.security.Principal;
import java.util.Collections;
import java.util.List;

import org.jline.reader.LineReader;
import org.jline.reader.impl.completer.ArgumentCompleter;
import org.jline.terminal.Terminal;
import org.jline.utils.NonBlockingReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.github.fonimus.ssh.shell.auth.SshAuthentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SshShellHelperTest {

	private static final String MESSAGE = "The message";

	private static SshShellHelper h;

	private static LineReader lr;

	private static Terminal ter;

	private static PrintWriter writer;

	@BeforeEach
	public void each() {
		h = new SshShellHelper();
		List<String> auth = Collections.singletonList("ACTUATOR");
		lr = mock(LineReader.class);
		ter = mock(Terminal.class);
		writer = mock(PrintWriter.class);
		when(ter.writer()).thenReturn(writer);
		NonBlockingReader reader = mock(NonBlockingReader.class);
		when(ter.reader()).thenReturn(reader);
		when(lr.getTerminal()).thenReturn(ter);
		Principal p = () -> "name";
		SshContext ctx = new SshContext(null, ter, lr, new SshAuthentication(null, null, null, auth));
		SshShellCommandFactory.SSH_THREAD_CONTEXT.set(ctx);
	}

	@Test
	void confirm() {
		setAnswer("y");
		assertTrue(h.confirm(MESSAGE));
		assertFalse(h.confirm(MESSAGE, "oui"));
		setAnswer("yes");
		assertTrue(h.confirm(MESSAGE));
		assertFalse(h.confirm(MESSAGE, "oui"));
		setAnswer("no");
		assertFalse(h.confirm(MESSAGE));
		setAnswer("nope");
		assertFalse(h.confirm(MESSAGE));
		setAnswer("y");
		setAnswer("oui");
		assertTrue(h.confirm(MESSAGE, "oui"));
		assertTrue(h.confirm(MESSAGE, "OUI"));
		assertFalse(h.confirm(MESSAGE, true, "OUI"));
	}

	private void setAnswer(String answer) {
		when(lr.getParsedLine()).thenReturn(new ArgumentCompleter.ArgumentLine(answer, 0));
	}

	@Test
	void read() {
		setAnswer(MESSAGE);
		assertEquals(MESSAGE, h.read("test"));
		verify(lr, times(2)).getTerminal();
		verify(ter, times(1)).writer();
		ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
		verify(writer, times(1)).println(captor.capture());
		assertEquals(1, captor.getAllValues().size());
		assertEquals("test", captor.getValue());

		reset(writer);
		assertEquals(MESSAGE, h.read());
		verify(writer, times(0)).println(anyString());
	}

	@Test
	void getSuccess() {
		assertEquals("\u001B[32m" + MESSAGE + "\u001B[0m", h.getSuccess(MESSAGE));
	}

	@Test
	void getInfo() {
		assertEquals("\u001B[36m" + MESSAGE + "\u001B[0m", h.getInfo(MESSAGE));
	}

	@Test
	void getWarning() {
		assertEquals("\u001B[33m" + MESSAGE + "\u001B[0m", h.getWarning(MESSAGE));
	}

	@Test
	void getError() {
		assertEquals("\u001B[31m" + MESSAGE + "\u001B[0m", h.getError(MESSAGE));
	}

	@Test
	void getColored() {
		assertEquals("\u001B[30m" + MESSAGE + "\u001B[0m", h.getColored(MESSAGE, PromptColor.BLACK));
	}

	@Test
	void printSuccess() {
		h.printSuccess(MESSAGE);
		verifyMessage("\u001B[32m" + MESSAGE + "\u001B[0m");
	}

	@Test
	void printInfo() {
		h.printInfo(MESSAGE);
		verifyMessage("\u001B[36m" + MESSAGE + "\u001B[0m");
	}

	@Test
	void printWarning() {
		h.printWarning(MESSAGE);
		verifyMessage("\u001B[33m" + MESSAGE + "\u001B[0m");
	}

	@Test
	void printError() {
		h.printError(MESSAGE);
		verifyMessage("\u001B[31m" + MESSAGE + "\u001B[0m");
	}

	@Test
	void print() {
		h.print(MESSAGE);
		verifyMessage(MESSAGE);
	}

	@Test
	void printColor() {
		h.print(MESSAGE, PromptColor.BLACK);
		verifyMessage("\u001B[30m" + MESSAGE + "\u001B[0m");
	}

	@Test
	void checkAuthorities() {
		assertTrue(h.checkAuthorities(Collections.singletonList("ACTUATOR")));
		assertFalse(h.checkAuthorities(Collections.singletonList("TOTO")));
		assertTrue(h.checkAuthorities(Collections.singletonList("ACTUATOR"), Collections.singletonList("ACTUATOR"), true));
		assertTrue(h.checkAuthorities(Collections.singletonList("ACTUATOR"), Collections.singletonList("ACTUATOR"), false));
		assertTrue(h.checkAuthorities(Collections.singletonList("ACTUATOR"), null, true));
		assertFalse(h.checkAuthorities(Collections.singletonList("ACTUATOR"), null, false));
	}

	@Test
	void getAuthentication() {
		assertNotNull(h.getAuthentication());
	}

	private void verifyMessage(String message) {
		verify(lr, times(1)).getTerminal();
		verify(ter, times(1)).writer();
		ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
		verify(writer, times(1)).println(captor.capture());
		assertEquals(1, captor.getAllValues().size());
		assertEquals(message, captor.getValue());
	}
}