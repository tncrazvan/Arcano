package com.github.tncrazvan.arcano.Http;

import static com.github.tncrazvan.arcano.Tool.Http.ContentType.resolveContentType;
import static com.github.tncrazvan.arcano.Tool.Encoding.JsonTools.jsonStringify;
import com.github.tncrazvan.arcano.Tool.System.ServerFile;
import com.google.gson.JsonArray;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import com.github.tncrazvan.arcano.Tool.Actions.VoidAction;

/**
 *
 * @author Administrator
 */
public class HttpResponse {
    private final HttpHeaders headers;
    private Object content;
    private final Class<?> type;
    private boolean raw;
    private VoidAction action = null;

    /**
     * Executes an action after the getContent(true) gets called.
     * @param action the action to execute.
     * @return the current HttpResponse.
     */
    public final HttpResponse then(final VoidAction action) {
        this.action = action;
        return this;
    }

    /**
     * Executes the specified action.
     */
    public final void todo() {
        if (action != null)
            action.callback();
    }

    public HttpResponse(final HttpHeaders headers, final Object content) {
        this((HashMap<String, String>) headers.getHashMap(), content);
    }

    public HttpResponse(final Object content) {
        this(new HashMap<String, String>(), content);
    }

    public HttpResponse(final HttpHeaders headers) {
        this((HashMap<String, String>) headers.getHashMap(), null);
    }

    public HttpResponse(final HashMap<String, String> headers) {
        this(headers, null);
    }

    public HttpResponse(final HashMap<String, String> headers, final Object content) {
        raw = false;
        type = content.getClass();
        this.headers = new HttpHeaders(headers);
        this.content = content;
    }

    /**
     * Resolve the output result of this http response.
     * This response may contain different types of objects such as Strings ints,<br />
     * JsonArrays, JsonObjects, custom Objects and so on.<br />
     * This method resolves that result into something that the http standard can understand,
     * For example custom objects are converted into JsonObjects and File objects in particular
     * are converted into raw byte arrays.
     * @return 
     */
    public final HttpResponse resolve() {
        try {
            if (type == JsonArray.class) {
                if (headers != null && !headers.isDefined("Content-Type")) {
                    headers.set("Content-Type", "application/json");
                }
                final String tmp = ((JsonArray) content).toString();
                this.content = tmp;
            } else if (type == File.class || type == ServerFile.class) {
                final File file = (File) content;
                if (headers != null && !headers.isDefined("Content-Type")) {
                    headers.set("Content-Type", resolveContentType(file.getName()));
                }
                final FileInputStream fis = new FileInputStream(file);
                this.content = fis.readAllBytes();
                fis.close();
                raw = true;
            } else if (type == String.class || type == Integer.class || type == Float.class || type == Double.class
                    || type == Boolean.class || type == Byte.class || type == Character.class || type == Short.class
                    || type == Long.class) {
                this.content = String.valueOf(content);
                if (headers != null && !headers.isDefined("Content-Type")) {
                    headers.set("Content-Type", "text/plain");
                }
            } else if (type == byte[].class) {
                if (headers != null && !headers.isDefined("Content-Type")) {
                    headers.set("Content-Type", "text/plain");
                }
                raw = true;
            } else {
                if(type.isArray()){
                    this.content = jsonStringify((Object[])content);
                }else{
                    this.content = jsonStringify(content);
                }
            }
        } catch (final UnsupportedEncodingException ex) {
            this.content = ex.getMessage().getBytes();
        } catch (final FileNotFoundException ex) {
            this.content = ex.getMessage();
        } catch (final IOException ex) {
            this.content = ex.getMessage();
        }
        return this;
    }

    /**
     * Get the headers of this response as a HashMap.
     * 
     * @return
     */
    public final HashMap<String, String> getHashMapHeaders() {
        return headers.getHashMap();
    }

    /**
     * Get the HttpHeaders of this response.
     * 
     * @return
     */
    public final HttpHeaders getHttpHeaders() {
        return headers;
    }

    /**
     * Get the payload of the response and execute the action if so specified. You
     * can specify the action by calling "then(Action a)" on the current
     * HttpResponse object.
     * 
     * @param todo if true the action will be executed other the action won't be
     *             executed.
     * @return the payload of the response.
     */
    public final Object getContent(final boolean todo) {
        if (todo)
            this.todo();
        return content;
    }

    /**
     * Get the payload of the response.
     * 
     * @return the payload of the response.
     */
    public final Object getContent() {
        return getContent(true);
    }

    /**
     * Specifies wether or not the payload should be treated as raw binary data or
     * not.
     * 
     * @param value if true the payload will be treated as raw binary data,
     *              otherwise it will be treated as a String.
     */
    public final void setRaw(final boolean value) {
        raw = value;
    }
    
    /**
     * Check is the payload is meant to be treated as raw binary data or not.
     * @return true if the content is meant to be raw, false otherwise.
     */
    public final boolean isRaw(){
        return raw;
    }
    
    /**
     * Get the type of the payload.
     * This is mainly an utility for the server.
     * It helps converting JSON objects, arrays, Strings and other objects accordingly.
     * @return the Class<?> of the payload.
     */
    public final Class<?> getType(){
        return type;
    }
}
