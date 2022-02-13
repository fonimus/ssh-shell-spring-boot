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
import com.github.fonimus.ssh.shell.SshShellProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Lazy;
import org.springframework.shell.Availability;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.shell.standard.ShellOption;
import org.springframework.shell.standard.commands.History;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Override history command to get history per user if not shared
 */
@Slf4j
@SshShellComponent
@ShellCommandGroup("Built-In Commands")
@ConditionalOnProperty(
        name = SshShellProperties.SSH_SHELL_PREFIX + ".commands." + HistoryCommand.GROUP + ".create",
        havingValue = "true", matchIfMissing = true
)
public class HistoryCommand extends AbstractCommand implements History.Command {

    public static final String GROUP = "history";
    public static final String COMMAND_HISTORY = GROUP;

    private org.jline.reader.History history;

    public HistoryCommand(SshShellProperties properties, SshShellHelper helper,
                          @Lazy org.jline.reader.History history) {
        super(helper, properties, properties.getCommands().getHistory());
        this.history = history;
    }

    @ShellMethod(key = COMMAND_HISTORY, value = "Display or save the history of previously run commands")
    @ShellMethodAvailability("historyAvailability")
    public List<String> history(@ShellOption(help = "A file to save history to.", defaultValue = ShellOption.NULL) File file) throws IOException {
        org.jline.reader.History historyToUse = this.history;
        if (!properties.isSharedHistory() && !helper.isLocalPrompt()) {
            LOGGER.debug("History is not shared and this is not from local prompt, getting specific user history");
            historyToUse = helper.getHistory();
        }
        return new History(historyToUse).history(file);
    }

    private Availability historyAvailability() {
        return availability(GROUP, COMMAND_HISTORY);
    }

}
