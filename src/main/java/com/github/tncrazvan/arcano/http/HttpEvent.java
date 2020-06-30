package com.github.tncrazvan.arcano.http;

import static com.github.tncrazvan.arcano.tool.http.Status.STATUS_INTERNAL_SERVER_ERROR;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;

import com.github.tncrazvan.arcano.SharedObject;
import com.github.tncrazvan.arcano.WebObject;
import com.github.tncrazvan.arcano.tool.action.HttpEventAction;
import com.google.gson.JsonObject;


/**
 *
 * @author Razvan Tanase
 */
public class HttpEvent extends HttpEventManager{

    private void sendHttpResponse(Exception e){
        final String message = e.getMessage();
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
            setResponseHeaderField("Content-Length", raw.length+"");
            push(raw);
        } else {
            final Object content = response.getContent();
            String tmp = content.toString();
            if (reader.so.config.responseWrapper) {
                if(!issetResponseHeaderField("Content-Type"))
                    setResponseHeaderField("Content-Type", "application/json");
                final JsonObject obj = new JsonObject();
                obj.addProperty(exception?"exception":"result", tmp);
                tmp = obj.toString();
            }
            //setResponseHeaderField("Content-Length", tmp.length()+"");
            push(tmp);
        }
    }

    public final void activateWebObject(final WebObject route){
        try {
            Object result = route.getAction().callback(this);

            //try to invokeMethod method
            if (result instanceof ShellScript) {
                final ShellScript script = (ShellScript) result;
                script.execute(this);
            }else if (result instanceof Void) {
                sendHttpResponse(SharedObject.RESPONSE_EMPTY);
            } else if (result instanceof HttpResponse) {
                final HttpResponse response = (HttpResponse) result;
                response.resolve();
                final HashMap<String, String> localHeaders = response.getHashMapHeaders();
                setResponseStatus(response.getHttpHeaders().getStatus());
                if (localHeaders != null) {
                    localHeaders.forEach((key, header) -> {
                        setResponseHeaderField(key, header);
                    });
                }
                sendHttpResponse(response);
            } else {
                // if it's some other type of object...
                sendHttpResponse(new HttpResponse(result == null ? "" : result).resolve());
            }
        } catch (final Exception  e) {
            e.printStackTrace();
            setResponseStatus(STATUS_INTERNAL_SERVER_ERROR);
            if (reader.so.config.sendExceptions) {
                sendHttpResponse(e);
            } else
                sendHttpResponse(SharedObject.RESPONSE_EMPTY);
        }
    }
    
    private static HttpController factory(HttpRequestReader reader) {
        String type = reader.request.headers.getMethod();
        HashMap<String, WebObject> method = reader.so.HTTP_ROUTES.get(type);
 
            String path = String.join("/", reader.location).toLowerCase();
            if(path.equals(""))
                path="/";
            if(method != null){
                WebObject route = null;
                for(Entry<String,WebObject> item : method.entrySet()){
                    if(route != null) break;
                    Matcher matcher = item.getValue().getPattern().matcher(path);
                    if(route == null && matcher.find()){
                        int len = matcher.groupCount();
                        if(len != item.getValue().paramNames.size()){
                            //Number of parameters don't match!
                            continue;
                        }
                        if(route == null){
                            route = method.get(item.getValue().getPath());
                            if(len >= 1)
                                for(int j = 1; j <= len; j++){
                                    String group = matcher.group(j);
                                    route.paramMap.put(route.paramNames.get(j-1), group);
                                }
                        }
                    }
                }
                
                //If resource has been found...
                if(route != null){
                    //..try to serve it
                    HttpController controller = new HttpController();
                    controller.requestParameters = route.paramMap;
                    controller.install(reader);
                    controller.activateWebObject(route);
                    return controller;
                }
            }
        
        if(method != null){
            WebObject route = method.get("@404");
            //If resource has been found...
            if(route != null){
                //..try to serve it
                HttpController controller = new HttpController();
                controller.install(reader);
                controller.activateWebObject(route);
                return controller;
            }
        }
        
        //fallback 404 response is supposed to be define inside the "/" route.
        return instantPackStatus(reader,STATUS_INTERNAL_SERVER_ERROR,"Fallback route \"@404\" is not defined.");
    }
    
    private static HttpController instantPackStatus(HttpRequestReader reader,String status,String message){
        HttpController controller = new HttpController();
        controller.install(reader);
        controller.setResponseStatus(status);
        controller.push(message);
        return controller;
    }

    // public static void serve(final StringBuilder url,String
    // httpMethod,String httpNotFoundNameOriginal,String httpNotFoundName) {
    public static final void serve(final HttpRequestReader reader) {
        final HttpController controller = factory(reader);
        if(controller != null ) controller.close();
    } 
}
