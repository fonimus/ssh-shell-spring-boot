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

package com.github.fonimus.ssh.shell.commands;

import com.github.fonimus.ssh.shell.SimpleTable;
import com.github.fonimus.ssh.shell.SshShellHelper;
import com.github.fonimus.ssh.shell.SshShellProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.core.MethodParameter;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.scheduling.config.CronTask;
import org.springframework.scheduling.config.FixedDelayTask;
import org.springframework.scheduling.config.FixedRateTask;
import org.springframework.scheduling.config.IntervalTask;
import org.springframework.scheduling.config.ScheduledTask;
import org.springframework.scheduling.config.ScheduledTaskHolder;
import org.springframework.scheduling.config.Task;
import org.springframework.scheduling.support.ScheduledMethodRunnable;
import org.springframework.scheduling.support.SimpleTriggerContext;
import org.springframework.shell.Availability;
import org.springframework.shell.CompletionContext;
import org.springframework.shell.CompletionProposal;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.shell.standard.ShellOption;
import org.springframework.shell.standard.ValueProviderSupport;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

import java.time.Duration;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;

import static org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor.DEFAULT_TASK_SCHEDULER_BEAN_NAME;

/**
 * Command to list available post processors
 */
@SshShellComponent
@ShellCommandGroup("Tasks Commands")
@ConditionalOnBean({ScheduledTaskHolder.class})
@ConditionalOnProperty(
        name = SshShellProperties.SSH_SHELL_PREFIX + ".commands." + TasksCommand.GROUP + ".create",
        havingValue = "true", matchIfMissing = true
)
public class TasksCommand extends AbstractCommand implements DisposableBean {

    public static final String GROUP = "tasks";
    private static final String COMMAND_TASKS_LIST = GROUP + "-list";
    private static final String COMMAND_TASKS_STOP = GROUP + "-stop";
    private static final String COMMAND_TASKS_RESTART = GROUP + "-restart";
    private static final String COMMAND_TASKS_SINGLE = GROUP + "-single";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final Collection<ScheduledTaskHolder> scheduledTaskHolders;

    private final Map<String, TaskState> statesByName = new HashMap<>();

    private final ApplicationContext applicationContext;

    private TaskScheduler taskScheduler;

    public TasksCommand(SshShellHelper helper, SshShellProperties properties,
                        Collection<ScheduledTaskHolder> scheduledTaskHolders, ApplicationContext applicationContext) {
        super(helper, properties, properties.getCommands().getTasks());
        this.scheduledTaskHolders = scheduledTaskHolders;
        this.applicationContext = applicationContext;
    }

    /**
     * <p>Specify specific task scheduler for task restart, use to set same scheduler if using registrar :</p>
     * <p>org.springframework.scheduling.annotation.SchedulingConfigurer#configureTasks(org.springframework
     * .scheduling.config.ScheduledTaskRegistrar)</p>
     * <p>Otherwise the one in context is used, if more than one, looking for the one named 'taskScheduler'</p>
     *
     * @param taskScheduler task scheduler
     */
    public void setTaskScheduler(TaskScheduler taskScheduler) {
        this.taskScheduler = taskScheduler;
    }

    private TaskScheduler taskScheduler() {
        if (this.taskScheduler != null) {
            return this.taskScheduler;
        }
        Map<String, TaskScheduler> taskSchedulers = this.applicationContext.getBeansOfType(TaskScheduler.class);
        if (taskSchedulers.size() == 1) {
            this.taskScheduler = taskSchedulers.values().iterator().next();
        } else if (taskSchedulers.size() > 1) {
            this.taskScheduler = taskSchedulers.get(DEFAULT_TASK_SCHEDULER_BEAN_NAME);
        }
        if (this.taskScheduler == null) {
            this.taskScheduler = new ConcurrentTaskScheduler(Executors.newSingleThreadScheduledExecutor());
        }
        return this.taskScheduler;
    }

