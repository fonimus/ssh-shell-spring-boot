package com.github.fonimus.ssh.shell.postprocess;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fonimus.ssh.shell.postprocess.provided.PrettyJsonPostProcessor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PrettyJsonPostProcessorTest {

    private static PrettyJsonPostProcessor processor;

    @BeforeAll
    static void init() {
        processor = new PrettyJsonPostProcessor();
    }

    @Test
    void process() throws Exception {
        String test = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(
                Health.down()
                        .withDetail("test", "value")
                        .withDetail("map", Collections.singletonMap("key", "map-value"))
                        .withDetail("list", Collections.singletonList("item"))
                        .build());

        assertEquals("\"test\"", processor.process("test", null));
        assertEquals(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(test),
                processor.process(test, null));
        assertThrows(PostProcessorException.class, () -> processor.process(new NotSerializableObject("test"), null));
    }

    public class NotSerializableObject {

        private String test;

        public NotSerializableObject(String test) {
            this.test = test;
        }
    }
}
