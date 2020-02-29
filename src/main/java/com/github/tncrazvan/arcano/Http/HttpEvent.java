package com.github.tncrazvan.arcano.Http;

import com.github.tncrazvan.arcano.Bean.Http.HttpParam;
import com.github.tncrazvan.arcano.Controller.Http.Get;
import com.github.tncrazvan.arcano.InvalidControllerConstructorException;
import com.github.tncrazvan.arcano.SharedObject;
import static com.github.tncrazvan.arcano.SharedObject.LOGGER;
import com.github.tncrazvan.arcano.Tool.Actions.CompleteAction;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.stream.Stream;

import com.github.tncrazvan.arcano.WebObject;
import com.github.tncrazvan.arcano.Tool.Encoding.JsonTools;
import com.github.tncrazvan.arcano.Tool.Reflect.ConstructorFinder;
import static com.github.tncrazvan.arcano.Tool.Http.Status.STATUS_INTERNAL_SERVER_ERROR;
import com.google.gson.JsonObject;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.logging.Logger;


/**
 *
 * @author Razvan
 */
public class HttpEvent extends HttpEventManager implements JsonTools{
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
            send(raw);
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
            setResponseHeaderField("Content-Length", tmp.length()+"");
            send(tmp);
        }
    }
    
    private static Object[] getMethodParameters(HttpEvent controller, Method method){
        //get query strings
        Parameter[] params = method.getParameters();
        Object[] methodInput = new Object[params.length];
        String paramName;
        for(short i = 0; i < methodInput.length; i++){
            HttpParam annotation = (HttpParam) params[i].getAnnotation(HttpParam.class);
            //if the parameter is not defined as an HttpParam...
            if(annotation == null){
                //set it to null.
                methodInput[i] = null;
                continue;
            }

            paramName = 
                    //if the name of HttpParam is blank...
                    annotation.name().isBlank()?
                    //use the name of the parameter itself
                    params[i].getName()
                    :
                    annotation.name();
            //NOTE: The name oh HttpParam is blank by default.

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
        try {
            if(reader.location.length == 0 || "".equals(reader.location[0]))
                reader.location = new String[]{"/"};
            else {
                File check = new File(reader.so.config.webRoot,reader.stringifiedLocation);
                if(check.exists() && !check.isDirectory()){
                    HttpController controller = new Get();
                    controller.install(reader);
                    Method m = controller.getClass().getDeclaredMethod("file");
                    controller.invokeMethod(m);
                    return controller;
                }
            }
            String type = reader.request.headers.get("@Method");
            int classId = -1;
            String tmp;
            
            for (int i = reader.location.length; i > 0; i--) {
                tmp = type + "/" + String.join("/", Arrays.copyOf(reader.location, i)).toLowerCase();
                //RESOURCE FOUND
                if (SharedObject.ROUTES.containsKey(tmp) && SharedObject.ROUTES.get(tmp).getType().equals(type)) {
                    classId = i - 1;
                    String[] key = Stream
                        .concat(Arrays.stream(new String[] { type }), Arrays.stream(reader.location))
                        .toArray(String[]::new);
                    WebObject wo = resolveClassName(classId + 1, key);
                    CompleteAction<Object,HttpEvent> action = wo.getAction();
                    if(action != null){
                        HttpController controller = new HttpController();
                        controller.install(reader);
                        controller.invokeCompleteAction(action);
                        return controller;
                    }
                    Class<?> cls = Class.forName(wo.getClassName());
                    String methodname = reader.location.length > classId ? wo.getMethodName() : "main";
                    Constructor<?> constructor = ConstructorFinder.getNoParametersConstructor(cls);
                    if (constructor == null) throw new InvalidControllerConstructorException(
                        String.format(
                            "\nController %s does not contain a valid constructor.\n"
                            + "A valid constructor for your controller is a constructor that has no parameters.\n"
                            + "Perhaps your class is an inner class and it's not public or static? Try make it a \"public static class\"!",
                            wo.getClassName()
                        )
                    );
                    HttpController controller = (HttpController) constructor.newInstance();
                    reader.args = resolveMethodArgs(classId + 1, reader.location);
                    controller.install(reader);
                    Method[] methods = controller.getClass().getDeclaredMethods();
                    for(Method m : methods){
                        if(!m.getName().equals(methodname)) continue;
                        controller.invokeMethod(m);
                        return controller;
                    }
                    reader.args = resolveMethodArgs(classId, reader.location);
                    for(Method m : methods){
                        if(!m.getName().equals("main")) continue;
                        controller.invokeMethod(m);
                        return controller;
                    }
                    throw new NoSuchMethodException("Method Name not set.");
                }
            }
            //TRY SERVE THE DEFAULT RESOURCE
            if(reader.location[0].equals("/") && reader.so.ROUTES.containsKey(SharedObject.HTTP_SERVICE_TYPE_DEFAULT)){
                WebObject wo = reader.so.ROUTES.get(SharedObject.HTTP_SERVICE_TYPE_DEFAULT);
                if(wo.getAction() != null){
                    CompleteAction<Object,HttpEvent> action = wo.getAction();
                    HttpController controller = new HttpController();
                    controller.install(reader);
                    controller.invokeCompleteAction(action);
                        return controller;
                }else {
                    Class<?> cls = Class.forName(wo.getClassName());
                    Constructor<?> constructor = cls.getDeclaredConstructor();
                    String methodname = wo.getMethodName();
                    HttpController controller = (HttpController) constructor.newInstance();
                    Method[] methods = controller.getClass().getDeclaredMethods();
                    for(Method m : methods){
                        if(!m.getName().equals(methodname)) continue;    
                        controller.install(reader);
                        controller.invokeMethod(m);
                        return controller;
                    }
                    return instantPackStatus(reader,STATUS_INTERNAL_SERVER_ERROR,"");
                }
            }else if(reader.so.ROUTES.containsKey(SharedObject.HTTP_SERVICE_TYPE_404)){
                WebObject o = reader.so.ROUTES.get(SharedObject.HTTP_SERVICE_TYPE_404);
                if(o.getAction() != null){
                    CompleteAction<Object,HttpEvent> action = o.getAction();
                    HttpController controller = new HttpController();
                    controller.install(reader);
                    controller.invokeCompleteAction(action);
                        return controller;
                }else {
                    Class<?> cls = Class.forName(o.getClassName());
                    Constructor<?> constructor = cls.getDeclaredConstructor();
                    String methodname = o.getMethodName();
                    if (constructor == null) throw new InvalidControllerConstructorException(
                        String.format(
                            "\nController %s does not contain a valid constructor.\n"
                            + "A valid constructor for your controller is a constructor that has no parameters.\n"
                            + "Perhaps your class is an inner class and it's not static or public? Try make it a \"static public class\"!",
                            cls.getName()
                        )
                    );
                    HttpController controller = (HttpController) constructor.newInstance();
                    Method[] methods = controller.getClass().getDeclaredMethods();
                    for(Method m : methods){
                        if(!m.getName().equals(methodname)) continue;    
                        controller.install(reader);
                        controller.invokeMethod(m);
                        return controller;
                    }
                    return instantPackStatus(reader,STATUS_INTERNAL_SERVER_ERROR,"");
                }
            }
            return instantPackStatus(reader,STATUS_INTERNAL_SERVER_ERROR,"");
        }catch(InvalidControllerConstructorException e){
            LOGGER.log(Level.SEVERE, null, e);
            return instantPackStatus(reader,STATUS_INTERNAL_SERVER_ERROR,"");
        }catch (InstantiationException e){
            LOGGER.log(Level.SEVERE, null, e);
            return instantPackStatus(reader,STATUS_INTERNAL_SERVER_ERROR,"");
        }catch (ClassNotFoundException e){
            LOGGER.log(Level.SEVERE, null, e);
            return instantPackStatus(reader,STATUS_INTERNAL_SERVER_ERROR,"");
        } catch (NoSuchMethodException e) {
            LOGGER.log(Level.SEVERE, null, e);
            return instantPackStatus(reader,STATUS_INTERNAL_SERVER_ERROR,"");
        } catch (SecurityException e) {
            LOGGER.log(Level.SEVERE, null, e);
            return instantPackStatus(reader,STATUS_INTERNAL_SERVER_ERROR,"");
        } catch (IllegalAccessException e) {
            LOGGER.log(Level.SEVERE, null, e);
            return instantPackStatus(reader,STATUS_INTERNAL_SERVER_ERROR,"");
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.SEVERE, null, e);
            return instantPackStatus(reader,STATUS_INTERNAL_SERVER_ERROR,"");
        } catch (InvocationTargetException e) {
            LOGGER.log(Level.SEVERE, null, e);
            return instantPackStatus(reader,STATUS_INTERNAL_SERVER_ERROR,"");
        }
    }
    
    private static HttpController instantPackStatus(HttpRequestReader reader,String status,String message){
        HttpController controller = new HttpController();
        controller.install(reader);
        controller.setResponseStatus(status);
        controller.send(message);
        return controller;
    }

    // public static void onControllerRequest(final StringBuilder url,String
    // httpMethod,String httpNotFoundNameOriginal,String httpNotFoundName) {
    public static final void onControllerRequest(final HttpRequestReader reader) {
        final HttpController controller = factory(reader);
        if(controller != null ) controller.close();
    } 
}
