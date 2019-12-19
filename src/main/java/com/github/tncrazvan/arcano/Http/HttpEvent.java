package com.github.tncrazvan.arcano.Http;

import com.github.tncrazvan.arcano.InvalidControllerConstructorException;
import static com.github.tncrazvan.arcano.SharedObject.LOGGER;
import com.github.tncrazvan.arcano.Tool.Cluster.NoKeyFoundException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Stream;

import com.github.tncrazvan.arcano.WebObject;
import com.github.tncrazvan.arcano.Tool.JsonTools;
import com.github.tncrazvan.arcano.Tool.Status;
import static com.github.tncrazvan.arcano.Tool.Status.STATUS_INTERNAL_SERVER_ERROR;
import static com.github.tncrazvan.arcano.Tool.Status.STATUS_NOT_FOUND;
import com.google.gson.JsonObject;
import java.lang.reflect.Constructor;


/**
 *
 * @author Razvan
 */
public class HttpEvent extends HttpEventManager implements JsonTools{

    public void invoke(final Object controller, final Method method) throws IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, InvocationTargetException {
        try {
            Class<?> type = method.getReturnType();
            if (type == void.class || type == Void.class) {
                method.invoke(controller);
            } else if (type == HttpResponse.class) {
                final HttpResponse response = (HttpResponse) method.invoke(controller);
                response.resolve(so);
                final HashMap<String, String> headers = response.getHashMapHeaders();
                if (headers != null) {
                    final Iterator it = headers.entrySet().iterator();
                    while (it.hasNext()) {
                        final Map.Entry pair = (Map.Entry) it.next();
                        setResponseHeaderField((String) pair.getKey(), (String) pair.getValue());
                        it.remove(); // avoids a ConcurrentModificationException
                    }
                }
                if (response.isRaw()) {
                    send((byte[]) response.getContent());
                } else {
                    type = response.getType();
                    if (type == String.class || type == Character.class) {
                        final Object content = response.getContent();
                        if (content != null) {
                            if (so.responseWrapper) {
                                final JsonObject obj = new JsonObject();
                                obj.addProperty("result", new String((byte[]) content));
                                setResponseHeaderField("Content-Type", "application/json");
                                send(obj.toString().getBytes(so.charset));
                            } else {
                                send(new String((byte[]) content));
                            }
                        } else
                            send("");
                    } else {
                        final Object content = response.getContent();
                        if (content != null) {
                            final String tmp = new String((byte[]) content);
                            if (so.responseWrapper) {
                                setResponseHeaderField("Content-Type", "application/json");
                                send(("{\"result\": " + tmp + "}").getBytes(so.charset));
                            } else {
                                send(tmp);
                            }
                        } else
                            send("");
                    }
                }
            } else {
                // if it's some other type of object...
                final Object data = method.invoke(controller);
                final HttpResponse response = new HttpResponse(data == null ? "" : data);
                response.resolve(so);
                if (data == null)
                    response.setRaw(true);

                if (response.isRaw()) {
                    send((byte[]) response.getContent());
                } else {
                    type = response.getType();
                    if (type == String.class || type == Character.class) {
                        final Object content = response.getContent();
                        if (content != null) {
                            if (so.responseWrapper) {
                                final JsonObject obj = new JsonObject();
                                obj.addProperty("result", new String((byte[]) content));
                                setResponseHeaderField("Content-Type", "application/json");
                                send(obj.toString().getBytes(so.charset));
                            } else {
                                send(new String((byte[]) content));
                            }
                        } else
                            send("");
                    } else {
                        final Object content = response.getContent();
                        if (content != null) {
                            final String tmp = new String((byte[]) content).trim();
                            if (so.responseWrapper) {
                                setResponseHeaderField("Content-Type", "application/json");
                                send(("{\"result\": " + tmp + "}").getBytes(so.charset));
                            } else {
                                send(tmp);
                            }

                        } else
                            send("");
                    }
                }
            }
        } catch (final InvocationTargetException e) {
            setResponseStatus(STATUS_INTERNAL_SERVER_ERROR);
            if (so.sendExceptions) {
                final String message = e.getTargetException().getMessage();
                if (so.responseWrapper) {
                    final JsonObject obj = new JsonObject();
                    final HttpResponse response = new HttpResponse(message == null ? "" : message);
                    response.resolve(so);
                    obj.addProperty("exception", new String((byte[]) (response.getContent())));
                    setResponseHeaderField("Content-Type", "application/json");
                    try {
                        send(obj.toString().getBytes(so.charset));
                    } catch (final UnsupportedEncodingException ex) {
                        send(obj.toString().getBytes());
                    }
                } else {
                    try {
                        send(message.getBytes(so.charset));
                    } catch (final UnsupportedEncodingException ex) {
                        send(message.getBytes());
                    }
                }
            } else
                send("");
        } catch (UnsupportedEncodingException | IllegalAccessException | IllegalArgumentException e) {
            setResponseStatus(STATUS_INTERNAL_SERVER_ERROR);
            if (so.sendExceptions) {
                final String message = e.getMessage();
                if (so.responseWrapper) {
                    final JsonObject obj = new JsonObject();
                    final HttpResponse response = new HttpResponse(message == null ? "" : message);
                    response.resolve(so);
                    obj.addProperty("exception", new String((byte[]) (response.getContent())));
                    setResponseHeaderField("Content-Type", "application/json");
                    try {
                        send(obj.toString().getBytes(so.charset));
                    } catch (final UnsupportedEncodingException ex) {
                        send(obj.toString().getBytes());
                    }
                } else {
                    try {
                        send(message.getBytes(so.charset));
                    } catch (final UnsupportedEncodingException ex) {
                        send(message.getBytes());
                    }
                }
            } else
                send("");
        }
    }

