package io.fonimus.ssh.shell;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.jline.PromptProvider;
import org.springframework.stereotype.Component;

@Component
public class SshPromptProvider
		implements PromptProvider {

	@Autowired
	private SshShellProperties properties;

	@Override
	public AttributedString getPrompt() {
		return new AttributedString(properties.getPrompt().getText(), AttributedStyle.DEFAULT.foreground(properties.getPrompt().getColor().getValue()));
	}
}
