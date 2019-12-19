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
        int expire = (int) Time.toTimestamp(Time.now()) + (int) e.so.sessionTtl;
        e.setCookie("sessionId", id, "/", null, expire);
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
    
    /**
     * Set an object to the session and map it to a key.
     * @param key the key of your session object.
     * @param object the object itself.
     */
    public void set(String key, Object object){
        STORAGE.put(key, object);
    }
    
    /**
     * Unset a session object.
     * @param key the key of the session object.
     */
    public void unset(String key){
        STORAGE.remove(key);
    }
    
    /**
     * Check if a session object exists.
     * @param key the key of the object.
     * @return true if the session storage contains the object, false otherwise.
     */
    public boolean isset(String key){
        return STORAGE.containsKey(key);
    }
    
    /**
     * Get an object from the session storage.
     * Returns null if the key doesn't exist.
     * NOTE: a return null doesn't necessarily mean the key doesn't exist, it could also mean that specific key is mapped to the value of null.
     * @param key the key of the object.
     * @return the object itself.
     */
    public Object get(String key){
        return STORAGE.get(key);
    }
}
