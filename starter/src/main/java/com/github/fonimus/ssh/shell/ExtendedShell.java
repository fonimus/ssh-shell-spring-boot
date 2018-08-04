package com.github.fonimus.ssh.shell;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.shell.Input;
import org.springframework.shell.ResultHandler;
import org.springframework.shell.Shell;

import com.github.fonimus.ssh.shell.postprocess.PostProcessorObject;
import com.github.fonimus.ssh.shell.postprocess.provided.SavePostProcessor;

import static com.github.fonimus.ssh.shell.SshShellCommandFactory.SSH_THREAD_CONTEXT;

public class ExtendedShell
		extends Shell {

	public static final String PIPE = "|";

	public static final String ARROW = ">";

	private static final Logger LOGGER = LoggerFactory.getLogger(ExtendedShell.class);

	private static final List<String> KEY_CHARS = Arrays.asList(PIPE, ARROW);

	public ExtendedShell(ResultHandler resultHandler) {
		super(resultHandler);
	}

	private static boolean isKeyCharInLine(String str) {
		for (String key : KEY_CHARS) {
			if (str.contains(key)) {
				return true;
			}
		}
		return false;
	}

	private static boolean isKeyCharInList(List<String> strList) {
		for (String key : KEY_CHARS) {
			if (strList.contains(key)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Object evaluate(Input input) {
		List<String> words = input.words();
		String raw = input.rawText();
		Input newInput = () -> raw != null && isKeyCharInLine(raw) ? raw.substring(0, firstIndexOfKeyChar(raw)) : raw;
		Object toReturn = super.evaluate(newInput);
		if (isKeyCharInList(words)) {
			List<Integer> indexes = IntStream.range(0, words.size()).filter(i -> KEY_CHARS.contains(words.get(i))).boxed().collect(Collectors.toList());
			List<PostProcessorObject> postProcessors = new ArrayList<>();
			for (Integer index : indexes) {
				if (words.size() > index + 1) {
					String keyChar = words.get(index);
					if (keyChar.equals(PIPE)) {
						String postProcessorName = words.get(index + 1);
						int currentIndex = 2;
						String word = words.size() > index + currentIndex ? words.get(index + currentIndex) : null;
						List<String> params = new ArrayList<>();
						while (word != null && !KEY_CHARS.contains(word)) {
							params.add(word);
							currentIndex++;
							word = words.size() > index + currentIndex ? words.get(index + currentIndex) : null;
						}
						postProcessors.add(new PostProcessorObject(postProcessorName, params));
					} else if (keyChar.equals(ARROW)) {
						postProcessors.add(new PostProcessorObject(SavePostProcessor.SAVE, Collections.singletonList(words.get(index + 1))));
					}
				}
			}
			LOGGER.debug("Found {} post processors", postProcessors.size());
			SshContext ctx = SSH_THREAD_CONTEXT.get();
			if (ctx != null) {
				ctx.setPostProcessorsList(postProcessors);
			}
		}
		return toReturn;
	}

	private int firstIndexOfKeyChar(String str) {
		int firstIndex = Integer.MAX_VALUE;
		for (String key : KEY_CHARS) {
			int keyIndex = str.indexOf(key);
			if (keyIndex > -1 && keyIndex < firstIndex) {
				firstIndex = keyIndex;
			}
		}
		return firstIndex;
	}
}