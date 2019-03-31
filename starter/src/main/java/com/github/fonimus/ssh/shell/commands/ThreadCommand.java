package com.github.fonimus.ssh.shell.commands;

import com.github.fonimus.ssh.shell.PromptColor;
import com.github.fonimus.ssh.shell.SshShellHelper;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.shell.table.*;

import java.util.*;

import static com.github.fonimus.ssh.shell.SshShellHelper.at;

/**
 * Thread command
 */
@SshShellComponent
@ShellCommandGroup("Built-In Commands")
public class ThreadCommand {

    private SshShellHelper helper;

    public ThreadCommand(SshShellHelper helper) {
        this.helper = helper;
    }

    private static Map<Long, Thread> getThreads() {
        ThreadGroup root = getRoot();
        Thread[] threads = new Thread[root.activeCount()];
        while (root.enumerate(threads, true) == threads.length) {
            threads = new Thread[threads.length * 2];
        }
        Map<Long, Thread> map = new HashMap<>();
        for (Thread thread : threads) {
            if (thread != null) {
                map.put(thread.getId(), thread);
            }
        }
        return map;
    }

    private static ThreadGroup getRoot() {
        ThreadGroup group = Thread.currentThread().getThreadGroup();
        ThreadGroup parent;
        while ((parent = group.getParent()) != null) {
            group = parent;
        }
        return group;
    }

    @ShellMethod("Thread command.")
    @SuppressWarnings("deprecation")
    public String threads(@ShellOption(defaultValue = "LIST") ThreadAction action,
                          @ShellOption(defaultValue = "ID") ThreadColumn orderBy,
                          @ShellOption(defaultValue = ShellOption.NULL) Long threadId,
                          boolean reverseOrder) {

        switch (action) {

            case LIST:
                Map<Long, Thread> threads = getThreads();
                String[][] data = new String[threads.size() + 1][ThreadColumn.values().length];
                TableModel model = new ArrayTableModel(data);
                TableBuilder tableBuilder = new TableBuilder(model);

                int i = 0;
                for (ThreadColumn column : ThreadColumn.values()) {
                    data[0][i] = column.name();
                    tableBuilder.on(at(0, i)).addAligner(SimpleHorizontalAligner.center);
                    i++;
                }
                int r = 1;
                List<Thread> ordered = new ArrayList<>(threads.values());
                ordered.sort(comparator(orderBy, reverseOrder));
                for (Thread t : ordered) {
                    data[r][0] = String.valueOf(t.getId());
                    data[r][1] = String.valueOf(t.getPriority());
                    data[r][2] = helper.getColored(t.getState().name(), color(t.getState()));
                    // because align implementations remove colors ! (trim())
                    tableBuilder.on(at(r, 2)).addAligner(new ColorFixAligner());
                    data[r][3] = String.valueOf(t.isInterrupted());
                    data[r][4] = String.valueOf(t.isDaemon());
                    data[r][5] = t.getName();
                    r++;
                }
                return tableBuilder.addHeaderAndVerticalsBorders(BorderStyle.fancy_double).build().render(helper.terminalSize().getRows());
            case DUMP:
                Thread t = get(threadId);
                Exception e = new Exception("Thread [" + t.getId() + "] stack trace");
                e.setStackTrace(t.getStackTrace());
                e.printStackTrace(helper.terminalWriter());
                return "";
            case INTERRUPT:
                get(threadId).stop();
                return helper.getSuccess("Thread " + threadId + " interrupted");
            default:
                throw new IllegalArgumentException("Unknown action: " + action);
        }
    }

    private Thread get(Long threadId) {
        if (threadId == null) {
            throw new IllegalArgumentException("Thread id is mandatory");
        }
        Thread t = getThreads().get(threadId);
        if (t == null) {
            throw new IllegalArgumentException("Could not find thread for id: " + threadId);
        }
        return t;
    }

    private Comparator<? super Thread> comparator(ThreadColumn orderBy, boolean reverseOrder) {
        Comparator<? super Thread> c;
        switch (orderBy) {

            case PRIORITY:
                c = Comparator.comparingDouble(Thread::getPriority);
                break;
            case STATE:
                c = Comparator.comparing(e -> e.getState().name());
                break;
            case INTERRUPTED:
                c = Comparator.comparing(Thread::isAlive);
                break;
            case DAEMON:
                c = Comparator.comparing(Thread::isDaemon);
                break;
            case NAME:
                c = Comparator.comparing(Thread::getName);
                break;
            default:
                c = Comparator.comparingDouble(Thread::getId);
                break;
        }
        if (reverseOrder) {
            c = c.reversed();
        }
        return c;
    }

    private PromptColor color(Thread.State state) {
        switch (state) {
            case RUNNABLE:
                return PromptColor.GREEN;
            case BLOCKED:
            case TERMINATED:
                return PromptColor.RED;
            case WAITING:
            case TIMED_WAITING:
                return PromptColor.CYAN;
            case NEW:
            default:
                return PromptColor.WHITE;

        }
    }

    enum ThreadAction {
        LIST, DUMP, INTERRUPT
    }

    enum ThreadColumn {
        ID, PRIORITY, STATE, INTERRUPTED, DAEMON, NAME
    }
}
