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

import com.github.fonimus.ssh.shell.ExtendedShell;
import com.github.fonimus.ssh.shell.SshShellHelper;
import com.github.fonimus.ssh.shell.SshShellProperties;
import com.github.fonimus.ssh.shell.interactive.Interactive;
import lombok.SneakyThrows;
import org.jline.reader.Parser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.shell.Shell;
import org.springframework.shell.jline.FileInputProvider;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class ScriptCommandTest {

    private static final File FILE = new File("src/test/resources/script.txt");
    private ExtendedShell shell;
    private SshShellHelper sshHelper;
    private ScriptCommand cmd;
    private File output;

    @BeforeEach
    @SneakyThrows
    void setUp() {
        shell = mock(ExtendedShell.class);
        Parser parser = mock(Parser.class);
        sshHelper = mock(SshShellHelper.class);
        cmd = new ScriptCommand(parser, sshHelper, new SshShellProperties());
        ApplicationContext context = mock(ApplicationContext.class);
        cmd.setApplicationContext(context);
        when(context.getBeanProvider(Shell.class)).thenReturn(new ObjectProvider<>() {
            @Override
            public Shell getObject(Object... objects) throws BeansException {
                return null;
            }

            @Override
            public Shell getIfAvailable() throws BeansException {
                return null;
            }

            @Override
            public Shell getIfUnique() throws BeansException {
                return null;
            }

            @Override
            public Shell getObject() throws BeansException {
                return shell;
            }
        });
        cmd.afterPropertiesSet();
        output = new File("target/result-" + UUID.randomUUID() + ".txt");
    }

    @Test
    void testNoBackground() throws Exception {
        cmd.script(FILE, null, false, ScriptCommand.ScriptAction.execute, true);
        verify(shell, times(1)).run(any(FileInputProvider.class), any());
    }

    @Test
    @SneakyThrows
    void testBackgroundExecuteFileNull() {
        assertThrows(IllegalArgumentException.class, () -> cmd.script(null, null, true,
                ScriptCommand.ScriptAction.execute, true));
        verify(shell, never()).run(any(FileInputProvider.class), any());
    }

    @Test
    @SneakyThrows
    void testBackgroundExecuteNoOutput() {
        assertThrows(IllegalArgumentException.class, () -> cmd.script(FILE, null, true,
                ScriptCommand.ScriptAction.execute, true));
        verify(shell, never()).run(any(FileInputProvider.class), any());
    }

    @Test
    @SneakyThrows
    void testBackgroundExecuteInvalidOutput() {
        assertThrows(IllegalArgumentException.class, () -> cmd.script(FILE, new File("target"), true,
                ScriptCommand.ScriptAction.execute, true));
        verify(shell, never()).run(any(FileInputProvider.class), any());
    }

    @Test
    @SneakyThrows
    void testBackgroundExecuteInvalidOutputFile() {
        assertThrows(IOException.class, () -> cmd.script(FILE, new File(UUID.randomUUID().toString(),
                UUID.randomUUID().toString()), true, ScriptCommand.ScriptAction.execute, true));
        verify(shell, never()).run(any(FileInputProvider.class), any());
    }

    @Test
    void testBackgroundExecute() throws Exception {
        launchShellOk();
    }

    @Test
    void testBackgroundExecuteInteractive() throws Exception {
        launchShellOk(false);
    }

    @Test
    void testBackgroundExecuteAlreadyRunning() throws Exception {
        mockLongProcess();

        launchShellOk();

        launch(ScriptCommand.ScriptAction.execute);
        // only one run : previous one
        verify(shell, times(1)).run(any(FileInputProvider.class), any());
        verify(sshHelper, times(1)).printWarning(eq("Script already running in background. Aborting."));
    }

    @Test
    void testBackgroundStatusNotRunning() throws Exception {
        launch(ScriptCommand.ScriptAction.status);
        verify(shell, never()).run(any(FileInputProvider.class), any());
        verify(sshHelper, times(1)).print(eq("No script running in background."));
    }

    @Test
    void testBackgroundStatusDone() throws Exception {
        launchShellOk();
        launch(ScriptCommand.ScriptAction.status);
        verify(sshHelper, times(1)).print(eq("Script done. 0 commands executed."));
    }

    @Test
    void testBackgroundStatusRunning() throws Exception {
        mockLongProcess();

        launchShellOk();

        launch(ScriptCommand.ScriptAction.status);
        // only one run : previous one
        verify(shell, times(1)).run(any(FileInputProvider.class), any());
        // interactive called : it means progress is shown
        verify(sshHelper, times(1)).interactive(any(Interactive.class));
    }

    @Test
    void testBackgroundStopNotRunning() throws Exception {
        launch(ScriptCommand.ScriptAction.stop);
        verify(shell, never()).run(any(FileInputProvider.class), any());
        verify(sshHelper, times(1)).print(eq("No script running in background."));
    }

    @Test
    void testBackgroundStopRunning() throws Exception {
        mockLongProcess();

        launchShellOk();

        launch(ScriptCommand.ScriptAction.stop);
        // only one run : previous one
        verify(shell, times(1)).run(any(FileInputProvider.class), any());
        verify(sshHelper, times(1)).print(eq("Script stopped. 0 commands executed."));
    }

    @Test
    void testBackgroundStopDone() throws Exception {
        launchShellOk();
        launch(ScriptCommand.ScriptAction.stop);
        verify(sshHelper, times(1)).print(eq("Script done. 0 commands executed."));
    }

    private void launchShellOk() throws Exception {
        launchShellOk(true);
    }

    private void launchShellOk(boolean notInteractive) throws Exception {
        launch(ScriptCommand.ScriptAction.execute, notInteractive);
        Thread.sleep(200);
        verify(shell, times(1)).run(any(FileInputProvider.class), any());
        verify(sshHelper, times(1)).print(eq("Script from file starting un background. Please check results at " + output.getAbsolutePath() + "."));
    }

    private void launch(ScriptCommand.ScriptAction action) throws Exception {
        launch(action, true);
    }

    private void launch(ScriptCommand.ScriptAction action, boolean notInteractive) throws Exception {
        reset(sshHelper);
        cmd.script(FILE, output, true, action, notInteractive);
    }

    @SneakyThrows
    private void mockLongProcess() {
        doAnswer(invocationOnMock -> {
            System.err.println("executing commands...");
            Thread.sleep(1000);
            return null;
        }).when(shell).run(any(), any());
        doAnswer(invocationOnMock -> {
            System.err.println("executing commands...");
            Thread.sleep(1000);
            return null;
        }).when(sshHelper).interactive(any(Interactive.class));
    }
}
