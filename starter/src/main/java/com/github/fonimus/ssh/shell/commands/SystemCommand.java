package com.github.fonimus.ssh.shell.commands;

import com.github.fonimus.ssh.shell.SshShellHelper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.table.*;

import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static com.github.fonimus.ssh.shell.SshShellHelper.at;
import static com.github.fonimus.ssh.shell.SshShellProperties.SSH_SHELL_PREFIX;

/**
 * System command
 */
@SshShellComponent
@ShellCommandGroup("Built-In Commands")
@ConditionalOnProperty(
        value = {
                SSH_SHELL_PREFIX + ".default-commands.jvm",
                SSH_SHELL_PREFIX + ".defaultCommands.jvm"
        }, havingValue = "true", matchIfMissing = true
)
public class SystemCommand {

    public static final String SPLIT_REGEX = "[:;]";

    private SshShellHelper helper;

    public SystemCommand(SshShellHelper helper) {
        this.helper = helper;
    }

    private static SizeConstraints.Extent extent(String[] raw, String regex) {
        int max = 0;
        int min = 0;
        for (String line : raw) {
            String[] words = line.split(regex);
            for (String word : words) {
                min = Math.max(min, word.length());
            }
            max = Math.max(max, line.length());
        }
        return new SizeConstraints.Extent(min, max);
    }

    @ShellMethod(key = "jvm-env", value = "List system environment.")
    public Object jvmEnv(boolean simpleView) {
        if (simpleView) {
            return buildSimple(System.getenv());
        }
        return buildTable(System.getenv()).render(helper.terminalSize().getRows());
    }

    @ShellMethod(key = "jvm-properties", value = "List system properties.")
    public Object jvmProperties(boolean simpleView) {
        Map<String, String> map =
                System.getProperties().entrySet().stream().filter(e -> e.getKey() != null).collect(Collectors.toMap(e -> e.getKey().toString(), e -> e.getValue() != null ? e.getValue().toString() : ""));
        if (simpleView) {
            return buildSimple(map);
        }
        return buildTable(map).render(helper.terminalSize().getRows());
    }

    private String buildSimple(Map<String, String> mapParam) {
        Map<String, String> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        map.putAll(mapParam);
        int maxColumn = helper.terminalSize().getRows() - 3 / 2;
        StringBuilder sb = new StringBuilder();
        int max = -1;
        for (String s : map.keySet()) {
            if (s.length() > max && s.length() < maxColumn) {
                max = s.length();
            }
        }

        for (Map.Entry<String, String> e : map.entrySet()) {
            sb.append(String.format("%-" + max + "s", e.getKey())).append(" | ").append(e.getValue()).append("\n");
        }
        return sb.toString();
    }

    private Table buildTable(Map<String, String> mapParam) {
        Map<String, String> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        map.putAll(mapParam);
        String[][] data = new String[map.size() + 1][2];
        TableModel model = new ArrayTableModel(data);
        TableBuilder tableBuilder = new TableBuilder(model);

        data[0][0] = "Key";
        data[0][1] = "Value";
        tableBuilder.on(at(0, 0)).addAligner(SimpleHorizontalAligner.center);
        tableBuilder.on(at(0, 1)).addAligner(SimpleHorizontalAligner.center);
        int i = 1;
        for (Map.Entry<String, String> e : map.entrySet()) {
            data[i][0] = e.getKey();
            data[i][1] = e.getValue();
            tableBuilder.on(at(i, 0)).addAligner(SimpleHorizontalAligner.center);
            tableBuilder.on(at(i, 0)).addAligner(SimpleVerticalAligner.middle);
            if (e.getKey().toLowerCase().contains("path") || e.getKey().toLowerCase().contains("dirs")) {
                tableBuilder.on(at(i, 1)).addSizer((raw, tableWidth, nbColumns) -> extent(raw, SPLIT_REGEX));
                tableBuilder.on(at(i, 1)).addFormatter(value -> value == null ? new String[]{""}
                        : value.toString().split(SPLIT_REGEX));
            }
            i++;
        }
        return tableBuilder.addFullBorder(BorderStyle.fancy_double).build();
    }
}
