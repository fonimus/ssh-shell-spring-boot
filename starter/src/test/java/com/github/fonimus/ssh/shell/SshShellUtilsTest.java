package com.github.fonimus.ssh.shell;

import org.apache.sshd.common.channel.PtyMode;
import org.jline.terminal.Attributes;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;

class SshShellUtilsTest {

    @Test
    void testFillAttributes() {
        Attributes attributes = new Attributes();
        Map<PtyMode, Integer> map = new HashMap<>();
        for (PtyMode value : PtyMode.values()) {
            map.put(value, 1);
        }
        SshShellUtils.fill(attributes, map);
        assertFalse(attributes.getControlChars().isEmpty());
    }
}
