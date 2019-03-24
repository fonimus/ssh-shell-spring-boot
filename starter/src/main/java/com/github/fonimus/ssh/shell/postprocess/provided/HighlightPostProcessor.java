package com.github.fonimus.ssh.shell.postprocess.provided;

import com.github.fonimus.ssh.shell.PromptColor;
import com.github.fonimus.ssh.shell.SshShellHelper;
import com.github.fonimus.ssh.shell.postprocess.PostProcessor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Grep post processor
 */
@Slf4j
public class HighlightPostProcessor
		implements PostProcessor<String> {

	private static final SshShellHelper HELPER = new SshShellHelper();

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
				finalResult = finalResult.replaceAll(toHighlight, HELPER.getBackgroundColored(toHighlight, PromptColor.YELLOW));
			}
			return finalResult;
		}
	}

}
