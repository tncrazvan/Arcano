package com.github.tncrazvan.arcano.WebSocket;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author razvan
 */
public class WebSocketGroupManager {
    private final Map<String,WebSocketGroup> groups = new HashMap<>();
    
    public void addGroup(WebSocketGroup group){
        groups.put(group.getKey(), group);
    }
    
    public WebSocketGroup removeGroup(WebSocketGroup group){
        return groups.remove(group.getKey());
    }
    
    public boolean groupExists(String key){
        return groups.containsKey(key);
    }
    
    public WebSocketGroup getGroup(String key){
        return groups.get(key);
    }
    
    public Map<String,WebSocketGroup> getAllGroups(){
        return groups;
    }
}
