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
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes =
        SshShellApplicationExclusionsTest.class, properties = {
        "ssh.shell.port=2344",
        "ssh.shell.commands.actuator.excludes[0]=info",
        "ssh.shell.commands.actuator.excludes[1]=beans",
        "ssh.shell.user=user",
        "ssh.shell.host=127.0.0.1",
        "ssh.shell.prompt.text=test>",
        "ssh.shell.prompt.color=red",
        "ssh.shell.hostKeyFile=target/test.tmp",
        "ssh.shell.enable=true",
        "management.endpoints.web.exposure.include=*",
        "spring.shell.interactive.enabled=false"
})
@SpringBootApplication
@DirtiesContext
public class SshShellApplicationExclusionsTest
        extends AbstractTest {

    @Test
    void testCommandAvailability() {
        setActuatorRole();

        assertFalse(cmd.infoAvailability().isAvailable());
        assertFalse(cmd.beansAvailability().isAvailable());
        assertTrue(cmd.configpropsAvailability().isAvailable());
    }
}
