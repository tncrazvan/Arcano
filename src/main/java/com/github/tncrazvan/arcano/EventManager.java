package com.github.tncrazvan.arcano;

import com.github.tncrazvan.arcano.Http.HttpHeaders;
import com.github.tncrazvan.arcano.Http.HttpRequest;
import com.github.tncrazvan.arcano.Http.HttpSession;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URLDecoder;
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
public abstract class EventManager{
    protected HttpRequest request;
    protected HashMap<String,String> queryString = new HashMap<>();
    protected StringBuilder location = new StringBuilder();
    protected Map<String,String> userLanguages = new HashMap<>();
    protected HttpHeaders headers;
    protected Socket client;
    protected HttpSession session = null;
    public SharedObject so;
    public static final File root = new File("/java");
    
    public void setHttpHeaders(HttpHeaders headers){
        this.headers=headers;
    }
    
    public void setSharedObject(SharedObject so){
        this.so=so;
    }
    public void setSocket(Socket client){
        this.client=client;
    }
    public void setHttpRequest(HttpRequest request){
        this.request=request;
    }
    
    public void initEventManager() throws UnsupportedEncodingException {
        String uri = request.headers.get("@Resource");
        try{
            uri = URLDecoder.decode(uri,so.charset);
        }catch(IllegalArgumentException ex){}
        
        String[] tmp,object;
        String[] uriParts = uri.split("\\?|\\&",2);
        
        location.append(uriParts[0].replaceAll("^\\/", ""));
        
        if(uriParts.length > 1){
            tmp = uriParts[1].split("\\&");
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
    
    public StringBuilder getLocation(){
        return location;
    }
    
    public boolean issetSession() throws UnsupportedEncodingException{
        return (issetCookie("sessionId") && so.sessions.issetSession(getCookie("sessionId")));
    }
    
    public HttpSession startSession() throws UnsupportedEncodingException{
        session = so.sessions.startSession(this,so.sessionTtl);
        return session;
    }
    
    public void stopSession() throws UnsupportedEncodingException{
        if(session == null) session = startSession();
        if(issetSession())
            so.sessions.stopSession(session);
    }
    
    public Socket getClient(){
        return client;
    }
    
    
    //FOR HTTP
    protected static int getClassnameIndex(String[] location,String httpMethod) throws ClassNotFoundException{
        String tmp;
        for(int i=location.length;i>0;i--){
            tmp = httpMethod+"/"+String.join("/", Arrays.copyOf(location, i)).toLowerCase();
            if(SharedObject.ROUTES.containsKey(tmp) && SharedObject.ROUTES.get(tmp).getType().equals(httpMethod)){
                return i-1;
            }
        }
        throw new ClassNotFoundException();
    }
    
    protected static WebObject resolveClassName(int classId,String[] location){
        String classname = "";
        for(int i=0;i<=classId;i++){
            if(i>0) {
                classname += "/"+location[i].toLowerCase();
            }else
                classname += location[i];
        }
        
        return SharedObject.ROUTES.get(classname);
    }
    
    protected static String[] resolveMethodArgs(int offset, String[] location){
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
    public boolean issetRequestQueryString(String key){
        return queryString.containsKey(key);
    }
    
    /**
     * 
     * @param key name of the query.
     * @return the value of the query.
     */
    public String getRequestQueryString(String key){
        return queryString.get(key);
    }
    
    /**
     * Finds the languages of the client application.
     * The value is stored in EventManager#userLanguages.
     */
    public void findRequestLanguages(){
        if(request.headers.get("Accept-Language") == null){
            userLanguages.put("unknown", "unknown");
        }else{
            String[] tmp = new String[2];
            String[] languages = request.headers.get("Accept-Language").split(",");
            userLanguages.put("DEFAULT-LANGUAGE", languages[0]);
            for(int i=1;i<languages.length;i++){
                tmp=languages[i].split(";");
                userLanguages.put(tmp[0], tmp[1]);
            }
        }
    }
    
    public String getRequestDefaultLanguage(){
        return userLanguages.get("DEFAULT-LANGUAGE");
    }
    
    public Map<String,String> getUserLanguages(){
        return userLanguages;
    }
    
    public HttpHeaders getRequestHttpHeaders(){
        return request.headers;
    }
    
    public String getRequestHeaderField(String key){
        return request.headers.get(key);
    }
    
    public String getRequestMethod(){
        return request.headers.get("Method");
    }
    
    
    public String getRequestUserAgent(){
        return request.headers.get("User-Agent");
    }
    
    public String getRequestAddress(){
        return client.getInetAddress().toString();
    }
    
    public int getRequestPort(){
        return client.getPort();
    }
    
    /**
     * Notices the client to unset the given cookie.
     * @param key name of the cookie
     * @param path path of the cookie
     * @param domain domain of the cookie
     * @throws java.io.UnsupportedEncodingException
     */
    public void unsetCookie(String key, String path, String domain) throws UnsupportedEncodingException{
        headers.setCookie(key,"deleted",path,domain,"0");
    }
    
    /**
     * Notices the client to unset the given cookie.
     * @param key name of the cookie
     * @param path path of the cookie
     * @throws java.io.UnsupportedEncodingException
     */
    public void unsetCookie(String key, String path) throws UnsupportedEncodingException{
        unsetCookie(key, path, request.headers.get("Host"));
    }
    
    /**
     * Notices the client to unset the given cookie.
     * @param key name of the cookie
     * @throws java.io.UnsupportedEncodingException
     */
    public void unsetCookie(String key) throws UnsupportedEncodingException{
        unsetCookie(key, "/", request.headers.get("Host"));
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
        headers.setCookie(name, value, path, domain, expire);
    }
    public void setCookie(String name,String value, String path, String domain, String expire) throws UnsupportedEncodingException{
        headers.setCookie(name, value, path, domain, expire);
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
        headers.setCookie(name, value, path, domain);
    }
    
    /**
     * Notices the client to set the given cookie.
     * @param name name of the cookie.
     * @param value value of the cookie.
     * @param path path of the cookie.
     * @throws java.io.UnsupportedEncodingException
     */
    public void setCookie(String name,String value, String path) throws UnsupportedEncodingException{
        headers.setCookie(name, value, path);
    }
    
    /**
     * Notices the client to set the given cookie.
     * @param name name of the cookie.
     * @param value value of the cookie.
     * @throws java.io.UnsupportedEncodingException
     */
    public void setCookie(String name,String value) throws UnsupportedEncodingException{
        headers.setCookie(name, value);
    }
    
    
    /**
     * Gets the value of the cookie.
     * @param name name of the cookie.
     * @return value of the cookie.
     * @throws java.io.UnsupportedEncodingException
     */
    public String getCookie(String name) throws UnsupportedEncodingException{
        return request.headers.getCookie(name);
    }
    
    /**
     * Checks if the cookie is set.
     * @param key name of the cookie.
     * @return true if cookie is set, otherwise false.
     */
    public boolean issetCookie(String key){
        return request.headers.issetCookie(key);
    }
}
