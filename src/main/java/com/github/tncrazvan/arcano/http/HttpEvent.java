package com.github.tncrazvan.arcano.http;

import static com.github.tncrazvan.arcano.tool.http.Status.STATUS_INTERNAL_SERVER_ERROR;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.tncrazvan.arcano.SharedObject;
import com.github.tncrazvan.arcano.WebObject;
import com.google.gson.JsonObject;


/**
 *
 * @author Razvan Tanase
 */
public class HttpEvent extends HttpEventManager{
    public HttpEvent(HttpRequestReader reader, SharedObject so) throws UnsupportedEncodingException {
        super(reader,so);
        HttpHeaders headers = HttpHeaders.response();
        for (Map.Entry<String, String> entry : so.config.headers.entrySet()) {
            headers.set(entry.getKey(), entry.getValue());
        }
        this.response.setHttpHeaders(headers);
        this.request.resolveId();
        this.request.findLanguages();
    }

    private void sendHttpResponse(Exception e){
        String message = Stream
                        .of(e.getStackTrace())
                        .map(StackTraceElement::toString)
                        .collect(Collectors.joining("\n"));
        final HttpResponse response = new HttpResponse(message == null ? "" : message);
        sendHttpResponse(response,true);
    }

    private void sendHttpResponse(HttpResponse response){
        sendHttpResponse(response, false);
    }
    private void sendHttpResponse(HttpResponse response,boolean exception){
        if (response.isRaw()) {
            final Object content = response.getContent();
            final byte[] raw = content == null?new byte[]{}:(byte[])content;
            this.response.headers.set("Content-Length", raw.length+"");
            push(raw);
        } else {
            final Object content = response.getContent();
            String tmp = content.toString();
            if (so.config.responseWrapper) {
                if(!this.response.headers.isDefined("Content-Type"))
                    this.response.headers.set("Content-Type", "application/json");
                final JsonObject obj = new JsonObject();
                obj.addProperty(exception?"exception":"result", tmp);
                tmp = obj.toString();
            }
            //setResponseHeaderField("Content-Length", tmp.length()+"");
            push(tmp);
        }
    }

    private final void activateWebObject(final WebObject route){
        try {
            Object result = route.getHttpEventAction().callback(this);
            //try to invokeMethod method
            if (result instanceof ShellScript) {
                final ShellScript script = (ShellScript) result;
                script.execute(this);
            }else if (result instanceof Void) {
                sendHttpResponse(SharedObject.HTTP_RESPONSE_EMPTY);
            } else if (result instanceof HttpResponse) {
                final HttpResponse response = (HttpResponse) result;
                response.resolve();
                final HashMap<String, String> localHeaders = response.getHashMapHeaders();
                this.response.headers.setStatus(response.getHttpHeaders().getStatus());
                if (localHeaders != null) {
                    localHeaders.forEach((key, header) -> {
                        this.response.headers.set(key, header);
                    });
                }
                sendHttpResponse(response);
            } else {
                // if it's some other type of object...
                final HttpResponse response = new HttpResponse(result == null ? "" : result);
                response.resolve();
                sendHttpResponse(response);
            }
        } catch (final Exception  e) {
            e.printStackTrace();
            this.response.headers.setStatus(STATUS_INTERNAL_SERVER_ERROR);
            if (so.config.sendExceptions) {
                sendHttpResponse(e);
            } else
                sendHttpResponse(SharedObject.HTTP_RESPONSE_EMPTY);
        }
    }
    
    private static HttpEvent instantPackStatus(HttpRequestReader reader,SharedObject so,String status,String message)
            throws UnsupportedEncodingException {
        HttpEvent event = new HttpEvent(reader,so);
        event.response.headers.setStatus(status);
        event.push(message);
        return event;
    }

    // public static void serve(final StringBuilder url,String
    // httpMethod,String httpNotFoundNameOriginal,String httpNotFoundName) {
    public static final void serve(final HttpRequestReader reader, final SharedObject so) throws UnsupportedEncodingException {
        String type = reader.content.headers.getMethod();
        HashMap<String, WebObject> method = so.HTTP_ROUTES.get(type);
 
        String path = String.join("/", reader.location).toLowerCase();
        if(path.equals(""))
            path="/";
        if(method != null){
            WebObject wo = null;
            for(Entry<String,WebObject> item : method.entrySet()){
                if(wo != null) break;
                Matcher matcher = item.getValue().getPattern().matcher(path);
                if(wo == null && matcher.find()){
                    int len = matcher.groupCount();
                    if(len != item.getValue().paramNames.size()){
                        //Number of parameters don't match!
                        continue;
                    }
                    if(wo == null){
                        wo = method.get(item.getValue().getPath());
                        if(len >= 1)
                            for(int j = 1; j <= len; j++){
                                String group = matcher.group(j);
                                wo.paramMap.put(wo.paramNames.get(j-1), group);
                            }
                    }
                }
            }
            
            //If resource has been found...
            if(wo != null){
                //..try to serve it
                HttpEvent event = new HttpEvent(reader,so);
                event.request.parameters = wo.paramMap;
                event.activateWebObject(wo);
                return;
            }
        }
        
        if(method != null){
            WebObject wo = method.get("@404");
            //If resource has been found...
            if(wo != null){
                //..try to serve it
                HttpEvent event = new HttpEvent(reader,so);
                event.activateWebObject(wo);
                return;
            }
        }
        
        //fallback 404 response is supposed to be define inside the "/" route.
        instantPackStatus(reader,so,STATUS_INTERNAL_SERVER_ERROR,"Fallback route \"@404\" is not defined.");
    } 
}
