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

import com.github.fonimus.ssh.shell.commands.actuator.ActuatorCommand;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;
import org.jline.terminal.Size;
import org.jline.terminal.Terminal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.actuate.health.HealthComponent;
import org.springframework.boot.actuate.metrics.MetricsEndpoint;
import org.springframework.boot.actuate.scheduling.ScheduledTasksEndpoint;
import org.springframework.boot.logging.LogLevel;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.Map;

import static com.github.fonimus.ssh.shell.SshHelperTest.*;
import static com.github.fonimus.ssh.shell.SshShellCommandFactory.SSH_THREAD_CONTEXT;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class AbstractCommandTest
        extends AbstractTest {

    protected void commonCommandAvailability() {
        assertAll(
                // since spring boot 2.2 audit,httptrace disabled by default
                // more info: https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-2
                // .2-Release-Notes#actuator-http-trace-and-auditing-are-disabled-by-default
                () -> assertFalse(cmd.auditAvailability().isAvailable()),
                () -> assertFalse(cmd.httpExchangesAvailability().isAvailable()),
                // all available except for shutdown
                () -> assertTrue(cmd.beansAvailability().isAvailable()),
                () -> assertTrue(cmd.conditionsAvailability().isAvailable()),
                () -> assertTrue(cmd.configpropsAvailability().isAvailable()),
                () -> assertTrue(cmd.envAvailability().isAvailable()),
                () -> assertTrue(cmd.healthAvailability().isAvailable()),
                () -> assertTrue(cmd.infoAvailability().isAvailable()),
                () -> assertTrue(cmd.loggersAvailability().isAvailable()),
                () -> assertTrue(cmd.metricsAvailability().isAvailable()),
                () -> assertTrue(cmd.mappingsAvailability().isAvailable()),
                () -> assertTrue(cmd.scheduledtasksAvailability().isAvailable()),
                () -> assertFalse(cmd.shutdownAvailability().isAvailable()),
                () -> assertTrue(cmd.threaddumpAvailability().isAvailable())
        );
    }

    @Test
    protected void testBeans() {
        assertEquals(beans.beans().getContexts().size(), cmd.beans().getContexts().size());
    }

    @Test
    void testConditions() {
        assertEquals(conditions.conditions().getContexts().size(),
                cmd.conditions().getContexts().size());
    }

    @Test
    void testConfigProps() {
        assertEquals(configprops.configurationProperties().getContexts().size(),
                cmd.configprops().getContexts().size());
    }

    @Test
    void testEnv() {
        assertEquals(env.environment(null).getActiveProfiles().size(),
                cmd.env(null).getActiveProfiles().size());
    }

    @Test
    void testHealth() {
        HealthComponent healthComponent = (HealthComponent) cmd.health(null);
        assertEquals(health.health().getStatus(), healthComponent.getStatus());
    }

    @Test
    void testInfo() {
        assertEquals(info.info(), cmd.info());
    }

    @Test
    void testListLoggers() {
        cmd.loggers(ActuatorCommand.LoggerAction.list, null, null);
    }

    @Test
    void testGetLogger() {
        assertTrue(((String) cmd.loggers(ActuatorCommand.LoggerAction.get, "test", null)).contains(
                "[test] : [con"));
    }

    @Test
    void testGetLoggerNameNull() {
        assertThrows(IllegalArgumentException.class, () -> cmd.loggers(ActuatorCommand.LoggerAction.get, null, null));
    }

    @Test
    void testConfigureLoggerNameNull() {
        assertThrows(IllegalArgumentException.class, () -> cmd.loggers(ActuatorCommand.LoggerAction.conf, null, null));
    }

    @Test
    void testConfigureLoggerLevelNull() {
        assertThrows(IllegalArgumentException.class,
                () -> cmd.loggers(ActuatorCommand.LoggerAction.conf, "test", null));
    }

    @Test
    void testConfigureLogger() {
        cmd.loggers(ActuatorCommand.LoggerAction.conf, "test", LogLevel.DEBUG);
    }

    @Test
    void testMetrics() {
        assertEquals(metrics.listNames().getNames().size(),
                ((MetricsEndpoint.MetricNamesDescriptor) cmd.metrics(null, null)).getNames().size());
    }

    @Test
    void testMetricsName() {
        assertEquals(metrics.metric("jvm.memory.max", null).getName(),
                ((MetricsEndpoint.MetricDescriptor) cmd.metrics("jvm.memory.max", null)).getName());
    }

    @Test
    void testMetricsTags() {
        assertEquals(metrics.metric("jvm.memory.max", Collections.singletonList("area:heap")).getName(),
                ((MetricsEndpoint.MetricDescriptor) cmd.metrics("jvm.memory.max",
                        "area:heap")).getName());
    }

    @Test
    void testUnknownMetricsTags() {
        assertThrows(IllegalArgumentException.class, () -> cmd.metrics("test", "key:value"));
    }

    @Test
    void testMappings() {
        assertEquals(mappings.mappings().getContexts().size(), cmd.mappings().getContexts().size());
    }

    @Test
    void testSessions() {
        assertEquals(sessions.sessionsForUsername(null).getSessions().size(),
                cmd.sessions().getSessions().size());
    }

    @Test
    void testScheduled() {
        ScheduledTasksEndpoint.ScheduledTasksDescriptor actual = cmd.scheduledtasks();
        ScheduledTasksEndpoint.ScheduledTasksDescriptor expected = scheduledtasks.scheduledTasks();
        assertEquals(expected.getCron().size(), actual.getCron().size());
        assertEquals(expected.getFixedDelay().size(), actual.getFixedDelay().size());
        assertEquals(expected.getFixedRate().size(), actual.getFixedRate().size());
    }

    @Test
    void testShutdownNo() throws Exception {
        setCtx("n");
        assertEquals("Aborting shutdown", cmd.shutdown());
    }

    @Test
    void testShutdownYes() throws Exception {
        setCtx("y");
        assertThrows(NoSuchBeanDefinitionException.class, () -> cmd.shutdown());
    }

    protected void setCtx(String response) throws Exception {
        LineReader lr = mock(LineReader.class);
        Terminal t = mock(Terminal.class);
        when(lr.getTerminal()).thenReturn(t);
        when(t.getSize()).thenReturn(new Size(300, 20));
        when(t.writer()).thenReturn(new PrintWriter("target/rh.tmp"));
        ParsedLine pl = mock(ParsedLine.class);
        when(pl.line()).thenReturn(response);
        when(lr.getParsedLine()).thenReturn(pl);
        SshShellRunnable runnable = mock(SshShellRunnable.class);
        SSH_THREAD_CONTEXT.set(new SshContext(runnable, t, lr, null));
    }

    @Test
    void testThreadDump() {
        assertEquals(threaddump.threadDump().getThreads().size(), cmd.threaddump().getThreads().size());
    }

    @Test
    void testSshCallInfoCommand() {
        Map<String, Object> result = info.info();
        call(properties, (is, os) -> {
            write(os, "info");
            verifyResponse(is, result.toString());
        });
    }
}
