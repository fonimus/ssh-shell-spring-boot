package com.github.fonimus.ssh.shell.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * Ssh authentication
 */
@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public class SshAuthentication {

    @NonNull
    private final Object principal;

    private Object details;

    private Object credentials;

    private List<String> authorities;
}
