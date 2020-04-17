package com.github.fonimus.ssh.shell.providers;

import org.springframework.core.MethodParameter;
import org.springframework.shell.CompletionContext;
import org.springframework.shell.CompletionProposal;
import org.springframework.shell.standard.ValueProviderSupport;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Fixed file value provider (mostly for windows)
 */
public class AnyOsFileValueProvider extends ValueProviderSupport {

    private boolean replaceAll;

    public AnyOsFileValueProvider(boolean replaceAll) {
        this.replaceAll = replaceAll;
    }

    @Override
    public boolean supports(MethodParameter parameter, CompletionContext completionContext) {
        if (replaceAll) {
            return parameter.getParameterType().equals(File.class);
        }
        return super.supports(parameter, completionContext);
    }

    @Override
    public List<CompletionProposal> complete(MethodParameter parameter, CompletionContext completionContext,
                                             String[] hints) {
        String input = completionContext.currentWordUpToCursor();
        int lastSlash = input.lastIndexOf("/");
        File currentDir = lastSlash > -1 ? new File(input.substring(0, lastSlash + 1)) : new File("./");
        String prefix = input.substring(lastSlash + 1);

        File[] files = currentDir.listFiles((dir, name) -> name.startsWith(prefix));
        if (files == null || files.length == 0) {
            return Collections.emptyList();
        }
        return Arrays.stream(files)
                .map(f -> new CompletionProposal(f.getPath().replaceAll("\\\\", "/")))
                .collect(Collectors.toList());
    }
}
