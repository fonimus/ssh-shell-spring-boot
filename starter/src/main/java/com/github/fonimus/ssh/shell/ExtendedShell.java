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
import org.jline.terminal.Terminal;
import org.springframework.context.annotation.Primary;
import org.springframework.shell.*;
import org.springframework.shell.command.CommandCatalog;
import org.springframework.shell.command.CommandOption;
import org.springframework.shell.command.CommandRegistration;
import org.springframework.shell.completion.CompletionResolver;
import org.springframework.shell.context.ShellContext;
import org.springframework.shell.exit.ExitCodeMappings;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.github.fonimus.ssh.shell.ExtendedInput.*;
import static com.github.fonimus.ssh.shell.SshShellCommandFactory.SSH_THREAD_CONTEXT;

/**
 * Extended shell which takes in account special characters
 */
@Slf4j
@Component
@Primary
public class ExtendedShell extends Shell {

    private final ResultHandlerService resultHandlerService;
    private final CommandCatalog commandRegistry;
    private final List<String> postProcessorNames = new ArrayList<>();

    /**
     * Extended shell to handle post processors
     *
     * @param resultHandlerService result handler service
     * @param commandRegistry      command registry
     * @param terminal             terminal
     * @param shellContext         shell context
     * @param exitCodeMappings     exit code mappipngs
     * @param postProcessors       post processors
     */
    public ExtendedShell(
            ResultHandlerService resultHandlerService, CommandCatalog commandRegistry,
            Terminal terminal, ShellContext shellContext, ExitCodeMappings exitCodeMappings,
            List<PostProcessor<?, ?>> postProcessors
    ) {
        super(resultHandlerService, commandRegistry, terminal, shellContext, exitCodeMappings);
        this.resultHandlerService = resultHandlerService;
        this.commandRegistry = commandRegistry;
        if (postProcessors != null) {
            this.postProcessorNames.addAll(postProcessors.stream().map(PostProcessor::getName).collect(Collectors.toList()));
        }
    }

    @Override
    public void run(InputProvider inputProvider) {
        run(inputProvider, () -> false);
    }

    /**
     * Run shell
     *
     * @param inputProvider input provider
     * @param shellNotifier shell notifier
     */
    public void run(InputProvider inputProvider, ShellNotifier shellNotifier) {
        Object result = null;
        // Handles ExitRequest thrown from Quit command
        while (!(result instanceof ExitRequest) && !shellNotifier.shouldStop()) {
            Input input;
            try {
                input = inputProvider.readInput();
            } catch (ExitRequest e) {
                // Handles ExitRequest thrown from hitting CTRL-C
                break;
            } catch (Exception e) {
                resultHandlerService.handle(e);
                continue;
            }
            if (input == null) {
                break;
            }

            result = evaluate(input);
            if (result != NO_INPUT && !(result instanceof ExitRequest)) {
                resultHandlerService.handle(result);
            }
        }
    }

