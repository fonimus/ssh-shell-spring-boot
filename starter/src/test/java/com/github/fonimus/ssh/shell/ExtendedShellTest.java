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

import com.github.fonimus.ssh.shell.postprocess.PostProcessorObject;
import com.github.fonimus.ssh.shell.postprocess.provided.GrepPostProcessor;
import com.github.fonimus.ssh.shell.postprocess.provided.SavePostProcessor;
import lombok.SneakyThrows;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;
import org.jline.reader.Parser;
import org.jline.utils.AttributedString;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.springframework.shell.*;
import org.springframework.shell.command.CommandCatalog;
import org.springframework.shell.command.CommandRegistration;
import org.springframework.shell.completion.RegistrationOptionsCompletionResolver;
import org.springframework.shell.jline.ExtendedDefaultParser;

import java.util.*;
import java.util.stream.Collectors;

import static com.github.fonimus.ssh.shell.SshShellCommandFactory.SSH_THREAD_CONTEXT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExtendedShellTest {

    private ExtendedShell shell;
    private ResultHandlerService resultHandlerService;

    @BeforeEach
    void setUp() {
        SSH_THREAD_CONTEXT.set(new SshContext(null, null, null, null));
        resultHandlerService = mock(ResultHandlerService.class);
        CommandCatalog commandRegistry = mock(CommandCatalog.class);
        Map<String, CommandRegistration> map = new HashMap<>();
        map.put("cmd", CommandRegistration.builder()
                .command("cmd").withTarget().function(commandContext -> "result").and()
                .withOption().longNames("ab").and()
                .withOption().longNames("ac").completion(context -> Arrays.asList(new CompletionProposal("cp1"), new CompletionProposal("cp2"))).and()
                .build());
        map.put("exit", CommandRegistration.builder().command("exit").withTarget().consumer(commandContext -> {
            throw new ExitRequest();
        }).and().build());
        when(commandRegistry.getRegistrations()).thenReturn(map);
        shell = new ExtendedShell(resultHandlerService, commandRegistry, null, null, null,
                Collections.singletonList(new GrepPostProcessor()));
        shell.setCompletionResolvers(Collections.singletonList(new RegistrationOptionsCompletionResolver()));
    }

    @Test
    @SneakyThrows
    void run() {
        LineReader lineReader = mock(LineReader.class);
        when(lineReader.getParsedLine())
                .thenReturn(parsedLine("cmd"))
                .thenReturn(parsedLine("unknown"))
                .thenThrow(new IllegalArgumentException("MOCK"))
                .thenThrow(new ExitRequest())
                .thenReturn(parsedLine("exit"));
        ExtendedInteractiveShellRunner.SshShellInputProvider input =
                new ExtendedInteractiveShellRunner.SshShellInputProvider(lineReader, () -> new AttributedString("prompt"));
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        doNothing().when(resultHandlerService).handle(captor.capture());

        shell.run(input);

        assertEquals(3, captor.getAllValues().size());
        assertEquals("result", captor.getAllValues().get(0));
        assertEquals(CommandNotFound.class, captor.getAllValues().get(1).getClass());
        assertEquals(IllegalArgumentException.class, captor.getAllValues().get(2).getClass());
    }

    @Test
    void evaluate() {
        shell.evaluate(() -> "one two three");
        assertEquals(Collections.emptyList(), SSH_THREAD_CONTEXT.get().getPostProcessorsList());

        shell.evaluate(() -> "one two three | grep test > /tmp/file");
        List<PostProcessorObject> postProcessors = SSH_THREAD_CONTEXT.get().getPostProcessorsList();
        assertNotNull(postProcessors);
        assertEquals(2, postProcessors.size());
        assertInList(postProcessors, new GrepPostProcessor().getName());
        assertInList(postProcessors, new SavePostProcessor().getName());
    }

    private void assertInList(List<PostProcessorObject> postProcessors, String name) {
        for (PostProcessorObject postProcessor : postProcessors) {
            if (postProcessor.getName().equals(name)) {
                return;
            }
        }
        fail("Could not find post processor with name [" + name + "] in list: " + postProcessors);
    }

    @ParameterizedTest
    @MethodSource("completeSource")
    void complete(String line, List<String> proposals) {
        ParsedLine parsedLine = parsedLine(line);
        CompletionContext context = new CompletionContext(parsedLine.words(), parsedLine.wordIndex(), parsedLine.wordCursor(), null, null);

        List<CompletionProposal> result = shell.complete(context);

        assertThat(result.stream().map(CompletionProposal::value).collect(Collectors.toList()))
                .containsExactlyInAnyOrder(proposals.toArray(new String[0]));
    }

    private static List<Arguments> completeSource() {
        return Arrays.asList(
                Arguments.of("", Arrays.asList("cmd", "exit")),
                Arguments.of("x", Collections.emptyList()),
                Arguments.of("cm", Collections.singletonList("cmd")),
                Arguments.of("cmd ", Arrays.asList("--ab", "--ac")),
                Arguments.of("cmd a", Arrays.asList("--ab", "--ac")),
                Arguments.of("cmd --a", Arrays.asList("--ab", "--ac")),
                Arguments.of("cmd ab", Arrays.asList("--ab", "--ac")),
                Arguments.of("cmd ac", Arrays.asList("--ab", "--ac")),
                // nothing to complete for ab option -> giving others options
                Arguments.of("cmd --ab ", Collections.singletonList("--ac")),
                // used complete adapter
                Arguments.of("cmd --ac ", Arrays.asList("--ab", "cp1", "cp2")),
                // propose only not used options
                Arguments.of("cmd --ac cp1 ", Collections.singletonList("--ab")),
                // after pipe, complete with post processors
                Arguments.of("cmd --ac cp1 --ab test | ", Collections.singletonList("grep"))
        );
    }

    private static ParsedLine parsedLine(String line) {
        return new ExtendedDefaultParser().parse(line, line.length(), Parser.ParseContext.COMPLETE);
    }
}
