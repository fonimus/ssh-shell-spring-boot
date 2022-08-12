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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fonimus.ssh.shell.auth.SshShellAuthenticationProvider;
import com.github.fonimus.ssh.shell.auth.SshShellPasswordAuthenticationProvider;
import com.github.fonimus.ssh.shell.auth.SshShellSecurityAuthenticationProvider;
import com.github.fonimus.ssh.shell.listeners.SshShellListener;
import com.github.fonimus.ssh.shell.listeners.SshShellListenerService;
import com.github.fonimus.ssh.shell.postprocess.provided.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.sshd.server.SshServer;
import org.jline.terminal.Terminal;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.shell.boot.LineReaderAutoConfiguration;
import org.springframework.shell.boot.SpringShellAutoConfiguration;
import org.springframework.shell.boot.SpringShellProperties;
import org.springframework.shell.context.InteractionMode;
import org.springframework.shell.context.ShellContext;
import org.springframework.shell.jline.PromptProvider;

import javax.annotation.PostConstruct;
import java.util.List;

import static com.github.fonimus.ssh.shell.SshShellProperties.SSH_SHELL_ENABLE;
import static com.github.fonimus.ssh.shell.SshShellProperties.SSH_SHELL_PREFIX;

/**
 * <p>Ssh shell auto configuration</p>
 * <p>Can be disabled by property <b>ssh.shell.enable=false</b></p>
 */