    private static HttpController serveController(HttpRequestReader reader) throws InstantiationException, IllegalAccessException,
            NoSuchMethodException, ClassNotFoundException, IllegalArgumentException, InvocationTargetException, UnsupportedEncodingException {
        
        String[] location = reader.location.toString().split("/");
        String httpMethod = reader.request.headers.get("Method");
        
        Method method;
        String[] args;
        Class<?> cls;
        HttpController controller = null;
        Constructor<?> constructor;
        WebObject wo;
        int classId;
        args = new String[0];
        if (location.length == 0 || location.length == 1 && location[0].equals("")) {
            location = new String[] { "" };
        }
        try {
            if (location.length > 0 && !location[0].equals("")) {
                classId = getClassnameIndex(location, httpMethod);
                final String[] typedLocation = Stream
                        .concat(Arrays.stream(new String[] { httpMethod }), Arrays.stream(location))
                        .toArray(String[]::new);
                wo = resolveClassName(classId + 1, typedLocation);
                if(wo.isLocked()){
                    if(!controller.request.headers.issetCookie("ArcanoKey")){
                        throw new NoKeyFoundException("");
                    }
                }
                cls = Class.forName(wo.getClassname());
                constructor = getNoParametersConstructor(cls);
                if(constructor == null){
                    throw new InvalidControllerConstructorException(
                            String
                                    .format("\nController %s does not contain a valid constructor.\n"
                                            + "A valid constructor for your controller is a constructor that has no parameters.\n"
                                            + "Perhaps your class is an inner class and it's not static or public? Try make it a \"static public class\"!", 
                                            wo.getClassname()
                                    )
                    );
                }
                controller = (HttpController)cls.getDeclaredConstructor().newInstance();

                final String methodname = location.length > classId ? wo.getMethodname() : "main";
                args = resolveMethodArgs(classId + 1, location);
                try {
                    method = controller.getClass().getDeclaredMethod(methodname);
                } catch (final NoSuchMethodException ex) {
                    args = resolveMethodArgs(classId, location);
                    method = controller.getClass().getDeclaredMethod("main");
                }
            } else {
                try {
                    // cls = Class.forName(httpDefaultName);
                    cls = Class.forName(reader.so.httpNotFoundName);
                } catch (final ClassNotFoundException eex) {
                    // cls = Class.forName(httpDefaultNameOriginal);
                    cls = Class.forName(reader.so.httpNotFoundNameOriginal);
                }
                controller = (HttpController) cls.getDeclaredConstructor().newInstance();
                method = controller.getClass().getDeclaredMethod("main");
            }
            controller.setHttpHeaders(new HttpHeaders());
            controller.setSharedObject(reader.so);
            controller.setDataOutputStream(reader.output);
            controller.setSocket(reader.client);
            controller.setHttpRequest(reader.request);
            controller.initEventManager();
            controller.initHttpEventManager();
            controller.findRequestLanguages();
            controller.setArgs(args);
            controller.invoke(controller, method);
        } catch (final ClassNotFoundException ex) {
            try {
                cls = Class.forName(reader.so.httpNotFoundName);
            } catch (final ClassNotFoundException eex) {
                cls = Class.forName(reader.so.httpNotFoundNameOriginal);
            }
            controller = (HttpController) cls.getDeclaredConstructor().newInstance();
            method = controller.getClass().getDeclaredMethod("main");
            controller.setHttpHeaders(new HttpHeaders());
            controller.setResponseStatus(STATUS_NOT_FOUND);
            controller.setSharedObject(reader.so);
            controller.setDataOutputStream(reader.output);
            controller.setSocket(reader.client);
            controller.setHttpRequest(reader.request);
            controller.initEventManager();
            controller.initHttpEventManager();
            controller.findRequestLanguages();
            controller.setArgs(args);
            controller.invoke(controller, method);
        } catch (NoKeyFoundException ex){
            controller = new HttpController();
            controller.setResponseStatus(Status.STATUS_LOCKED);
            return controller;
        } catch (InvalidControllerConstructorException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return (HttpController) controller;
    }
    
    private static Constructor<?> getNoParametersConstructor(Class<?> cls){
        for (Constructor<?> constructor : cls.getDeclaredConstructors()) {
            if(constructor.getParameterCount() == 0)
                return constructor;
        }
        
        return null;
    }
    
    //public static void onControllerRequest(final StringBuilder url,String httpMethod,String httpNotFoundNameOriginal,String httpNotFoundName) {
    public static void onControllerRequest(HttpRequestReader reader) {
        try {
            HttpController controller = serveController(reader);
            if(controller != null ) controller.close();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        } catch (IllegalArgumentException | InvocationTargetException | UnsupportedEncodingException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    } 
}
