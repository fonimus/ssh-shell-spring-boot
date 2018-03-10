package com.github.fonimus.ssh.shell.sample;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(properties = {"ssh.shell.port=2345"})
@ExtendWith(SpringExtension.class)
@DirtiesContext
public class DemoApplicationWebEnvTest extends AbstractDemoApplicationTest {
}
