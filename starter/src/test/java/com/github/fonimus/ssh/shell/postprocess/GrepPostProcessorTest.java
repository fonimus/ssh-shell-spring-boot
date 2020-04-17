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
