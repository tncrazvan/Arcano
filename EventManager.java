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
package elkserver;

import elkserver.Http.HttpHeader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author razvan
 */
public class EventManager {
    protected final HttpHeader clientHeader;
    protected final Map<String,String> queryString = new HashMap<>();
    protected final String location;
    protected final Map<String,String> userLanguages = new HashMap<>();
    protected final HttpHeader header;
    public EventManager(HttpHeader clientHeader) {
        header = new HttpHeader();
        this.clientHeader=clientHeader;
        
        String[] parts = clientHeader.get("Resource").split("\\?");
        String[] tmp,object;
        
        if(parts.length > 1){
            try {
                tmp = java.net.URLDecoder.decode(parts[1], "UTF-8").split("\\&");
                for (String part : tmp) {
                    object = part.split("=", 2);
                    if(object.length > 1){
                        queryString.put(object[0].trim(), object[1]);
                    }else{
                        queryString.put(object[0].trim(), "");
                    }
                }
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(EventManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        location = parts[0];
    }
    
    public boolean issetUrlQuery(String key){
        return queryString.containsKey(key);
    }
    
    public String getUrlQuery(String key){
        return queryString.get(key);
    }
    
    protected void findUserLanguages(){
        if(clientHeader.get("Accept-Language") == null){
            userLanguages.put("unknown", "unknown");
        }else{
            String[] tmp = new String[2];
            String[] languages = clientHeader.get("Accept-Language").split(",");
            userLanguages.put("DEFAULT-LANGUAGE", languages[0]);
            for(int i=1;i<languages.length;i++){
                tmp=languages[i].split(";");
                userLanguages.put(tmp[0], tmp[1]);
            }
        }
    }
    public void unsetCookie(String key, String path, String domain){
        header.setCookie(key,"deleted",path,domain,"0");
    }
    
    public void unsetCookie(String key, String path){
        unsetCookie(key, path, clientHeader.get("Host")+":"+ELK.PORT);
    }
    
    public void unsetCookie(String key){
        unsetCookie(key, "/", clientHeader.get("Host")+":"+ELK.PORT);
    }
    
    public void setCookie(String name,String value, String path, String domain, String expire){
        header.setCookie(name, value, path, domain, expire);
    }
    public void setCookie(String name,String value, String path, String domain){
        header.setCookie(name, value, path, domain);
    }
    public void setCookie(String name,String value, String path){
        header.setCookie(name, value, path);
    }
    public void setCookie(String name,String value){
        header.setCookie(name, value);
    }
    
    public String getCookie(String name){
        return clientHeader.getCookie(name);
    }
    public boolean cookieIsset(String key){
        return clientHeader.cookieIsset(key);
    }
}
