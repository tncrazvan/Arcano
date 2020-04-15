/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.tncrazvan.arcano.Http;

import static com.github.tncrazvan.arcano.SharedObject.LOGGER;
import static com.github.tncrazvan.arcano.Tool.System.Time.toLocalDateTime;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.tncrazvan.arcano.SharedObject;
import com.github.tncrazvan.arcano.Tool.Http.Status;

/**
 *
 * @author Administrator
 */
public class HttpHeaders {
    private final HashMap<String, String> headers = new HashMap<>();
    private Map<String, String[]> cookies = new HashMap<>();
    public static final int TYPE_REQUEST=0,TYPE_RESPONSE=1;
    private final int type;
    private String method,resource,version,status;
    
    private HttpHeaders(int type) {
        this(type,new HashMap<String,String>());
    }

    private HttpHeaders(int type,final HashMap<String, String> map) {
        this.type = type;
        map.forEach((key, value) -> {
            headers.put(key, value);
        });
    }
    
    public static HttpHeaders request(String resource){
        return new HttpHeaders(HttpHeaders.TYPE_REQUEST).setResource(resource);
    }
    public static HttpHeaders request(final HashMap<String, String> map){
        return new HttpHeaders(HttpHeaders.TYPE_REQUEST,map);
    }
    public static HttpHeaders request(String resource, final HashMap<String, String> map){
        return new HttpHeaders(HttpHeaders.TYPE_REQUEST,map)
                .setResource(resource);
    }
    public static HttpHeaders request(String method, String resource, final HashMap<String, String> map){
        return new HttpHeaders(HttpHeaders.TYPE_REQUEST,map)
                .setMethod(method)
                .setResource(resource);
    }
    
    public static HttpHeaders response(){
        return new HttpHeaders(HttpHeaders.TYPE_RESPONSE).setStatus(Status.STATUS_SUCCESS);
    }
    public static HttpHeaders response(String status){
        return new HttpHeaders(HttpHeaders.TYPE_RESPONSE).setStatus(status);
    }
    public static HttpHeaders response(final HashMap<String, String> map){
        return new HttpHeaders(HttpHeaders.TYPE_RESPONSE,map).setStatus(Status.STATUS_SUCCESS);
    }
    public static HttpHeaders response(String status, final HashMap<String, String> map){
        return new HttpHeaders(HttpHeaders.TYPE_RESPONSE,map).setStatus(Status.STATUS_SUCCESS);
    }
    
    public final String fieldToString(final String key) {
        return fieldToString(key, headers.get(key));
    }
    public final String fieldToString(final String key, final String value) {
        return key + ": " + value + "\r\n";
    }

    private static final ZoneId londonTimezone = ZoneId.of("Europe/London");
    private static final DateTimeFormatter formatHttpDefaultDate = DateTimeFormatter
            .ofPattern("EEE, d MMM y HH:mm:ss z", Locale.US).withZone(londonTimezone);

    public final String cookieToString(final String key) {
        return cookieToString(key, cookies.get(key));
    }
    public final String cookieToString(final String key, String[] value) {
        final String[] c = value;
        final LocalDateTime time = (c[3] == null ? null
                : toLocalDateTime(SharedObject.londonTimezone, Integer.parseInt(c[3])));
        // Thu, 01 Jan 1970 00:00:00 GMT
        return c[4] + ": " + key + "=" + c[0] + (c[1] == null ? "" : "; path=" + c[1])
                + (c[2] == null ? "" : "; domain=" + c[2])
                + (c[3] == null ? "" : "; expires=" + formatHttpDefaultDate.format(time)) + "\r\n";
    }

    
    
    @Override
    public final String toString() {
        String str = "";
        if(type == TYPE_REQUEST){
            str = method+" "+resource+" HTTP/1.1\r\n";
        }else if(type == TYPE_RESPONSE){
            str = "HTTP/1.1 "+status+" \r\n";
        }
        
        for (Map.Entry<String,String> entry : headers.entrySet()) {
            str += fieldToString(entry.getKey(), entry.getValue());
        }
        
        for (Map.Entry<String,String[]> entry : cookies.entrySet()) {
            str += cookieToString(entry.getKey(), entry.getValue());
        }

        return str;
    }

