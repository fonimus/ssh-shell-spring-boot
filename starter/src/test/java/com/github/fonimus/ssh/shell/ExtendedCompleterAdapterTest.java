package com.github.fonimus.ssh.shell;

import org.jline.reader.Candidate;
import org.jline.reader.impl.completer.ArgumentCompleter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.shell.CompletionProposal;
import org.springframework.shell.Shell;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ExtendedCompleterAdapterTest {

    private ExtendedCompleterAdapter adapter;
    private Shell shell;

    @BeforeEach
    void setUp() {
        shell = mock(Shell.class);
        adapter = new ExtendedCompleterAdapter(shell);
    }

    @Test
    void complete() {
        when(shell.complete(any())).thenReturn(Arrays.asList(
                new CompletionProposal("co"),
                new ExtendedCompletionProposal("ecp", false)
        ));
        List<Candidate> list = new ArrayList<>();

        adapter.complete(null, new ArgumentCompleter.ArgumentLine("test", 0), list);

        assertEquals(2, list.size());
    }
}
