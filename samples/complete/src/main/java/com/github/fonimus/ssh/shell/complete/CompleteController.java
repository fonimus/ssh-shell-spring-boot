package com.github.fonimus.ssh.shell.complete;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CompleteController {

    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }
}
