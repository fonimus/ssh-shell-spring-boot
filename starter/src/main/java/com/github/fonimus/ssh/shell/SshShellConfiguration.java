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
import lombok.extern.slf4j.Slf4j;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.auth.pubkey.RejectAllPublickeyAuthenticator;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;

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
     * Start ssh server
     *
     * @throws IOException in case of error
     */
    @PostConstruct
    public void startServer() throws IOException {
        sshServer().start();
        LOGGER.info("Ssh server started [{}:{}]", properties.getHost(), properties.getPort());
    }

    /**
     * Stop ssh server
     *
     * @throws IOException in case of error
     */
    @PreDestroy
    public void stopServer() throws IOException {
        sshServer().stop();
    }

    /**
     * Construct ssh server thanks to ssh shell properties
     *
     * @return ssh server
     */
    @Bean
    public SshServer sshServer() {
        SshServer server = SshServer.setUpDefaultServer();
        server.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(properties.getHostKeyFile().toPath()));
        server.setHost(properties.getHost());
        server.setPasswordAuthenticator(passwordAuthenticator);
        server.setPublickeyAuthenticator(RejectAllPublickeyAuthenticator.INSTANCE);
        if (properties.getAuthorizedPublicKeysFile() != null) {
            if (properties.getAuthorizedPublicKeysFile().exists() && properties.getAuthorizedPublicKeysFile().canRead()) {
                server.setPublickeyAuthenticator(new SshShellPublicKeyAuthenticationProvider(properties.getAuthorizedPublicKeysFile()));
            } else {
                LOGGER.warn("Could not read authorized public keys file [{}], public key authentication is disabled.",
                        properties.getAuthorizedPublicKeysFile().getAbsolutePath());
            }
        }
        server.setPort(properties.getPort());
        server.setShellFactory(channelSession -> shellCommandFactory);
        server.setCommandFactory((channelSession, s) -> shellCommandFactory);
        return server;
    }

}
