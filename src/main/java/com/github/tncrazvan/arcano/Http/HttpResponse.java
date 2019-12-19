package com.github.tncrazvan.arcano.Http;

import com.github.tncrazvan.arcano.SharedObject;
import com.github.tncrazvan.arcano.Tool.Action;
import static com.github.tncrazvan.arcano.Tool.Http.ContentType.resolveContentType;
import static com.github.tncrazvan.arcano.Tool.JsonTools.jsonStringify;
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
public class HttpResponse {
    private final HttpHeaders headers;
    private Object content;
    private final Class<?> type;
    private boolean raw;
    private Action<Void> action = null;

    /**
     * Executes an action after the getContent(true) gets called.
     * @param action the action to execute.
     * @return the current HttpResponse.
     */
    public HttpResponse then(Action<Void> action){
        this.action = action;
        return this;
    }
    
    /**
     * Executes the specified action.
     */
    public void todo(){
        if(action != null)
            action.callback(null);
    }
    
    public HttpResponse(final HttpHeaders headers,final Object content){
        this((HashMap<String, String>) headers.getHashMap(), content);
    }
    
    public HttpResponse(final Object content){
        this(new HashMap<String,String>(){{}}, content);
    }
    
    public HttpResponse(final HttpHeaders headers){
        this((HashMap<String,String>)headers.getHashMap(), null);
    }
    
    public HttpResponse(final HashMap<String,String> headers){
        this(headers, null);
    }
    
    public HttpResponse(final HashMap<String,String> headers,final Object content) {
        raw = false;
        type = content.getClass();
        this.headers = new HttpHeaders(headers);
        this.content = content;
    }
    
    public void resolve(SharedObject so){
        try{
            if(type == JsonArray.class){
                if(headers != null && !headers.isDefined("Content-Type")){
                    headers.set("Content-Type", "application/json");
                }
                String tmp = ((JsonArray) content).toString();
                if(headers != null && !headers.isDefined("Content-Length")){
                    headers.set("Content-Length", String.valueOf(tmp.length()));
                }
                this.content = tmp;
            }else if(type == File.class || type == ServerFile.class){
                File file = (File) content;
                if(headers != null && !headers.isDefined("Content-Type")){
                    headers.set("Content-Type", resolveContentType(file.getName()));
                }
                if(headers != null && !headers.isDefined("Content-Length")){
                    headers.set("Content-Length", String.valueOf(file.length()));
                }
                FileInputStream fis = new FileInputStream(file);
                this.content = fis.readAllBytes();
                raw = true;
            }else if(type == String.class || type == Integer.class || type == Float.class
                        || type == Double.class || type == Boolean.class
                        || type == Byte.class || type == Character.class || type == Short.class
                        || type == Long.class){
                this.content = String.valueOf(content).getBytes(so.charset);
            }else if(type == byte[].class){
                this.content = content;
            }else {
                this.content = jsonStringify(content).getBytes(so.charset);
            }
        }catch(UnsupportedEncodingException ex){
            this.content = ex.getMessage().getBytes();
        } catch (FileNotFoundException ex) {
            try{
                this.content = ex.getMessage().getBytes(so.charset);
            }catch(UnsupportedEncodingException ex1){
                this.content = ex1.getMessage().getBytes();
            }
        } catch (IOException ex) {
            try{
                this.content = ex.getMessage().getBytes(so.charset);
            }catch(UnsupportedEncodingException ex1){
                this.content = ex1.getMessage().getBytes();
            }
        }
    }

    /**
     * Get the headers of this response as a HashMap.
     * @return 
     */
    public HashMap<String, String> getHashMapHeaders() {
        return headers.getHashMap();
    }
    
    /**
     * Get the HttpHeaders of this response.
     * @return 
     */
    public HttpHeaders getHttpHeaders(){
        return headers;
    }

    /**
     * Get the payload of the response and execute the action if so specified.
     * You can specify the action by calling "then(Action a)" on the current HttpResponse object.
     * @param todo if true the action will be executed other the action won't be executed.
     * @return the payload of the response.
     */
    public Object getContent(boolean todo) {
        if(todo)
            this.todo();
        return content;
    }
    /**
     * Get the payload of the response.
     * @return the payload of the response.
     */
    public Object getContent() {
        return getContent(false);
    }
    
    /**
     * Specifies wether or not the payload should be treated as raw binary data or not.
     * @param value if true the payload will be treated as raw binary data, otherwise it will be treated as a String.
     */
    public void setRaw(boolean value){
        raw = value;
    }
    
    /**
     * Check is the payload is meant to be treated as raw binary data or not.
     * @return true if the content is meant to be raw, false otherwise.
     */
    public boolean isRaw(){
        return raw;
    }
    
    /**
     * Get the type of the payload.
     * This is mainly an utility for the server.
     * It helps converting JSON objects, arrays, Strings and other objects accordingly.
     * @return the Class<?> of the payload.
     */
    public Class<?> getType(){
        return type;
    }
}
