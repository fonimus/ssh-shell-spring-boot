package com.github.fonimus.ssh.shell;

import org.junit.jupiter.api.Test;
import org.springframework.shell.Input;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class ExtendedInputTest {

    private Input input = () -> "one, two three";

    private Input inputWithQuotes = () -> "one, \'two\' three";

    private Input inputWithChars = () -> "one, two three | grep toto > /tmp/file";

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