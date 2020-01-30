package com.github.tncrazvan.arcano;

import com.github.tncrazvan.arcano.Http.HttpHeaders;
import com.github.tncrazvan.arcano.Http.HttpRequest;
import com.github.tncrazvan.arcano.Http.HttpRequestReader;
import com.github.tncrazvan.arcano.Http.HttpSession;
import static com.github.tncrazvan.arcano.SharedObject.NAME_SESSION_ID;
import com.github.tncrazvan.arcano.Tool.System.Time;
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
    public String[] args;
    public HttpRequestReader reader = null;
    
    //public HttpRequest request;
    public HashMap<String,String> queryString = new HashMap<>();
    public StringBuilder location = new StringBuilder();
    public Map<String,String> userLanguages = new HashMap<>();
    protected HttpHeaders responseHeaders;
    //public Socket client;
    public HttpSession session = null;
    public SharedObject so;
    public Configuration config;
    
    public void setResponseHttpHeaders(final HttpHeaders headers) {
        this.responseHeaders = headers;
    }

    public void setSharedObject(final SharedObject so) {
        this.so = so;
        this.config = so.config;
    }

    public void setSocket(final Socket client) {
        this.reader.client = client;
    }

    public void setHttpRequest(final HttpRequest request) {
        this.reader.request = request;
    }

    public void initEventManager() throws UnsupportedEncodingException {
        String uri = reader.request.headers.get("@Resource");
        try {
            uri = URLDecoder.decode(uri, so.config.charset);
        } catch (final IllegalArgumentException ex) {
        }

        String[] tmp, object;
        final String[] uriParts = uri.split("\\?|\\&", 2);

        location.append(uriParts[0].replaceAll("^\\/", ""));

        if (uriParts.length > 1) {
            tmp = uriParts[1].split("\\&");
            for (final String part : tmp) {
                object = part.split("=", 2);
                if (object.length > 1) {
                    queryString.put(object[0].trim(), object[1]);
                } else {
                    queryString.put(object[0].trim(), "");
                }
            }
        }
    }

    public StringBuilder getLocation() {
        return location;
    }

    public boolean issetSession() {
        return (issetRequestCookie(NAME_SESSION_ID) && so.sessions.issetSession(getRequestCookie(NAME_SESSION_ID)));
    }

    /**
     * Start an HttpSession. This method will request the client to set a
     * SharedObject.NAME_SESSION_ID cookie which will identify this session. If the client already
     * has a VALID SharedObject.NAME_SESSION_ID, then that SharedObject.NAME_SESSION_ID is used instead, thus fetching an
     * existing session instead of creating a new one. This means that you can
     * safely call this method multiple times and can expect it to return the same
     * HttpSession (unless the session itself has expired meanwhile) object. The
     * session's Time To Live is set to the SharedObject.sessionTtl, which has its
     * value set directly from the configuration file. Here's an example of a
     * configuration file that sets the sessino's Time To Live to 60 minutes: {
     * "port": 80, "serverRoot":"server", "webRoot":"www", "charset":"UTF-8", ...
     * "sessionTtl": 3600, ... "threadPoolSize": 3, "sendExceptions": true,
     * "responseWrapper": false }
     * 
     * @return the HttpSession object. This is a new object if the client's
     *         SharedObject.NAME_SESSION_ID is invalid or non existent, or an already existing
     *         HttpSession object if the client provides a valid and existing
     *         SharedObject.NAME_SESSION_ID.
     */
    public HttpSession startSession() {
        session = so.sessions.startSession(this, so.config.session.ttl);
        return session;
    }

    /**
     * Stops the current HttpSession of the client if it has one. This will also
     * delete the client's SharedObject.NAME_SESSION_ID cookie.
     */
    public void stopSession() {
        if (session == null)
            session = startSession();
        if (issetSession())
            so.sessions.stopSession(session);
    }

    /**
     * Get the Socket connection to the client.
     * 
     * @return
     */
    public Socket getClient() {
        return reader.client;
    }

    // FOR HTTP
    protected static int getClassnameIndex(final String[] location, final String httpMethod)
            throws ClassNotFoundException {
        String tmp;
        for (int i = location.length; i > 0; i--) {
            tmp = httpMethod + "/" + String.join("/", Arrays.copyOf(location, i)).toLowerCase();
            if (SharedObject.ROUTES.containsKey(tmp) && SharedObject.ROUTES.get(tmp).getType().equals(httpMethod)) {
                return i - 1;
            }
        }
        throw new ClassNotFoundException();
    }

    protected static WebObject resolveClassName(final int classId, final String[] location) {
        String classname = "";
        for (int i = 0; i <= classId; i++) {
            if (i > 0) {
                classname += "/" + location[i].toLowerCase();
            } else
                classname += location[i];
        }

        return SharedObject.ROUTES.get(classname);
    }

    protected static String[] resolveMethodArgs(final int offset, final String[] location) {
        String[] args = new String[0];
        if (location.length - 1 > offset - 1) {
            final int length = location.length - offset;
            args = Arrays.copyOfRange(location, offset, offset + length);
        }
        return args;
    }

    public HashMap<String,String> getRequestQueryStringHashMap(){
        return queryString;
    }
    
    /**
     * Checks if the requested URL contains the given key as a query.
     * 
     * @param key name of the query.
     * @return
     */
    public boolean issetRequestQueryString(final String key) {
        return queryString.containsKey(key);
    }

    /**
     * Get the value of a specific query string. For example:<br />
     * given the url <b>http://127.0.0.1/new/user?username=my_username</b><br />
     * you can get the username from the url by calling
     * getRequestQueryString("username")
     * 
     * @param key name of the query.
     * @return the value of the query.
     */
    public String getRequestQueryString(final String key) {
        return queryString.get(key);
    }
    
    /**
     * Get the value of a specific query string. For example:<br />
     * given the url <b>http://127.0.0.1/new/user?username=my_username</b><br />
     * you can get the username from the url by calling
     * getRequestQueryString("username").<br />
     * The difference between this method and getRequestQueryString is that this method
     * tries to cast the String value to an int if possible and return it as an Object.<br />
     * From the user point of view there are no advantages using this method.<br />
     * This method is used by the HttpEvent when packing your @HttpParam parameters to seemengly cast your parameters.
     * 
     * @param key name of the query.
     * @return the value of the query.
     */
    public Object getRequestQueryStringAsObject(final String key) {
        String value = queryString.get(key);
        try{
            return Integer.parseInt(value);
        }catch(Exception e){
            return value;
        }
    }
    
    /**
     * Finds the languages of the client application. The value is stored in
     * EventManager#userLanguages.
     */
    public void findRequestLanguages() {
        if (reader.request.headers.get("Accept-Language") == null) {
            userLanguages.put("unknown", "unknown");
        } else {
            String[] tmp = new String[2];
            final String[] languages = reader.request.headers.get("Accept-Language").split(",");
            userLanguages.put("DEFAULT-LANGUAGE", languages[0]);
            for (int i = 1; i < languages.length; i++) {
                tmp = languages[i].split(";");
                userLanguages.put(tmp[0], tmp[1]);
            }
        }
    }

    public String getRequestDefaultLanguage() {
        return userLanguages.get("DEFAULT-LANGUAGE");
    }

    public Map<String, String> getUserLanguages() {
        return userLanguages;
    }

    /**
     * Get the HttpHeaders object of the request.
     * 
     * @return the HttpHeaders object.
     */
    public HttpHeaders getRequestHttpHeaders() {
        return reader.request.headers;
    }

    /**
     * Get a header field from the HttpRequest.
     * 
     * @param name name of the header.
     * @return the value of the header as a String.
     */
    public String getRequestHeaderField(final String name) {
        return reader.request.headers.get(name);
    }

    /**
     * Get the method of the HttpRequest.
     * 
     * @return the name of the method as a String.
     */
    public String getRequestMethod() {
        return reader.request.headers.get("Method");
    }

    /**
     * Get the user agent of the client.
     * 
     * @return name of the user agent.
     */
    public String getRequestUserAgent() {
        return reader.request.headers.get("User-Agent");
    }

    /**
     * Get the address of the client.
     * 
     * @return the remote IP address that identifies the client.
     */
    public String getRequestAddress() {
        return reader.client.getInetAddress().toString();
    }

    /**
     * Get the client port number.
     * 
     * @return the remote port number that's sending the request or 0 if the
     *         connection is not alive.
     */
    public int getRequestPort() {
        return reader.client.getPort();
    }

    /**
     * Notices the client to unset the given cookie.
     * 
     * @param key    name of the cookie
     * @param path   path of the cookie
     * @param domain domain of the cookie
     */
    public void unsetResponseCookie(final String key, final String path, final String domain) {
        responseHeaders.setCookie(key, "deleted", path, domain, 0, so.config.charset);
    }

    /**
     * Notices the client to unset the given cookie.
     * 
     * @param key  name of the cookie
     * @param path path of the cookie
     */
    public void unsetResponseCookie(final String key, final String path) {
        EventManager.this.unsetResponseCookie(key, path, reader.request.headers.get("Host"));
    }

    /**
     * Notices the client to unset the given cookie.
     * 
     * @param key name of the cookie
     */
    public void unsetResponseCookie(final String key) {
        EventManager.this.unsetResponseCookie(key, "/", reader.request.headers.get("Host"));
    }

    /**
     * Notices the client to set the given cookie.
     * 
     * @param name   name of the cookie.
     * @param value  value of the cookie.
     * @param path   path of the cookie.
     * @param domain domain of the cooke.
     * @param expire time to live of the cookie.
     */
    public void setResponseCookie(final String name, final String value, final String path, final String domain,
            final int expire) {
        responseHeaders.setCookie(name, value, path, domain, expire, so.config.charset);
    }

    /**
     * Notices the client to set the given cookie.
     * 
     * @param name   name of the cookie.
     * @param value  value of the cookie.
     * @param path   path of the cookie.
     * @param domain domain of the cooke.
     */
    public void setResponseCookie(final String name, final String value, final String path, final String domain) {
        responseHeaders.setCookie(name, value, path, domain,
                (int) (Time.now(SharedObject.londonTimezone) + so.config.cookie.ttl), so.config.charset);
    }

    /**
     * Notices the client to set the given cookie.
     * 
     * @param name  name of the cookie.
     * @param value value of the cookie.
     * @param path  path of the cookie.
     */
    public void setResponseCookie(final String name, final String value, final String path) {
        responseHeaders.setCookie(name, value, path, null, (int) (Time.now(SharedObject.londonTimezone) + so.config.cookie.ttl),
                so.config.charset);
    }

    /**
     * Notices the client to set the given cookie.
     * 
     * @param name  name of the cookie.
     * @param value value of the cookie.
     */
    public void setResponseCookie(final String name, final String value) {
        responseHeaders.setCookie(name, value, "/", null, (int) (Time.now(SharedObject.londonTimezone) + so.config.cookie.ttl),
                so.config.charset);
    }

    /**
     * Gets the value of the cookie.
     * 
     * @param name name of the cookie.
     * @return value of the cookie.
     */
    public String getRequestCookie(final String name) {
        return reader.request.headers.getCookie(name, so.config.charset);
    }

    /**
     * Checks if the cookie is set.
     * 
     * @param key name of the cookie.
     * @return true if cookie is set, otherwise false.
     */
    public boolean issetRequestCookie(final String key) {
        return reader.request.headers.issetCookie(key);
    }
}
