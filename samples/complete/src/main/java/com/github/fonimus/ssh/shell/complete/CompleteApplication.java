package com.github.fonimus.ssh.shell.complete;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Complete application example
 */
@SpringBootApplication
@EnableScheduling
public class CompleteApplication {

	public static void main(String[] args) {
		SpringApplication.run(CompleteApplication.class, args);
	}
}
