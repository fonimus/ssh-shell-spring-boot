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

package com.github.fonimus.ssh.shell;

import com.github.fonimus.ssh.shell.postprocess.PostProcessor;
import com.github.fonimus.ssh.shell.postprocess.PostProcessorObject;
import com.github.fonimus.ssh.shell.postprocess.provided.SavePostProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.shell.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.github.fonimus.ssh.shell.ExtendedInput.*;
import static com.github.fonimus.ssh.shell.SshShellCommandFactory.SSH_THREAD_CONTEXT;

/**
 * Extended shell which takes in account special characters
 */
@Slf4j
public class ExtendedShell
        extends Shell {

    private final List<String> postProcessorNames = new ArrayList<>();

    /**
     * Default constructor
     *
     * @param resultHandler  result handler
     * @param postProcessors post processors list
     */
    public ExtendedShell(ResultHandler resultHandler, List<PostProcessor> postProcessors) {
        super(resultHandler);
        if (postProcessors != null) {
            postProcessorNames.addAll(postProcessors.stream().map(PostProcessor::getName).collect(Collectors.toList()));
        }
    }

    @Override
    public Object evaluate(Input input) {
        List<String> words = input.words();
        Object toReturn = super.evaluate(new ExtendedInput(input));
        SshContext ctx = SSH_THREAD_CONTEXT.get();
        if (ctx != null) {
            ctx.setPostProcessorsList(null);
            if (isKeyCharInList(words)) {
                List<Integer> indexes =
                        IntStream.range(0, words.size()).filter(i -> KEY_CHARS.contains(words.get(i))).boxed().collect(Collectors.toList());
                List<PostProcessorObject> postProcessors = new ArrayList<>();
                for (Integer index : indexes) {
                    if (words.size() > index + 1) {
                        String keyChar = words.get(index);
                        if (keyChar.equals(PIPE)) {
                            String postProcessorName = words.get(index + 1);
                            int currentIndex = 2;
                            String word = words.size() > index + currentIndex ? words.get(index + currentIndex) : null;
                            List<String> params = new ArrayList<>();
                            while (word != null && !KEY_CHARS.contains(word)) {
                                params.add(word);
                                currentIndex++;
                                word = words.size() > index + currentIndex ? words.get(index + currentIndex) : null;
                            }
                            postProcessors.add(new PostProcessorObject(postProcessorName, params));
                        } else if (keyChar.equals(ARROW)) {
                            postProcessors.add(new PostProcessorObject(SavePostProcessor.SAVE,
                                    Collections.singletonList(words.get(index + 1))));
                        }
                    }
                }
                LOGGER.debug("Found {} post processors", postProcessors.size());
                ctx.setPostProcessorsList(postProcessors);
            }
        }
        return toReturn;
    }

    @Override
    public List<CompletionProposal> complete(CompletionContext context) {
        if (context.getWords().contains("|")) {
            return postProcessorNames.stream().map(CompletionProposal::new).collect(Collectors.toList());
        }
        return super.complete(context);
    }

    private static boolean isKeyCharInList(List<String> strList) {
        for (String key : KEY_CHARS) {
            if (strList.contains(key)) {
                return true;
            }
        }
        return false;
    }
}