    /**
     * Check if header is defined.
     * 
     * @param key name of the header.
     * @return true if the header exists, false otherwise.
     */
    public final boolean isDefined(final String key) {
        return headers.get(key) != null;
    }
    
    public final HttpHeaders setStatus(String value){
        status = value;
        return this;
    }
    
    public final HttpHeaders setResource(String value){
        resource = value;
        return this;
    }
    
    public final HttpHeaders setMethod(String value){
        method = value;
        return this;
    }
    
    public final String getStatus(){
        return status;
    }
    
    public final String getResource(){
        return resource;
    }
    
    public final String getMethod(){
        return method;
    }
    
    /**
     * Set the value of a specific header.
     * 
     * @param name  name of the header.
     * @param value value of the header.
     * @return the current HttpHeaders object.
     */
    public final HttpHeaders set(final String name, String value) {
        switch(name){
            case "@Status":
                status = value;
                return this;
            case "@Resource":
                resource = value;
                return this;
            case "@Method":
                method = value;
                return this;
        }
        headers.put(name, value);
        return this;
    }

    /**
     * Set the value of a specific header.
     * 
     * @param name  name of the header.
     * @param value value of the header.
     * @return the current HttpHeaders object.
     */
    public final HttpHeaders set(final String name, Object value) {
        return set(name,value.toString());
    }
    
    
    
    /**
     * Get the value of a specific header.
     * 
     * @param name name of the header
     * @return value of the header as a String.
     */
    public final String get(final String name) {
        switch(name){
            case "@Status":
                return status;
            case "@Resource":
                return resource;
            case "@Method":
                return method;
        }
        if (!headers.containsKey(name)) {
            return null;
        }
        return headers.get(name).trim();
    }

