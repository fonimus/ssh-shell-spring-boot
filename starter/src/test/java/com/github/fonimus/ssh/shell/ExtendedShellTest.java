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

package com.github.fonimus.ssh.shell;

import com.github.fonimus.ssh.shell.postprocess.PostProcessorObject;
import com.github.fonimus.ssh.shell.postprocess.provided.GrepPostProcessor;
import com.github.fonimus.ssh.shell.postprocess.provided.SavePostProcessor;
import org.junit.jupiter.api.Test;
import org.springframework.shell.ResultHandlerService;
import org.springframework.shell.command.CommandCatalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static com.github.fonimus.ssh.shell.SshShellCommandFactory.SSH_THREAD_CONTEXT;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ExtendedShellTest {

    @Test
    void evaluate() {
        SSH_THREAD_CONTEXT.set(new SshContext(null, null, null, null));
        ResultHandlerService resultHandlerService = mock(ResultHandlerService.class);
        CommandCatalog commandRegistry = mock(CommandCatalog.class);
        when(commandRegistry.getRegistrations()).thenReturn(new HashMap<>());
        ExtendedShell shell = new ExtendedShell(resultHandlerService, commandRegistry, null, null, null, new ArrayList<>());
        shell.evaluate(() -> "one two three");
        assertEquals(Collections.emptyList(), SSH_THREAD_CONTEXT.get().getPostProcessorsList());

        shell.evaluate(() -> "one two three | grep test > /tmp/file");
        List<PostProcessorObject> postProcessors = SSH_THREAD_CONTEXT.get().getPostProcessorsList();
        assertNotNull(postProcessors);
        assertEquals(2, postProcessors.size());
        assertInList(postProcessors, new GrepPostProcessor().getName());
        assertInList(postProcessors, new SavePostProcessor().getName());
    }

    private void assertInList(List<PostProcessorObject> postProcessors, String name) {
        for (PostProcessorObject postProcessor : postProcessors) {
            if (postProcessor.getName().equals(name)) {
                return;
            }
        }
        fail("Could not find post processor with name [" + name + "] in list: " + postProcessors);
    }
}
