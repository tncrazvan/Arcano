package com.github.tncrazvan.arcano.Http;

import com.github.tncrazvan.arcano.SharedObject;
import com.github.tncrazvan.arcano.EventManager;
import static com.github.tncrazvan.arcano.SharedObject.NAME_SESSION_ID;
import static com.github.tncrazvan.arcano.Tool.Encoding.Hashing.getSha1String;
import com.github.tncrazvan.arcano.Tool.System.Time;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author razvan
 */
public class HttpSession{
    private long time;
    private final String id;
    private final HashMap<String,Object> STORAGE = new HashMap<>();
    
    
    protected HttpSession(final EventManager e) {
        id = getSha1String(
                e.getClient().getInetAddress().toString() + "," + e.getClient().getPort() + "," + Math.random(),
                "UTF-8");
        final int expire = (int) Time.now(SharedObject.londonTimezone) + (int) e.reader.so.config.session.ttl;
        e.setResponseCookie(NAME_SESSION_ID, id, "/", null, expire);
        this.time = System.currentTimeMillis();
    }

    protected final long getTime() {
        return time;
    }

    protected final void setTime(final long time) {
        this.time = time;
    }

    public final String id() {
        return id;
    }

    public final Map<String, Object> storage() {
        return STORAGE;
    }

    /**
     * Set an object to the session and map it to a key.
     * 
     * @param key    the key of your session object.
     * @param object the object itself.
     */
    public final void set(final String key, final Object object) {
        STORAGE.put(key, object);
    }

    /**
     * Unset a session object.
     * 
     * @param key the key of the session object.
     */
    public final void unset(final String key) {
        STORAGE.remove(key);
    }

    /**
     * Check if a session object exists.
     * 
     * @param key the key of the object.
     * @return true if the session storage contains the object, false otherwise.
     */
    public final boolean isset(final String key) {
        return STORAGE.containsKey(key);
    }

    /**
     * Get an object from the session storage. Returns null if the key doesn't
     * exist. NOTE: a return null doesn't necessarily mean the key doesn't exist, it
     * could also mean that specific key is mapped to the value of null.
     * 
     * @param key the key of the object.
     * @return the object itself.
     */
    public final Object get(final String key) {
        return STORAGE.get(key);
    }
}
