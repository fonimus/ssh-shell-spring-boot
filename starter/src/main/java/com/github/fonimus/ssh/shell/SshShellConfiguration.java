package com.github.fonimus.ssh.shell;

import java.io.IOException;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.pubkey.RejectAllPublickeyAuthenticator;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Ssh shell configuration
 */
@Configuration
public class SshShellConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(SshShellConfiguration.class);

    private SshShellProperties properties;

    private SshShellCommandFactory shellCommandFactory;

    public SshShellConfiguration(SshShellProperties properties,
                                 SshShellCommandFactory shellCommandFactory) {
        this.properties = properties;
        this.shellCommandFactory = shellCommandFactory;
    }

    /**
     * Start ssh server
     *
     * @throws IOException in case of error
     */
    @PostConstruct
    public void startServer() throws IOException {
        sshServer().start();
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
        server.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(properties.getHostKeyFile()));
        server.setPublickeyAuthenticator(RejectAllPublickeyAuthenticator.INSTANCE);
        server.setHost(properties.getHost());
        String password = properties.getPassword();
        if (password == null) {
            password = UUID.randomUUID().toString();
            LOGGER.info(" --- Generating password for ssh connection: {}", password);
        }
        final String finalPassword = password;
        server.setPasswordAuthenticator((username, pass, serverSession) -> "user".equals(username) && pass.equals(finalPassword));
        server.setPort(properties.getPort());
        server.setShellFactory(() -> shellCommandFactory);
        server.setCommandFactory(command -> shellCommandFactory);
        return server;
    }

}
