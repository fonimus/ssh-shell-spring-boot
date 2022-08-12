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
import org.springframework.shell.CompletionContext;
import org.springframework.shell.CompletionProposal;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class ExtendedFileValueProviderTest {

    @Test
    void complete() {
        ExtendedFileValueProvider provider = new ExtendedFileValueProvider();
        List<CompletionProposal> result = provider.complete(
                new CompletionContext(Arrays.asList("--file", "src"), 1, 3, null, null));
        assertNotEquals(0, result.size());
        result = provider.complete(
                new CompletionContext(Arrays.asList("--file", "xxx"), 1, 3, null, null));
        assertEquals(0, result.size());
    }

}
