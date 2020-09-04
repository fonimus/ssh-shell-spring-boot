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

class ExtendedFileValueProviderTest {

    @Test
    void complete() {
        ExtendedFileValueProvider provider = new ExtendedFileValueProvider(true);
        List<CompletionProposal> result = provider.complete(null,
                new CompletionContext(Arrays.asList("--file", "src"), 1, 3), null);
        assertNotEquals(0, result.size());
        result = provider.complete(null,
                new CompletionContext(Arrays.asList("--file", "xxx"), 1, 3), null);
        assertEquals(0, result.size());
    }

    @Test
    void testSupport() throws Exception {
        ExtendedFileValueProvider providerForAll = new ExtendedFileValueProvider(true);

        Method method = TestCommand.class.getDeclaredMethod("test", File.class, File.class, String.class);

        MethodParameter paramFileWithProvider = MethodParameter.forParameter(method.getParameters()[0]);
        MethodParameter paramWithoutProvider = MethodParameter.forParameter(method.getParameters()[1]);
        MethodParameter paramNotFile = MethodParameter.forParameter(method.getParameters()[2]);

        assertTrue(providerForAll.supports(paramFileWithProvider, null));
        assertTrue(providerForAll.supports(paramWithoutProvider, null));
        assertFalse(providerForAll.supports(paramNotFile, null));

        ExtendedFileValueProvider providerForDeclaredOnly = new ExtendedFileValueProvider(false);
        assertTrue(providerForDeclaredOnly.supports(paramFileWithProvider, null));
        assertFalse(providerForDeclaredOnly.supports(paramWithoutProvider, null));
        assertFalse(providerForDeclaredOnly.supports(paramNotFile, null));
    }

    private static class TestCommand {

        private void test(@ShellOption(valueProvider = ExtendedFileValueProvider.class) File file, File otherFile,
                          String notAFile) {

        }
    }
}
