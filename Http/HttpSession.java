/**
 * ElkServer is a Java library that makes it easier
 * to program and manage a Java servlet by providing different tools
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
package elkserver.Http;

import elkserver.ELK;
import elkserver.WebSocket.WebSocketEvent;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author razvan
 */
public class HttpSession{
    private static final Map<String,Object> SESSION_STORAGE = new HashMap<>();
    private final String id;
    public HttpSession(HttpEvent e) {
        if(e.cookieIsset("sessionId")){
            id = e.getCookie("sessionId");
        }else{
            id = ELK.getSha1String(e.getClient().getInetAddress().toString()+","+e.getClient().getPort()+","+Math.random());
            e.setCookie("sessionId", id, "/");
            SESSION_STORAGE.put(id, null);
        }
    }
    
    public HttpSession(WebSocketEvent e) {
        if(e.cookieIsset("sessionId")){
            id = e.getCookie("sessionId");
        }else{
            id = ELK.getSha1String(e.getClient().getInetAddress().toString()+","+e.getClient().getPort()+","+Math.random());
            e.setCookie("sessionId", id, "/");
            SESSION_STORAGE.put(id, null);
        }
    }
    
    public boolean sessionIsnull(){
        return getSessionObject() == null;
    }
    
    public String getSessionId(){
        return id;
    }
    public void setSessionObject(Object o){
        SESSION_STORAGE.put(id, o);
    }
    public void unsetSessionObject(){
        SESSION_STORAGE.remove(id);
    }
    
    public Object getSessionObject(){
        return SESSION_STORAGE.get(id);
    }
}
