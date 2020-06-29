package com.github.tncrazvan.arcano.http;

import static com.github.tncrazvan.arcano.SharedObject.LOGGER;
import static com.github.tncrazvan.arcano.tool.http.Status.STATUS_INTERNAL_SERVER_ERROR;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.regex.Matcher;

import com.github.tncrazvan.arcano.SharedObject;
import com.github.tncrazvan.arcano.WebObject;
import com.github.tncrazvan.arcano.bean.http.HttpServiceParam;
import com.github.tncrazvan.arcano.tool.action.CompleteAction;
import com.github.tncrazvan.arcano.tool.encoding.JsonTools;
import com.google.gson.JsonObject;


/**
 *
 * @author Razvan Tanase
 */
public class HttpEvent extends HttpEventManager implements JsonTools{
    public HashMap<String, String> params = new HashMap<>();
    private void sendHttpResponse(Exception e){
        final String message = e.getMessage();
        final HttpResponse response = new HttpResponse(message == null ? "" : message);
        sendHttpResponse(response,true);
    }
    private void sendHttpResponse(InvocationTargetException e){
        final String message = e.getTargetException().getMessage();
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
    
    private static Object[] getMethodParameters(HttpEvent controller, Method method){
        //get query strings
        Parameter[] params = method.getParameters();
        Object[] methodInput = new Object[params.length];
        String paramName;
        for(short i = 0; i < methodInput.length; i++){
            HttpServiceParam annotation = (HttpServiceParam) params[i].getAnnotation(HttpServiceParam.class);
            //if the parameter is not defined as an HttpServiceParam...
            if(annotation == null){
                //set it to null.
                methodInput[i] = null;
                continue;
            }

            paramName = 
                    //if the name of HttpServiceParam is blank...
                    annotation.name().isBlank()?
                    //use the name of the parameter itself
                    params[i].getName()
                    :
                    annotation.name();
            //NOTE: The name oh HttpServiceParam is blank by default.

            //if the query string exists...
            if(controller.issetRequestQueryString(paramName)){
                //...use it.
                methodInput[i] = controller.getRequestQueryStringAsObject(paramName);
            }else{
                //otherwise set the param to null.
                methodInput[i] = null;
            }
        }
        return methodInput;
    }
    
    public final void invokeCompleteAction(final CompleteAction<Object,HttpEvent> action){
        try {
            Object result = action.callback(this);

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
            setResponseStatus(STATUS_INTERNAL_SERVER_ERROR);
            if (reader.so.config.sendExceptions) {
                sendHttpResponse(e);
            } else
                sendHttpResponse(SharedObject.RESPONSE_EMPTY);
        }
    }
    
    public final void invokeMethod(final Method method) throws IllegalAccessException,
        IllegalArgumentException, InvocationTargetException, InvocationTargetException {
        try {
            Class<?> type = method.getReturnType();
            Object[] params = getMethodParameters(this, method);

            //try to invokeMethod method
            if (type == ShellScript.class) {
                final ShellScript script = (ShellScript) method.invoke(this,params);
                script.execute(this);
            }else if (type == void.class || type == Void.class) {
                method.invoke(this,params);
                sendHttpResponse(SharedObject.RESPONSE_EMPTY);
            } else if (type == HttpResponse.class) {
                final HttpResponse response = (HttpResponse) method.invoke(this,params);
                response.resolve();
                final HashMap<String, String> localHeaders = response.getHashMapHeaders();
                if (localHeaders != null) {
                    localHeaders.forEach((key, header) -> {
                        setResponseHeaderField(key, header);
                    });
                }
                setResponseStatus(response.getHttpHeaders().getStatus());
                sendHttpResponse(response);
            } else {
                // if it's some other type of object...
                final Object data = method.invoke(this,params);
                sendHttpResponse(new HttpResponse(data == null ? "" : data).resolve());
            }
        } catch (final InvocationTargetException  e) {
            setResponseStatus(STATUS_INTERNAL_SERVER_ERROR);
            if (reader.so.config.sendExceptions) {
                sendHttpResponse(e);
            } else
                sendHttpResponse(SharedObject.RESPONSE_EMPTY);
        }catch (IllegalAccessException | IllegalArgumentException e) {
            setResponseStatus(STATUS_INTERNAL_SERVER_ERROR);
            if (reader.so.config.sendExceptions) {
                sendHttpResponse(e);
            } else
                sendHttpResponse(SharedObject.RESPONSE_EMPTY);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }
    
    private static HttpController factory(HttpRequestReader reader) {
        
        String type = reader.request.headers.getMethod();
        HashMap<String, WebObject> method = reader.so.HTTP_ROUTES.get(type);
        for (int i = reader.location.length; i > 0; i--) {
            String path = String.join("/", Arrays.copyOf(reader.location, i)).toLowerCase();
            if(path.equals(""))
                path="/";
            if(method != null){
                WebObject route = null;
                for(Entry<String,WebObject> item : method.entrySet()){
                    if(route != null) break;
                    Matcher matcher = item.getValue().getPattern().matcher(path);
                    if(route == null && matcher.find()){
                        int len = matcher.groupCount();
                        if(len != reader.location.length - i){
                            //Number of parameters don't match!
                            break;
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
                    CompleteAction<Object,HttpEvent> action = route.getAction();
                    if(action != null){
                        HttpController controller = new HttpController();
                        controller.params = route.paramMap;
                        controller.install(reader);
                        controller.invokeCompleteAction(action);
                        return controller;
                    }
                }
            }
        }
        if(method != null){
            WebObject route = method.get("@404");
            //If resource has been found...
            if(route != null){
                //..try to serve it
                CompleteAction<Object,HttpEvent> action = route.getAction();
                if(action != null){
                    HttpController controller = new HttpController();
                    controller.install(reader);
                    controller.invokeCompleteAction(action);
                    return controller;
                }
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
