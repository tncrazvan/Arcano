package com.github.tncrazvan.arcano.Http;

import com.github.tncrazvan.arcano.Bean.Http.HttpParam;
import com.github.tncrazvan.arcano.Controller.Http.Get;
import com.github.tncrazvan.arcano.InvalidControllerConstructorException;
import com.github.tncrazvan.arcano.SharedObject;
import static com.github.tncrazvan.arcano.SharedObject.LOGGER;
import com.github.tncrazvan.arcano.Tool.Actions.CompleteAction;
import com.github.tncrazvan.arcano.Tool.Cluster.NoSecretFoundException;
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
import static com.github.tncrazvan.arcano.Tool.Http.Status.STATUS_LOCKED;
import static com.github.tncrazvan.arcano.Tool.Http.Status.STATUS_NOT_FOUND;
import com.github.tncrazvan.arcano.Tool.Security.JwtMessage;
import com.google.gson.JsonObject;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;


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
    
    public final void invokeCompleteAction(final CompleteAction<Object,HttpRequestReader> action){
        try {
            Object result = action.callback(reader);

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

    private static HttpController serveController(final HttpRequestReader reader)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, ClassNotFoundException,
            IllegalArgumentException, InvocationTargetException, UnsupportedEncodingException, IOException {
        final String httpMethod = reader.request.headers.get("@Method");
        //final boolean abusiveUrl = Regex.match(reader.location.toString(), "w3e478tgdf8723qioiuy");
        Method method = null;
        Class<?> cls = null;
        Constructor<?> constructor = null;
        if (reader.location.length == 0) {
            reader.location = new String[] { "" };
        }else {
            File check = new File(reader.so.config.webRoot,reader.stringifiedLocation);
            if(check.exists() && !check.isDirectory()){
                HttpController controller = new Get();
                controller.install(reader);
                method = controller.getClass().getDeclaredMethod("file");
                controller.invokeMethod(method);
                return controller;
            }
        }
        try {
            int classId = getClassnameIndex(reader.location, httpMethod);
            final String[] typedLocation = Stream
                    .concat(Arrays.stream(new String[] { httpMethod }), Arrays.stream(reader.location))
                    .toArray(String[]::new);
            WebObject wo = resolveClassName(classId + 1, typedLocation);
            
            CompleteAction<Object,HttpRequestReader> action = wo.getAction();
            
            if (wo.isLocked()) {
                if (!reader.request.headers.issetCookie("JavaArcanoKey")) {
                    throw new NoSecretFoundException("An unauthorized client attempted to access a locked HttpController. No key specified.");
                } else {
                    final String key = reader.request.headers.getCookie("JavaArcanoKey");
                    if (!JwtMessage.verify(key,reader.so.config.key,reader.so.config.charset)) {
                        throw new NoSecretFoundException("An unauthorized client attempted to access a locked HttpController. The specified key is invalid.");
                    }
                }
            }
            
            if(action != null){
                HttpController controller = new HttpController();
                controller.install(reader);
                controller.invokeCompleteAction(action);
                return controller;
            }

            cls = Class.forName(wo.getClassName());
            constructor = ConstructorFinder.getNoParametersConstructor(cls);
            if (constructor == null) {
                throw new InvalidControllerConstructorException(String.format(
                        "\nController %s does not contain a valid constructor.\n"
                                + "A valid constructor for your controller is a constructor that has no parameters.\n"
                                + "Perhaps your class is an inner class and it's not public or static? Try make it a \"public static class\"!",
                        wo.getClassName()));
            }
            HttpController controller = (HttpController) constructor.newInstance();

            final String methodname = reader.location.length > classId ? wo.getMethodName() : "main";
            reader.args = resolveMethodArgs(classId + 1, reader.location);
            controller.install(reader);
            Method[] methods = controller.getClass().getDeclaredMethods();
            for(Method m : methods){
                if(!m.getName().equals(methodname)) continue;    
                method = m;
                break;
            }
            if(method == null) {
                reader.args = resolveMethodArgs(classId, reader.location);
                for(Method m : methods){
                    if(!m.getName().equals("main")) continue;    
                    method = m;
                    break;
                }
            }
            if(method == null){
                LOGGER.log(Level.SEVERE, "Method Name not set.");
                return instantPackStatus(reader,STATUS_INTERNAL_SERVER_ERROR);
            }
            controller.invokeMethod(method);
            return controller;
        } catch (final ClassNotFoundException ex) {
            String methodname = "main";
            if(reader.location.length == 1 && reader.location[0].equals("")
                    && reader.so.ROUTES.containsKey(reader.so.HTTP_SERVICE_TYPE_DEFAULT)){
                WebObject o = reader.so.ROUTES.get(reader.so.HTTP_SERVICE_TYPE_DEFAULT);
                if(o.getAction() != null){
                    CompleteAction<Object,HttpRequestReader> action = reader.so.ROUTES.get(reader.so.HTTP_SERVICE_TYPE_DEFAULT).getAction();
                    HttpController controller = new HttpController();
                    controller.install(reader);
                    controller.invokeCompleteAction(action);
                        return controller;
                }else try {
                    cls = Class.forName(o.getClassName());
                    methodname = o.getMethodName();
                } catch (final ClassNotFoundException ex2) {
                    LOGGER.log(Level.SEVERE, null, ex2);
                    return instantPackStatus(reader,STATUS_INTERNAL_SERVER_ERROR);
                }
            }else if(reader.so.ROUTES.containsKey(reader.so.HTTP_SERVICE_TYPE_404)) {
                WebObject o = reader.so.ROUTES.get(reader.so.HTTP_SERVICE_TYPE_404);
                if(o.getAction() != null){
                    CompleteAction<Object,HttpRequestReader> action = reader.so.ROUTES.get(reader.so.HTTP_SERVICE_TYPE_404).getAction();
                    HttpController controller = new HttpController();
                    controller.install(reader);
                    controller.invokeCompleteAction(action);
                        return controller;
                }else try {
                    cls = Class.forName(o.getClassName());
                    methodname = o.getMethodName();
                } catch (final ClassNotFoundException ex2) {
                    LOGGER.log(Level.SEVERE, null, ex2);
                    return instantPackStatus(reader,STATUS_INTERNAL_SERVER_ERROR);
                }
            }
            
            if(cls == null){
                LOGGER.log(Level.SEVERE, "Class Name not set.");
                return instantPackStatus(reader,STATUS_INTERNAL_SERVER_ERROR);
            }else if(methodname == null){
                LOGGER.log(Level.SEVERE, "Method Name not set.");
                return instantPackStatus(reader,STATUS_INTERNAL_SERVER_ERROR);
            }
            constructor = cls.getDeclaredConstructor();
            if (constructor == null){
                
                LOGGER.log(Level.SEVERE, String.format(
                        "\nController %s does not contain a valid constructor.\n"
                                + "A valid constructor for your controller is a constructor that has no parameters.\n"
                                + "Perhaps your class is an inner class and it's not static or public? Try make it a \"static public class\"!",
                        cls.getName()));
                return instantPackStatus(reader,STATUS_INTERNAL_SERVER_ERROR);
            }else{
                HttpController controller = (HttpController) constructor.newInstance();
                Method[] methods = controller.getClass().getDeclaredMethods();
                for(Method m : methods){
                    if(!m.getName().equals(methodname)) continue;    
                    method = m;
                    break;
                }
                controller.install(reader);
                controller.invokeMethod(method);
                return controller;
            }
            
        } catch (final NoSecretFoundException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            return instantPackStatus(reader,STATUS_LOCKED);
        } catch (InvalidControllerConstructorException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            return instantPackStatus(reader,STATUS_INTERNAL_SERVER_ERROR);
        }
    }
    
    private static HttpController instantPackStatus(HttpRequestReader reader,String status){
        HttpController controller = new HttpController();
        controller.install(reader);
        controller.setResponseStatus(status);
        controller.send("");
        return controller;
    }

    // public static void onControllerRequest(final StringBuilder url,String
    // httpMethod,String httpNotFoundNameOriginal,String httpNotFoundName) {
    public static final void onControllerRequest(final HttpRequestReader reader) {
        try {
            final HttpController controller = serveController(reader);
            if(controller != null ) controller.close();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException | IllegalArgumentException | InvocationTargetException | UnsupportedEncodingException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            try {
                reader.client.close();
            } catch (IOException ex1) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            try {
                reader.client.close();
            } catch (IOException ex1) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }
    } 
}
