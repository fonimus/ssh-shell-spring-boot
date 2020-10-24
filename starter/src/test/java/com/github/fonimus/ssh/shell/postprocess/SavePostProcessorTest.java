/*
 * Copyright (c) 2020 François Onimus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
        assertTrue(processor.process(TEST, Arrays.asList("target/test.txt", "other param ignored")).startsWith(
                "Result saved to file:"));
    }
}
