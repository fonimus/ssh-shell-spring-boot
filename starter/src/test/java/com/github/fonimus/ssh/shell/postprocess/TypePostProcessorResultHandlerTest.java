package com.github.fonimus.ssh.shell.postprocess;

import com.github.fonimus.ssh.shell.SshContext;
import com.github.fonimus.ssh.shell.postprocess.provided.GrepPostProcessor;
import com.github.fonimus.ssh.shell.postprocess.provided.SavePostProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.shell.ResultHandler;

import java.util.Arrays;
import java.util.Collections;

import static com.github.fonimus.ssh.shell.SshShellCommandFactory.SSH_THREAD_CONTEXT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TypePostProcessorResultHandlerTest {

    private TypePostProcessorResultHandler rh;
    private ArgumentCaptor<Object> captor;

    @BeforeEach
    void setUp() {
        ResultHandler rhMock = Mockito.mock(ResultHandler.class);
        captor = ArgumentCaptor.forClass(Object.class);
        Mockito.doNothing().when(rhMock).handleResult(captor.capture());
        rh = new TypePostProcessorResultHandler(rhMock,
                Arrays.asList(new GrepPostProcessor(), new GrepPostProcessor(), new SavePostProcessor())
        );
        SSH_THREAD_CONTEXT.set(new SshContext(null, null, null, null));
    }

    @Test
    void handleResultNull() {
        rh.handleResult(null);
        assertEquals(0, captor.getAllValues().size());
    }

    @Test
    void handleResultThrowable() {
        IllegalArgumentException ex = new IllegalArgumentException("[TEST]");
        rh.handleResult(ex);
        assertEquals(1, captor.getAllValues().size());
        assertEquals(ex, captor.getAllValues().get(0));
    }

    @Test
    void handleResultUnknownPostProcessor() {
        SSH_THREAD_CONTEXT.get().setPostProcessorsList(Collections.singletonList(
                new PostProcessorObject("unknown")
        ));

        rh.handleResult("result");
        assertEquals(2, captor.getAllValues().size());
        assertTrue(((String) captor.getAllValues().get(0)).contains("Unknown post processor"));
        assertEquals("result", captor.getAllValues().get(1));
    }

    @Test
    void handleResultWrongPostProcessorArgument() {
        SSH_THREAD_CONTEXT.get().setPostProcessorsList(Collections.singletonList(
                new PostProcessorObject("grep")
        ));

        Object obj = new PostProcessorObject("test");
        rh.handleResult(obj);
        assertEquals(2, captor.getAllValues().size());
        assertTrue(((String) captor.getAllValues().get(0)).contains("can only apply to class"));
        assertEquals(obj, captor.getAllValues().get(1));
    }

    @Test
    void handleResultPostProcessorError() {
        SSH_THREAD_CONTEXT.get().setPostProcessorsList(Collections.singletonList(
                new PostProcessorObject("save"))
        );
        rh.handleResult("result");
        assertEquals(1, captor.getAllValues().size());
        assertTrue(((String) captor.getAllValues().get(0)).contains("Cannot save"));
    }

    @Test
    void handleResultNominal() {
        SSH_THREAD_CONTEXT.get().setPostProcessorsList(Collections.singletonList(
                new PostProcessorObject("grep", Collections.singletonList("result")))
        );
        rh.handleResult("result");
        assertEquals(1, captor.getAllValues().size());
        assertEquals("result", captor.getAllValues().get(0));
    }
}
