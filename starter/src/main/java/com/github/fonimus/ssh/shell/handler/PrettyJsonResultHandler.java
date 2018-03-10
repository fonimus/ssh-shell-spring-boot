package com.github.fonimus.ssh.shell.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.shell.result.TerminalAwareResultHandler;

import javax.validation.constraints.NotNull;

/**
 * Pretty json result handler
 */
public class PrettyJsonResultHandler extends TerminalAwareResultHandler<PrettyJson> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PrettyJsonResultHandler.class);

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    protected void doHandleResult(@NotNull PrettyJson result) {
        if (result != null) {
            terminal.writer().println(result.isPrettify() ? prettify(result.getObject()) : result.getObject());
        }
    }

    private Object prettify(Object object) {
        try {
            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            LOGGER.warn("Unable to prettify object: {}", object);
            return object;
        }
    }
}
