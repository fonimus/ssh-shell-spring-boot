package com.github.fonimus.ssh.shell.postprocess;

/**
 * Post processor exception
 */
public class PostProcessorException
		extends Exception {

	public PostProcessorException(String message) {
		super(message);
	}

	public PostProcessorException(String message, Throwable cause) {
		super(message, cause);
	}
}
