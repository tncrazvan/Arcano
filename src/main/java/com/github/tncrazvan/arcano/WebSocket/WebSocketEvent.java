package com.github.tncrazvan.arcano.WebSocket;

import com.github.tncrazvan.arcano.Http.HttpHeaders;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import com.github.tncrazvan.arcano.Http.HttpRequestReader;
import com.github.tncrazvan.arcano.WebObject;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Stream;


/**
 *
 * @author Razvan
 */
public abstract class WebSocketEvent extends WebSocketManager{
    
    public static void serveController(final HttpRequestReader reader)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, NoSuchMethodException, IOException {
        final String[] args = new String[0];
        WebSocketController controller;
        Class<?> cls;
        final String[] location = reader.location.toString().split("/");
        try {
            final int classId = getClassnameIndex(location, "WS");
            final String[] typedLocation = Stream.concat(Arrays.stream(new String[] { "WS" }), Arrays.stream(location))
                    .toArray(String[]::new);
            final WebObject wo = resolveClassName(classId + 1, typedLocation);
            cls = Class.forName(wo.getClassname());
            controller = (WebSocketController) cls.getDeclaredConstructor().newInstance();
            controller.setArgs(args);
        } catch (final ClassNotFoundException ex) {
            cls = Class.forName(reader.so.config.webSocket.controllerNotFound.getClassname());
            controller = (WebSocketController) cls.getDeclaredConstructor().newInstance();
            controller.setArgs(location);
        }
        controller.setBufferedReader(reader.bufferedReader);

        controller.setHttpHeaders(new HttpHeaders());
        controller.setSharedObject(reader.so);
        controller.setSocket(reader.client);
        controller.setHttpRequest(reader.request);
        controller.init();
        controller.execute();
    }

    protected abstract void onOpen();

    protected abstract void onMessage(WebSocketMessage payload);

    protected abstract void onClose();

    @Override
    protected void manageOnOpen() {
        if (so.WEB_SOCKET_EVENTS.get(this.getClass().getName()) == null) {
            final ArrayList<WebSocketEvent> tmp = new ArrayList<>();
            tmp.add(this);
            so.WEB_SOCKET_EVENTS.put(this.getClass().getName(), tmp);
        } else {
            so.WEB_SOCKET_EVENTS.get(this.getClass().getName()).add(this);
        }
        this.onOpen();
    }

    @Override
    protected void manageOnMessage(final WebSocketMessage payload) {
        this.onMessage(payload);
    }
    
    @Override
    protected void manageOnClose() {
        so.WEB_SOCKET_EVENTS.get(this.getClass().getName()).remove(this);
        this.onClose();
    }
    
}