    public Set<String> getTaskNames() {
        return statesByName.keySet();
    }

    @Override
    public void destroy() {
        refresh(true);
        this.statesByName.values().stream().filter(s -> s.getFuture() != null).forEach(s -> s.getFuture().cancel(true));
    }

    @ShellMethod(key = COMMAND_TASKS_LIST, value = "Display the available scheduled tasks")
    @ShellMethodAvailability("tasksListAvailability")
    public String tasksList(
            @ShellOption(value = {"-f", "--filter"}, help = "Filter on status (running, stopped)",
                    defaultValue = ShellOption.NULL) TaskStatus status,
            @ShellOption(value = {"-r", "--refresh"}, help = "Refresh task from context") boolean refresh
    ) {
        refresh(refresh);

        if (this.statesByName.isEmpty()) {
            return "No task found in context";
        }

        SimpleTable.SimpleTableBuilder builder = SimpleTable.builder()
                .column("Task").column("Running").column("Type").column("Trigger").column("Next execution");
        for (TaskState state : this.statesByName.values()) {
            if (status == null || state.getStatus() == status) {
                List<Object> line = new ArrayList<>();
                line.add(state.getName());
                if (state.getScheduledTask() != null) {
                    line.add(state.getStatus());
                    Task task = state.getScheduledTask().getTask();
                    if (task instanceof CronTask) {
                        line.add("cron");
                        CronTask cronTask = ((CronTask) task);
                        line.add("expression : " + cronTask.getExpression());
                        Date next = cronTask.getTrigger().nextExecutionTime(new SimpleTriggerContext());
                        line.add(next == null ? "-" :
                                FORMATTER.format(next.toInstant().atOffset(ZoneOffset.UTC).toLocalDateTime()));
                    } else if (task instanceof FixedDelayTask) {
                        line.add("fixed-delay");
                        line.add(getTrigger((FixedDelayTask) task));
                        line.add("-");
                    } else if (task instanceof FixedRateTask) {
                        line.add("fixed-rate");
                        line.add(getTrigger((FixedRateTask) task));
                        line.add("-");
                    } else {
                        line.add("custom");
                        line.add("-");
                        line.add("-");
                    }
                } else {
                    // In case the task was started only once and from this class
                    line.add(state.getFuture() == null || state.getFuture().isDone() ? TaskStatus.stopped : TaskStatus.running);
                    line.add("single");
                    line.add("-");
                    line.add("none");
                }
                builder.line(line);
            }
        }

        return helper.renderTable(builder.build());
    }

    private void refresh(boolean refresh) {
        if (this.statesByName.isEmpty() || refresh) {
            if (refresh) {
                // Remove single executions that are done
                this.statesByName.entrySet().removeIf(
                        e -> e.getValue().getScheduledTask() == null
                        && (e.getValue().getFuture() == null || e.getValue().getFuture().isDone())
                );
            }
            for (ScheduledTaskHolder scheduledTaskHolder : this.scheduledTaskHolders) {
                for (ScheduledTask scheduledTask : scheduledTaskHolder.getScheduledTasks()) {
                    String taskName = getTaskName(scheduledTask.getTask().getRunnable());
                    this.statesByName.putIfAbsent(taskName, new TaskState(taskName, scheduledTask, TaskStatus.running,
                            null));
                }
            }
        }
    }

