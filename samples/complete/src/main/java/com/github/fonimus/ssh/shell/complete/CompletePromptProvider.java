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

import org.jline.utils.AttributedString;
import org.springframework.shell.jline.PromptProvider;
import org.springframework.stereotype.Component;

import static org.jline.utils.AttributedStyle.CYAN;
import static org.jline.utils.AttributedStyle.DEFAULT;

@Component
public class CompletePromptProvider implements PromptProvider {

    @Override
    public AttributedString getPrompt() {
        return new AttributedString("complete::>", DEFAULT.foreground(CYAN));
    }
}
