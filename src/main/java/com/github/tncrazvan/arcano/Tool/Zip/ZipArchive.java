/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.tncrazvan.arcano.Tool.Zip;

import com.github.tncrazvan.arcano.Tool.System.ServerFile;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import static com.github.tncrazvan.arcano.SharedObject.LOGGER;

/**
 *
 * @author Administrator
 */
 public class ZipArchive{
    private final String filename;
    private ServerFile file;
    private final ArrayList<ZipEntryData> entries;
    
    public ZipArchive(String filename) throws FileNotFoundException {
        this.filename = filename;
        entries = new ArrayList<>();
    }

    private class ZipEntryData{
        public ZipEntry entry;
        public byte[] data;

        public ZipEntryData(ZipEntry entry, byte[] data) {
            this.entry = entry;
            this.data = data;
        }

    }

    public void addEntry(String filename, String contents, String charset) throws IOException{
        addEntry(filename, contents.getBytes(charset));
    }

    public void addEntry(String filename, ServerFile file) throws IOException{
        addEntry(filename, file.read());
    }
    public void addEntry(String filename, byte[] data) throws IOException{
        ZipEntry e = new ZipEntry(filename);
        entries.add(new ZipEntryData(e, data));
    }

    public void make() throws IOException{
        file = new ServerFile(filename);
        try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(file))) {
            entries.forEach((e) -> {
                try {
                    out.putNextEntry(e.entry);
                    out.write(e.data, 0, e.data.length);
                    out.closeEntry();
                } catch (IOException ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
                }
            });
        }
    }

    public ServerFile getFile(){
        return file;
    }
}