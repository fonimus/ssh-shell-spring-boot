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

import com.github.fonimus.ssh.shell.PromptColor;
import com.github.fonimus.ssh.shell.SshShellHelper;
import com.github.fonimus.ssh.shell.postprocess.provided.HighlightPostProcessor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

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
                () -> assertEquals(TEST.replaceAll("test", SshShellHelper.getBackgroundColoredMessage("test",
                        PromptColor.YELLOW)), processor.process(TEST, Collections.singletonList("test"))),
                () -> assertEquals(TEST.replaceAll("toto", SshShellHelper.getBackgroundColoredMessage("toto",
                        PromptColor.YELLOW)), processor.process(TEST, Collections.singletonList("toto"))),
                () -> assertEquals(TEST.replaceAll("test", SshShellHelper.getBackgroundColoredMessage("test",
                        PromptColor.YELLOW)).replaceAll("toto", SshShellHelper.getBackgroundColoredMessage("toto",
                        PromptColor.YELLOW)), processor.process(TEST, Arrays.asList("test", "toto")))
        );
    }
}
