package com.github.tncrazvan.arcano.Http;

import com.github.tncrazvan.arcano.SharedObject;
import static com.github.tncrazvan.arcano.SharedObject.LOGGER;
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
import java.util.logging.Level;
import static com.github.tncrazvan.arcano.Tool.Time.now;

/**
 *
 * @author Razvan
 */
public class HttpHeaders {
    private final HashMap<String, String> headers = new HashMap();
    public Map<String, String[]> cookies = new HashMap();
    public HttpHeaders(final boolean createSuccessHeader) {
        this(new HashMap<String,String>(){{}}, createSuccessHeader);
    }
    public HttpHeaders(HashMap<String, String> map,final boolean createSuccessHeader) {
        if (createSuccessHeader) {
            headers.put("@Status", "HTTP/1.1 200 OK");
            headers.put("Date", formatHttpDefaultDate.format(now()));
            headers.put("Cache-Control", "no-store");
        }
        
        map.forEach((key, value) -> {
            headers.put(key, value);
        });
    }

    public HttpHeaders() {
        this(true);
    }
    public HttpHeaders(HashMap<String, String> map) {
        this(map, true);
    }

    public String fieldToString(final String key) {
        final String value = headers.get(key);

        if (key.equals("@Resource") || key.equals("@Status") || value.equalsIgnoreCase(key)) {
            return value + "\r\n";
        }
        return key + ": " + value + "\r\n";
    }
    
    private static ZoneId londonTimezone = ZoneId.of("Europe/London");
    private static DateTimeFormatter formatHttpDefaultDate = DateTimeFormatter.ofPattern("EEE, d MMM y HH:mm:ss z",Locale.US).withZone(londonTimezone);
    public String cookieToString(final String key) {
        final String[] c = cookies.get(key);
        final LocalDateTime time = (c[3] == null ? null : now(SharedObject.londonTimezone,Integer.parseInt(c[3])));
        // Thu, 01 Jan 1970 00:00:00 GMT
        return c[4] + ": " + key + "=" + c[0] + (c[1] == null ? "" : "; path=" + c[1])
                + (c[2] == null ? "" : "; domain=" + c[2])
                + (c[3] == null ? "" : "; expires=" + formatHttpDefaultDate.format(time)) + "\r\n";
    }

    @Override
    public String toString() {
        String str = "";
        str = headers.keySet().stream().map((key) -> this.fieldToString(key)).reduce(str, String::concat);

        str = cookies.keySet().stream().map((key) -> this.cookieToString(key)).reduce(str, String::concat);
        return str;
    }
    
    /**
     * Check if header is defined.
     * @param key name of the header.
     * @return true if the header exists, false otherwise.
     */
    public boolean isDefined(final String key) {
        return headers.get(key) != null;
    }

    /**
     * Set the value of a specific header.
     * @param name name of the header.
     * @param value value of the header.
     */
    public void set(final String name, String value) {
        if (name.equals("@Status"))
            value = "HTTP/1.1 " + value;
        headers.put(name, value);
    }

    /**
     * Get the value of a specific header.
     * @param name name of the header
     * @return value of the header as a String.
     */
    public String get(final String name) {
        if (!headers.containsKey(name)) {
            return null;
        }
        return headers.get(name).trim();
    }

    /**
     * Check if a cookie is set.
     * @param name name of the cookie.
     * @return true if the cookie is set, false otherwise.
     */
    public boolean issetCookie(final String name) {
        final Iterator i = cookies.entrySet().iterator();
        Map.Entry pair;
        String tmp = "";
        while (i.hasNext()) {
            pair = (Map.Entry) i.next();
            tmp = (String) pair.getKey();
            if (tmp.trim().equals(name.trim())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the value of a specific cookie.
     * @param name name of the cookie.
     * @return value of the cookie as a String.
     */
    public String getCookie(final String name) {
        return getCookie(name,"UTF-8");
    }
    public String getCookie(final String key, String charset) {
        final String[] cookie = cookies.get(key);
        if (cookie == null)
            return null;
        try{
            return URLDecoder.decode(cookie[0], charset);
        }catch(UnsupportedEncodingException ex){
            LOGGER.log(Level.SEVERE, null, ex);
            return URLDecoder.decode(cookie[0]);
        }
    }
    /**
     * Set a specific cookie and its properties.
     * @param name name of the cookie.
     * @param value value of the cookie.
     * @param path this is the path the cookie will be attached to.
     * @param domain this is the domain the cookie will be attached to.
     * @param expire the date the cookie should expire. This value is expected to be a unix timestamp (in seconds).
     * @param charset charset to use when encoding the cookie.
     */

    public void setCookie(final String name, final String value, String path, final String domain, final int expire, String charset){
        if (path == null)
            path = "/";
        final String[] b = new String[5];
        try{
            b[0] = URLEncoder.encode(value, charset);
        }catch(UnsupportedEncodingException ex){
            b[0] = URLEncoder.encode(value);
        }
        b[1] = path;
        b[2] = domain;
        b[3] = ""+expire;
        b[4] = "Set-Cookie";
        cookies.put(name.trim(), b);
    }

    /**
     * Set a specific cookie and its properties.
     * @param name name of the cookie.
     * @param value value of the cookie.
     * @param path this is the path the cookie will be attached to.
     * @param domain this is the domain the cookie will be attached to.
     * @param charset charset to use when encoding the cookie.
     */
    public void setCookie(final String name, final String value, final String path, final String domain, String charset){
        setCookie(name, value, path, domain, 0, charset);
    }
    
    /**
     * Set a specific cookie and its properties.
     * @param name name of the cookie.
     * @param value value of the cookie.
     * @param path this is the path the cookie will be attached to.
     * @param charset charset to use when encoding the cookie.
     */
    public void setCookie(final String name, final String value, final String path, String charset){
        setCookie(name, value, path, null, charset);
    }

    /**
     * Set a specific cookie and its properties.
     * @param name name of the cookie.
     * @param value value of the cookie.
     * @param charset charset to use when encoding the cookie.
     */
    public void setCookie(final String name, final String value, String charset){
        setCookie(name, value, "/", null, 0,charset);
    }

    /**
     * Get the headers as a HashMap.
     * @return headers as HashMap.
     */
    public HashMap<String, String> getHashMap() {
        return headers;
    }
    
    /**
     * get an HttpHeaders object from a HashMap.
     * @param map the HashMap to read.
     * @return the HttpHeaders object.
     */
    public static HttpHeaders fromHashMap(HashMap<String,String> map){
        return new HttpHeaders(map,false);
    }
    
    /**
     * Parse a line as an http header. If the line is not a valid http header, it will still be added to the header an will be treated as a custom header.
     * @param line a String containing the http header.
     * @return false if the input is blank, true otherwise.
     */
    public boolean parseLine(String line){
        if (line.trim().equals("")) {
            return false;
        }
        final String[] item = line.split(":\\s*", 2);
        if (item.length > 1) {
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
            if (line.matches("^.+(?=\\s\\/).*HTTPS?\\/.*$")) {
                final String[] parts = line.split("\\s+");
                this.set("Method",parts[0]);
                this.set("@Resource",parts[1]);
                this.set("Version",parts[2]);
            } else {
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
    public static HttpHeaders fromString(final String string) {
        final HttpHeaders headers = new HttpHeaders(false);
        final String[] lines = string.split("\\r\\n");
        for (final String line : lines) {
            headers.parseLine(line);
        }
        return headers;
    }
}
