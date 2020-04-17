package com.github.fonimus.ssh.shell.postprocess;

import com.github.fonimus.ssh.shell.postprocess.provided.SavePostProcessor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SavePostProcessorTest {

    public static final String TEST = "to-write";

    private static SavePostProcessor processor;

    @BeforeAll
    static void init() {
        processor = new SavePostProcessor();
    }

    @Test
    void process() throws Exception {
        File file = new File("target/test.txt");
        if (file.exists()) {
            assertTrue(Files.deleteIfExists(file.toPath()));
        }
        assertTrue(
                assertThrows(PostProcessorException.class, () -> processor.process(TEST, null)).getMessage().startsWith("Cannot save without file path !"));
        assertTrue(assertThrows(PostProcessorException.class, () -> processor.process(TEST,
                Collections.singletonList(""))).getMessage()
                .startsWith("Cannot save without file path !"));
        assertTrue(assertThrows(PostProcessorException.class, () -> processor.process(TEST,
                Collections.singletonList(null))).getMessage()
                .startsWith("Cannot save without file path !"));
        assertTrue(
                assertThrows(PostProcessorException.class, () -> processor.process(TEST, Collections.singletonList(
                        "target/not-existing-dir/file.txt")))
                        .getMessage().startsWith("Unable to write to file:"));
        assertTrue(processor.process(TEST, Collections.singletonList("target/test.txt")).startsWith("Result saved to " +
                "file:"));
        assertTrue(assertThrows(PostProcessorException.class, () -> processor.process(TEST,
                Collections.singletonList("target/test.txt"))).getMessage()
                .startsWith("File already exists:"));
        assertTrue(assertThrows(PostProcessorException.class, () -> processor.process(TEST, Arrays.asList("target" +
                "/test.txt", "dont-care"))).getMessage()
                .startsWith("File already exists:"));
    }
}
