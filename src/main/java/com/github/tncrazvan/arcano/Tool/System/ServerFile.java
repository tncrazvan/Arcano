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

    public ServerFile(File parent, File file){
        super(parent,file.getAbsolutePath());
    }
    
    public ServerFile(File file){
        super(file.getAbsolutePath());
    }
    
    public ServerFile(URI filename) {
        super(filename);
    }
    
    public ServerFile(String filename) {
        super(filename);
    }
    
    public ServerFile(String parent,String filename) {
        super(parent,filename);
    }

    public ServerFile(File parent,String filename) {
        super(parent,filename);
    }

    public byte[] read() throws FileNotFoundException, IOException{
        byte[] result;
        try (FileInputStream fis = new FileInputStream(this)) {
            result = fis.readAllBytes();
        }
        return result;
    }

    public void write(String contents,String charset) throws UnsupportedEncodingException, IOException{
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