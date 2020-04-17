package com.github.fonimus.ssh.shell.commands;

import com.github.fonimus.ssh.shell.PromptColor;
import com.github.fonimus.ssh.shell.SshShellHelper;
import com.github.fonimus.ssh.shell.interactive.Interactive;
import com.github.fonimus.ssh.shell.interactive.KeyBinding;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.shell.table.ArrayTableModel;
import org.springframework.shell.table.BorderStyle;
import org.springframework.shell.table.SimpleHorizontalAligner;
import org.springframework.shell.table.TableBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.github.fonimus.ssh.shell.SshShellHelper.*;
import static com.github.fonimus.ssh.shell.SshShellProperties.SSH_SHELL_PREFIX;

/**
 * Thread command
 */
@SshShellComponent
@ShellCommandGroup("Built-In Commands")
@ConditionalOnProperty(
        value = {
                SSH_SHELL_PREFIX + ".default-commands.threads",
                SSH_SHELL_PREFIX + ".defaultCommands.threads"
        }, havingValue = "true", matchIfMissing = true
)
public class ThreadCommand {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd:MM:yyyy HH:mm:ss");

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
            default:
                return PromptColor.WHITE;

        }
    }

    private static ThreadGroup getRoot() {
        ThreadGroup group = Thread.currentThread().getThreadGroup();
        ThreadGroup parent;
        while ((parent = group.getParent()) != null) {
            group = parent;
        }
        return group;
    }

    enum ThreadColumn {
        ID, PRIORITY, STATE, INTERRUPTED, DAEMON, NAME
    }

    @ShellMethod("Thread command.")
    public String threads(@ShellOption(defaultValue = "LIST") ThreadAction action,
                          @ShellOption(help = "Order by column. Default is: ID", defaultValue = "ID") ThreadColumn orderBy,
                          @ShellOption(help = "Reverse order by column. Default is: false") boolean reverseOrder,
                          @ShellOption(help = "Not interactive. Default is: false") boolean staticDisplay,
                          @ShellOption(help = "Only for DUMP action", defaultValue = ShellOption.NULL) Long threadId) {

        if (action == ThreadAction.DUMP) {
            Thread th = get(threadId);
            helper.print("Name  : " + th.getName());
            helper.print("State : " + helper.getColored(th.getState().name(), color(th.getState())) + "\n");
            Exception e = new Exception("Thread [" + th.getId() + "] stack trace");
            e.setStackTrace(th.getStackTrace());
            e.printStackTrace(helper.terminalWriter());
            return "";
        }

        if (staticDisplay) {
            return table(orderBy, reverseOrder, false);
        }

        boolean[] finalReverseOrder = {reverseOrder};
        ThreadColumn[] finalOrderBy = {orderBy};

        Interactive.InteractiveBuilder builder = Interactive.builder();
        for (ThreadColumn value : ThreadColumn.values()) {
            String key = value == ThreadColumn.INTERRUPTED ? "t" : value.name().toLowerCase().substring(0, 1);
            builder.binding(KeyBinding.builder().description("ORDER_" + value.name()).key(key)
                    .input(() -> {
                        if (value == finalOrderBy[0]) {
                            finalReverseOrder[0] = !finalReverseOrder[0];
                        } else {
                            finalOrderBy[0] = value;
                        }
                    }).build());
        }
        builder.binding(KeyBinding.builder().key("r").description("REVERSE")
                .input(() -> finalReverseOrder[0] = !finalReverseOrder[0]).build());

        helper.interactive(builder.input((size, currentDelay) -> {
            List<AttributedString> lines = new ArrayList<>(size.getRows());

            lines.add(new AttributedStringBuilder()
                    .append("Time: ")
                    .append(FORMATTER.format(LocalDateTime.now()), AttributedStyle.BOLD)
                    .append(", refresh delay: ")
                    .append(String.valueOf(currentDelay), AttributedStyle.BOLD)
                    .append(" ms\n")
                    .toAttributedString());

            for (String s : table(finalOrderBy[0], finalReverseOrder[0], true).split("\n")) {
                lines.add(AttributedString.fromAnsi(s));
            }

            lines.add(AttributedString.fromAnsi("Press 'r' to reverse order, first column letter to change order by"));
            String msg = INTERACTIVE_LONG_MESSAGE.length() <= helper.terminalSize().getColumns() ?
                    INTERACTIVE_LONG_MESSAGE : INTERACTIVE_SHORT_MESSAGE;
            lines.add(AttributedString.fromAnsi(msg));

            return lines;
        }).build());
        return "";
    }

    private String table(ThreadColumn orderBy, boolean reverseOrder, boolean fullscreen) {
        List<Thread> ordered = new ArrayList<>(getThreads().values());
        ordered.sort(comparator(orderBy, reverseOrder));

        // handle maximum rows: 1 line for headers, 3 borders, 3 description lines
        int maxWithHeadersAndBorders = helper.terminalSize().getRows() - 8;
        int tableSize = ordered.size() + 1;
        boolean addDotLine = false;
        if (fullscreen && ordered.size() > maxWithHeadersAndBorders) {
            ordered = ordered.subList(0, maxWithHeadersAndBorders);
            tableSize = maxWithHeadersAndBorders + 2;
            addDotLine = true;
        }

        String[][] data = new String[tableSize][ThreadColumn.values().length];
        TableBuilder tableBuilder = new TableBuilder(new ArrayTableModel(data));

        int i = 0;
        for (ThreadColumn column : ThreadColumn.values()) {
            data[0][i] = column.name();
            tableBuilder.on(at(0, i)).addAligner(SimpleHorizontalAligner.center);
            i++;
        }
        int r = 1;
        for (Thread t : ordered) {
            data[r][0] = String.valueOf(t.getId());
            data[r][1] = String.valueOf(t.getPriority());
            data[r][2] = t.getState().name();
            tableBuilder.on(at(r, 2)).addAligner(new ColorAligner(color(t.getState())));
            data[r][3] = String.valueOf(t.isInterrupted());
            data[r][4] = String.valueOf(t.isDaemon());
            data[r][5] = t.getName();
            r++;
        }
        if (addDotLine) {
            String dots = "...";
            data[r][0] = dots;
            data[r][1] = dots;
            data[r][2] = dots;
            data[r][3] = dots;
            data[r][4] = dots;
            data[r][5] = "... not enough rows to display all threads";
        }
        return tableBuilder.addHeaderAndVerticalsBorders(BorderStyle.fancy_double).build().render(helper.terminalSize().getRows());
    }

    enum ThreadAction {
        LIST, DUMP
    }
}
