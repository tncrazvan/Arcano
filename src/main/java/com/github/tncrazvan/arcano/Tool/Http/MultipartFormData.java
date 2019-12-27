/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.tncrazvan.arcano.Tool.Http;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Administrator
 */
public interface MultipartFormData {
    /**
     * Reads and input string and returns a Map<String, String> 
     * that contains the multipart form data.
     * 
     * @param content the raw characters to read.
     * @return a map containing every pair of key and value of the multipart form data. Both key and value are String.
     */
    public static Map<String,String> readAsMultipartFormData(String content){
        Map<String,String> object = new HashMap<>();
        
        String[] lines = content.split("\r\n");
        String currentLabel = null,
                currentValue = "";
        Pattern pattern1 = Pattern.compile("^Content-Disposition");
        Pattern pattern2 = Pattern.compile("(?<=name\\=\\\").*?(?=\\\")");
        Matcher matcher;
        boolean next = false, skippedBlank = false;
        for(int i = 0; i<lines.length; i++){
            matcher = pattern1.matcher(lines[i]);
            if(matcher.find()){
                matcher = pattern2.matcher(lines[i]);
                if(matcher.find() && currentLabel == null){
                    currentLabel = matcher.group();
                    i +=2;
                    currentValue = lines[i];
                    object.put(currentLabel, currentValue);
                    currentLabel = null;
                }
            }
        }
        
        return object;
    }
    
    static char[] MULTIPART_CHARS =
             "-_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
                  .toCharArray();
    
    /**
     * Generates a unique string that can be used to define a multipart form data boundary.
     * @return a unique multipart form data boundary.
     */
    public static String generateMultipartBoundary() {
        StringBuilder buffer = new StringBuilder();
        Random rand = new Random();
        int count = rand.nextInt(11) + 30; // a random size from 30 to 40
        for (int i = 0; i < count; i++) {
           buffer.append(MULTIPART_CHARS[rand.nextInt(MULTIPART_CHARS.length)]);
        }
        return buffer.toString();
    }
}
