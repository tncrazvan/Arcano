package com.github.tncrazvan.arcano.WebSocket;

import com.github.tncrazvan.arcano.Common;
import com.github.tncrazvan.arcano.Http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author razvan
 */
public class WebSocketGroup {
    public static final int 
            PRIVATE = 0,
            PUBLIC = 1;
    private final String key;
    private final Map<String, WebSocketEvent> events = new HashMap<>();
    private WebSocketEvent master = null;
    private int visibility = PRIVATE;
    private String name;

    public WebSocketGroup(HttpSession session) {
        this.key = Common.getBCryptString(session.id());
    }
    
    public void setGroupName(String name){
        this.name = name;
    }
    
    public String getGroupName(){
        return name;
    }
    
    public void setVisibility(int v){
        visibility = v;
    }
    
    public int getVisibility(){
        return visibility;
    }
    
    public void addClient(WebSocketEvent e) throws UnsupportedEncodingException{
        e.startSession();
        events.put(e.session.id(), e);
    }
    
    public WebSocketEvent removeClient(WebSocketEvent e){
        if(matchCreator(e)){
            master = null;
        }
        return events.remove(e.session.id());
    }
    public boolean clientExists(WebSocketEvent e){
        return events.containsKey(e.session.id());
    }
    
    public Map<String,WebSocketEvent> getMap(){
        return events;
    }
    
    public String getKey(){
        return key;
    }
    
    public WebSocketEvent getGroupMaster(){
        return master;
    }
    
    public boolean groupMasterIsset(){
        return master != null;
    }
    
    public void setGroupMaster(WebSocketEvent e){
        master = e;
    }
    
    public void unsetGroupMaster(){
        master = null;
    }
    
    public boolean matchCreator(WebSocketEvent e){
        return Common.validateBCryptString(e.session.id(), key);
    }
}
