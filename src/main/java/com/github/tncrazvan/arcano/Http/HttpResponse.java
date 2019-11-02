/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.tncrazvan.arcano.Http;

import com.github.tncrazvan.arcano.Common;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

/**
 *
 * @author Administrator
 */
public class HttpResponse extends Common{
    private final HashMap<String,String> headers;
    private Object content;
    private final Class<?> type;
    private boolean raw;

    public HttpResponse(HashMap<String,String> headers, Object content) {
        raw = false;
        type = content.getClass();
        try{
            if(type == File.class){
                File file = (File) content;
                if(headers != null && !headers.containsKey("Content-Type")){
                    headers.put("Content-Type", Common.resolveContentType(file.getName()));
                }
                if(headers != null && !headers.containsKey("Content-Length")){
                    headers.put("Content-Length", String.valueOf(file.length()));
                }
                FileInputStream fis = new FileInputStream(file);
                this.content = fis.readAllBytes();
                raw = true;
            }else if(type == String.class || type == Integer.class || type == Float.class
                        || type == Double.class || type == Boolean.class
                        || type == Byte.class || type == Character.class || type == Short.class
                        || type == Long.class){
                this.content = String.valueOf(content).getBytes(charset);
            }else{
                this.content = jsonEncode(content).getBytes(charset);
            }
        }catch(UnsupportedEncodingException ex){
            this.content = ex.getMessage().getBytes();
        } catch (FileNotFoundException ex) {
            try{
                this.content = ex.getMessage().getBytes(charset);
            }catch(UnsupportedEncodingException ex1){
                this.content = ex1.getMessage().getBytes();
            }
        } catch (IOException ex) {
            try{
                this.content = ex.getMessage().getBytes(charset);
            }catch(UnsupportedEncodingException ex1){
                this.content = ex1.getMessage().getBytes();
            }
        }
        
        
        this.headers = headers;
    }

    public HashMap<String, String> getHeaders() {
        return headers;
    }

    public Object getContent() {
        return content;
    }
    
    public void setRaw(boolean value){
        raw = value;
    }
    
    public boolean isRaw(){
        return raw;
    }
    
    public Class<?> getType(){
        return type;
    }
}
