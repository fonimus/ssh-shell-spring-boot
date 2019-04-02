package com.github.fonimus.ssh.shell.interactive;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.Singular;

import java.util.List;

/**
 * Key binding bean
 */
@Data
@Builder
public class KeyBinding {

    @NonNull
    private String id;

    @NonNull
    private KeyBindingInput input;

    @NonNull
    @Singular
    private List<String> keys;
}
