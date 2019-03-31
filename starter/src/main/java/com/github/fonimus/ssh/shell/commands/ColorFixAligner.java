package com.github.fonimus.ssh.shell.commands;

import org.springframework.shell.table.Aligner;

/**
 * Add this aligner in case of colored table case
 * <p>Because align implementations remove colors ! (trim())</p>
 */
public class ColorFixAligner implements Aligner {

    @Override
    public String[] align(String[] text, int cellWidth, int cellHeight) {
        String[] newText = new String[text.length];
        int i1 = 0;
        for (String s : text) {
            newText[i1] = "\u001B" + s + "        ";
            i1++;
        }
        return newText;
    }
}