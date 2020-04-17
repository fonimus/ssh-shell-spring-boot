package com.github.fonimus.ssh.shell.postprocess.provided;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fonimus.ssh.shell.postprocess.PostProcessor;
import com.github.fonimus.ssh.shell.postprocess.PostProcessorException;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Pretty json post processor
 */
@Slf4j
public class PrettyJsonPostProcessor
        implements PostProcessor<Object> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public String getName() {
        return "pretty";
    }

    @Override
    public String process(Object result, List<String> parameters) throws PostProcessorException {
        try {
            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(result);
        } catch (JsonProcessingException e) {
            LOGGER.warn("Unable to prettify object: {}", result);
            throw new PostProcessorException("Unable to prettify object. " + e.getMessage(), e);
        }
    }
}
