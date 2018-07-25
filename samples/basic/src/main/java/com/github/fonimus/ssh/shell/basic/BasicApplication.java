package com.github.fonimus.ssh.shell.basic;

import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * Basic application example
 */
@SpringBootApplication
public class BasicApplication {

	public static void main(String[] args) {
		new SpringApplicationBuilder(BasicApplication.class).bannerMode(Banner.Mode.OFF).run(args);
	}
}