    @ShellMethod(key = COMMAND_TASKS_STOP, value = "Stop all or specified task(s)")
    @ShellMethodAvailability("tasksStopAvailability")
    public String tasksStop(
            @ShellOption(value = {"-a", "--all"}, help = "Stop all tasks") boolean all,
            @ShellOption(value = {"-t", "--task"}, help = "Task name to stop",
                    valueProvider = TaskNameValuesProvider.class, defaultValue = ShellOption.NULL) String task) {

        List<String> toStop = listTasks(all, task, true);
        if (toStop.isEmpty()) {
            return "No task to stop";
        }
        if (!helper.confirm("Do you really want to stop tasks " + toStop + " ?")) {
            return "Stop aborted";
        }

        List<String> stopped = new ArrayList<>();
        for (String taskName : toStop) {
            TaskState state = this.statesByName.get(taskName);
            if (state != null) {
                if (state.getStatus() == TaskStatus.running) {
                    if (state.getScheduledTask() != null) {
                        state.getScheduledTask().cancel();
                    }
                    if (state.getFuture() != null) {
                        state.getFuture().cancel(true);
                        state.setFuture(null);
                    }
                    state.setStatus(TaskStatus.stopped);
                    stopped.add(taskName);
                } else {
                    helper.printWarning("Task [" + taskName + "] already stopped.");
                }
            }
        }
        if (stopped.isEmpty()) {
            return "No task stopped";
        }
        return helper.getSuccess("Tasks " + stopped + " stopped");
    }

    @ShellMethod(key = COMMAND_TASKS_RESTART, value = "Restart all or specified task(s)")
    @ShellMethodAvailability("tasksRestartAvailability")
    public String tasksRestart(
            @ShellOption(value = {"-a", "--all"}, help = "Stop all tasks") boolean all,
            @ShellOption(value = {"-t", "--task"}, help = "Task name to stop",
                    valueProvider = TaskNameValuesProvider.class, defaultValue = ShellOption.NULL) String task) {

        List<String> toRestart = listTasks(all, task, false);
        if (toRestart.isEmpty()) {
            return "No task to restart";
        }
        if (!helper.confirm("Do you really want to restart tasks " + toRestart + " ?")) {
            return "Restart aborted";
        }

        List<String> started = new ArrayList<>();
        for (String taskName : toRestart) {
            TaskState state = this.statesByName.get(taskName);
            if (state != null && state.getScheduledTask() != null) {
                if (state.getStatus() == TaskStatus.stopped) {
                    Task taskObj = state.getScheduledTask().getTask();
                    ScheduledFuture<?> future = null;
                    if (taskObj instanceof CronTask) {
                        future = taskScheduler().schedule(state.getScheduledTask().getTask().getRunnable(),
                                ((CronTask) taskObj).getTrigger());
                    } else if (taskObj instanceof FixedDelayTask) {
                        future =
                                taskScheduler().scheduleWithFixedDelay(state.getScheduledTask().getTask().getRunnable(),
                                        ((FixedDelayTask) taskObj).getInterval());
                    } else if (taskObj instanceof FixedRateTask) {
                        future = taskScheduler().scheduleAtFixedRate(state.getScheduledTask().getTask().getRunnable(),
                                ((FixedRateTask) taskObj).getInterval());
                    } else {
                        helper.printWarning("Task [" + taskName + "] of class [" + taskObj.getClass().getName() + "] "
                                + "cannot be restarted.");
                    }
                    if (future != null) {
                        state.setFuture(future);
                        state.setStatus(TaskStatus.running);
                        started.add(taskName);
                    }
                } else {
                    helper.printWarning("Task [" + taskName + "] already running.");
                }
            } else {
                // If the user tries to launch again a task execution launched from 'tasks-single'
                helper.printWarning("Cannot relaunch this task execution [" + task + "]. Use the original task instead.");
            }
        }
        if (started.isEmpty()) {
            return "No task restarted";
        }
        return helper.getSuccess("Tasks " + started + " restarted");
    }

