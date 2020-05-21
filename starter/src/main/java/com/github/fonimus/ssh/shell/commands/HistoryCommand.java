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

import com.github.fonimus.ssh.shell.SshShellHelper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.shell.standard.commands.History;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static com.github.fonimus.ssh.shell.SshShellProperties.SSH_SHELL_PREFIX;

/**
 * Override history command to get history per user if not shared
 */
@SshShellComponent
@ShellCommandGroup("Built-In Commands")
@ConditionalOnProperty(name = SSH_SHELL_PREFIX + ".shared-history", havingValue = "false")
public class HistoryCommand implements History.Command {

    private SshShellHelper helper;

    public HistoryCommand(SshShellHelper helper) {
        this.helper = helper;
    }

    @ShellMethod(value = "Display or save the history of previously run commands")
    public List<String> history(@ShellOption(help = "A file to save history to.", defaultValue = ShellOption.NULL) File file) throws IOException {
        return new History(helper.getHistory()).history(file);
    }
}
