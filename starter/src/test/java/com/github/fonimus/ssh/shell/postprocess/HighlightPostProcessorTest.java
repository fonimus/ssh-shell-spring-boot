package com.github.fonimus.ssh.shell.postprocess;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.github.fonimus.ssh.shell.PromptColor;
import com.github.fonimus.ssh.shell.SshShellHelper;
import com.github.fonimus.ssh.shell.postprocess.provided.HighlightPostProcessor;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class HighlightPostProcessorTest {

	public static final String TEST = "test\ntoto\ntiti\ntest";

	private static HighlightPostProcessor processor;

	@BeforeAll
	static void init() {
		processor = new HighlightPostProcessor();
	}

	@Test
	void process() {
		assertAll("highlight",
				() -> assertEquals(TEST, processor.process(TEST, null)),
				() -> assertEquals(TEST, processor.process(TEST, Collections.singletonList(""))),
				() -> assertEquals(TEST.replaceAll("test", SshShellHelper.getBackgroundColored("test", PromptColor.YELLOW)),
						processor.process(TEST, Collections.singletonList("test"))),
				() -> assertEquals(TEST.replaceAll("toto", SshShellHelper.getBackgroundColored("toto", PromptColor.YELLOW)),
						processor.process(TEST, Collections.singletonList("toto"))),
				() -> assertEquals(TEST
								.replaceAll("test", SshShellHelper.getBackgroundColored("test", PromptColor.YELLOW))
								.replaceAll("toto", SshShellHelper.getBackgroundColored("toto", PromptColor.YELLOW)),
						processor.process(TEST, Arrays.asList("test", "toto")))
		);
	}
}