package com.github.tncrazvan.arcano.WebSocket;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author razvan
 */
public class WebSocketGroupManager {
    private final Map<String,WebSocketGroup> groups = new HashMap<>();
    
    public void addGroup(final WebSocketGroup group) {
        groups.put(group.getKey(), group);
    }

    public final WebSocketGroup removeGroup(final WebSocketGroup group) {
        return groups.remove(group.getKey());
    }

    public final boolean groupExists(final String key) {
        return groups.containsKey(key);
    }

    public final WebSocketGroup getGroup(final String key) {
        return groups.get(key);
    }
    
    public final Map<String,WebSocketGroup> getAllGroups(){
        return groups;
    }
}
