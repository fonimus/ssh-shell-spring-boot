package com.github.fonimus.ssh.shell;

import com.github.fonimus.ssh.shell.commands.actuator.ActuatorCommand;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.actuate.metrics.MetricsEndpoint;
import org.springframework.boot.actuate.scheduling.ScheduledTasksEndpoint;
import org.springframework.boot.logging.LogLevel;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public abstract class AbstractCommandTest extends AbstractTest {

    void commonCommandAvailability() {
        assertTrue(cmd.auditAvailability().isAvailable());
        assertTrue(cmd.beansAvailability().isAvailable());
        assertTrue(cmd.conditionsAvailability().isAvailable());
        assertTrue(cmd.configpropsAvailability().isAvailable());
        assertTrue(cmd.envAvailability().isAvailable());
        assertTrue(cmd.healthAvailability().isAvailable());
        assertTrue(cmd.infoAvailability().isAvailable());
        assertTrue(cmd.loggersAvailability().isAvailable());
        assertTrue(cmd.metricsAvailability().isAvailable());
        assertTrue(cmd.mappingsAvailability().isAvailable());
        assertTrue(cmd.scheduledtasksAvailability().isAvailable());
        assertFalse(cmd.shutdownAvailability().isAvailable());
        assertTrue(cmd.threaddumpAvailability().isAvailable());
    }

    @Test
    void testAudit() {
        assertEquals(audit.events(null, null, null).getEvents().size(),
                     cmd.audit(null, null).getObject().getEvents().size());
    }

    @Test
    protected void testBeans() {
        assertEquals(beans.beans().getContexts().size(), cmd.beans().getObject().getContexts().size());
    }

    @Test
    void testConditions() {
        assertEquals(conditions.applicationConditionEvaluation().getContexts().size(),
                     cmd.conditions().getObject().getContexts().size());
    }

    @Test
    void testConfigProps() {
        assertEquals(configprops.configurationProperties().getContexts().size(),
                     cmd.configprops().getObject().getContexts().size());
    }

    @Test
    void testEnv() {
        assertEquals(env.environment(null).getActiveProfiles().size(),
                     cmd.env(null).getObject().getActiveProfiles().size());
    }

    @Test
    void testHealth() {
        assertEquals(health.health().getStatus(), cmd.health().getObject().getStatus());
    }

    @Test
    void testHttpTrace() {
        assertEquals(httptrace.traces().getTraces().size(), cmd.httptrace().getObject().getTraces().size());
    }

    @Test
    void testInfo() {
        assertEquals(info.info(), cmd.info().getObject());
    }

    @Test
    void testListLoggers() {
        cmd.loggers(ActuatorCommand.LoggerAction.list, null, null);
    }

    @Test
    void testGetLogger() {
        assertTrue(((String) cmd.loggers(ActuatorCommand.LoggerAction.get, "test", null).getObject()).contains(
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
                     ((MetricsEndpoint.ListNamesResponse) cmd.metrics(null, null).getObject()).getNames().size());
    }

    @Test
    void testMetricsName() {
        assertEquals(metrics.metric("jvm.memory.max", null).getName(),
                     ((MetricsEndpoint.MetricResponse) cmd.metrics("jvm.memory.max", null).getObject()).getName());
    }

    @Test
    void testMetricsTags() {
        assertEquals(metrics.metric("jvm.memory.max", Collections.singletonList("area:heap")).getName(),
                     ((MetricsEndpoint.MetricResponse) cmd.metrics("jvm.memory.max",
                                                                   "area:heap").getObject()).getName());
    }

    @Test
    void testUnknownMetricsTags() {
        assertThrows(IllegalArgumentException.class, () -> cmd.metrics("test", "key:value"));
    }

    @Test
    void testMappings() {
        assertEquals(mappings.mappings().getContexts().size(), cmd.mappings().getObject().getContexts().size());
    }

    @Test
    void testSessions() {
        assertEquals(sessions.sessionsForUsername(null).getSessions().size(),
                     cmd.sessions().getObject().getSessions().size());
    }

    @Test
    void testScheduled() {
        ScheduledTasksEndpoint.ScheduledTasksReport actual = cmd.scheduledtasks().getObject();
        ScheduledTasksEndpoint.ScheduledTasksReport expected = scheduledtasks.scheduledTasks();
        assertEquals(expected.getCron().size(), actual.getCron().size());
        assertEquals(expected.getFixedDelay().size(), actual.getFixedDelay().size());
        assertEquals(expected.getFixedRate().size(), actual.getFixedRate().size());
    }

    @Test
    void testShutdown() {
        assertThrows(NoSuchBeanDefinitionException.class, () -> cmd.shutdown());
    }

    @Test
    void testThreadDump() {
        assertEquals(threaddump.threadDump().getThreads().size(), cmd.threaddump().getObject().getThreads().size());
    }
}
