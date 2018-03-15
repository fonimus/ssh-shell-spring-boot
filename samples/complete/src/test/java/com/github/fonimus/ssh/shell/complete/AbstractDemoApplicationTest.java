package com.github.fonimus.ssh.shell.complete;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.info.InfoEndpoint;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class AbstractDemoApplicationTest {

	@Autowired
	private InfoEndpoint info;

	@Test
	void testApplicationStartup() {
		Map<String, Object> i = info.info();
		assertFalse(i.isEmpty());
		assertTrue(i.containsKey("build"));
		assertTrue(i.containsKey("dependencies"));
	}
}
