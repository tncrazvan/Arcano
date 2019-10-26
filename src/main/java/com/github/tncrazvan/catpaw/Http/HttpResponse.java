/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.tncrazvan.catpaw.Http;

import com.github.tncrazvan.catpaw.Common;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author Administrator
 */
public class HttpResponse {
    private HashMap<String,String> headers;
    private String content;

    public HttpResponse(HashMap<String,String> headers, String content) {
        this.headers = headers;
        this.content = content;
    }
    
    public HttpResponse(HashMap<String,String> headers, File file) throws FileNotFoundException, IOException {
        FileInputStream fis = new FileInputStream(file);
        if(headers != null && !headers.containsKey("Content-Type")){
            headers.put("Content-Type", Common.resolveContentType(file.getName()));
        }
        if(headers != null && !headers.containsKey("Content-Length")){
            headers.put("Content-Length", String.valueOf(file.length()));
        }
        
        this.headers = headers;
        this.content = new String(fis.readAllBytes());
    }

    public HashMap<String, String> getHeaders() {
        return headers;
    }

    public String getContent() {
        return content;
    }
}
