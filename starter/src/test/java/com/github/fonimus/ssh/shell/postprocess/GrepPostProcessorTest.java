package com.github.fonimus.ssh.shell.postprocess;

import com.github.fonimus.ssh.shell.postprocess.provided.GrepPostProcessor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class GrepPostProcessorTest {

    public static final String TEST = "test\ntoto\ntiti\ntest";

    private static GrepPostProcessor processor;

    @BeforeAll
    static void init() {
        processor = new GrepPostProcessor();
    }

    @Test
    void process() {
        assertAll("grep",
                () -> assertEquals(TEST, processor.process(TEST, null)),
                () -> assertEquals(TEST, processor.process(TEST, Collections.singletonList(""))),
                () -> assertEquals("test\ntest", processor.process(TEST, Collections.singletonList("test"))),
                () -> assertEquals("test\ntoto\ntest", processor.process(TEST, Arrays.asList("test", "toto"))),
                () -> assertEquals("toto", processor.process(TEST, Collections.singletonList("toto")))
        );
    }
}
