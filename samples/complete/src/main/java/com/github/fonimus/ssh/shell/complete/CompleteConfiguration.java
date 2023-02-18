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

package com.github.fonimus.ssh.shell.complete;

import com.github.fonimus.ssh.shell.commands.TasksCommand;
import com.github.fonimus.ssh.shell.listeners.SshShellListener;
import com.github.fonimus.ssh.shell.postprocess.PostProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.web.exchanges.HttpExchangeRepository;
import org.springframework.boot.actuate.web.exchanges.InMemoryHttpExchangeRepository;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.util.CollectionUtils;

import javax.sql.DataSource;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * Demo configuration
 */
@Slf4j
@Configuration
public class CompleteConfiguration implements SchedulingConfigurer {

    private final TasksCommand tasksCommand;

    public CompleteConfiguration(TasksCommand tasksCommand) {
        this.tasksCommand = tasksCommand;
    }

    @Bean
    public PostProcessor<String, String> quotePostProcessor() {
        return new PostProcessor<>() {

            @Override
            public String getName() {
                return "quote";
            }

            @Override
            public String getDescription() {
                return "Add quotes";
            }

            @Override
            public String process(String input, List<String> parameters) {
                return "'" + input + "'";
            }
        };
    }

    @Bean
    public PostProcessor<String, ZonedDateTime> datePostProcessor() {
        return new PostProcessor<>() {

            @Override
            public String getName() {
                return "date";
            }

            @Override
            public String getDescription() {
                return "Parse date";
            }

            @Override
            public ZonedDateTime process(String input, List<String> parameters) {
                return ZonedDateTime.parse(input);
            }
        };
    }

    @Bean
    public PostProcessor<ZonedDateTime, ZonedDateTime> uctPostProcessor() {
        return new PostProcessor<>() {

            @Override
            public String getName() {
                return "zoned";
            }

            @Override
            public String getDescription() {
                return "Zoned date";
            }

            @Override
            public ZonedDateTime process(ZonedDateTime input, List<String> parameters) {
                String zone = "UTC";
                if (!CollectionUtils.isEmpty(parameters)) {
                    zone = parameters.get(0);
                }
                return input.withZoneSameInstant(ZoneId.of(zone));
            }
        };
    }

    @Bean
    public SshShellListener sshShellListener() {
        return event -> LOGGER.info("[listener] event '{}' [id={}, ip={}]",
                event.getType(),
                event.getSession().getServerSession().getIoSession().getId(),
                event.getSession().getServerSession().getIoSession().getRemoteAddress());
    }

    @Bean
    @Primary
    @ConfigurationProperties(prefix = "spring.first-datasource")
    public DataSourceProperties firstDsProps() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    @ConfigurationProperties("spring.first-datasource.configuration")
    public DataSource firstDataSource() {
        return firstDsProps().initializeDataSourceBuilder().build();
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.second-datasource")
    public DataSourceProperties secondDsProps() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties("spring.second-datasource.configuration")
    public DataSource secondDataSource() {
        return secondDsProps().initializeDataSourceBuilder().build();
    }

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(5);
        threadPoolTaskScheduler.setThreadNamePrefix("my-pool-");
        return threadPoolTaskScheduler;
    }

    @Bean
    public TaskScheduler threadPoolTaskExecutor2() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(6);
        threadPoolTaskScheduler.setThreadNamePrefix("my-pool2-");
        return threadPoolTaskScheduler;
    }

    @Bean
    public HttpExchangeRepository httpTraceRepository() {
        return new InMemoryHttpExchangeRepository();
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();

        scheduler.setPoolSize(10);
        scheduler.setThreadNamePrefix("my-scheduled-task-pool-");
        scheduler.initialize();

        taskRegistrar.setTaskScheduler(scheduler);
        tasksCommand.setTaskScheduler(scheduler);

        taskRegistrar.addCronTask(() -> LOGGER.info("In 'cron' scheduled task (registrar).."), "0/60 * * * * *");
    }
}
