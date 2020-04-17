/*
 * Copyright (c) Worldline 2019.
 */

package com.github.fonimus.ssh.shell;

import lombok.Getter;
import lombok.Setter;
import org.apache.sshd.server.ExitCallback;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Ssh io
 */
@Getter
@Setter
public class SshIO {

    private InputStream is;

    private OutputStream os;

    private ExitCallback ec;
}
