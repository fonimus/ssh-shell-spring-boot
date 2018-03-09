package io.fonimus.ssh.shell.commands.actuator;

import java.util.Arrays;

import io.fonimus.ssh.shell.SshShellProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.boot.actuate.trace.http.HttpTraceEndpoint;
import org.springframework.boot.actuate.web.mappings.MappingsEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.logging.LogLevel;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.shell.Availability;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.shell.standard.ShellOption;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import static io.fonimus.ssh.shell.SshShellProperties.SSH_SHELL_PREFIX;

/**
 * Actuator shell command
 */
@ShellComponent
@ShellCommandGroup("Actuator Commands")
@ConditionalOnClass(Endpoint.class)
@ConditionalOnProperty(value = SSH_SHELL_PREFIX + ".actuator.enable", havingValue = "true", matchIfMissing = true)
public class ActuatorCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActuatorCommand.class);

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    @Lazy
    private AuditEventsEndpoint audit;

    @Autowired
    @Lazy
    private BeansEndpoint beans;

    @Autowired
    @Lazy
    private ConditionsReportEndpoint conditions;

    @Autowired
    @Lazy
    private ConfigurationPropertiesReportEndpoint configprops;

    @Autowired
    @Lazy
    private EnvironmentEndpoint env;

    @Autowired
    @Lazy
    private HealthEndpoint health;

    @Autowired
    @Lazy
    private HttpTraceEndpoint httptrace;

    @Autowired
    @Lazy
    private InfoEndpoint info;

    @Autowired
    @Lazy
    private LoggersEndpoint loggers;

    @Autowired
    @Lazy
    private MetricsEndpoint metrics;

    @Autowired
    @Lazy
    private MappingsEndpoint mappings;

    @Autowired
    @Lazy
    private ScheduledTasksEndpoint scheduledtasks;

    //	@Autowired
    //	@Lazy
    //	private SessionsEndpoint sessions;

    @Autowired
    @Lazy
    private ShutdownEndpoint shutdown;

    @Autowired
    @Lazy
    private ThreadDumpEndpoint threaddump;

    @Autowired
    private Environment environment;

    @Autowired
    private SshShellProperties properties;

    /**
     * Audit method
     *
     * @return audit
     */
    @ShellMethod(key = "audit", value = "Display audit endpoint.")
    @ShellMethodAvailability("auditAvailability")
    public Object audit(
            @ShellOption(value = {"-p", "--principal"}, defaultValue = ShellOption.NULL, help = "Principal to filter on") String principal,
            @ShellOption(value = {"-t", "--type"}, defaultValue = ShellOption.NULL, help = "Type to filter on") String type
    ) {
        return prettify(audit.events(principal, null, null));
    }

    private Availability auditAvailability() {
        return availability("audit");
    }

    /**
     * Beans method
     *
     * @return beans
     */
    @ShellMethod(key = "beans", value = "Display beans endpoint.")
    @ShellMethodAvailability("beansAvailability")
    public Object beans() {
        return prettify(beans.beans());
    }

    private Availability beansAvailability() {
        return availability("beans");
    }

    /**
     * Conditions method
     *
     * @return conditions
     */
    @ShellMethod(key = "conditions", value = "Display conditions endpoint.")
    @ShellMethodAvailability("conditionsAvailability")
    public Object conditions() {
        return prettify(conditions.applicationConditionEvaluation());
    }

    private Availability conditionsAvailability() {
        return availability("conditions");
    }

    /**
     * Config props method
     *
     * @return configprops
     */
    @ShellMethod(key = "configprops", value = "Display configprops endpoint.")
    @ShellMethodAvailability("configpropsAvailability")
    public Object configprops() {
        return prettify(configprops.configurationProperties());
    }

    private Availability configpropsAvailability() {
        return availability("configprops");
    }

    /**
     * Environment method
     *
     * @return env
     */
    @ShellMethod(key = "env", value = "Display env endpoint.")
    @ShellMethodAvailability("envAvailability")
    public Object env(@ShellOption(value = {"-p", "--pattern"}, defaultValue = ShellOption.NULL, help = "Pattern to filter on") String pattern) {
        return prettify(env.environment(pattern));
    }

    private Availability envAvailability() {
        return availability("env");
    }

    /**
     * Health method
     *
     * @return health
     */
    @ShellMethod(key = "health", value = "Display health endpoint.")
    @ShellMethodAvailability("healthAvailability")
    public Object health() {
        return prettify(health.health());
    }

    private Availability healthAvailability() {
        return availability("health");
    }

    /**
     * Http traces method
     *
     * @return httptrace
     */
    @ShellMethod(key = "httptrace", value = "Display httptrace endpoint.")
    @ShellMethodAvailability("httptraceAvailability")
    public Object httptrace() {
        return prettify(httptrace.traces());
    }

    private Availability httptraceAvailability() {
        return availability("httptrace");
    }

    /**
     * Info method
     *
     * @return info
     */
    @ShellMethod(key = "info", value = "Display info endpoint.")
    @ShellMethodAvailability("infoAvailability")
    public Object info() {
        return prettify(info.info());
    }

    private Availability infoAvailability() {
        return availability("info");
    }

    /**
     * Loggers method
     *
     * @return loggers
     */
    @ShellMethod(key = "loggers", value = "Display or configure loggers.")
    @ShellMethodAvailability("loggersAvailability")
    public Object loggers(
            @ShellOption(value = {"-a", "--action"}, help = "Action to perform", defaultValue = "list") LoggerAction action,
            @ShellOption(value = {"-n", "--name"}, help = "Logger name for configuration or display", defaultValue = ShellOption.NULL) String loggerName,
            @ShellOption(value = {"-l", "--level"}, help = "Logger level for configuration", defaultValue = ShellOption.NULL) LogLevel loggerLevel
    ) {
        if ((action == LoggerAction.get || action == LoggerAction.conf) && loggerName == null) {
            throw new IllegalArgumentException("Logger name is mandatory for '" + action + "' action");
        }
        switch (action) {
            case get:
                LoggersEndpoint.LoggerLevels levels = loggers.loggerLevels(loggerName);
                return "Logger named [" + loggerName + "] : [configured: " + levels.getConfiguredLevel() + ", effective: " + levels.getEffectiveLevel() + "]";
            case conf:
                if (loggerLevel == null) {
                    throw new IllegalArgumentException("Logger level is mandatory for '" + action + "' action");
                }
                loggers.configureLogLevel(loggerName, loggerLevel);
                return "Logger named [" + loggerName + "] now configured to level [" + loggerLevel + "]";
            default:
                return prettify(loggers.loggers());
        }
    }

    private Availability loggersAvailability() {
        return availability("loggers");
    }

    /**
     * Metrics method
     *
     * @return metrics
     */
    @ShellMethod(key = "metrics", value = "Display metrics endpoint.")
    @ShellMethodAvailability("metricsAvailability")
    public Object metrics(
            @ShellOption(value = {"-n", "--name"}, help = "Metric name to get", defaultValue = ShellOption.NULL) String name,
            @ShellOption(value = {"-t", "--tags"}, help = "Tags (key=value, separated by coma)", defaultValue = ShellOption.NULL) String tags
    ) {
        if (name != null) {
            MetricsEndpoint.MetricResponse result = metrics.metric(name, tags != null ? Arrays.asList(tags.split(",")) : null);
            if (result == null) {
                String tagsStr = tags != null ? " and tags: " + tags : "";
                throw new IllegalArgumentException("No result for metrics name: " + name + tagsStr);
            }
            return prettify(result);
        }
        return prettify(metrics.listNames());
    }

    private Availability metricsAvailability() {
        return availability("metrics");
    }

    /**
     * Mappings method
     *
     * @return mappings
     */
    @ShellMethod(key = "mappings", value = "Display mappings endpoint.")
    @ShellMethodAvailability("mappingsAvailability")
    public Object mappings() {
        return prettify(mappings.mappings());
    }

    private Availability mappingsAvailability() {
        return availability("mappings");
    }

    /**
     * Scheduled tasks method
     *
     * @return scheduledtasks
     */
    @ShellMethod(key = "scheduledtasks", value = "Display scheduledtasks endpoint.")
    @ShellMethodAvailability("scheduledtasksAvailability")
    public Object scheduledtasks() {
        return prettify(scheduledtasks.scheduledTasks());
    }

    private Availability scheduledtasksAvailability() {
        return availability("scheduledtasks");
    }

    //	/**
    //	 * Sessions method
    //	 *
    //	 * @return sessions
    //	 */
    //	@ShellMethod(key = "sessions", value = "Display sessions endpoint.")
    //	@ShellMethodAvailability("sessionsAvailability")
    //	public Object sessions() {
    //		return prettify(sessions.sessionsForUsername(null));
    //	}
    //
    //	private Availability sessionsAvailability() {
    //		return availability("sessions");
    //	}

    /**
     * Shutdown method
     */
    @ShellMethod(key = "shutdown", value = "Shutdown application.")
    @ShellMethodAvailability("shutdownAvailability")
    public void shutdown() {
        shutdown.shutdown();
    }

    private Availability shutdownAvailability() {
        return availability("shutdown", false);
    }

    /**
     * Thread dump method
     *
     * @return threaddump
     */
    @ShellMethod(key = "threaddump", value = "Display threaddump endpoint.")
    @ShellMethodAvailability("threaddumpAvailability")
    public Object threaddump() {
        return prettify(threaddump.threadDump());
    }

    private Availability threaddumpAvailability() {
        return availability("threaddump");
    }

    private Availability availability(String name) {
        return availability(name, true);
    }

    private Availability availability(String name, boolean defaultValue) {
        String property = "management.endpoint." + name + ".enabled";
        if (!environment.getProperty(property, Boolean.TYPE, defaultValue)) {
            return Availability.unavailable("endpoint '" + name + "' deactivated (please check property '" + property + "')");
        } else if (properties.getActuator().getExcludes().contains(name)) {
            return Availability.unavailable("command is present in exclusion (please check property '" +
                    SshShellProperties.SSH_SHELL_PREFIX + ".actuator.excludes')");
        }
        return Availability.available();
    }

    private Object prettify(Object object) {
        try {
            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            LOGGER.warn("Unable to prettify object: {}", object);
            return object;
        }
    }

    public enum LoggerAction {
        list, get, conf
    }
}
