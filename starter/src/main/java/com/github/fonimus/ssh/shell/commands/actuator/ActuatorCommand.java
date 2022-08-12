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

package com.github.fonimus.ssh.shell.commands.actuator;

import com.github.fonimus.ssh.shell.SshShellHelper;
import com.github.fonimus.ssh.shell.SshShellProperties;
import com.github.fonimus.ssh.shell.commands.AbstractCommand;
import com.github.fonimus.ssh.shell.commands.SshShellComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.actuate.audit.AuditEventsEndpoint;
import org.springframework.boot.actuate.autoconfigure.condition.ConditionsReportEndpoint;
import org.springframework.boot.actuate.beans.BeansEndpoint;
import org.springframework.boot.actuate.context.ShutdownEndpoint;
import org.springframework.boot.actuate.context.properties.ConfigurationPropertiesReportEndpoint;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
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
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.logging.LogLevel;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.shell.Availability;
import org.springframework.shell.standard.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

/**
 * Actuator shell command
 */
@SshShellComponent
@ShellCommandGroup("Actuator Commands")
@ConditionalOnClass(Endpoint.class)
@ConditionalOnProperty(
        name = SshShellProperties.SSH_SHELL_PREFIX + ".commands." + ActuatorCommand.GROUP + ".create",
        havingValue = "true", matchIfMissing = true
)
public class ActuatorCommand extends AbstractCommand {

    public static final String GROUP = "actuator";

    private static final Logger LOGGER = LoggerFactory.getLogger(ActuatorCommand.class);

    private final ApplicationContext applicationContext;

    private final Environment environment;

    private final AuditEventsEndpoint audit;

    private final BeansEndpoint beans;

    private final ConditionsReportEndpoint conditions;

    private final ConfigurationPropertiesReportEndpoint configprops;

    private final EnvironmentEndpoint env;

    private final HealthEndpoint health;

    private final HttpTraceEndpoint httptrace;

    private final InfoEndpoint info;

    private final LoggersEndpoint loggers;

    private final MetricsEndpoint metrics;

    private final MappingsEndpoint mappings;

    private final ScheduledTasksEndpoint scheduledtasks;

    private final ShutdownEndpoint shutdown;

    private final ThreadDumpEndpoint threaddump;

    public ActuatorCommand(ApplicationContext applicationContext, Environment environment,
                           SshShellProperties properties, SshShellHelper helper,
                           @Lazy AuditEventsEndpoint audit,
                           @Lazy BeansEndpoint beans,
                           @Lazy ConditionsReportEndpoint conditions,
                           @Lazy ConfigurationPropertiesReportEndpoint configprops,
                           @Lazy EnvironmentEndpoint env,
                           @Lazy HealthEndpoint health,
                           @Lazy HttpTraceEndpoint httptrace,
                           @Lazy InfoEndpoint info,
                           @Lazy LoggersEndpoint loggers,
                           @Lazy MetricsEndpoint metrics,
                           @Lazy MappingsEndpoint mappings,
                           @Lazy ScheduledTasksEndpoint scheduledtasks,
                           @Lazy ShutdownEndpoint shutdown,
                           @Lazy ThreadDumpEndpoint threaddump) {
        super(helper, properties, properties.getCommands().getActuator());
        this.applicationContext = applicationContext;
        this.environment = environment;
        this.audit = audit;
        this.beans = beans;
        this.conditions = conditions;
        this.configprops = configprops;
        this.env = env;
        this.health = health;
        this.httptrace = httptrace;
        this.info = info;
        this.loggers = loggers;
        this.metrics = metrics;
        this.mappings = mappings;
        this.scheduledtasks = scheduledtasks;
        this.shutdown = shutdown;
        this.threaddump = threaddump;
    }

    /**
     * Audit method
     *
     * @param principal principal to filter with
     * @param type      to filter with
     * @return audit
     */
    @ShellMethod(key = "audit", value = "Display audit endpoint.")
    @ShellMethodAvailability("auditAvailability")
    public AuditEventsEndpoint.AuditEventsDescriptor audit(
            @ShellOption(defaultValue = ShellOption.NULL, help = "Principal to filter on") String principal,
            @ShellOption(defaultValue = ShellOption.NULL, help = "Type to filter on") String type
    ) {
        return audit.events(principal, null, type);
    }

    /**
     * @return whether `audit` command is available
     */
    public Availability auditAvailability() {
        return availability("audit", AuditEventsEndpoint.class);
    }

    /**
     * Beans method
     *
     * @return beans
     */
    @ShellMethod(key = "beans", value = "Display beans endpoint.")
    @ShellMethodAvailability("beansAvailability")
    public BeansEndpoint.ApplicationBeans beans() {
        return beans.beans();
    }

    /**
     * @return whether `beans` command is available
     */
    public Availability beansAvailability() {
        return availability("beans", BeansEndpoint.class);
    }

