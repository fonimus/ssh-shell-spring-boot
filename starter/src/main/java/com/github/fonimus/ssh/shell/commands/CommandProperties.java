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

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;

import static com.github.fonimus.ssh.shell.SshShellProperties.ADMIN_ROLE;

/**
 * Command specific properties
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommandProperties {

    private boolean enable = true;

    private boolean restricted = true;

    private List<String> authorizedRoles = Arrays.asList(ADMIN_ROLE);

    public CommandProperties(List<String> authorizedRoles) {
        this.authorizedRoles = authorizedRoles;
    }

    public static CommandProperties disabledByDefault() {
        CommandProperties properties = new CommandProperties();
        properties.setEnable(false);
        return properties;
    }

    public static CommandProperties notRestrictedByDefault() {
        CommandProperties properties = new CommandProperties();
        properties.setRestricted(false);
        properties.setAuthorizedRoles(null);
        return properties;
    }
}
