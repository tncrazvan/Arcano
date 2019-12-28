/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.tncrazvan.arcano.Tool.System;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.file.Files;
import java.util.Map;

/**
 *
 * @author Administrator
 */
public class ServerFile extends File{
    private static final long serialVersionUID = 4567989494529454756L;

    public ServerFile(final File parent, final File file) {
        super(parent, file.getAbsolutePath());
    }

    public ServerFile(final File file) {
        super(file.getAbsolutePath());
    }

    public ServerFile(final URI filename) {
        super(filename);
    }

    public ServerFile(final String filename) {
        super(filename);
    }

    public ServerFile(final String parent, final String filename) {
        super(parent, filename);
    }

    public ServerFile(final File parent, final String filename) {
        super(parent, filename);
    }

    public byte[] read() throws FileNotFoundException, IOException {
        byte[] result;
        try (FileInputStream fis = new FileInputStream(this)) {
            result = fis.readAllBytes();
        }
        return result;
    }

    public void write(final String contents, final String charset) throws UnsupportedEncodingException, IOException {
        write(contents.getBytes(charset));
    }

    public void write(final byte[] contents) throws FileNotFoundException, IOException {
        final FileOutputStream fos = new FileOutputStream(this);
        fos.write(contents);
        fos.close();
    }

    public Map<String, Object> info() throws IOException {
        return info("*");
    }

    public Map<String, Object> info(final String selection) throws IOException {
         return Files.readAttributes(this.toPath(), selection);
    }
}