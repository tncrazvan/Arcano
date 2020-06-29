package com.github.tncrazvan.arcano.http;

import static com.github.tncrazvan.arcano.tool.encoding.JsonTools.jsonStringify;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import com.github.tncrazvan.arcano.tool.action.VoidAction;
import com.github.tncrazvan.arcano.tool.http.Status;
import com.github.tncrazvan.arcano.tool.system.ServerFile;
import com.google.gson.JsonArray;

/**
 *
 * @author Razvan Tanase
 */
public class HttpResponse {
    private boolean exception = false;
    private final HttpHeaders headers;
    private Object content;
    private final Class<?> type;
    private boolean raw;
    private VoidAction action = null;

    public final void setException(boolean exception){
        this.exception = exception;
    }
    
    public final boolean isException(){
        return exception;
    }
    
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
        raw = false;
        type = content.getClass();
        this.headers = headers;
        this.content = content;
    }

    public HttpResponse(final Object content) {
        this(HttpHeaders.response(), content);
    }

    public HttpResponse(final HttpHeaders headers) {
        this(headers, null);
    }

    public HttpResponse(final HashMap<String, String> headers) {
        this(headers, null);
    }

    public HttpResponse(final HashMap<String, String> headers, final Object content) {
        this(HttpHeaders.response(headers), content);
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
                final ServerFile file = (ServerFile) content;
                
                if(file.getRanges().size() > 1){
                    if(headers != null){
                        headers.setStatus(Status.STATUS_PARTIAL_CONTENT);
                        headers.set("Content-Type","multipart/byteranges; boundary="+file.getMultipartBoundary());
                    }
                    this.content = file.readAsMultipart(headers);
                }else if(file.getRanges().size() > 0){
                    if(headers != null){
                        headers.setStatus(Status.STATUS_PARTIAL_CONTENT);
                        if(!headers.isDefined("Content-Type")){
                            headers.set("Content-Type", file.getContentType());
                        }
                        headers.set("Content-Range", file.getContentRange());
                    }
                    int start = file.getRanges().get(0)[0];
                    int end = file.getRanges().get(0)[1];
                    if(end < 0)
                        end = (int)file.length()-start;
                    int length = end-start+1;
                    this.content = file.read(start, length);
                }else{
                    if(headers != null && !headers.isDefined("Content-Type")){
                        headers.set("Content-Type", file.getContentType());
                    }
                    this.content = file.read();
                }
                
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
