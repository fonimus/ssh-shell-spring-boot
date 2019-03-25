package com.github.fonimus.ssh.shell.providers;

import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.shell.CompletionContext;
import org.springframework.shell.CompletionProposal;
import org.springframework.shell.standard.ShellOption;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AnyOsFileValueProviderTest {

    @Test
    void complete() {
        AnyOsFileValueProvider provider = new AnyOsFileValueProvider(true);
        List<CompletionProposal> result = provider.complete(null,
                new CompletionContext(Arrays.asList("--file", "src"), 1, 3), null);
        assertNotEquals(0, result.size());
        result = provider.complete(null,
                new CompletionContext(Arrays.asList("--file", "xxx"), 1, 3), null);
        assertEquals(0, result.size());
    }

    @Test
    void testSupport() throws Exception {
        AnyOsFileValueProvider providerForAll = new AnyOsFileValueProvider(true);

        Method method = TestCommand.class.getDeclaredMethod("test", File.class, File.class, String.class);

        MethodParameter paramFileWithProvider = MethodParameter.forParameter(method.getParameters()[0]);
        MethodParameter paramWithoutProvider = MethodParameter.forParameter(method.getParameters()[1]);
        MethodParameter paramNotFile = MethodParameter.forParameter(method.getParameters()[2]);

        assertTrue(providerForAll.supports(paramFileWithProvider, null));
        assertTrue(providerForAll.supports(paramWithoutProvider, null));
        assertFalse(providerForAll.supports(paramNotFile, null));

        AnyOsFileValueProvider providerForDeclaredOnly = new AnyOsFileValueProvider(false);
        assertTrue(providerForDeclaredOnly.supports(paramFileWithProvider, null));
        assertFalse(providerForDeclaredOnly.supports(paramWithoutProvider, null));
        assertFalse(providerForDeclaredOnly.supports(paramNotFile, null));
    }

    private static class TestCommand {

        private void test(@ShellOption(valueProvider = AnyOsFileValueProvider.class) File file, File otherFile, String notAFile) {

        }
    }
}