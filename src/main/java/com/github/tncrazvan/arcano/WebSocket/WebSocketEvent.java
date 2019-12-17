package com.github.tncrazvan.arcano.WebSocket;

import com.github.tncrazvan.arcano.Http.HttpHeaders;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import com.github.tncrazvan.arcano.Http.HttpRequestReader;
import com.github.tncrazvan.arcano.Http.HttpSession;
import com.github.tncrazvan.arcano.WebObject;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Stream;


/**
 *
 * @author Razvan
 */
public abstract class WebSocketEvent extends WebSocketManager{
    public HttpSession session;
    
    public static void serveController(HttpRequestReader reader) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, IOException{
        String[] args = new String[0];
            WebSocketController controller;
        Class<?> cls;
        String[] location = reader.location.toString().split("/");
        try{
            int classId = getClassnameIndex(location,"WS");
            String[] typedLocation = Stream.concat(Arrays.stream(new String[]{"WS"}), Arrays.stream(location)).toArray(String[]::new);
            WebObject wo = resolveClassName(classId+1,typedLocation);
            cls = Class.forName(wo.getClassname());
            controller = (WebSocketController) cls.getDeclaredConstructor().newInstance();
            controller.setArgs(args);
        }catch(ClassNotFoundException ex){
            try{
                cls = Class.forName(reader.so.webSocketNotFoundName);
            }catch(ClassNotFoundException eex){
                cls = Class.forName(reader.so.webSocketNotFoundNameOriginal);
            }
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
        if(so.WEB_SOCKET_EVENTS.get(this.getClass().getName()) == null){
            ArrayList<WebSocketEvent> tmp = new ArrayList<>();
            tmp.add(this);
            so.WEB_SOCKET_EVENTS.put(this.getClass().getName(), tmp);
        }else{
            so.WEB_SOCKET_EVENTS.get(this.getClass().getName()).add(this);
        }
        this.onOpen();
    }

    @Override
    protected void manageOnMessage(WebSocketMessage payload) {
        this.onMessage(payload);
    }
    
    @Override
    protected void manageOnClose() {
        so.WEB_SOCKET_EVENTS.get(this.getClass().getName()).remove(this);
        this.onClose();
    }
    
}
