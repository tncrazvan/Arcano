package com.github.tncrazvan.arcano.Http;

import com.github.tncrazvan.arcano.SharedObject;
import static com.github.tncrazvan.arcano.SharedObject.LOGGER;
import java.io.DataOutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Stream;

import com.github.tncrazvan.arcano.WebObject;
import com.github.tncrazvan.arcano.Tool.JsonTools;
import static com.github.tncrazvan.arcano.Tool.Status.STATUS_INTERNAL_SERVER_ERROR;
import static com.github.tncrazvan.arcano.Tool.Status.STATUS_NOT_FOUND;
import com.google.gson.JsonObject;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.logging.Logger;


/**
 *
 * @author Razvan
 */
public class HttpEvent extends HttpEventManager implements JsonTools{
    private Method method;
    private String[] args;
    private Class<?> cls;
    private Object controller;
    private WebObject wo;
    private int classId;
    
    public HttpEvent(SharedObject so,final DataOutputStream output, final HttpHeader clientHeader, final Socket client,
            final byte[] input) throws UnsupportedEncodingException {
        super(so, output, clientHeader, client, input);
    }

    private void invoke(final Object controller, final Method method) throws IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, InvocationTargetException {
        try {
            Class<?> type = method.getReturnType();
            if (type == void.class || type == Void.class) {
                method.invoke(controller);
            } else if (type == HttpResponse.class) {
                final HttpResponse response = (HttpResponse) method.invoke(controller);
                final HashMap<String, String> headers = response.getHeaders();
                if (headers != null) {
                    final Iterator it = headers.entrySet().iterator();
                    while (it.hasNext()) {
                        final Map.Entry pair = (Map.Entry) it.next();
                        setHeaderField((String) pair.getKey(), (String) pair.getValue());
                        it.remove(); // avoids a ConcurrentModificationException
                    }
                }
                if (response.isRaw()) {
                    send((byte[]) response.getContent(true));
                } else {
                    type = response.getType();
                    if (type == String.class || type == Character.class) {
                        final Object content = response.getContent(true);
                        if (content != null) {
                            if (so.responseWrapper) {
                                final JsonObject obj = new JsonObject();
                                obj.addProperty("result", new String((byte[]) content));
                                setHeaderField("Content-Type", "application/json");
                                send(obj.toString().getBytes(so.charset));
                            } else {
                                send(new String((byte[]) content));
                            }
                        } else
                            send("");
                    } else {
                        final Object content = response.getContent(true);
                        if (content != null) {
                            final String tmp = new String((byte[]) content);
                            if (so.responseWrapper) {
                                setHeaderField("Content-Type", "application/json");
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
                final HttpResponse response = new HttpResponse(new HashMap<String, String>(){{}}, data == null ? "" : data);
                if (data == null)
                    response.setRaw(true);

                if (response.isRaw()) {
                    send((byte[]) response.getContent(true));
                } else {
                    type = response.getType();
                    if (type == String.class || type == Character.class) {
                        final Object content = response.getContent(true);
                        if (content != null) {
                            if (so.responseWrapper) {
                                final JsonObject obj = new JsonObject();
                                obj.addProperty("result", new String((byte[]) content));
                                setHeaderField("Content-Type", "application/json");
                                send(obj.toString().getBytes(so.charset));
                            } else {
                                send(new String((byte[]) content));
                            }
                        } else
                            send("");
                    } else {
                        final Object content = response.getContent(true);
                        if (content != null) {
                            final String tmp = content.toString().trim();
                            if (so.responseWrapper) {
                                setHeaderField("Content-Type", "application/json");
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
            setStatus(STATUS_INTERNAL_SERVER_ERROR);
            if (so.sendExceptions) {
                final String message = e.getTargetException().getMessage();
                if (so.responseWrapper) {
                    final JsonObject obj = new JsonObject();
                    final HttpResponse response = new HttpResponse(null, message == null ? "" : message);
                    obj.addProperty("exception", new String((byte[]) (response.getContent(true))));
                    setHeaderField("Content-Type", "application/json");
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
            setStatus(STATUS_INTERNAL_SERVER_ERROR);
            if (so.sendExceptions) {
                final String message = e.getMessage();
                if (so.responseWrapper) {
                    final JsonObject obj = new JsonObject();
                    final HttpResponse response = new HttpResponse(null, message == null ? "" : message);
                    obj.addProperty("exception", new String((byte[]) (response.getContent(true))));
                    setHeaderField("Content-Type", "application/json");
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
    private void serveController(String[] location) throws InstantiationException, IllegalAccessException,
            NoSuchMethodException, ClassNotFoundException, IllegalArgumentException, InvocationTargetException, MalformedURLException {
        args = new String[0];
        if (location.length == 0 || location.length == 1 && location[0].equals("")) {
            location = new String[] { "" };
        }
        try {
            classId = getClassnameIndex(location, getMethod());
            final String[] typedLocation = Stream
                    .concat(Arrays.stream(new String[] { getMethod() }), Arrays.stream(location))
                    .toArray(String[]::new);
            wo = resolveClassName(classId + 1, typedLocation);
            cls = Class.forName(wo.getClassname());
            
            controller = cls.getDeclaredConstructor().newInstance();

            final String methodname = location.length > classId ? wo.getMethodname() : "main";
            args = resolveMethodArgs(classId + 1, location);
            try {
                method = controller.getClass().getDeclaredMethod(methodname);
            } catch (final NoSuchMethodException ex) {
                args = resolveMethodArgs(classId, location);
                method = controller.getClass().getDeclaredMethod("main");
            }
            ((HttpController) controller).setEvent(this);
            ((HttpController) controller).setArgs(args);
            ((HttpController) controller).setInput(input);
            invoke(controller, method);
        } catch (final ClassNotFoundException ex) {
            try {
                cls = Class.forName(so.httpNotFoundName);
            } catch (final ClassNotFoundException eex) {
                cls = Class.forName(so.httpNotFoundNameOriginal);
            }
            controller = cls.getDeclaredConstructor().newInstance();
            method = controller.getClass().getDeclaredMethod("main");
            // Method onClose = controller.getClass().getDeclaredMethod("onClose");

            ((HttpController) controller).setEvent(this);
            ((HttpController) controller).setArgs(location);
            ((HttpController) controller).setInput(input);
            setStatus(STATUS_NOT_FOUND);
            invoke(controller, method);
        }
    }

    @Override
    void onControllerRequest(final StringBuilder url) {
        try {
            serveController(url.toString().split("/"));
            close();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        } catch (IllegalArgumentException | InvocationTargetException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } catch (MalformedURLException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    } 
}
