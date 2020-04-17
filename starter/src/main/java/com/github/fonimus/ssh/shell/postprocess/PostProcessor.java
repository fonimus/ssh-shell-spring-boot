package com.github.fonimus.ssh.shell.postprocess;

import java.util.List;

/**
 * Post processor interface
 */
public interface PostProcessor<T> {

    String getName();

    String process(T result, List<String> parameters) throws PostProcessorException;
}
