package com.github.tncrazvan.arcano.Http;

import com.github.tncrazvan.arcano.Common;
import com.github.tncrazvan.arcano.EventManager;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author razvan
 */
public class HttpSession extends Common{
    public static final Map<String,HttpSession> LIST = new HashMap<>();
    private long time;
    private final String id;
    private final Map<String,Object> STORAGE = new HashMap<>();
    
    
    protected HttpSession(EventManager e) throws UnsupportedEncodingException {
        id = getSha1String(e.getClient().getInetAddress().toString()+","+e.getClient().getPort()+","+Math.random());
        e.setCookie("sessionId", id, "/");
        this.time = System.currentTimeMillis();
    }
    
    protected long getTime(){
        return time;
    }
    
    protected void setTime(long time){
        this.time=time;
    }
    
    public String id(){
        return id;
    }
    
    public Map<String,Object> storage(){
        return STORAGE;
    }
    public void set(String key, Object o){
        STORAGE.put(key, o);
    }
    public void unset(String key){
        STORAGE.remove(key);
    }
    
    public boolean isset(String key){
        return STORAGE.containsKey(key);
    }
    
    public Object get(String key){
        return STORAGE.get(key);
    }
}
