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
		} else {
			if (parameters.size() != 1) {
				LOGGER.debug("[{}] post processor only need one parameter, rest will be ignored", getName());
			}
			String toFind = parameters.get(0);
			if (toFind != null && !toFind.isEmpty()) {
				StringBuilder sb = new StringBuilder();
				int i = 1;
				for (String s : result.split("\n")) {
					if (s.contains(toFind)) {
						sb.append(i).append(". ").append(s).append("\n");
					}
					i++;
				}
				return sb.toString().substring(0, sb.toString().length() - 1);
			}
		}
		return result;
	}

}
