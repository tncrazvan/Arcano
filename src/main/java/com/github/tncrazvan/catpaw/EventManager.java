/**
 * CatPaw is a Java library that makes it easier
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
package com.github.tncrazvan.catpaw;

import com.github.tncrazvan.catpaw.Http.HttpHeader;
import com.github.tncrazvan.catpaw.Http.HttpSession;
import com.github.tncrazvan.catpaw.Http.HttpSessionManager;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides a layer of abstraction for both HttpEventManager and WebSocketEventManager.
 * Contains a few methods that are useful to both classes, 
 * such as Http Header managing methods, tools to set, unset, 
 * and read cookie, and more.
 * @author razvan
 */
public abstract class EventManager extends Server{
    protected final HttpHeader clientHeader;
    protected final Map<String,String> queryString = new HashMap<>();
    protected final StringBuilder location = new StringBuilder();
    protected final Map<String,String> userLanguages = new HashMap<>();
    protected final HttpHeader header;
    protected final Socket client;
    protected HttpSession session = null;
    public EventManager(Socket client, HttpHeader clientHeader) throws UnsupportedEncodingException {
        this.client=client;
        header = new HttpHeader();
        this.clientHeader=clientHeader;
        String uri = clientHeader.get("@Resource");
        try{
            uri = URLDecoder.decode(uri,charset);
        }catch(IllegalArgumentException ex){}
        
        String[] ruiParts = uri.split("\\?|\\&",2);
        String[] tmp,object;
        
        location.append(ruiParts[0].replaceAll("^\\/", ""));
        
        if(ruiParts.length > 1){
            tmp = ruiParts[1].split("\\&");
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
    
    public boolean issetSession() throws UnsupportedEncodingException{
        return (issetCookie("sessionId") && HttpSessionManager.issetSession(getCookie("sessionId")));
    }
    
    public HttpSession startSession() throws UnsupportedEncodingException{
        session = HttpSessionManager.startSession(this);
        return session;
    }
    
    public void stopSession() throws UnsupportedEncodingException{
        if(session == null) session = startSession();
        if(issetSession())
            HttpSessionManager.stopSession(session);
    }
    
    public Socket getClient(){
        return client;
    }
    
    
    //FOR HTTP
    protected int getClassnameIndex(String[] location,String httpMethod) throws ClassNotFoundException{
        String tmp;
        for(int i=location.length;i>0;i--){
            tmp = "/"+String.join("/", Arrays.copyOf(location, i));
            if(Server.routes.containsKey(tmp) && Server.routes.get(tmp).getHttpMethod().equals(httpMethod)){
                return i-1;
            }
                
        }
        throw new ClassNotFoundException();
    }
    
    protected WebMethod resolveClassName(int classId,String[] location){
        String classname = "";
        for(int i=0;i<=classId;i++){
            classname +="/"+location[i];
        }
        
        return Server.routes.get(classname);
    }
    
    
    //FOR WEBSOCKETS
    protected int getClassnameIndex(String root, String[] location) throws ClassNotFoundException{
        String currentName = root;
        for(int i=0;i<location.length;i++){
            currentName +="."+location[i];
            try{
                Class.forName(currentName);
                return i;
            }catch(Exception e){}
                
        }
        throw new ClassNotFoundException();
    }
    
    protected String resolveClassName(int classId,String root,String[] location){
        String classname = root;
        for(int i=0;i<=classId;i++){
            classname +="."+location[i];
        }
        
        return classname;
    }
    
    protected String[] resolveMethodArgs(int offset, String[] location){
        String[] args = new String[0];
        if(location.length-1>offset-1){
            int length = location.length-offset;
            args = Arrays.copyOfRange(location,offset,offset+length);
        }
        return args;
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
     * @throws java.io.UnsupportedEncodingException
     */
    public void unsetCookie(String key, String path, String domain) throws UnsupportedEncodingException{
        header.setCookie(key,"deleted",path,domain,"0");
    }
    
    /**
     * Notices the client to unset the given cookie.
     * @param key name of the cookie
     * @param path path of the cookie
     * @throws java.io.UnsupportedEncodingException
     */
    public void unsetCookie(String key, String path) throws UnsupportedEncodingException{
        unsetCookie(key, path, clientHeader.get("Host"));
    }
    
    /**
     * Notices the client to unset the given cookie.
     * @param key name of the cookie
     * @throws java.io.UnsupportedEncodingException
     */
    public void unsetCookie(String key) throws UnsupportedEncodingException{
        unsetCookie(key, "/", clientHeader.get("Host"));
    }
    
    /**
     * Notices the client to set the given cookie.
     * @param name name of the cookie.
     * @param value value of the cookie.
     * @param path path of the cookie.
     * @param domain domain of the cooke.
     * @param expire time to live of the cookie.
     * @throws java.io.UnsupportedEncodingException
     */
    public void setCookie(String name,String value, String path, String domain, int expire) throws UnsupportedEncodingException{
        header.setCookie(name, value, path, domain, expire);
    }
    public void setCookie(String name,String value, String path, String domain, String expire) throws UnsupportedEncodingException{
        header.setCookie(name, value, path, domain, expire);
    }
    
    /**
     * Notices the client to set the given cookie.
     * @param name name of the cookie.
     * @param value value of the cookie.
     * @param path path of the cookie.
     * @param domain domain of the cooke.
     * @throws java.io.UnsupportedEncodingException
     */
    public void setCookie(String name,String value, String path, String domain) throws UnsupportedEncodingException{
        header.setCookie(name, value, path, domain);
    }
    
    /**
     * Notices the client to set the given cookie.
     * @param name name of the cookie.
     * @param value value of the cookie.
     * @param path path of the cookie.
     * @throws java.io.UnsupportedEncodingException
     */
    public void setCookie(String name,String value, String path) throws UnsupportedEncodingException{
        header.setCookie(name, value, path);
    }
    
    /**
     * Notices the client to set the given cookie.
     * @param name name of the cookie.
     * @param value value of the cookie.
     * @throws java.io.UnsupportedEncodingException
     */
    public void setCookie(String name,String value) throws UnsupportedEncodingException{
        header.setCookie(name, value);
    }
    
    
    /**
     * Gets the value of the cookie.
     * @param name name of the cookie.
     * @return value of the cookie.
     * @throws java.io.UnsupportedEncodingException
     */
    public String getCookie(String name) throws UnsupportedEncodingException{
        return clientHeader.getCookie(name);
    }
    
    /**
     * Checks if the cookie is set.
     * @param key name of the cookie.
     * @return true if cookie is set, otherwise false.
     */
    public boolean issetCookie(String key){
        return clientHeader.issetCookie(key);
    }
}
