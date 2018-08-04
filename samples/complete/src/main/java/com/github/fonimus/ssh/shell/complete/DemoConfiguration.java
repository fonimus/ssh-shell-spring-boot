package com.github.fonimus.ssh.shell.complete;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.fonimus.ssh.shell.postprocess.PostProcessor;

/**
 * Demo configuration
 */
@Configuration
public class DemoConfiguration {

	@Bean
	public PostProcessor quotePostProcessor() {
		return new PostProcessor<String>() {

			@Override
			public String getName() {
				return "quote";
			}

			@Override
			public String process(String result, List parameters) {
				return "'" + result + "'";
			}
		};
	}
}
