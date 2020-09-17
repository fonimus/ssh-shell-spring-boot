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

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Properties;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.fail;

public class SshHelperTest {

    public static final Logger LOGGER = LoggerFactory.getLogger(SshHelperTest.class);

    public static void call(SshShellProperties properties, Executor executor) {
        call(properties.getUser(), properties.getPassword(), properties.getHost(), properties.getPort(), executor);
    }

    public static void call(String user, String pass, SshShellProperties properties, Executor executor) {
        call(user, pass, properties.getHost(), properties.getPort(), executor);
    }

    public static void call(String user, String pass, String host, int port, Executor executor) {
        try {
            JSch jsch = new JSch();
            Session session = jsch.getSession(user, host, port);
            session.setPassword(pass);
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect();
            Channel channel = session.openChannel("shell");
            PipedInputStream pis = new PipedInputStream();
            PipedOutputStream pos = new PipedOutputStream();
            channel.setInputStream(new PipedInputStream(pos));
            channel.setOutputStream(new PipedOutputStream(pis));
            channel.connect();
            try {
                executor.execute(pis, pos);
            } catch (Exception e) {
                fail(e.toString());
            } finally {
                pis.close();
                pos.close();
                channel.disconnect();
                session.disconnect();
            }
        } catch (JSchException | IOException ex) {
            fail(ex.toString());
        }
    }

    public static void verifyResponse(InputStream pis, String response) {
        StringBuilder sb = new StringBuilder();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            fail("Got interrupted exception while waiting");
        }
        try {
            await().atMost(Duration.ofSeconds(5)).until(() -> {
                while (true) {
                    sb.append((char) pis.read());
                    String s = sb.toString();
                    if (s.contains(response)) {
                        break;
                    }
                }
                return true;
            });
        } finally {
            LOGGER.info("--------------- received::start ---------------");
            LOGGER.info(sb.toString());
            LOGGER.info("--------------- received::end   ---------------");
        }
    }

    public static void write(OutputStream os, String... input) throws IOException {
        for (String s : input) {
            os.write((s + "\r").getBytes(StandardCharsets.UTF_8));
            os.flush();
        }
    }

    @FunctionalInterface
    public interface Executor {

        void execute(InputStream is, OutputStream os) throws Exception;
    }
}
