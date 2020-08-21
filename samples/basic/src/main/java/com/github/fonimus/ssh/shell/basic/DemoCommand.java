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

package com.github.fonimus.ssh.shell.basic;

import com.github.fonimus.ssh.shell.PromptColor;
import com.github.fonimus.ssh.shell.SimpleTable;
import com.github.fonimus.ssh.shell.SshShellHelper;
import com.github.fonimus.ssh.shell.auth.SshAuthentication;
import com.github.fonimus.ssh.shell.commands.SshShellComponent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.shell.table.BorderStyle;
import org.springframework.shell.table.SimpleHorizontalAligner;
import org.springframework.shell.table.SimpleVerticalAligner;

import java.util.Arrays;

/**
 * Demo command for example
 */
@SshShellComponent
public class DemoCommand {

    private final SshShellHelper helper;

    public DemoCommand(SshShellHelper helper) {
        this.helper = helper;
    }

    /**
     * Echo command
     *
     * @param message message to print
     * @param color   color for the message
     * @return message
     */
    @ShellMethod("Echo command")
    public String echo(String message, @ShellOption(defaultValue = ShellOption.NULL) PromptColor color) {
        if (color != null) {
            return new AttributedStringBuilder().append(message,
                    AttributedStyle.DEFAULT.foreground(color.toJlineAttributedStyle())).toAnsi();
        }
        return message;
    }

    /**
     * Pojo command
     * <p>Try the post processors like pretty, grep with it</p>
     *
     * @return pojo
     */
    @ShellMethod("Pojo command")
    public Pojo pojo() {
        return new Pojo("value1", "value2");
    }

    /**
     * Authentication example command
     *
     * @return principal
     */
    @ShellMethod("Authentication command")
    public SshAuthentication authentication() {
        return helper.getAuthentication();
    }

    /**
     * Simple table example command
     *
     * @return principal
     */
    @ShellMethod("Simple table command")
    public String tableSimple() {
        return helper.renderTable(SimpleTable.builder()
                .column("col1")
                .column("col2")
                .column("col3")
                .column("col4")
                .line(Arrays.asList("line1 col1", "line1 col2", "line1 col3", "line1 col4"))
                .line(Arrays.asList("line2 col1", "line2 col2", "line2 col3", "line2 col4"))
                .line(Arrays.asList("line3 col1", "line3 col2", "line3 col3", "line3 col4"))
                .line(Arrays.asList("line4 col1", "line4 col2", "line4 col3", "line4 col4"))
                .line(Arrays.asList("line5 col1", "line5 col2", "line5 col3", "line5 col4"))
                .line(Arrays.asList("line6 col1", "line6 col2", "line6 col3", "line6 col4"))
                .build());
    }
    /**
     * Simple table example command with more data in each columns
     *
     * @return principal
     */
    @ShellMethod("Simple table command")
    public String tableSimpleLarge() {
        return helper.renderTable(SimpleTable.builder()
                .column("col1")
                .column("col2")
                .column("col3")
                .column("col4")
                .line(Arrays.asList("Table with a lot more data in line1 col1", "Table with a lot more data in line1 col2", "Table with a lot more data in line1 col3", "Table with a lot more data in line1 col4"))
                .line(Arrays.asList("Table with a lot more data in line2 col1", "Table with a lot more data in line2 col2", "Table with a lot more data in line2 col3", "Table with a lot more data in line2 col4"))
                .line(Arrays.asList("Table with a lot more data in line3 col1", "Table with a lot more data in line3 col2", "Table with a lot more data in line3 col3", "Table with a lot more data in line3 col4"))
                .line(Arrays.asList("Table with a lot more data in line4 col1", "Table with a lot more data in line4 col2", "Table with a lot more data in line4 col3", "Table with a lot more data in line4 col4"))
                .line(Arrays.asList("Table with a lot more data in line5 col1", "Table with a lot more data in line5 col2", "Table with a lot more data in line5 col3", "Table with a lot more data in line5 col4"))
                .line(Arrays.asList("Table with a lot more data in line6 col1", "Table with a lot more data in line6 col2", "Table with a lot more data in line6 col3", "Table with a lot more data in line6 col4"))
                .build());
    }

    /**
     * Complex table example command
     *
     * @return principal
     */
    @ShellMethod("Complex table command")
    public String tableComplex() {
        return helper.renderTable(SimpleTable.builder()
                .column("col1")
                .column("col2")
                .column("col3")
                .column("col4")
                .line(Arrays.asList("line1 col1", "line1 col2", "line1 col3", "line1 col4"))
                .line(Arrays.asList("line2 col1", "line2 col2", "line2 col3", "line2 col4"))
                .line(Arrays.asList("line3 col1", "line3 col2", "line3 col3", "line3 col4"))
                .line(Arrays.asList("line4 col1", "line4 col2", "line4 col3", "line4 col4"))
                .line(Arrays.asList("line5 col1", "line5 col2", "line5 col3", "line5 col4"))
                .line(Arrays.asList("line6 col1", "line6 col2", "line6 col3", "line6 col4"))
                .headerAligner(SimpleHorizontalAligner.right)
                .lineAligner(SimpleHorizontalAligner.left)
                .lineAligner(SimpleVerticalAligner.bottom)
                .useFullBorder(false)
                .borderStyle(BorderStyle.fancy_heavy_double_dash)
                .tableBuilderListener(tableBuilder -> {
                    tableBuilder.addInnerBorder(BorderStyle.fancy_light_double_dash);
                    tableBuilder.addOutlineBorder(BorderStyle.fancy_double);
                }).build());
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class Pojo {

        private String key1;

        private String key2;
    }
}
