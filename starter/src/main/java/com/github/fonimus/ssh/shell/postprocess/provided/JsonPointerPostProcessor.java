package com.github.fonimus.ssh.shell.postprocess.provided;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fonimus.ssh.shell.postprocess.PostProcessor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;

/**
 * Json pointer post processor
 */
@Slf4j
public class JsonPointerPostProcessor
        implements PostProcessor<String> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public String getName() {
        return "json";
    }

    @Override
    public String process(String result, List<String> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            LOGGER.debug("Cannot use [{}] post processor without any parameters", getName());
        } else {
            if (parameters.size() != 1) {
                LOGGER.debug("[{}] post processor only need one parameter, rest will be ignored", getName());
            }
            String path = parameters.get(0);
            try {
                JsonNode node = MAPPER.readTree(result).at(path);
                if (node.isMissingNode()) {
                    return "No node found with json path expression: " + path;
                } else {
                    if (node.isTextual()) {
                        return node.asText();
                    } else {
                        return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(node);
                    }
                }
            } catch (IOException e) {
                LOGGER.warn("Unable to read tree", e);
            } catch (IllegalArgumentException e) {
                LOGGER.warn("Illegal argument: " + path, e);
                return e.getMessage();
            }
        }
        return result;
    }
}
