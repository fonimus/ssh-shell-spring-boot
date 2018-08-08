package com.github.fonimus.ssh.shell.postprocess.provided;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.fonimus.ssh.shell.PromptColor;
import com.github.fonimus.ssh.shell.SshShellHelper;
import com.github.fonimus.ssh.shell.postprocess.PostProcessor;

/**
 * Grep post processor
 */
public class HighlightPostProcessor
		implements PostProcessor<String> {

	private static final Logger LOGGER = LoggerFactory.getLogger(HighlightPostProcessor.class);

	@Override
	public String getName() {
		return "highlight";
	}

	@Override
	public String process(String result, List<String> parameters) {
		if (parameters == null || parameters.isEmpty()) {
			LOGGER.debug("Cannot use [{}] post processor without any parameters", getName());
			return result;
		} else {
			String finalResult = result;
			for (String toHighlight : parameters) {
				finalResult = finalResult.replaceAll(toHighlight, SshShellHelper.getBackgroundColored(toHighlight, PromptColor.YELLOW));
			}
			return finalResult;
		}
	}

}
