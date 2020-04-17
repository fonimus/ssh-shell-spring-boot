package com.github.fonimus.ssh.shell.postprocess.provided;

import com.github.fonimus.ssh.shell.postprocess.PostProcessor;
import com.github.fonimus.ssh.shell.postprocess.PostProcessorException;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

@Slf4j
public class SavePostProcessor
        implements PostProcessor<String> {

    public static final String SAVE = "save";

    @Override
    public String getName() {
        return SAVE;
    }

    @Override
    public String process(String result, List<String> parameters) throws PostProcessorException {
        if (parameters == null || parameters.isEmpty()) {
            throw new PostProcessorException("Cannot save without file path !");
        } else {
            if (parameters.size() != 1) {
                LOGGER.debug("[{}] post processor only need one parameter, rest will be ignored", getName());
            }
            String path = parameters.get(0);
            if (path == null || path.isEmpty()) {
                throw new PostProcessorException("Cannot save without file path !");
            }
            File file = new File(path);
            try {
                if (file.exists()) {
                    throw new PostProcessorException("File already exists: " + file.getAbsolutePath());
                }
                String toWrite = result;
                toWrite = toWrite.replaceAll("(\\x1b\\x5b|\\x9b)[\\x30-\\x3f]*[\\x20-\\x2f]*[\\x40-\\x7e]", "");
                Files.write(file.toPath(), toWrite.getBytes(StandardCharsets.UTF_8));
                return "Result saved to file: " + file.getAbsolutePath();
            } catch (IOException e) {
                LOGGER.debug("Unable to write to file: " + file.getAbsolutePath(), e);
                throw new PostProcessorException("Unable to write to file: " + file.getAbsolutePath() + ". " + e.getMessage(), e);
            }
        }
    }
}
