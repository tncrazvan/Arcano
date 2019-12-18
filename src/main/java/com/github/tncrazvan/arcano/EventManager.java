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
    public HttpSession session = null;
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
    
    public boolean issetSession() {
        return (issetCookie("sessionId") && so.sessions.issetSession(getCookie("sessionId")));
    }
    
    /**
     * Start an HttpSession.
     * This method will request the client to set a "sessionId" cookie which will identify this session.
     * If the client already has a VALID sessionId, then that sessionId is used instead, thus fetching an existing session instead of creating a new one.
     * This means that you can safely call this method multiple times and can expect it to return the same HttpSession (unless the session itself has expired meanwhile) object.
     * The session's Time To Live is set to the SharedObject.sessionTtl, which has its value set directly from the configuration file.
     * Here's an example of a configuration file that sets the sessino's Time To Live to 60 minutes:
     * {
     *  "port": 80,
     *  "serverRoot":"server",
     *  "webRoot":"www",
     *  "charset":"UTF-8",
     *  ...
     *  "sessionTtl": 3600,
     *  ...
     *  "threadPoolSize": 3,
     *  "sendExceptions": true,
     *  "responseWrapper": false
     * }
     * @return 
     */
    public HttpSession startSession() {
        session = so.sessions.startSession(this,so.sessionTtl);
        return session;
    }
    
    /**
     * Stops the current HttpSession of the client if it has one.
     * This will also delete the client's "sessionId" cookie.
     */
    public void stopSession(){
        if(session == null) session = startSession();
        if(issetSession())
            so.sessions.stopSession(session);
    }
    
    /**
     * Get the Socket connection to the client.
     * @return 
     */
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
     */
    public void unsetCookie(String key, String path, String domain){
        headers.setCookie(key,"deleted",path,domain,0,so.charset);
    }
    
    /**
     * Notices the client to unset the given cookie.
     * @param key name of the cookie
     * @param path path of the cookie
     */
    public void unsetCookie(String key, String path){
        unsetCookie(key, path, request.headers.get("Host"));
    }
    
    /**
     * Notices the client to unset the given cookie.
     * @param key name of the cookie
     */
    public void unsetCookie(String key){
        unsetCookie(key, "/", request.headers.get("Host"));
    }
    
    /**
     * Notices the client to set the given cookie.
     * @param name name of the cookie.
     * @param value value of the cookie.
     * @param path path of the cookie.
     * @param domain domain of the cooke.
     * @param expire time to live of the cookie.
     */
    public void setCookie(String name,String value, String path, String domain, int expire){
        headers.setCookie(name, value, path, domain, expire, so.charset);
    }
    
    /**
     * Notices the client to set the given cookie.
     * @param name name of the cookie.
     * @param value value of the cookie.
     * @param path path of the cookie.
     * @param domain domain of the cooke.
     */
    public void setCookie(String name,String value, String path, String domain){
        headers.setCookie(name, value, path, domain, so.charset);
    }
    
    /**
     * Notices the client to set the given cookie.
     * @param name name of the cookie.
     * @param value value of the cookie.
     * @param path path of the cookie.
     */
    public void setCookie(String name,String value, String path){
        headers.setCookie(name, value, path, so.charset);
    }
    
    /**
     * Notices the client to set the given cookie.
     * @param name name of the cookie.
     * @param value value of the cookie.
     */
    public void setCookie(String name, String value){
        headers.setCookie(name, value, so.charset);
    }
    
    
    /**
     * Gets the value of the cookie.
     * @param name name of the cookie.
     * @return value of the cookie.
     */
    public String getCookie(String name){
        return request.headers.getCookie(name, so.charset);
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
