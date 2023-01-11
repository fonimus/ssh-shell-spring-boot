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

import com.github.fonimus.ssh.shell.commands.DatasourceCommand;
import com.github.fonimus.ssh.shell.commands.JmxCommand;
import com.github.fonimus.ssh.shell.commands.TasksCommand;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = SshShellApplicationTest.class,
        properties = {
                "ssh.shell.port=2345",
                "ssh.shell.password=pass",
                "management.endpoints.web.exposure.include=*",
                "spring.session.store-type=jdbc",
                "spring.shell.interactive.enabled=false",
                "spring.jmx.enabled=true"
        })
@SpringBootApplication
@DirtiesContext
public class SshShellApplicationTest
        extends AbstractCommandTest {

    @Autowired(required = false)
    protected JmxCommand jmx;

    @Autowired(required = false)
    protected DatasourceCommand ds;

    @Autowired(required = false)
    protected TasksCommand tasks;

    @Test
    void testCommandAvailability() {
        setActuatorRole();

        super.commonCommandAvailability();

        assertFalse(cmd.httpExchangesAvailability().isAvailable());
    }

    @Test
    void testDatasourceCommand() {
        assertNotNull(ds);
        assertNotNull(ds.datasourceList());
        assertNotNull(ds.datasourceQuery(0, "select 1"));
        assertThrows(IllegalStateException.class, () -> ds.datasourceProperties(0, "test"));
        assertThrows(IllegalStateException.class, () -> ds.datasourceUpdate(0, "unknown"));
    }

    @Test
    void testJmxCommand() {
        assertNotNull(jmx);
        jmx.jmxList(null);
        jmx.jmxList("org.springframework.boot:type=Endpoint,name=Shutdown");
        jmx.jmxList("unknown");
    }

    @Test
    void testTasksCommand() {
        assertNotNull(tasks);
        assertNotNull(tasks.tasksList(null, true));
        assertThrows(IllegalArgumentException.class, () -> tasks.tasksStop(false, "unknown"));
        assertThrows(IllegalArgumentException.class, () -> tasks.tasksRestart(false, "unknown"));
        assertThrows(IllegalArgumentException.class, () -> tasks.tasksSingle(false, "unknown"));
    }
}
