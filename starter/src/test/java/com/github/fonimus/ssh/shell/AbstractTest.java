package com.github.fonimus.ssh.shell;

import com.github.fonimus.ssh.shell.commands.actuator.ActuatorCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.audit.AuditEventsEndpoint;
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
import org.springframework.boot.actuate.trace.http.HttpTraceEndpoint;
import org.springframework.boot.actuate.web.mappings.MappingsEndpoint;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;

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
    protected AuditEventsEndpoint audit;

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
    @Lazy
    protected HttpTraceEndpoint httptrace;

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
}
