package com.github.fonimus.ssh.shell.interactive;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.Singular;
import org.jline.terminal.Size;

import java.util.List;

/**
 * Interactive bean
 */
@Data
@Builder
public class Interactive {

    @NonNull
    private InteractiveInput input;

    @Builder.Default
    private long refreshDelay = 3000;

    @Builder.Default
    private boolean fullScreen = true;

    @Builder.Default
    private boolean exit = true;

    @Builder.Default
    private boolean increase = true;

    @Builder.Default
    private boolean decrease = true;

    @Singular
    private List<KeyBinding> bindings;

    private Size size;
}
