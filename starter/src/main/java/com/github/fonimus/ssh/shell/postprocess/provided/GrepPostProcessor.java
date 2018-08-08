package com.github.fonimus.ssh.shell.postprocess.provided;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.fonimus.ssh.shell.postprocess.PostProcessor;

/**
 * Grep post processor
 */
public class GrepPostProcessor
		implements PostProcessor<String> {

	private static final Logger LOGGER = LoggerFactory.getLogger(GrepPostProcessor.class);

	@Override
	public String getName() {
		return "grep";
	}

	@Override
	public String process(String result, List<String> parameters) {
		if (parameters == null || parameters.isEmpty()) {
			LOGGER.debug("Cannot use [{}] post processor without any parameters", getName());
			return result;
		} else {
			StringBuilder sb = new StringBuilder();
			for (String line : result.split("\n")) {
				if (contains(line, parameters)) {
					sb.append(line).append("\n");
				}
			}
			return sb.toString().isEmpty() ? sb.toString() : sb.toString().substring(0, sb.toString().length() - 1);
		}
	}

	private boolean contains(String line, List<String> parameters) {
		for (String parameter : parameters) {
			if (parameter == null || parameter.isEmpty() || line.contains(parameter)) {
				return true;
			}
		}
		return false;
	}

}
