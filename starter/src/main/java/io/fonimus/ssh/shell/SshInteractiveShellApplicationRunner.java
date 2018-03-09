package io.fonimus.ssh.shell;

import org.jline.reader.LineReader;
import org.jline.reader.Parser;
import org.springframework.boot.ApplicationArguments;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.shell.Shell;
import org.springframework.shell.jline.InteractiveShellApplicationRunner;
import org.springframework.shell.jline.PromptProvider;
import org.springframework.stereotype.Component;

/**
 * Interactive shell which always responds 'true' for {@link InteractiveShellApplicationRunner#isEnabled()}
 */
@Component
@Primary
public class SshInteractiveShellApplicationRunner
		extends InteractiveShellApplicationRunner {

	public SshInteractiveShellApplicationRunner(LineReader lineReader, PromptProvider promptProvider,
			Parser parser, Shell shell, Environment environment) {
		super(lineReader, promptProvider, parser, shell, environment);
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void run(ApplicationArguments args) {
		// do nothing
	}
}
