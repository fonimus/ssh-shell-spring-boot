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
import com.jcraft.jsch.Session;

import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.charset.StandardCharsets;

public class JavaConnect {

    public static void main(String[] args) {

        String host = "localhost";
        String user = "user";
        int port = 2222;
        String password = "password";
        String command = "help";

        PipedInputStream pis = new PipedInputStream();
        PipedOutputStream pos = new PipedOutputStream();

        try {
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            JSch jsch = new JSch();
            Session session = jsch.getSession(user, host, port);
            session.setPassword(password);
            session.setConfig(config);
            session.connect();
            System.out.println("> Connected");

            Channel channel = session.openChannel("shell");

            channel.setInputStream(new PipedInputStream(pos));
            channel.setOutputStream(new PipedOutputStream(pis));
            channel.connect();

            System.out.println("> Typing command '" + command + "'");
            pos.write((command + "\r").getBytes(StandardCharsets.UTF_8));
            pos.flush();

            byte[] tmp = new byte[1024];
            // Note: this main will never stop unless ssh server is stopped
            while (true) {
                while (pis.available() > 0) {
                    int i = pis.read(tmp, 0, 1024);
                    if (i < 0) break;
                    System.out.print(new String(tmp, 0, i));
                }
                if (channel.isClosed()) {
                    System.out.println("\n> exit-status: " + channel.getExitStatus());
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (Exception ee) {
                    // nothing to do
                }
            }
            channel.disconnect();
            session.disconnect();
            System.out.println("> Done");
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

}
