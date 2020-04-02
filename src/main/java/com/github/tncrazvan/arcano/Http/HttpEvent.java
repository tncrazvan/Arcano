package com.github.tncrazvan.arcano.Http;

import static com.github.tncrazvan.arcano.SharedObject.LOGGER;
import static com.github.tncrazvan.arcano.Tool.Http.Status.STATUS_INTERNAL_SERVER_ERROR;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Level;

import com.github.tncrazvan.arcano.InvalidControllerConstructorException;
import com.github.tncrazvan.arcano.SharedObject;
import com.github.tncrazvan.arcano.WebObject;
import com.github.tncrazvan.arcano.Bean.Http.HttpServiceParam;
import com.github.tncrazvan.arcano.Tool.Actions.CompleteAction;
import com.github.tncrazvan.arcano.Tool.Encoding.JsonTools;
import com.github.tncrazvan.arcano.Tool.Reflect.ConstructorFinder;
import com.google.gson.JsonObject;


/**
 *
 * @author Razvan Tanase
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
        try {
            String type = reader.request.headers.getMethod();
            for (int i = reader.location.length; i > 0; i--) {
                String path = String.join("/", Arrays.copyOf(reader.location, i)).toLowerCase();
                if(path.equals(""))
                    path="/";
                HashMap<String, WebObject> method = reader.so.HTTP_ROUTES.get(type);
                if(method != null){
                    WebObject route = method.get(path);
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
                        Class<?> cls = Class.forName(route.getClassName());
                        String methodname = reader.location.length > i - 1 ? route.getMethodName() : "main";
                        Constructor<?> constructor = ConstructorFinder.getNoParametersConstructor(cls);
                        if (constructor == null) throw new InvalidControllerConstructorException(
                            String.format(
                                "\nController %s does not contain a valid constructor.\n"
                                + "A valid constructor for your controller is a constructor that has no parameters.\n"
                                + "Perhaps your class is an inner class and it's not public or static? Try make it a \"public static class\"!",
                                route.getClassName()
                            )
                        );
                        HttpController controller = (HttpController) constructor.newInstance();
                        reader.args = resolveMethodArgs(i, reader.location);
                        controller.install(reader);
                        Method[] methods = controller.getClass().getDeclaredMethods();
                        for(Method m : methods){
                            if(!m.getName().equals(methodname)) continue;
                            controller.invokeMethod(m);
                            return controller;
                        }
                        reader.args = resolveMethodArgs(i - 1, reader.location);
                        for(Method m : methods){
                            if(!m.getName().equals("main")) continue;
                            controller.invokeMethod(m);
                            return controller;
                        }
                        throw new NoSuchMethodException("Method Name not set.");
                    }
                }
            }
            //fallback 404 response is supposed to be define inside the "/" route.
            return instantPackStatus(reader,STATUS_INTERNAL_SERVER_ERROR,"Fallback route \"/\" is not defined.");
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