    /**
     * Conditions method
     *
     * @return conditions
     */
    @ShellMethod(key = "conditions", value = "Display conditions endpoint.")
    @ShellMethodAvailability("conditionsAvailability")
    public ConditionsReportEndpoint.ApplicationConditionEvaluation conditions() {
        return conditions.applicationConditionEvaluation();
    }

    /**
     * @return whether `conditions` command is available
     */
    public Availability conditionsAvailability() {
        return availability("conditions", ConditionsReportEndpoint.class);
    }

    /**
     * Config props method
     *
     * @return configprops
     */
    @ShellMethod(key = "configprops", value = "Display configprops endpoint.")
    @ShellMethodAvailability("configpropsAvailability")
    public ConfigurationPropertiesReportEndpoint.ApplicationConfigurationProperties configprops() {
        return configprops.configurationProperties();
    }

    /**
     * @return whether `configprops` command is available
     */
    public Availability configpropsAvailability() {
        return availability("configprops", ConfigurationPropertiesReportEndpoint.class);
    }

    /**
     * Environment method
     *
     * @param pattern pattern to filter with
     * @return env
     */
    @ShellMethod(key = "env", value = "Display env endpoint.")
    @ShellMethodAvailability("envAvailability")
    public EnvironmentEndpoint.EnvironmentDescriptor env(@ShellOption(defaultValue = ShellOption.NULL, help = "Pattern to filter on") String pattern) {
        return env.environment(pattern);
    }

    /**
     * @return whether `env` command is available
     */
    public Availability envAvailability() {
        return availability("env", EnvironmentEndpoint.class);
    }

    /**
     * Health method
     *
     * @return health
     */
    @ShellMethod(key = "health", value = "Display health endpoint.")
    @ShellMethodAvailability("healthAvailability")
    public Object health(@ShellOption(defaultValue = ShellOption.NULL,
            help = "Path to query health (component name, group name)") String path) {
        try {
            if (path != null) {
                return health.healthForPath(path);
            } else {
                return health.health();
            }
        } catch (NoSuchMethodError e) {
            // spring boot 1.9.x
            try {
                Method method = health.getClass().getMethod("health");
                return method.invoke(health);
            } catch (NoSuchMethodException ex) {
                LOGGER.debug("Unable to get method: health from HealthEndpoint class: {}",
                        health.getClass().getName(), ex);
                throw e;
            } catch (IllegalAccessException | InvocationTargetException ex) {
                LOGGER.trace("Unable to invoke method: health from HealthEndpoint class: {}",
                        health.getClass().getName(), ex);
                throw e;
            }
        }
    }

    /**
     * @return whether `health` command is available
     */
    public Availability healthAvailability() {
        return availability("health", HealthEndpoint.class);
    }

    /**
     * Http traces method
     *
     * @return httptrace
     */
    @ShellMethod(key = "httptrace", value = "Display httptrace endpoint.")
    @ShellMethodAvailability("httptraceAvailability")
    public HttpTraceEndpoint.HttpTraceDescriptor httptrace() {
        return httptrace.traces();
    }

    /**
     * @return whether `httptrace` command is available
     */
    public Availability httptraceAvailability() {
        return availability("httptrace", HttpTraceEndpoint.class);
    }

    /**
     * Info method
     *
     * @return info
     */
    @ShellMethod(key = "info", value = "Display info endpoint.")
    @ShellMethodAvailability("infoAvailability")
    public Map<String, Object> info() {
        return info.info();
    }

    /**
     * @return whether `info` command is available
     */
    public Availability infoAvailability() {
        return availability("info", InfoEndpoint.class);
    }

    /**
     * Loggers method
     *
     * @param action      action to make
     * @param loggerName  logger name for get or configure
     * @param loggerLevel logger level for configure
     * @return loggers
     */
    @ShellMethod(key = "loggers", value = "Display or configure loggers.")
    @ShellMethodAvailability("loggersAvailability")
    public Object loggers(
            @ShellOption(help = "Action to perform", defaultValue = "list", valueProvider = EnumValueProvider.class) LoggerAction action,
            @ShellOption(help = "Logger name for configuration or display", defaultValue = ShellOption.NULL) String loggerName,
            @ShellOption(help = "Logger level for configuration", defaultValue = ShellOption.NULL, valueProvider = EnumValueProvider.class) LogLevel loggerLevel) {
        if ((action == LoggerAction.get || action == LoggerAction.conf) && loggerName == null) {
            throw new IllegalArgumentException("Logger name is mandatory for '" + action + "' action");
        }
        switch (action) {
            case get:
                LoggersEndpoint.LoggerLevels levels = loggers.loggerLevels(loggerName);
                return "Logger named [" + loggerName + "] : [configured: " + levels.getConfiguredLevel() + "]";
            case conf:
                if (loggerLevel == null) {
                    throw new IllegalArgumentException("Logger level is mandatory for '" + action + "' action");
                }
                loggers.configureLogLevel(loggerName, loggerLevel);
                return "Logger named [" + loggerName + "] now configured to level [" + loggerLevel + "]";
            default:
                // list
                return loggers.loggers();
        }
    }

