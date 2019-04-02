package com.github.fonimus.ssh.shell.commands;

import com.github.fonimus.ssh.shell.PromptColor;
import com.github.fonimus.ssh.shell.SshShellHelper;
import org.springframework.shell.table.Aligner;

/**
 * Add this aligner to color cell
 */
public class ColorAligner implements Aligner {

    private SshShellHelper helper;
    private PromptColor color;

    /**
     * Default constructor
     *
     * @param color the cell text color
     */
    public ColorAligner(PromptColor color) {
        this.helper = new SshShellHelper();
        this.color = color;
    }

    @Override
    public String[] align(String[] text, int cellWidth, int cellHeight) {
        String[] result = new String[text.length];
        for (int i = 0; i < text.length; i++) {
            result[i] = helper.getColored(text[i], color);
        }
        return result;
    }
}