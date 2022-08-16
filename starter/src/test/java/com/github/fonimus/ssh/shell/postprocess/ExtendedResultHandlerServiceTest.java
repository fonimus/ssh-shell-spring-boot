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

import com.github.fonimus.ssh.shell.SshContext;
import com.github.fonimus.ssh.shell.postprocess.provided.GrepPostProcessor;
import com.github.fonimus.ssh.shell.postprocess.provided.SavePostProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.shell.ResultHandlerService;

import java.util.Arrays;
import java.util.Collections;

import static com.github.fonimus.ssh.shell.SshShellCommandFactory.SSH_THREAD_CONTEXT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExtendedResultHandlerServiceTest {

    private ExtendedResultHandlerService rh;
    private ArgumentCaptor<Object> captor;

    @BeforeEach
    void setUp() {
        ResultHandlerService rhMock = Mockito.mock(ResultHandlerService.class);
        captor = ArgumentCaptor.forClass(Object.class);
        Mockito.doNothing().when(rhMock).handle(captor.capture());
        rh = new ExtendedResultHandlerService(rhMock,
                Arrays.asList(new GrepPostProcessor(), new GrepPostProcessor(), new SavePostProcessor())
        );
        SSH_THREAD_CONTEXT.set(new SshContext(null, null, null, null));
    }

    @Test
    void handleResultNull() {
        rh.handle(null);
        assertEquals(0, captor.getAllValues().size());
    }

    @Test
    void handleResultThrowable() {
        IllegalArgumentException ex = new IllegalArgumentException("[TEST]");
        rh.handle(ex);
        assertEquals(1, captor.getAllValues().size());
        assertEquals(ex, captor.getAllValues().get(0));
    }

    @Test
    void handleResultUnknownPostProcessor() {
        SSH_THREAD_CONTEXT.get().getPostProcessorsList().add(
                new PostProcessorObject("unknown"
                ));

        rh.handle("result");
        assertEquals(2, captor.getAllValues().size());
        assertTrue(((String) captor.getAllValues().get(0)).contains("Unknown post processor"));
        assertEquals("result", captor.getAllValues().get(1));
    }

    @Test
    void handleResultWrongPostProcessorArgument() {
        SSH_THREAD_CONTEXT.get().getPostProcessorsList().add(
                new PostProcessorObject("grep"
                ));

        Object obj = new PostProcessorObject("test");
        rh.handle(obj);
        assertEquals(2, captor.getAllValues().size());
        assertTrue(((String) captor.getAllValues().get(0)).contains("can only apply to class"));
        assertEquals(obj, captor.getAllValues().get(1));
    }

    @Test
    void handleResultPostProcessorError() {
        SSH_THREAD_CONTEXT.get().getPostProcessorsList().add(
                new PostProcessorObject("save")
        );
        rh.handle("result");
        assertEquals(1, captor.getAllValues().size());
        assertTrue(((String) captor.getAllValues().get(0)).contains("Cannot save"));
    }

    @Test
    void handleResultNominal() {
        SSH_THREAD_CONTEXT.get().getPostProcessorsList().add(
                new PostProcessorObject("grep", Collections.singletonList("result"))
        );
        rh.handle("result");
        assertEquals(1, captor.getAllValues().size());
        assertEquals("result", captor.getAllValues().get(0));
    }
}
