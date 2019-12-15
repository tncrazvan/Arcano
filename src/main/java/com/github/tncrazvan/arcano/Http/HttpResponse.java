package com.github.tncrazvan.arcano.Http;

import com.github.tncrazvan.arcano.SharedObject;
import com.github.tncrazvan.arcano.Tool.Action;
import static com.github.tncrazvan.arcano.Tool.Http.ContentType.resolveContentType;
import com.github.tncrazvan.arcano.Tool.JsonTools;
import com.github.tncrazvan.arcano.Tool.ServerFile;
import com.google.gson.JsonArray;
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
public class HttpResponse extends SharedObject implements JsonTools{
    private final HashMap<String,String> headers;
    private Object content;
    private final Class<?> type;
    private boolean raw;
    private Action<Void> action = null;

    public HttpResponse then(Action<Void> action){
        this.action = action;
        return this;
    }
    
    public void todo(){
        if(action != null)
            action.callback(null);
    }
    
    public HttpResponse(HashMap<String,String> headers, Object content) {
        raw = false;
        type = content.getClass();
        try{
            if(type == JsonArray.class){
                if(headers != null && !headers.containsKey("Content-Type")){
                    headers.put("Content-Type", "application/json");
                }
                String tmp = ((JsonArray) content).toString();
                if(headers != null && !headers.containsKey("Content-Length")){
                    headers.put("Content-Length", String.valueOf(tmp.length()));
                }
                this.content = tmp;
            }else if(type == File.class || type == ServerFile.class){
                File file = (File) content;
                if(headers != null && !headers.containsKey("Content-Type")){
                    headers.put("Content-Type", resolveContentType(file.getName()));
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
            }else if(type == byte[].class){
                this.content = content;
            }else {
                this.content = jsonStringify(content).getBytes(charset);
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

    public Object getContent(boolean todo) {
        if(todo)
            this.todo();
        return content;
    }
    
    public Object getContent() {
        return getContent(false);
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