@Slf4j
@Configuration
@ConditionalOnClass(SshServer.class)
@ConditionalOnProperty(name = SSH_SHELL_ENABLE, havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties({SshShellProperties.class})
@AutoConfigureAfter(value = {
        SpringShellAutoConfiguration.class, LineReaderAutoConfiguration.class
}, name = {
        "org.springframework.boot.actuate.autoconfigure.audit.AuditEventsEndpointAutoConfiguration",
        "org.springframework.boot.actuate.autoconfigure.beans.BeansEndpointAutoConfiguration",
        "org.springframework.boot.actuate.autoconfigure.condition.ConditionsReportEndpointAutoConfiguration",
        "org.springframework.boot.actuate.autoconfigure.context.properties.ConfigurationPropertiesReportEndpointAutoConfiguration",
        "org.springframework.boot.actuate.autoconfigure.context.ShutdownEndpointAutoConfiguration",
        "org.springframework.boot.actuate.autoconfigure.endpoint.EndpointAutoConfiguration",
        "org.springframework.boot.actuate.autoconfigure.endpoint.jmx.JmxEndpointAutoConfiguration",
        "org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointAutoConfiguration",
        "org.springframework.boot.actuate.autoconfigure.env.EnvironmentEndpointAutoConfiguration",
        "org.springframework.boot.actuate.autoconfigure.flyway.FlywayEndpointAutoConfiguration",
        "org.springframework.boot.actuate.autoconfigure.health.HealthEndpointAutoConfiguration",
        "org.springframework.boot.actuate.autoconfigure.info.InfoEndpointAutoConfiguration",
        "org.springframework.boot.actuate.autoconfigure.jolokia.JolokiaEndpointAutoConfiguration",
        "org.springframework.boot.actuate.autoconfigure.liquibase.LiquibaseEndpointAutoConfiguration",
        "org.springframework.boot.actuate.autoconfigure.logging.LogFileWebEndpointAutoConfiguration",
        "org.springframework.boot.actuate.autoconfigure.logging.LoggersEndpointAutoConfiguration",
        "org.springframework.boot.actuate.autoconfigure.management.HeapDumpWebEndpointAutoConfiguration",
        "org.springframework.boot.actuate.autoconfigure.management.ThreadDumpEndpointAutoConfiguration",
        "org.springframework.boot.actuate.autoconfigure.metrics.MetricsEndpointAutoConfiguration",
        "org.springframework.boot.actuate.autoconfigure.scheduling.ScheduledTasksEndpointAutoConfiguration",
        "org.springframework.boot.actuate.autoconfigure.session.SessionsEndpointAutoConfiguration",
        "org.springframework.boot.actuate.autoconfigure.trace.http.HttpTraceEndpointAutoConfiguration",
        "org.springframework.boot.actuate.autoconfigure.web.mappings.MappingsEndpointAutoConfiguration"
})
@ComponentScan(basePackages = {"com.github.fonimus.ssh.shell"})
@AllArgsConstructor
public class SshShellAutoConfiguration {

    private final ApplicationContext context;
    private final SshShellProperties properties;
    private final SpringShellProperties springShellProperties;
    private final ShellContext shellContext;

    /**
     * Initialize ssh shell auto config
     */
    @PostConstruct
    public void init() {
        // override some spring shell properties
        springShellProperties.getHistory().setName(properties.getHistoryFile().getAbsolutePath());
        // set interactive mode so that ThrowableResultHandler.showShortError() returns true
        shellContext.setInteractionMode(InteractionMode.INTERACTIVE);
    }

    @Bean
    @ConditionalOnProperty(value = "spring.main.lazy-initialization", havingValue = "true")
    public ApplicationListener<ContextRefreshedEvent> lazyInitApplicationListener() {
        return event -> {
            LOGGER.info("Lazy initialization enabled, calling configuration bean explicitly to start ssh server");
            context.getBean(SshShellConfiguration.SshServerLifecycle.class);
            // also need to get terminal to initialize thread context of main thread
            context.getBean(Terminal.class);
        };
    }

    // post processors

    @Bean
    @ConditionalOnClass(name = "com.fasterxml.jackson.databind.ObjectMapper")
    public JsonPointerPostProcessor jsonPointerPostProcessor(ObjectMapper mapper) {
        return new JsonPointerPostProcessor(mapper);
    }

    @Bean
    @ConditionalOnClass(name = "com.fasterxml.jackson.databind.ObjectMapper")
    public PrettyJsonPostProcessor prettyJsonPostProcessor(ObjectMapper mapper) {
        return new PrettyJsonPostProcessor(mapper);
    }

    @Bean
    public SavePostProcessor savePostProcessor() {
        return new SavePostProcessor();
    }

    @Bean
    public GrepPostProcessor grepPostProcessor() {
        return new GrepPostProcessor();
    }

    @Bean
    public HighlightPostProcessor highlightPostProcessor() {
        return new HighlightPostProcessor();
    }

    @Bean
    public SshShellHelper sshShellHelper() {
        return new SshShellHelper(properties.getConfirmationWords());
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(name = "org.springframework.security.authentication.AuthenticationManager")
    @ConditionalOnProperty(value = SSH_SHELL_PREFIX + ".authentication", havingValue = "security")
    public SshShellAuthenticationProvider sshShellSecurityAuthenticationProvider() {
        return new SshShellSecurityAuthenticationProvider(context, properties.getAuthProviderBeanName());
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(value = SSH_SHELL_PREFIX + ".authentication", havingValue = "simple", matchIfMissing = true)
    public SshShellAuthenticationProvider sshShellSimpleAuthenticationProvider() {
        return new SshShellPasswordAuthenticationProvider(properties.getUser(), properties.getPassword());
    }

    /**
     * Primary prompt provider
     *
     * @return prompt provider
     */
    @Bean
    @ConditionalOnMissingBean
    public PromptProvider sshPromptProvider() {
        return () -> new AttributedString(properties.getPrompt().getText(),
                AttributedStyle.DEFAULT.foreground(properties.getPrompt().getColor().toJlineAttributedStyle()));
    }

    /**
     * Creates ssh listener service
     *
     * @param listeners found listeners in context
     * @return listener service
     */
    @Bean
    public SshShellListenerService sshShellListenerService(@Autowired(required = false) List<SshShellListener> listeners) {
        return new SshShellListenerService(listeners);
    }

}

