/**
 * ElkServer is a Java library that makes it easier
 * to program and manage a Java Web Server by providing different tools
 * such as:
 * 1) An MVC (Model-View-Controller) alike design pattern to manage 
 *    client requests without using any URL rewriting rules.
 * 2) A WebSocket Manager, allowing the server to accept and manage 
 *    incoming WebSocket connections.
 * 3) Direct access to every socket bound to every client application.
 * 4) Direct access to the headers of the incomming and outgoing Http messages.
 * Copyright (C) 2016-2018  Tanase Razvan Catalin
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.razshare.elkserver.WebSocket;

import com.razshare.elkserver.Elk;
import com.razshare.elkserver.Http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author razvan
 */
public class WebSocketGroup {
    private final String key;
    private final Map<String, WebSocketEvent> events = new HashMap<>();
    private WebSocketEvent master = null;

    public WebSocketGroup(HttpSession session) {
        this.key = Elk.getBCryptString(session.getSessionId());
    }
    
    public void addClient(WebSocketEvent e){
        events.put(e.getSession().getSessionId(), e);
    }
    public WebSocketEvent removeClient(WebSocketEvent e){
        if(matchCreator(e)){
            master = null;
        }
        return events.remove(e.getSession().getSessionId());
    }
    public boolean clientExists(WebSocketEvent e){
        return events.containsKey(e.getSession().getSessionId());
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
        return Elk.validateBCryptString(e.getSession().getSessionId(), key);
    }
}