    @Override
    public Object evaluate(Input input) {
        List<String> words = input.words();
        Object toReturn = super.evaluate(new ExtendedInput(input));
        SshContext ctx = SSH_THREAD_CONTEXT.get();
        if (ctx != null) {
            if (!ctx.isBackground()) {
                // clear potential post processors from previous commands
                ctx.getPostProcessorsList().clear();
            }
            if (isKeyCharInList(words)) {
                List<Integer> indexes =
                        IntStream.range(0, words.size()).filter(i -> KEY_CHARS.contains(words.get(i))).boxed().collect(Collectors.toList());
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
                            ctx.getPostProcessorsList().add(new PostProcessorObject(postProcessorName, params));
                        } else if (keyChar.equals(ARROW)) {
                            ctx.getPostProcessorsList().add(new PostProcessorObject(SavePostProcessor.SAVE,
                                    Collections.singletonList(words.get(index + 1))));
                        }
                    }
                }
                LOGGER.debug("Found {} post processors", ctx.getPostProcessorsList().size());
            }
        }
        return toReturn;
    }

    @Override
    public List<CompletionProposal> complete(CompletionContext context) {
        if (context.getWords().contains("|")) {
            return postProcessorNames.stream().map(CompletionProposal::new).collect(Collectors.toList());
        }

        String prefix = context.upToCursor();

        List<CompletionProposal> candidates = new ArrayList<>(duplicatedCommandsStartingWith(prefix));

        String best = duplicatedFindLongestCommand(prefix);
        if (best != null) {
            context = context.drop(best.split(" ").length);
            CommandRegistration registration = commandRegistry.getRegistrations().get(best);
            CompletionContext argsContext = context.commandRegistration(registration);

            final List<String> words = context.getWords().stream().filter(StringUtils::hasText).collect(Collectors.toList());
            String lastWord = words.isEmpty() ? null : words.get(words.size() - 1);

            List<CommandOption> matchedArgOptions = new ArrayList<>();
            if (lastWord != null) {
                // last word used instead of first to check if matching args
                matchedArgOptions.addAll(duplicatedMatchOptions(registration.getOptions(), lastWord));
            }
            if (matchedArgOptions.isEmpty()) {
                // only add command options if last word did not match option
                for (CompletionResolver resolver : completionResolvers) {
                    List<CompletionProposal> resolved = resolver.apply(argsContext);
                    candidates.addAll(resolved.stream().filter(cp -> !words.contains(cp.value())).collect(Collectors.toList()));
                }
            }

            List<CompletionProposal> argProposals = matchedArgOptions.stream()
                    .flatMap(o -> {
                        Function<CompletionContext, List<CompletionProposal>> completion = o.getCompletion();
                        if (completion != null) {
                            List<CompletionProposal> apply = completion.apply(argsContext.commandOption(o));
                            return apply.stream();
                        }
                        return Stream.empty();
                    })
                    .collect(Collectors.toList());

            candidates.addAll(argProposals);
        }
        return candidates;
    }

    private static boolean isKeyCharInList(List<String> strList) {
        for (String key : KEY_CHARS) {
            if (strList.contains(key)) {
                return true;
            }
        }
        return false;
    }

    //---------------------------------
    // Private methods from Shell
    //---------------------------------

    private List<CommandOption> duplicatedMatchOptions(List<CommandOption> options, String arg) {
        List<CommandOption> matched = new ArrayList<>();
        String trimmed = StringUtils.trimLeadingCharacter(arg, '-');
        int count = arg.length() - trimmed.length();
        if (count == 1) {
            if (trimmed.length() == 1) {
                Character trimmedChar = trimmed.charAt(0);
                options.stream()
                        .filter(o -> {
                            for (Character sn : o.getShortNames()) {
                                if (trimmedChar.equals(sn)) {
                                    return true;
                                }
                            }
                            return false;
                        })
                        .findFirst()
                        .ifPresent(matched::add);
            } else if (trimmed.length() > 1) {
                trimmed.chars().mapToObj(i -> (char) i)
                        .forEach(c -> options.forEach(o -> {
                            for (Character sn : o.getShortNames()) {
                                if (c.equals(sn)) {
                                    matched.add(o);
                                }
                            }
                        }));
            }
        } else if (count == 2) {
            options.stream()
                    .filter(o -> {
                        for (String ln : o.getLongNames()) {
                            if (trimmed.equals(ln)) {
                                return true;
                            }
                        }
                        return false;
                    })
                    .findFirst()
                    .ifPresent(matched::add);
        }
        return matched;
    }

    private List<CompletionProposal> duplicatedCommandsStartingWith(String prefix) {
        // Workaround for https://github.com/spring-projects/spring-shell/issues/150
        // (sadly, this ties this class to JLine somehow)
        int lastWordStart = prefix.lastIndexOf(' ') + 1;
        return commandRegistry.getRegistrations().entrySet().stream()
                .filter(e -> e.getKey().startsWith(prefix))
                .map(e -> {
                    String c = e.getKey();
                    c = c.substring(lastWordStart);
                    return duplicatedToCommandProposal(c, e.getValue());
                })
                .collect(Collectors.toList());
    }

    private CompletionProposal duplicatedToCommandProposal(String command, CommandRegistration registration) {
        return new CompletionProposal(command)
                .dontQuote(true)
                .category("Available commands")
                .description(registration.getDescription());
    }

    /**
     * Returns the longest command that can be matched as first word(s) in the given buffer.
     *
     * @return a valid command name, or {@literal null} if none matched
     */
    private String duplicatedFindLongestCommand(String prefix) {
        String result = commandRegistry.getRegistrations().keySet().stream()
                .filter(command -> prefix.equals(command) || prefix.startsWith(command + " "))
                .reduce("", (c1, c2) -> c1.length() > c2.length() ? c1 : c2);
        return "".equals(result) ? null : result;
    }

    /**
     * Shell notifier interface
     */
    @FunctionalInterface
    public interface ShellNotifier {

        /**
         * Method used to break loop if shell should be stopped
         *
         * @return if shell should stop or not
         */
        boolean shouldStop();
    }
}