    /**
     * Check if a cookie is set.
     * 
     * @param name name of the cookie.
     * @return true if the cookie is set, false otherwise.
     */
    public final boolean issetCookie(final String name) {
        for(Entry<String,String[]> pair : cookies.entrySet()){
            if (pair.getKey().trim().equals(name.trim())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the value of a specific cookie.
     * 
     * @param name name of the cookie.
     * @return value of the cookie as a String.
     */
    public final String getCookie(final String name) {
        return getCookie(name, "UTF-8");
    }

    public final String getCookie(final String key, final String charset) {
        final String[] cookie = cookies.get(key);
        if (cookie == null)
            return null;
        try {
            return URLDecoder.decode(cookie[0], charset);
        } catch (final UnsupportedEncodingException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            return URLDecoder.decode(cookie[0]);
        }
    }

    /**
     * Set a specific cookie and its properties.
     * 
     * @param name    name of the cookie.
     * @param value   value of the cookie.
     * @param path    this is the path the cookie will be attached to.
     * @param domain  this is the domain the cookie will be attached to.
     * @param expire  the date the cookie should expire on. This value is expected
     *                to be a unix timestamp (in seconds).
     * @param charset charset to use when encoding the cookie.
     */

    public final void setCookie(final String name, final String value, String path, final String domain, final int expire,
            final String charset) {
        if (path == null)
            path = "/";
        final String[] b = new String[5];
        try {
            b[0] = URLEncoder.encode(value, charset);
        } catch (final UnsupportedEncodingException ex) {
            b[0] = URLEncoder.encode(value);
        }
        b[1] = path;
        b[2] = domain;
        b[3] = "" + expire;
        b[4] = "Set-Cookie";
        cookies.put(name.trim(), b);
    }

    /**
     * Set a specific cookie and its properties.
     * 
     * @param name    name of the cookie.
     * @param value   value of the cookie.
     * @param path    this is the path the cookie will be attached to.
     * @param domain  this is the domain the cookie will be attached to.
     * @param charset charset to use when encoding the cookie.
     */
    public final void setCookie(final String name, final String value, final String path, final String domain,
            final String charset) {
        setCookie(name, value, path, domain, 0, charset);
    }

    /**
     * Set a specific cookie and its properties.
     * 
     * @param name    name of the cookie.
     * @param value   value of the cookie.
     * @param path    this is the path the cookie will be attached to.
     * @param charset charset to use when encoding the cookie.
     */
    public final void setCookie(final String name, final String value, final String path, final String charset) {
        setCookie(name, value, path, null, charset);
    }

    /**
     * Set a specific cookie and its properties.
     * 
     * @param name    name of the cookie.
     * @param value   value of the cookie.
     * @param charset charset to use when encoding the cookie.
     */
    public final void setCookie(final String name, final String value, final String charset) {
        setCookie(name, value, "/", null, 0, charset);
    }

    /**
     * Get the headers as a HashMap.
     * 
     * @return headers as HashMap.
     */
    public final HashMap<String, String> getHashMap() {
        return headers;
    }

    private static final Pattern PATTERN_FIRST_LINE_REQUEST = Pattern.compile("^(\\w+)(\\s+)(.+)(\\s+)(HTTP\\/[0-9]\\.[0-9])");
    private static final Pattern PATTERN_FIRST_LINE_RESPONSE = Pattern.compile("^(HTTP\\/[0-9]\\.[0-9])(\\s+)([0-9]+\\s\\w+)");
    /**
     * Parse a line as an http header. If the line is not a valid http header, it
     * will still be added to the header an will be treated as a custom header.
     * 
     * @param line a String containing the http header.
     * @return false if the input is blank, true otherwise.
     */
    public final boolean parseLine(final String line,int i) {
        if (line.trim().equals("")) {
            return false;
        }
        final String[] item = i == 0?new String[0]:line.split(":\\s*", 2);
        if (i > 0 && item.length > 1) {
            if (item[0].equals("Cookie")) {
                final String[] c = item[1].split(";");
                for (final String c1 : c) {
                    final String[] cookieInfo = c1.split("=(?!\\s|\\s|$)");
                    if (cookieInfo.length > 1) {
                        final String[] b = new String[5];
                        b[0] = cookieInfo[1];
                        b[1] = cookieInfo.length > 2 ? cookieInfo[2] : null;
                        b[2] = cookieInfo.length > 3 ? cookieInfo[3] : null;
                        b[3] = cookieInfo.length > 3 ? cookieInfo[3] : null;
                        b[4] = "Cookie";
                        this.cookies.put(cookieInfo[0].replaceFirst("((?<=^)\\s)?", ""), b);
                    }
                }
            } else {
                this.set(item[0], item[1]);
            }
        } else {
            Matcher m = PATTERN_FIRST_LINE_REQUEST.matcher(line);
            
            if(m.matches() && m.groupCount() == 5){
                method = m.group(1);
                resource = m.group(3);
                version = m.group(5);
            }else{
                m = PATTERN_FIRST_LINE_RESPONSE.matcher(line);
                if(m.groupCount() == 3){
                    method = null;
                    resource = m.group(1);
                    version = m.group(3);
                }else 
                    this.set(line, null);
            }
        }
        return true;
    }
    
    /**
     * Get an HttpHeaders object from a string.
     * This will parse http headers and map each one of them by their keys and will also map the cookies with their keys.
     * @param string input string.
     * @return an HttpHeaders object.
     */
    public static final HttpHeaders requestFromString(final String string) {
        final HttpHeaders headers = new HttpHeaders(TYPE_REQUEST);
        final String[] lines = string.split("\\r\\n");
        
        for (int i = 0;i < lines.length;i++) {
            headers.parseLine(lines[i],i);
        }
        return headers;
    }
    
    /**
     * Get an HttpHeaders object from a string.
     * This will parse http headers and map each one of them by their keys and will also map the cookies with their keys.
     * @param string input string.
     * @return an HttpHeaders object.
     */
    public static final HttpHeaders responseFromString(final String string) {
        final HttpHeaders headers = new HttpHeaders(TYPE_RESPONSE);
        final String[] lines = string.split("\\r\\n");
        
        for (int i = 0;i < lines.length;i++) {
            headers.parseLine(lines[i],i);
        }
        return headers;
    }
}