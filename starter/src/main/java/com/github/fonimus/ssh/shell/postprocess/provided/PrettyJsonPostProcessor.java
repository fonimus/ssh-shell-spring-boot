package com.github.fonimus.ssh.shell.postprocess.provided;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fonimus.ssh.shell.postprocess.PostProcessor;
import com.github.fonimus.ssh.shell.postprocess.PostProcessorException;

/**
 * Pretty json post processor
 */
public class PrettyJsonPostProcessor
		implements PostProcessor<Object> {

	private static final ObjectMapper MAPPER = new ObjectMapper();

	private static final Logger LOGGER = LoggerFactory.getLogger(PrettyJsonPostProcessor.class);

	@Override
	public String getName() {
		return "pretty";
	}

	@Override
	public String process(Object result, List<String> parameters) throws PostProcessorException {
		try {
			return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(result);
		} catch (JsonProcessingException e) {
			LOGGER.warn("Unable to prettify object: {}", result);
			throw new PostProcessorException("Unable to prettify object. " + e.getMessage(), e);
		}
	}
}
