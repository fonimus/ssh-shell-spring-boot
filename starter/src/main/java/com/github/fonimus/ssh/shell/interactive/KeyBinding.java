package com.github.fonimus.ssh.shell.interactive;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;

import java.util.List;

/**
 * Key binding bean
 */
@Builder
@Getter
public class KeyBinding {

    @NonNull
    private String description;

    @NonNull
    private KeyBindingInput input;

    @NonNull
    @Singular
    private List<String> keys;
}
