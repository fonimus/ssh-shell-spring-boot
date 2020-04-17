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

import com.github.fonimus.ssh.shell.conf.SshShellPasswordConfigurationTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static com.github.fonimus.ssh.shell.SshHelperTest.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
        classes = {SshShellApplicationCustomAuthenticatorTest.class, SshShellPasswordConfigurationTest.class},
        properties = {"ssh.shell.port=2349", "management.endpoints.web.exposure.include=*"}
)
@ExtendWith(SpringExtension.class)
@SpringBootApplication
@DirtiesContext
public class SshShellApplicationCustomAuthenticatorTest
        extends AbstractTest {

    @Test
    void testSshCallInfoCommand() {
        call("user", "user", properties, (is, os) -> {
            write(os, "info");
            verifyResponse(is, "{}");
        });
    }

    @Test
    void testSshCallInfoCommandOtherUser() {
        call("myself", "myself", properties, (is, os) -> {
            write(os, "info");
            verifyResponse(is, "{}");
        });
    }

}
