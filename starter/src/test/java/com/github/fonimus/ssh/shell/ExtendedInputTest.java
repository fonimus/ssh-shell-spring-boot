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

import org.junit.jupiter.api.Test;
import org.springframework.shell.Input;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class ExtendedInputTest {

    private final Input input = () -> "one, two three";

    private final Input inputWithQuotes = () -> "one, 'two' three";

    private final Input inputWithChars = () -> "one, two three | grep toto > /tmp/file";

    @Test
    void input() {
        ExtendedInput i = new ExtendedInput(input);
        assertEquals(input.rawText(), i.rawText());
        assertEquals(input.words(), i.words());
        i = new ExtendedInput(inputWithQuotes);
        assertEquals(inputWithQuotes.rawText(), i.rawText());
        assertEquals(inputWithQuotes.words(), i.words());
    }

    @Test
    void inputWithChars() {
        ExtendedInput i = new ExtendedInput(inputWithChars);
        assertNotEquals(inputWithChars.rawText(), i.rawText());
        assertEquals(input.rawText() + " ", i.rawText());
        assertNotEquals(inputWithChars.words(), i.words());
        assertEquals(input.words(), i.words());
    }
}
