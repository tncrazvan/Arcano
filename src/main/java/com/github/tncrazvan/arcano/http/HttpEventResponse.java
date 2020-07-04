package com.github.tncrazvan.arcano.http;

import com.github.tncrazvan.arcano.EventManager;
import com.github.tncrazvan.arcano.SharedObject;
import com.github.tncrazvan.arcano.tool.system.Time;

public class HttpEventResponse {
    private EventManager e;
    public HttpHeaders headers = null;
    public HttpEventResponse(final EventManager eventManager){
        this.e = eventManager;
    }

    public final void setHttpHeaders(final HttpHeaders headers) {
        this.headers = headers;
    }

    /**
     * Notices the client to unset the given cookie.
     * 
     * @param key    name of the cookie
     * @param path   path of the cookie
     * @param domain domain of the cookie
     */
    public final void unsetCookie(final String key, final String path, final String domain) {
        headers.setCookie(key, "deleted", path, domain, 0, e.so.config.charset);
    }

    /**
     * Notices the client to unset the given cookie.
     * 
     * @param key  name of the cookie
     * @param path path of the cookie
     */
    public final void unsetCookie(final String key, final String path) {
        this.unsetCookie(key, path, e.request.reader.content.headers.get("Host"));
    }

    /**
     * Notices the client to unset the given cookie.
     * 
     * @param key name of the cookie
     */
    public final void unsetCookie(final String key) {
        this.unsetCookie(key, "/", e.request.reader.content.headers.get("Host"));
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
    public final void setCookie(final String name, final String value, final String path, final String domain,final int expire) {
        headers.setCookie(name, value, path, domain, expire, e.so.config.charset);
    }

    /**
     * Notices the client to set the given cookie.
     * 
     * @param name   name of the cookie.
     * @param value  value of the cookie.
     * @param path   path of the cookie.
     * @param domain domain of the cooke.
     */
    public final void setCookie(final String name, final String value, final String path, final String domain) {
        headers.setCookie(name, value, path, domain,
                (int) (Time.now(SharedObject.londonTimezone) + e.so.config.cookie.ttl), e.so.config.charset);
    }

    /**
     * Notices the client to set the given cookie.
     * 
     * @param name  name of the cookie.
     * @param value value of the cookie.
     * @param path  path of the cookie.
     */
    public final void setCookie(final String name, final String value, final String path) {
        headers.setCookie(name, value, path, null, (int) (Time.now(SharedObject.londonTimezone) + e.so.config.cookie.ttl),
                e.so.config.charset);
    }

    /**
     * Notices the client to set the given cookie.
     * 
     * @param name  name of the cookie.
     * @param value value of the cookie.
     */
    public final void setCookie(final String name, final String value) {
        headers.setCookie(name, value, "/", null, (int) (Time.now(SharedObject.londonTimezone) + e.so.config.cookie.ttl),
                e.so.config.charset);
    }
}