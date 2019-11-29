package com.github.tncrazvan.arcano.Tool.JavaScript.FileSystem;

import static com.github.tncrazvan.arcano.Common.charset;
import static com.github.tncrazvan.arcano.Common.logger;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 *
 * @author Administrator
 */
public interface LoaderJSZip {
    public class JSZip{
        private final String filename;
        private LoaderJSFile.JSFile file;
        private final ArrayList<ZipEntryData> entries;
        public JSZip(String filename) throws FileNotFoundException {
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
        
        public void addEntry(String filename, String contents) throws IOException{
            addEntry(filename, contents.getBytes(charset));
        }
        
        public void addEntry(String filename, LoaderJSFile.JSFile file) throws IOException{
            addEntry(filename, file.read());
        }
        public void addEntry(String filename, byte[] data) throws IOException{
            ZipEntry e = new ZipEntry(filename);
            entries.add(new ZipEntryData(e, data));
        }
        
        public void make() throws IOException{
            file = new LoaderJSFile.JSFile(filename);
            try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(file))) {
                entries.forEach((e) -> {
                    try {
                        out.putNextEntry(e.entry);
                        out.write(e.data, 0, e.data.length);
                        out.closeEntry();
                    } catch (IOException ex) {
                        logger.log(Level.SEVERE, null, ex);
                    }
                });
            }
        }
        
        public LoaderJSFile.JSFile getFile(){
            return file;
        }
    }
}
