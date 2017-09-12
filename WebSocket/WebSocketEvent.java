/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javahttpserver.WebSocket;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javahttpserver.Http.HttpHeader;
import javahttpserver.JHS;


/**
 *
 * @author Razvan
 */
public class WebSocketEvent extends WebSocketManager{

    private FileOutputStream fs;
    private Method onCloseMethod = null;
    private Method onOpenMethod = null;
    private Method onMessageMethod = null;
    private WebSocketEvent singleton = null;
    private ArrayList<String> args = new ArrayList<>();
    private Class<?> c = null;
    private Object x = null;
    public WebSocketEvent(BufferedReader reader,Socket client,HttpHeader clientHeader,String requestId) throws IOException, InstantiationException, IllegalAccessException, NoSuchMethodException {
        super(reader,client,clientHeader,requestId);
        singleton = this;
        String location = clientHeader.get("Resource");
        String[] uri = JHS.decodeUrl(location).split("/");
        if(uri.length>1){
            //System.out.println("Class defined");
            String[] classpath = uri[1].split("\\.");
            String classname =JHS.WS_CONTROLLER_PACKAGE_NAME;
            String tmp = "";
            for(int i =0;i<classpath.length;i++){
                tmp = classpath[i].substring(0, 1);
                if(tmp.equals("@")){
                    classname +="."+(classpath[i].substring(1).substring(0,1).toUpperCase()+classpath[i].substring(2));
                }else{
                    classname +="."+classpath[i];
                }
            }
            
            if(tmp.equals("@")){
                try {
                    c = Class.forName(classname);
                    x = c.newInstance();
                    if(uri.length > 2){
                        //System.out.println("Parameters defined");
                        for(int i = 2;i<uri.length;i++){
                            args.add(uri[i]);
                        }
                    }
                    
                    onOpenMethod = x.getClass().getMethod("onOpen",this.getClass(),args.getClass());
                    onMessageMethod = x.getClass().getMethod("onMessage",this.getClass(),byte[].class,args.getClass());
                    onCloseMethod = x.getClass().getMethod("onClose",this.getClass(),args.getClass());
                } catch (ClassNotFoundException ex) {
                    //Logger.getLogger(WebSocketEvent.class.getName()).log(Level.SEVERE, null, ex);
                    //System.out.println("HERE");
                    try {
                        c = Class.forName(JHS.WS_CONTROLLER_PACKAGE_NAME+".ControllerNotFound");
                        x = c.newInstance();
                        if(uri.length > 2){
                            //System.out.println("Parameters defined");
                            for(int i = 2;i<uri.length;i++){
                                args.add(uri[i]);
                            }
                        }

                        onOpenMethod = x.getClass().getMethod("onOpen",this.getClass(),args.getClass());
                        onMessageMethod = x.getClass().getMethod("onMessage",this.getClass(),byte[].class,args.getClass());
                        onCloseMethod = x.getClass().getMethod("onClose",this.getClass(),args.getClass());
                    } catch (ClassNotFoundException ex1) {
                        Logger.getLogger(WebSocketEvent.class.getName()).log(Level.SEVERE, null, ex1);
                    }
                }
            }else{
                try {
                    c = Class.forName(JHS.WS_CONTROLLER_PACKAGE_NAME+".ControllerNotFound");
                    x = c.newInstance();
                    if(uri.length > 2){
                        //System.out.println("Parameters defined");
                        for(int i = 2;i<uri.length;i++){
                            args.add(uri[i]);
                        }
                    }

                    onOpenMethod = x.getClass().getMethod("onOpen",this.getClass(),args.getClass());
                    onMessageMethod = x.getClass().getMethod("onMessage",this.getClass(),byte[].class,args.getClass());
                    onCloseMethod = x.getClass().getMethod("onClose",this.getClass(),args.getClass());
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(WebSocketEvent.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }else{
            try {
                c = Class.forName(JHS.WS_CONTROLLER_PACKAGE_NAME+".ControllerNotFound");
                x = c.newInstance();
                if(uri.length > 2){
                    //System.out.println("Parameters defined");
                    for(int i = 2;i<uri.length;i++){
                        args.add(uri[i]);
                    }
                }

                onOpenMethod = x.getClass().getMethod("onOpen",this.getClass(),args.getClass());
                onMessageMethod = x.getClass().getMethod("onMessage",this.getClass(),byte[].class,args.getClass());
                onCloseMethod = x.getClass().getMethod("onClose",this.getClass(),args.getClass());
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(WebSocketEvent.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }


    @Override
    protected void onClose(Socket client) {
        
        try {
            JHS.EVENT_WS.remove(singleton);
            onCloseMethod.invoke(x,this.singleton,args);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(WebSocketEvent.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected void onOpen(Socket client) {        
        try {
            JHS.EVENT_WS.add(singleton);
            onOpenMethod.invoke(x,this.singleton,args);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(WebSocketEvent.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected void onMessage(Socket client, byte[] data) {
        try {
            onMessageMethod.invoke(x,this.singleton,data,args);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(WebSocketEvent.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
