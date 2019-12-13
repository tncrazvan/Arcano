/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.tncrazvan.arcano.Tool;

import static com.github.tncrazvan.arcano.Common.charset;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.util.Map;

/**
 *
 * @author Administrator
 */
public class ServerFile extends File{

    public ServerFile(String filename) {
        super(filename);
    }


    public byte[] read() throws FileNotFoundException, IOException{
        FileInputStream fis = new FileInputStream(this);
        byte[] result = fis.readAllBytes();
        fis.close();
        return result;
    }

    public void write(String contents) throws UnsupportedEncodingException, IOException{
        write(contents.getBytes(charset));
    }

    public void write(byte[] contents) throws FileNotFoundException, IOException{
        FileOutputStream fos = new FileOutputStream(this);
        fos.write(contents);
        fos.close();
    }

    public Map<String,Object> info() throws IOException{
        return info("*");
    }
    public Map<String,Object> info(String selection) throws IOException{
         return Files.readAttributes(this.toPath(), selection);
    }
}