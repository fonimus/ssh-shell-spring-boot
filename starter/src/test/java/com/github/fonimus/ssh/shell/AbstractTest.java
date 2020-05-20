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

import com.github.fonimus.ssh.shell.auth.SshAuthentication;
import com.github.fonimus.ssh.shell.commands.actuator.ActuatorCommand;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.condition.ConditionsReportEndpoint;
import org.springframework.boot.actuate.beans.BeansEndpoint;
import org.springframework.boot.actuate.context.ShutdownEndpoint;
import org.springframework.boot.actuate.context.properties.ConfigurationPropertiesReportEndpoint;
import org.springframework.boot.actuate.env.EnvironmentEndpoint;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.info.InfoEndpoint;
import org.springframework.boot.actuate.logging.LoggersEndpoint;
import org.springframework.boot.actuate.management.ThreadDumpEndpoint;
import org.springframework.boot.actuate.metrics.MetricsEndpoint;
import org.springframework.boot.actuate.scheduling.ScheduledTasksEndpoint;
import org.springframework.boot.actuate.session.SessionsEndpoint;
import org.springframework.boot.actuate.web.mappings.MappingsEndpoint;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;

import java.util.Collections;

public abstract class AbstractTest {

    @Autowired
    protected ApplicationContext context;

    @Autowired
    protected Environment environment;

    @Autowired
    protected SshShellProperties properties;

    @Autowired
    protected ActuatorCommand cmd;

    @Autowired
    protected BeansEndpoint beans;

    @Autowired
    protected ConditionsReportEndpoint conditions;

    @Autowired
    protected ConfigurationPropertiesReportEndpoint configprops;

    @Autowired
    protected EnvironmentEndpoint env;

    @Autowired
    protected HealthEndpoint health;

    @Autowired
    protected InfoEndpoint info;

    @Autowired
    protected LoggersEndpoint loggers;

    @Autowired
    protected MetricsEndpoint metrics;

    @Autowired
    protected MappingsEndpoint mappings;

    @Autowired
    protected ScheduledTasksEndpoint scheduledtasks;

    @Autowired
    protected SessionsEndpoint sessions;

    @Autowired
    @Lazy
    protected ShutdownEndpoint shutdown;

    @Autowired
    protected ThreadDumpEndpoint threaddump;

    protected void setRole(String role) {
        SshShellCommandFactory.SSH_THREAD_CONTEXT.set(new SshContext(new SshShellRunnable(properties, null, null,
                null, null, null, null, null, null, null, null, null, null, null), null, null, new SshAuthentication(
                "user", "user", null, null, Collections.singletonList(role))));
    }

    protected void setActuatorRole() {
        setRole("ACTUATOR");
    }

    @AfterEach
    protected void afterEach() {
        SshShellCommandFactory.SSH_THREAD_CONTEXT.remove();
    }
}
