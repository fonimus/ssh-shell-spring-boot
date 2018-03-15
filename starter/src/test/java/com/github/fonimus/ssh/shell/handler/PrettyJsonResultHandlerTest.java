package com.github.fonimus.ssh.shell.handler;

import java.io.PrintWriter;

import org.jline.terminal.Terminal;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.actuate.health.Health;

import static org.mockito.Mockito.when;

public class PrettyJsonResultHandlerTest {

	@Test
	public void doHandleResult() throws Exception {
		PrettyJsonResultHandler rh = new PrettyJsonResultHandler();
		Terminal terminal = Mockito.mock(Terminal.class);
		when(terminal.writer()).thenReturn(new PrintWriter("target/rh.tmp"));
		rh.setTerminal(terminal);
		Health object = new Health.Builder().up().build();
		rh.doHandleResult(null);
		rh.doHandleResult(new PrettyJson<>(object));
		rh.doHandleResult(new PrettyJson<>(object, false));
		rh.doHandleResult(new PrettyJson<>(new NotSerializableObject("test"), true));
	}

	public class NotSerializableObject {

		private String test;

		public NotSerializableObject(String test) {
			this.test = test;
		}
	}

}
