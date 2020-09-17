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

package com.github.fonimus.ssh.shell.commands.system;

import com.github.fonimus.ssh.shell.SshShellHelper;
import com.github.fonimus.ssh.shell.SshShellProperties;
import com.github.fonimus.ssh.shell.commands.AbstractCommand;
import com.github.fonimus.ssh.shell.commands.SshShellComponent;
import org.springframework.shell.Availability;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.shell.table.*;

import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static com.github.fonimus.ssh.shell.SshShellHelper.at;

/**
 * Jvm command
 */
@SshShellComponent
@ShellCommandGroup("System Commands")
public class JvmCommand extends AbstractCommand {

    private static final String GROUP = "jvm";
    private static final String COMMAND_JVM_ENV = GROUP + "-env";
    private static final String COMMAND_JVM_PROPERTIES = GROUP + "-properties";

    public static final String SPLIT_REGEX = "[:;]";

    private SshShellHelper helper;

    public JvmCommand(SshShellHelper helper, SshShellProperties properties) {
        super(helper, properties, properties.getCommands().getJvm());
        this.helper = helper;
    }

    @ShellMethod(key = COMMAND_JVM_ENV, value = "List system environment.")
    @ShellMethodAvailability("jvmEnvAvailability")
    public Object jvmEnv(boolean simpleView) {
        if (simpleView) {
            return buildSimple(System.getenv());
        }
        return buildTable(System.getenv()).render(helper.terminalSize().getRows());
    }

    @ShellMethod(key = COMMAND_JVM_PROPERTIES, value = "List system properties.")
    @ShellMethodAvailability("jvmPropertiesAvailability")
    public Object jvmProperties(boolean simpleView) {
        Map<String, String> map =
                System.getProperties().entrySet().stream().filter(e -> e.getKey() != null)
                        .collect(Collectors.toMap(e -> e.getKey().toString(), e -> e.getValue() != null ?
                                e.getValue().toString() : ""));
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

    private Availability jvmEnvAvailability() {
        return availability(GROUP, COMMAND_JVM_ENV);
    }

    private Availability jvmPropertiesAvailability() {
        return availability(GROUP, COMMAND_JVM_PROPERTIES);
    }
}