    @ShellMethod(key = COMMAND_TASKS_SINGLE, value = "Launch one execution of all or specified task(s)")
    @ShellMethodAvailability("tasksSingleAvailability")
    public String tasksSingle(
            @ShellOption(value = {"-a", "--all"}, help = "Launch one execution of all tasks") boolean all,
            @ShellOption(value = {"-t", "--task"}, help = "Task name to launch once",
                    valueProvider = TaskNameValuesProvider.class, defaultValue = ShellOption.NULL) String task) {

        List<String> toLaunch = listTasks(all, task, true);
        if (!helper.confirm("Do you really want to launch tasks " + toLaunch + " ?")) {
            return "Launch aborted";
        }

        List<String> started = new ArrayList<>();
        for (String taskName : toLaunch) {
            TaskState state = this.statesByName.get(taskName);
            if (state.getScheduledTask() != null) {
                try {
                    String executionId = taskName + "-" + generateExecutionId();
                    // Will run the Runnable immediately or as soon as possible
                    ScheduledFuture<?> future = taskScheduler.schedule(state.getScheduledTask().getTask().getRunnable(), new Date());
                    statesByName.put(executionId, new TaskState(executionId, null, TaskStatus.running, future));
                    started.add(executionId);
                } catch (TaskRejectedException e) {
                    helper.printError("The task '" + taskName + "' was not accepted for internal reasons");
                }
            } else if (task != null) {
                // If the user tries to launch again a task execution launched from this method
                helper.printWarning("Cannot relaunch this task execution [" + task + "]. Use the original task instead.");
            }
        }
        return started.isEmpty() ? "No task started" : helper.getSuccess("Tasks " + started + " started");
    }

    private static String generateExecutionId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    private List<String> listTasks(boolean all, String task, boolean running) {
        refresh(false);
        List<String> result = new ArrayList<>();
        if (all) {
            TaskStatus filter = running ? TaskStatus.running : TaskStatus.stopped;
            result.addAll(this.statesByName.entrySet().stream().filter(e -> e.getValue().getStatus() == filter)
                    .map(Map.Entry::getKey).collect(Collectors.toList()));
        } else {
            if (task == null || task.isEmpty()) {
                throw new IllegalArgumentException("You need to set either all option or task one");
            }
            if (!this.statesByName.containsKey(task)) {
                throw new IllegalArgumentException("Unknown task : " + task);
            }
            result.add(task);
        }
        return result;
    }

    private static String getTrigger(IntervalTask task) {
        String initialDelay = Duration.ofMillis(task.getInitialDelay()).toString();
        String interval = Duration.ofMillis(task.getInterval()).toString();
        return "interval : " + interval + " (" + task.getInterval() + "), init-delay : " + initialDelay + " (" + task.getInitialDelay() + ")";
    }

    private static String getTaskName(Runnable runnable) {
        if (runnable instanceof ScheduledMethodRunnable) {
            Method method = ((ScheduledMethodRunnable) runnable).getMethod();
            return method.getDeclaringClass().getName() + "." + method.getName();
        } else {
            return runnable.getClass().getName();
        }
    }

    private Availability tasksListAvailability() {
        return availability(GROUP, COMMAND_TASKS_LIST);
    }

    private Availability tasksStopAvailability() {
        return availability(GROUP, COMMAND_TASKS_STOP);
    }

    private Availability tasksRestartAvailability() {
        return availability(GROUP, COMMAND_TASKS_RESTART);
    }

    private Availability tasksSingleAvailability() {
        return availability(GROUP, COMMAND_TASKS_SINGLE);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaskState {

        private String name;

        private ScheduledTask scheduledTask;

        private TaskStatus status;

        private volatile ScheduledFuture<?> future;
    }

    public enum TaskStatus {
        running, stopped
    }

}

@Slf4j
@Component
@ConditionalOnBean(ScheduledTaskHolder.class)
class TaskNameValuesProvider
        extends ValueProviderSupport {

    private final TasksCommand tasksCommand;

    TaskNameValuesProvider(TasksCommand tasksCommand) {
        this.tasksCommand = tasksCommand;
    }

    @Override
    public List<CompletionProposal> complete(
            MethodParameter parameter, CompletionContext completionContext, String[] hints) {
        return tasksCommand.getTaskNames().stream().map(CompletionProposal::new).collect(Collectors.toList());
    }
}
