package com.github.fonimus.ssh.shell.postprocess;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * Post processor object
 */
@RequiredArgsConstructor
@AllArgsConstructor
@Getter
public class PostProcessorObject {

    @NonNull
    private String name;

    private List<String> parameters;
}
