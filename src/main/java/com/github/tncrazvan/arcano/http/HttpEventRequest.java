package com.github.tncrazvan.arcano.http;

import static com.github.tncrazvan.arcano.tool.encoding.Hashing.getSha1String;

import java.util.HashMap;
import java.util.Map;

import com.github.tncrazvan.arcano.EventManager;

public class HttpEventRequest {
    private EventManager e;
    private String requestId;
    public HttpRequestReader reader = null;
    public Map<String,String> userLanguages = new HashMap<>();
    
    public HashMap<String,String> queryStrings = new HashMap<>();
    public HashMap<String, String> parameters = new HashMap<>();

    public HttpEventRequest(final EventManager eventManager, final HttpRequestReader reader){
        this.e = eventManager;
        this.reader = reader;
    }

    public final void resolveId(){
        requestId = getSha1String(System.identityHashCode(reader.client) + "::" + System.currentTimeMillis(),e.so.config.charset);
    }

    public final String getRequestId(){
        return requestId;
    }

    /**
     * Finds the languages of the client application. The value is stored in
     * EventManager#userLanguages.
     */
    public final void findLanguages() {
        if (reader.content.headers.get("Accept-Language") == null) {
            userLanguages.put("unknown", "unknown");
        } else {
            String[] tmp = new String[2];
            final String[] languages = reader.content.headers.get("Accept-Language").split(",");
            userLanguages.put("DEFAULT-LANGUAGE", languages[0]);
            for (int i = 1; i < languages.length; i++) {
                tmp = languages[i].split(";");
                userLanguages.put(tmp[0], tmp[1]);
            }
        }
    }

    public final String getDefaultLanguage() {
        return userLanguages.get("DEFAULT-LANGUAGE");
    }

    public final Map<String, String> getUserLanguages() {
        return userLanguages;
    }

    /**
     * Get the HttpHeaders object of the request.
     * 
     * @return the HttpHeaders object.
     */
    public final HttpHeaders getHttpHeaders() {
        return reader.content.headers;
    }

    /**
     * Get a header field from the HttpRequest.
     * 
     * @param name name of the header.
     * @return the value of the header as a String.
     */
    public final String getHeaderField(final String name) {
        return reader.content.headers.get(name);
    }

    /**
     * Check if a header is set.
     * 
     * @param name name of the header.
     * @return true if the header is defined, false otherwise.
     */
    public final boolean issetHeaderField(final String name) {
        return reader.content.headers.isDefined(name);
    }

    /**
     * Get the method of the HttpRequest.
     * 
     * @return the name of the method as a String.
     */
    public final String getMethod() {
        return reader.content.headers.getMethod();
    }

    /**
     * Get the user agent of the client.
     * 
     * @return name of the user agent.
     */
    public final String getUserAgent() {
        return reader.content.headers.get("User-Agent");
    }

    /**
     * Get the address of the client.
     * 
     * @return the remote IP address that identifies the client.
     */
    public final String getAddress() {
        return reader.client.getInetAddress().toString();
    }

    /**
     * Get the client port number.
     * 
     * @return the remote port number that's sending the request or 0 if the
     *         connection is not alive.
     */
    public final int getPort() {
        return reader.client.getPort();
    }

    /**
     * Gets the value of the cookie.
     * 
     * @param name name of the cookie.
     * @return value of the cookie.
     */
    public final String getCookie(final String name) {
        return reader.content.headers.getCookie(name, e.so.config.charset);
    }

    /**
     * Checks if the cookie is set.
     * 
     * @param key name of the cookie.
     * @return true if cookie is set, otherwise false.
     */
    public final boolean issetCookie(final String key) {
        return reader.content.headers.issetCookie(key);
    }
}