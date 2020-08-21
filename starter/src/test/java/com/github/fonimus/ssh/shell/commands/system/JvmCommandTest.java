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

package com.github.fonimus.ssh.shell.commands.system;

import com.github.fonimus.ssh.shell.SshShellHelper;
import com.github.fonimus.ssh.shell.SshShellProperties;
import org.jline.terminal.Size;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JvmCommandTest {

    private JvmCommand cmd;

    @BeforeEach
    void setUp() {
        SshShellHelper helper = mock(SshShellHelper.class);
        when(helper.terminalSize()).thenReturn(new Size(100, 100));
        cmd = new JvmCommand(helper, new SshShellProperties());
    }

    @Test
    void jvmEnv() {
        cmd.jvmEnv(false);
        cmd.jvmEnv(true);
    }

    @Test
    void jvmProperties() {
        cmd.jvmProperties(false);
        cmd.jvmProperties(true);
    }
}