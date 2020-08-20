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

package com.github.fonimus.ssh.shell.commands;

import com.github.fonimus.ssh.shell.SshShellHelper;
import com.github.fonimus.ssh.shell.SshShellProperties;
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
        String result =
                new Postprocessors(new SshShellHelper(), new SshShellProperties(), Arrays.asList(grep, json)).postprocessors().toString();

        assertTrue(result.startsWith("Available Post-Processors"));
        assertTrue(result.contains(grep.getName()));
        assertTrue(result.contains(json.getName()));
    }
}
