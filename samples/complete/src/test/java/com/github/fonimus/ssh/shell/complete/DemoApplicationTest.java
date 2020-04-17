package com.github.fonimus.ssh.shell.complete;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, properties = {"ssh.shell.port=2346"})
@ExtendWith(SpringExtension.class)
@DirtiesContext
public class DemoApplicationTest
        extends AbstractDemoApplicationTest {

}
