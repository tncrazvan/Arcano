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
package com.razshare.elkserver.Http;

import com.razshare.elkserver.Elk;
import com.razshare.elkserver.WebSocket.WebSocketEvent;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author razvan
 */
public class HttpSession{
    private static final Map<String,HttpSession> LIST = new HashMap<>();
    private final String id;
    private final Map<String,Object> STORAGE = new HashMap<>();
    private HttpSession(HttpEvent e) {
        id = Elk.getSha1String(e.getClient().getInetAddress().toString()+","+e.getClient().getPort()+","+Math.random());
        e.setCookie("sessionId", id, "/");
    }
    
    private HttpSession(WebSocketEvent e) {
        id = Elk.getSha1String(e.getClient().getInetAddress().toString()+","+e.getClient().getPort()+","+Math.random());
        e.setCookie("sessionId", id, "/");
    }
    
    public static HttpSession start(HttpEvent e){
        if(e.issetCookie("sessionId")){
            final String sessionId = e.getCookie("sessionId");
            if(LIST.containsKey(sessionId)){
                return LIST.get(sessionId);
            }
        }
        final HttpSession session = new HttpSession(e);
        set(session);
        return session;
    }
    public static HttpSession start(WebSocketEvent e){
        if(e.issetCookie("sessionId")){
            final String sessionId = e.getCookie("sessionId");
            if(LIST.containsKey(sessionId)){
                return LIST.get(sessionId);
            }
        }
        final HttpSession session = new HttpSession(e);
        set(session);
        return session;
    }
    
    public static HttpSession get(String sessionId){
        return LIST.get(sessionId);
    }
    
    public static void set(HttpSession session){
        LIST.put(session.getSessionId(), session);
    }
    
    public static boolean isset(String sessionId){
        return LIST.containsKey(sessionId);
    }
    
    public static void unset(HttpSession session){
        LIST.remove(session.getSessionId());
    }
    
    public String getSessionId(){
        return id;
    }
    public void setProperty(String key, Object o){
        STORAGE.put(key, o);
    }
    public void unsetProperty(String key){
        STORAGE.remove(key);
    }
    
    public boolean issetProperty(String key){
        return STORAGE.containsKey(key);
    }
    
    public Object getProperty(String key){
        return STORAGE.get(key);
    }
    
}
