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
 import org.springframework.shell.context.ShellContext;
 import org.springframework.shell.exit.ExitCodeMappings;
 import org.springframework.stereotype.Component;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.stream.Collectors;
 import java.util.stream.IntStream;
 
 import static com.github.fonimus.ssh.shell.ExtendedInput.*;
 import static com.github.fonimus.ssh.shell.SshShellCommandFactory.SSH_THREAD_CONTEXT;
 
 /**
  * Extended shell which takes into account special characters.
  */
 @Slf4j
 @Component
 @Primary
 public class ExtendedShell extends Shell {
 
     private final ResultHandlerService resultHandlerService;
     private final List<String> postProcessorNames = new ArrayList<>();
 
     /**
      * Extended shell to handle post processors.
      *
      * @param resultHandlerService result handler service
      * @param commandRegistry      command registry
      * @param terminal             terminal
      * @param shellContext         shell context
      * @param exitCodeMappings     exit code mappings
      * @param postProcessors       post processors
      */
     protected ExtendedShell(
             ResultHandlerService resultHandlerService, CommandCatalog commandRegistry,
             Terminal terminal, ShellContext shellContext, ExitCodeMappings exitCodeMappings,
             List<PostProcessor<?, ?>> postProcessors
     ) {
         super(resultHandlerService, commandRegistry, terminal, shellContext, exitCodeMappings);
         this.resultHandlerService = resultHandlerService;
         if (postProcessors != null) {
             this.postProcessorNames.addAll(postProcessors.stream().map(PostProcessor::getName).toList());
         }
     }
 
     @Override
     public void run(InputProvider inputProvider) {
         run(inputProvider, () -> false);
     }
 
     /**
      * Run shell.
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
             clearPostProcessorsIfNotBackground(ctx);
             processKeyCharacters(words, ctx);
         }
 
         return toReturn;
     }
 
     /**
      * Clears post processors from previous commands if the context is not in the background.
      *
      * @param ctx the SSH context
      */
     private void clearPostProcessorsIfNotBackground(SshContext ctx) {
         if (!ctx.isBackground()) {
             ctx.getPostProcessorsList().clear();
         }
     }
 
     /**
      * Processes key characters in the input words and updates the post processors.
      *
      * @param words the input words
      * @param ctx   the SSH context
      */
     private void processKeyCharacters(List<String> words, SshContext ctx) {
         if (!isKeyCharInList(words)) {
             return;
         }
 
         List<Integer> indexes = IntStream.range(0, words.size())
                 .filter(i -> KEY_CHARS.contains(words.get(i)))
                 .boxed()
                 .toList();
 
         for (Integer index : indexes) {
             if (words.size() > index + 1) {
                 processKeyCharacter(words, index, ctx);
             }
         }
 
         LOGGER.debug("Found {} post processors", ctx.getPostProcessorsList().size());
     }
 
     /**
      * Processes a single key character and updates the post processors in the context.
      *
      * @param words the input words
      * @param index the index of the key character
      * @param ctx   the SSH context
      */
     private void processKeyCharacter(List<String> words, Integer index, SshContext ctx) {
         String keyChar = words.get(index);
         if (keyChar.equals(PIPE)) {
             processPipeKey(words, index, ctx);
         } else if (keyChar.equals(ARROW)) {
             ctx.getPostProcessorsList().add(new PostProcessorObject(SavePostProcessor.SAVE,
                     Collections.singletonList(words.get(index + 1))));
         }
     }
 
     /**
      * Processes the pipe ('|') key character and updates the post processors in the context.
      *
      * @param words the input words
      * @param index the index of the pipe key
      * @param ctx   the SSH context
      */
     private void processPipeKey(List<String> words, Integer index, SshContext ctx) {
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
 
     /**
      * Shell notifier interface.
      */
     @FunctionalInterface
     public interface ShellNotifier {
 
         /**
          * Method used to break the loop if the shell should be stopped.
          *
          * @return if the shell should stop or not
          */
         boolean shouldStop();
     }
 }
 
