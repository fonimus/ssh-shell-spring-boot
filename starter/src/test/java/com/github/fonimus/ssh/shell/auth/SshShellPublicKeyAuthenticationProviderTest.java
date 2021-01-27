package com.github.fonimus.ssh.shell.auth;

import org.apache.sshd.common.io.IoSession;
import org.apache.sshd.server.session.ServerSession;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SshShellPublicKeyAuthenticationProviderTest {

    private static PublicKey pub;
    private static PublicKey wrongPub;

    private SshShellPublicKeyAuthenticationProvider pubKeyAuthProv;

    @BeforeAll
    public static void init() throws Exception {
        KeyFactory kf = KeyFactory.getInstance("RSA");
        pub = kf.generatePublic(new X509EncodedKeySpec(Files.readAllBytes(Paths.get("src/test/resources/.ssh/pub.der"))));
        wrongPub = kf.generatePublic(new X509EncodedKeySpec(Files.readAllBytes(Paths.get("src/test/resources/.ssh/wrong_pub.der"))));
    }

    @Test
    public void testFile() throws Exception {
        File file = new File("src/test/resources/.ssh/authorized.keys");
        assertTrue(file.exists());
        internalTest(file);
    }

    @Test
    public void testSpringFileResource() throws Exception {
        FileSystemResource resource = new FileSystemResource("src/test/resources/.ssh/authorized.keys");
        assertTrue(resource.exists());
        internalTest(resource.getFile());
    }

    @Test
    public void testSpringClasspathResource() throws Exception {
        ClassPathResource resource = new ClassPathResource(".ssh/authorized.keys");
        assertTrue(resource.exists());
        internalTest(resource.getFile());
    }

    @Test
    public void testNotExisting() throws Exception {
        pubKeyAuthProv = new SshShellPublicKeyAuthenticationProvider(new File("not-existing"));
        assertFalse(pubKeyAuthProv.exists());
        assertEquals(-1, pubKeyAuthProv.size());
    }

    private void internalTest(File file) throws Exception {
        pubKeyAuthProv = new SshShellPublicKeyAuthenticationProvider(file);
        assertTrue(pubKeyAuthProv.exists());
        ServerSession session = mock(ServerSession.class);
        IoSession io = mock(IoSession.class);
        when(session.getIoSession()).thenReturn(io);
        assertTrue(pubKeyAuthProv.authenticate("user", pub, session));
        assertFalse(pubKeyAuthProv.authenticate("user", wrongPub, session));
    }

}