    /**
     * @return whether `loggers` command is available
     */
    public Availability loggersAvailability() {
        return availability("loggers", LoggersEndpoint.class);
    }

    /**
     * Metrics method
     *
     * @param name metrics name to display
     * @param tags tags to filter with
     * @return metrics
     */
    @ShellMethod(key = "metrics", value = "Display metrics endpoint.")
    @ShellMethodAvailability("metricsAvailability")
    public Object metrics(
            @ShellOption(help = "Metric name to get", defaultValue = ShellOption.NULL) String name,
            @ShellOption(help = "Tags (key=value, separated by coma)", defaultValue = ShellOption.NULL) String tags
    ) {
        if (name != null) {
            MetricsEndpoint.MetricResponse result = metrics.metric(name, tags != null ? Arrays.asList(tags.split(",")
            ) : null);
            if (result == null) {
                String tagsStr = tags != null ? " and tags: " + tags : "";
                throw new IllegalArgumentException("No result for metrics name: " + name + tagsStr);
            }
            return result;
        }
        return metrics.listNames();
    }

    /**
     * @return whether `metrics` command is available
     */
    public Availability metricsAvailability() {
        return availability("metrics", MetricsEndpoint.class);
    }

    /**
     * Mappings method
     *
     * @return mappings
     */
    @ShellMethod(key = "mappings", value = "Display mappings endpoint.")
    @ShellMethodAvailability("mappingsAvailability")
    public MappingsEndpoint.ApplicationMappings mappings() {
        return mappings.mappings();
    }

    /**
     * @return whether `mappings` command is available
     */
    public Availability mappingsAvailability() {
        return availability("mappings", MappingsEndpoint.class);
    }

    /**
     * Sessions method
     *
     * @return sessions
     */
    @ShellMethod(key = "sessions", value = "Display sessions endpoint.")
    @ShellMethodAvailability("sessionsAvailability")
    public SessionsEndpoint.SessionsReport sessions() {
        return applicationContext.getBean(SessionsEndpoint.class).sessionsForUsername(null);
    }

    /**
     * @return whether `sessions` command is available
     */
    public Availability sessionsAvailability() {
        return availability("sessions", SessionsEndpoint.class);
    }

    /**
     * Scheduled tasks method
     *
     * @return scheduledtasks
     */
    @ShellMethod(key = "scheduledtasks", value = "Display scheduledtasks endpoint.")
    @ShellMethodAvailability("scheduledtasksAvailability")
    public ScheduledTasksEndpoint.ScheduledTasksReport scheduledtasks() {
        return scheduledtasks.scheduledTasks();
    }

    /**
     * @return whether `scheduledtasks` command is available
     */
    public Availability scheduledtasksAvailability() {
        return availability("scheduledtasks", ScheduledTasksEndpoint.class);
    }

    /**
     * Shutdown method
     *
     * @return shutdown message
     */
    @ShellMethod(key = "shutdown", value = "Shutdown application.")
    @ShellMethodAvailability("shutdownAvailability")
    public String shutdown() {
        if (helper.confirm("Are you sure you want to shutdown application ? [y/N]")) {
            helper.print("Shutting down application...");
            shutdown.shutdown();
            return "";
        } else {
            return "Aborting shutdown";
        }
    }

    /**
     * @return whether `shutdown` command is available
     */
    public Availability shutdownAvailability() {
        return availability("shutdown", ShutdownEndpoint.class, false);
    }

    /**
     * Thread dump method
     *
     * @return threaddump
     */
    @ShellMethod(key = "threaddump", value = "Display threaddump endpoint.")
    @ShellMethodAvailability("threaddumpAvailability")
    public ThreadDumpEndpoint.ThreadDumpDescriptor threaddump() {
        return threaddump.threadDump();
    }

    /**
     * @return whether `threaddump` command is available
     */
    public Availability threaddumpAvailability() {
        return availability("threaddump", ThreadDumpEndpoint.class);
    }

    private Availability availability(String name, Class<?> clazz) {
        return availability(name, clazz, true);
    }

    private Availability availability(String name, Class<?> clazz, boolean defaultValue) {
        Availability av = availability(GROUP, name);
        boolean forbidden = av.getReason() != null && av.getReason().contains("forbidden");
        if (!av.isAvailable() && (!forbidden || !"info".equals(name))) {
            // not available from abstract, and not forbidden, or if forbidden not info
            return av;
        }
        String property = "management.endpoint." + name + ".enabled";
        if (!environment.getProperty(property, Boolean.TYPE, defaultValue)) {
            return Availability.unavailable("endpoint '" + name + "' deactivated (please check property '" + property
                    + "')");
        }
        try {
            applicationContext.getBean(clazz);
        } catch (NoSuchBeanDefinitionException e) {
            return Availability.unavailable(clazz.getName() + " is not in application context");
        }
        return Availability.available();
    }

    /**
     * Logger action enum
     */
    public enum LoggerAction {
        list, get, conf
    }
}
