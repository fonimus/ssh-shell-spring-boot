package com.github.fonimus.ssh.shell.postprocess.provided;

import com.github.fonimus.ssh.shell.postprocess.PostProcessor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Grep post processor
 */
@Slf4j
public class GrepPostProcessor
		implements PostProcessor<String> {

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
