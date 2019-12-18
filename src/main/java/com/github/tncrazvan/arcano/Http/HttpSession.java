package com.github.tncrazvan.arcano.Http;

import com.github.tncrazvan.arcano.SharedObject;
import com.github.tncrazvan.arcano.EventManager;
import static com.github.tncrazvan.arcano.Tool.Hashing.getSha1String;
import com.github.tncrazvan.arcano.Tool.Time;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author razvan
 */
public class HttpSession extends SharedObject{
    private long time;
    private final String id;
    private final HashMap<String,Object> STORAGE = new HashMap<>();
    
    
    protected HttpSession(EventManager e) {
        id = getSha1String(e.getClient().getInetAddress().toString()+","+e.getClient().getPort()+","+Math.random(),charset);
        e.setCookie("sessionId", id, "/", null, (int) Time.toTimestamp(Time.now()) + (int) e.so.sessionTtl);
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
