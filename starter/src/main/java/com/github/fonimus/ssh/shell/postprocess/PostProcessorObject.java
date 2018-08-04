package com.github.fonimus.ssh.shell.postprocess;

import java.util.List;

/**
 * Post processor object
 */
public class PostProcessorObject {

	private String name;

	private List<String> parameters;

	public PostProcessorObject(String name, List<String> parameters) {
		this.name = name;
		this.parameters = parameters;
	}

	public String getName() {
		return name;
	}

	public List<String> getParameters() {
		return parameters;
	}
}
