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

import com.github.fonimus.ssh.shell.auth.SshShellPublicKeyAuthenticationProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.sshd.common.util.io.IoUtils;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.auth.pubkey.RejectAllPublickeyAuthenticator;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.*;
import java.nio.file.Files;

/**
 * Ssh shell configuration
 */

@Slf4j
@Configuration
public class SshShellConfiguration {

    private SshShellProperties properties;

    private SshShellCommandFactory shellCommandFactory;

    private PasswordAuthenticator passwordAuthenticator;

    public SshShellConfiguration(SshShellProperties properties,
                                 SshShellCommandFactory shellCommandFactory,
                                 PasswordAuthenticator passwordAuthenticator) {
        this.properties = properties;
        this.shellCommandFactory = shellCommandFactory;
        this.passwordAuthenticator = passwordAuthenticator;
    }

    /**
     * Create the bean responsible for starting and stopping the SSH server
     * @param sshServer the ssh server to manage
     * @return ssh server lifecycle
     */
    @Bean
    public SshServerLifecycle sshServerLifecycle(SshServer sshServer) {
        return new SshServerLifecycle(sshServer, this.properties);
    }

    /**
     * Construct ssh server thanks to ssh shell properties
     *
     * @return ssh server
     */
    @Bean
    public SshServer sshServer() throws IOException {
        SshServer server = SshServer.setUpDefaultServer();
        server.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(properties.getHostKeyFile().toPath()));
        server.setHost(properties.getHost());
        server.setPasswordAuthenticator(passwordAuthenticator);
        server.setPublickeyAuthenticator(RejectAllPublickeyAuthenticator.INSTANCE);
        if (properties.getAuthorizedPublicKeys() != null) {
            if (properties.getAuthorizedPublicKeys().exists()) {
                server.setPublickeyAuthenticator(
                        new SshShellPublicKeyAuthenticationProvider(getFile(properties.getAuthorizedPublicKeys()))
                );
                LOGGER.info("Using authorized public keys from : {}",
                        properties.getAuthorizedPublicKeys().getDescription());
            } else {
                LOGGER.warn("Could not read authorized public keys from : {}, public key authentication is disabled.",
                        properties.getAuthorizedPublicKeys().getDescription());
            }
        }
        server.setPort(properties.getPort());
        server.setShellFactory(channelSession -> shellCommandFactory);
        server.setCommandFactory((channelSession, s) -> shellCommandFactory);
        return server;
    }

    private File getFile(Resource authorizedPublicKeys) throws IOException {
        if ("file".equals(authorizedPublicKeys.getURL().getProtocol())) {
            return authorizedPublicKeys.getFile();
        } else {
            File tmp = Files.createTempFile("sshShellPubKeys-", ".tmp").toFile();
            try (InputStream is = authorizedPublicKeys.getInputStream();
                 OutputStream os = new FileOutputStream(tmp)) {
                IoUtils.copy(is, os);
            }
            tmp.deleteOnExit();
            LOGGER.info("Copying {} to following temporary file : {}", authorizedPublicKeys, tmp.getAbsolutePath());
            return tmp;
        }
    }

    @RequiredArgsConstructor
    public static class SshServerLifecycle {

        private final SshServer sshServer;

        private final SshShellProperties properties;

        /**
         * Start ssh server
         *
         * @throws IOException in case of error
         */
        @PostConstruct
        public void startServer() throws IOException {
            sshServer.start();
            LOGGER.info("Ssh server started [{}:{}]", properties.getHost(), properties.getPort());
        }

        /**
         * Stop ssh server
         *
         * @throws IOException in case of error
         */
        @PreDestroy
        public void stopServer() throws IOException {
            sshServer.stop();
        }
    }
}

