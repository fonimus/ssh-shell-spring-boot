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

package com.github.fonimus.ssh.shell.basic;

import com.github.fonimus.ssh.shell.PromptColor;
import com.github.fonimus.ssh.shell.SshShellHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = BasicApplication.class, properties = {
        "spring.shell.interactive.enabled=false"
})
public class DemoApplicationWebTest {

    @Autowired
    private BasicCommands demo;

    @Test
    void testApplicationStartup() {
        assertEquals("message", demo.echo("message", null));
        assertEquals(SshShellHelper.getColoredMessage("message", PromptColor.CYAN),
                demo.echo("message", PromptColor.CYAN));
        assertNotNull(demo.pojo());
    }
}
