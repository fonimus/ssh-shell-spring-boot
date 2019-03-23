package com.github.fonimus.ssh.shell.commands;

import com.github.fonimus.ssh.shell.postprocess.provided.GrepPostProcessor;
import com.github.fonimus.ssh.shell.postprocess.provided.JsonPointerPostProcessor;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PostprocessorsTest {

    @Test
    void postprocessors() {
        GrepPostProcessor grep = new GrepPostProcessor();
        JsonPointerPostProcessor json = new JsonPointerPostProcessor();
        String result = new Postprocessors(Arrays.asList(grep, json)).postprocessors().toString();

        assertTrue(result.startsWith("Available Post-Processors"));
        assertTrue(result.contains(grep.getName()));
        assertTrue(result.contains(json.getName()));
    }
}