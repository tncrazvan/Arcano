/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javahttpserver.Http;

import com.google.gson.JsonObject;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javahttpserver.JHS;

/**
 *
 * @author Razvan
 */
public class HttpEvent extends HttpEventManager{
    private final HttpEvent singleton;
    private final JsonObject post;
    public HttpEvent(BufferedWriter writer, HttpHeader clientHeader, Socket client,JsonObject post) {
        super(writer,clientHeader,client);
        singleton = this;
        this.post = post;
    }
    
    @Override
    void onControllerRequest(String location) {
        final ArrayList<String> args = new ArrayList<>();
        final Class<?> c;
        final Object x;
        final Method m;
        
        
        String[] uri = JHS.decodeUrl(location).split("/");
        if(uri.length>1){
            //System.out.println("Class defined");
            String[] classpath = uri[1].split("\\.");
            String classname =JHS.HTTP_CONTROLLER_PACKAGE_NAME;
            String tmp = "";
            for(int i =0;i<classpath.length;i++){
                tmp = classpath[i].substring(0, 1);
                if(tmp.equals("@")){
                    classname +="."+(classpath[i].substring(1).substring(0,1).toUpperCase()+classpath[i].substring(2));
                }else{
                    classname +="."+classpath[i];
                }
            }
            
            try {
                if(!tmp.equals("@")){
                    setContentType("text/html");
                    sendFileContents(JHS.INDEX_FILE);
                    client.close();
                }else{
                    c = Class.forName(classname);
                    x = c.newInstance();
                    if(uri.length>2){
                        //System.out.println("Method defined");
                        if(uri.length > 3){
                            //System.out.println("Parameters defined");
                            for(int i = 3;i<uri.length;i++){
                                args.add(uri[i]);
                            }
                            m = x.getClass().getDeclaredMethod(uri[2],this.getClass(),args.getClass(),post.getClass());
                        }else{
                            //System.out.println("Parameters not defined");
                            m = x.getClass().getDeclaredMethod(uri[2],this.getClass(),args.getClass(),post.getClass());
                        }
                    }else{
                        //System.out.println("Method not defined");
                        m = x.getClass().getMethod("main",this.getClass(),args.getClass(),post.getClass());
                    }
                    try {
                        m.invoke(x,singleton,args,post);
                        client.close();
                    } catch (IllegalAccessException | 
                            IllegalArgumentException | 
                            InvocationTargetException | 
                            IOException ex) {
                        Logger.getLogger(HttpEvent.class.getName()).log(Level.SEVERE, null, ex);
                        try {
                            client.close();
                        } catch (IOException ex2) {
                            Logger.getLogger(HttpEvent.class.getName()).log(Level.SEVERE, null, ex2);
                        }
                    }
                }
            } catch (ClassNotFoundException ex) {
                //Logger.getLogger(HttpEvent.class.getName()).log(Level.SEVERE, null, ex);

                    
                try {
                    final Class<?> cNotFound = Class.forName(JHS.HTTP_CONTROLLER_PACKAGE_NAME+"."+JHS.HTTP_CONTROLLER_NOT_FOUND);
                    final Object xNotFound = cNotFound.newInstance();
                    final Method mNotFound = xNotFound.getClass().getDeclaredMethod("main",this.getClass(),args.getClass(),post.getClass());
                    mNotFound.invoke(xNotFound,singleton,args,post);
                    client.close();
                } catch (ClassNotFoundException | IOException | IllegalAccessException | 
                        IllegalArgumentException | InvocationTargetException | NoSuchMethodException | 
                        SecurityException | InstantiationException ex1) {
                    Logger.getLogger(HttpEvent.class.getName()).log(Level.SEVERE, null, ex1);
                    try {
                        client.close();
                    } catch (IOException ex2) {
                        Logger.getLogger(HttpEvent.class.getName()).log(Level.SEVERE, null, ex2);
                    }
                }
            } catch (InstantiationException | 
                    IllegalAccessException | 
                    NoSuchMethodException | 
                    SecurityException | 
                    IllegalArgumentException | IOException ex) {
                Logger.getLogger(HttpEvent.class.getName()).log(Level.SEVERE, null, ex);
                try {
                    client.close();
                } catch (IOException ex2) {
                    Logger.getLogger(HttpEvent.class.getName()).log(Level.SEVERE, null, ex2);
                }
            }
        }else{
            try {
                setContentType("text/html");
                sendFileContents(JHS.INDEX_FILE);
            } catch (IOException ex) {
                Logger.getLogger(HttpEvent.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                client.close();
            } catch (IOException ex) {
                Logger.getLogger(HttpEvent.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    } 
}
