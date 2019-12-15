package com.github.tncrazvan.arcano.WebSocket;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import com.github.tncrazvan.arcano.Http.HttpHeader;
import com.github.tncrazvan.arcano.SharedObject;
import com.github.tncrazvan.arcano.Http.HttpSession;
import static com.github.tncrazvan.arcano.SharedObject.LOGGER;
import com.github.tncrazvan.arcano.WebObject;
import java.util.Arrays;
import java.util.stream.Stream;


/**
 *
 * @author Razvan
 */
public class WebSocketEvent extends WebSocketManager{
    
    private Method onCloseMethod;
    private Method onOpenMethod;
    private Method onMessageMethod;
    private String[] args;
    private Class<?> cls;
    private Object controller;
    private WebObject wo;
    private int classId;
    public HttpSession session;
    
    private void serveController(String[] location) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException{
        args = new String[0];
        try{
            classId = getClassnameIndex(location,"WS");
            String[] typedLocation = Stream.concat(Arrays.stream(new String[]{"WS"}), Arrays.stream(location)).toArray(String[]::new);
            wo = resolveClassName(classId+1,typedLocation);
            cls = Class.forName(wo.getClassname());
            controller = cls.getDeclaredConstructor().newInstance();
            ((WebSocketController)controller).setArgs(args);
        }catch(ClassNotFoundException ex){
            try{
                cls = Class.forName(so.webSocketNotFoundName);
            }catch(ClassNotFoundException eex){
                cls = Class.forName(so.webSocketNotFoundNameOriginal);
            }
            controller = cls.getDeclaredConstructor().newInstance();
            ((WebSocketController)controller).setArgs(location);
        }
        
        ((WebSocketController)controller).setEvent(this);
        
        onOpenMethod = controller.getClass().getMethod("onOpen");
        onMessageMethod = controller.getClass().getMethod("onMessage",byte[].class);
        onCloseMethod = controller.getClass().getMethod("onClose");
    }
    
    public WebSocketEvent(SharedObject so,BufferedReader reader,Socket client,HttpHeader clientHeader) throws IOException, InstantiationException, IllegalAccessException, NoSuchMethodException, ClassNotFoundException, IllegalArgumentException, InvocationTargetException {
        super(so,reader,client,clientHeader);
        serveController(location.toString().split("/"));
    }


    @Override
    protected void onOpen() {        
        try {
            if(so.WEB_SOCKET_EVENTS.get(cls.getCanonicalName()) == null){
                ArrayList<WebSocketEvent> tmp = new ArrayList<>();
                tmp.add(this);
                so.WEB_SOCKET_EVENTS.put(cls.getCanonicalName(), tmp);
            }else{
                so.WEB_SOCKET_EVENTS.get(cls.getCanonicalName()).add(this);
            }
            onOpenMethod.invoke(controller);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            LOGGER.log(Level.SEVERE,null,ex);
        }
    }

    @Override
    protected void onMessage(byte[] data) {
        try {
            onMessageMethod.invoke(controller,data);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            LOGGER.log(Level.SEVERE,null,ex);
        }

    }
    
    @Override
    protected void onClose() {
        try {
            so.WEB_SOCKET_EVENTS.get(cls.getCanonicalName()).remove(this);
            onCloseMethod.invoke(controller);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            LOGGER.log(Level.SEVERE,null,ex);
        }
    }
    
}
