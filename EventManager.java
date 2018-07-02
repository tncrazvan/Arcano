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
package com.razshare.elkserver;

import com.razshare.elkserver.Http.HttpHeader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides a layer of abstraction for both HttpEventManager and WebSocketEventManager.
 * Contains a few methods that are useful to both classes, 
 * such as Http Header managing methods, tools to set, unset, 
 * and read cookie, and more.
 * @author razvan
 */
public abstract class EventManager extends Elk{
    protected final HttpHeader clientHeader;
    protected final Map<String,String> queryString = new HashMap<>();
    protected final String location;
    protected final Map<String,String> userLanguages = new HashMap<>();
    protected final HttpHeader header;
    public EventManager(HttpHeader clientHeader) throws UnsupportedEncodingException {
        header = new HttpHeader();
        this.clientHeader=clientHeader;
        String[] parts = URLDecoder.decode(clientHeader.get("Resource"),charset).split("\\?");
        String[] tmp,object;
        
        location = parts[0];
        
        if(parts.length > 1){
            tmp = parts[1].split("\\&");
            for (String part : tmp) {
                object = part.split("=", 2);
                if(object.length > 1){
                    queryString.put(object[0].trim(), object[1]);
                }else{
                    queryString.put(object[0].trim(), "");
                }
            }
        }
    }
    
    /**
     * Checks if the requested URL contains the given key as a query.
     * @param key name of the query.
     * @return 
     */
    public boolean issetUrlQuery(String key){
        return queryString.containsKey(key);
    }
    
    /**
     * 
     * @param key name of the query.
     * @return the value of the query.
     */
    public String getUrlQuery(String key){
        return queryString.get(key);
    }
    
    /**
     * Finds the languages of the client application.
     * The value is stored in EventManager#userLanguages.
     */
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
    
    /**
     * Notices the client to unset the given cookie.
     * @param key name of the cookie
     * @param path path of the cookie
     * @param domain domain of the cookie
     */
    public void unsetCookie(String key, String path, String domain){
        header.setCookie(key,"deleted",path,domain,"0");
    }
    
    /**
     * Notices the client to unset the given cookie.
     * @param key name of the cookie
     * @param path path of the cookie
     */
    public void unsetCookie(String key, String path){
        unsetCookie(key, path, clientHeader.get("Host"));
    }
    
    /**
     * Notices the client to unset the given cookie.
     * @param key name of the cookie
     */
    public void unsetCookie(String key){
        unsetCookie(key, "/", clientHeader.get("Host"));
    }
    
    /**
     * Notices the client to set the given cookie.
     * @param name name of the cookie.
     * @param value value of the cookie.
     * @param path path of the cookie.
     * @param domain domain of the cooke.
     * @param expire time to live of the cookie.
     */
    public void setCookie(String name,String value, String path, String domain, String expire){
        header.setCookie(name, value, path, domain, expire);
    }
    
    /**
     * Notices the client to set the given cookie.
     * @param name name of the cookie.
     * @param value value of the cookie.
     * @param path path of the cookie.
     * @param domain domain of the cooke.
     */
    public void setCookie(String name,String value, String path, String domain){
        header.setCookie(name, value, path, domain);
    }
    
    /**
     * Notices the client to set the given cookie.
     * @param name name of the cookie.
     * @param value value of the cookie.
     * @param path path of the cookie.
     */
    public void setCookie(String name,String value, String path){
        header.setCookie(name, value, path);
    }
    
    /**
     * Notices the client to set the given cookie.
     * @param name name of the cookie.
     * @param value value of the cookie.
     */
    public void setCookie(String name,String value){
        header.setCookie(name, value);
    }
    
    
    /**
     * Gets the value of the cookie.
     * @param name name of the cookie.
     * @return value of the cookie.
     */
    public String getCookie(String name){
        return clientHeader.getCookie(name);
    }
    
    /**
     * Checks if the cookie is set.
     * @param key name of the cookie.
     */
    public boolean issetCookie(String key){
        return clientHeader.issetCookie(key);
    }
    public boolean cookieIsset(String key){
        return issetCookie(key);
    }
}
