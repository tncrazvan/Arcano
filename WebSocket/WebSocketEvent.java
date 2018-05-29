/**
 * ElkServer is a Java library that makes it easier
 * to program and manage a Java Web Server by providing different tools
 * such as:
 * 1) An MVC (Model-View-Controller) alike design pattern to manage 
 *    client requests without using any URL rewriting rules.
 * 2) A WebSocket Manager, allowing the server to accept and manage 
 *    incoming WebSocket connections.
 * 3) Direct access to every socket bound to every client application.
 * 4) Direct access to the headers of the incomming and outgoing Http messages.
 * Copyright (C) 2016-2018  Tanase Razvan Catalin
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.razshare.elkserver.WebSocket;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import com.razshare.elkserver.Http.HttpHeader;
import com.razshare.elkserver.Elk;
import com.razshare.elkserver.Http.HttpSession;


/**
 *
 * @author Razvan
 */
public class WebSocketEvent extends WebSocketManager{
    
    private Method onCloseMethod = null;
    private Method onOpenMethod = null;
    private Method onMessageMethod = null;
    private WebSocketEvent singleton = null;
    private ArrayList<String> args = new ArrayList<>();
    private Class<?> c = null;
    private Object x = null;
    protected HttpSession session;
    
    public WebSocketEvent(BufferedReader reader,Socket client,HttpHeader clientHeader) throws IOException, InstantiationException, IllegalAccessException, NoSuchMethodException {
        super(reader,client,clientHeader);
        singleton = this;
        String[] uri = Elk.decodeUrl(location).split("/");
        if(uri.length>1){
            //System.out.println("Class defined");
            String[] classpath = uri[1].split("\\.");
            String classname =Elk.wsControllerPackageName;
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
                        c = Class.forName(Elk.wsControllerPackageName+".ControllerNotFound");
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
                        logger.log(Level.SEVERE,null,ex);
                    }
                }
            }else{
                try {
                    c = Class.forName(Elk.wsControllerPackageName+".ControllerNotFound");
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
                    logger.log(Level.SEVERE,null,ex);
                }
            }
        }else{
            try {
                c = Class.forName(Elk.wsControllerPackageName+".ControllerNotFound");
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
                logger.log(Level.SEVERE,null,ex);
            }
        }
    }
    
    public void sessionStart(){
        session = HttpSession.start(this);
    }


    @Override
    protected void onClose(Socket client) {
        
        try {
            Elk.WS_EVENTS.get(c.getCanonicalName()).remove(singleton);
            onCloseMethod.invoke(x,this.singleton,args);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            logger.log(Level.SEVERE,null,ex);
        }
    }

    @Override
    protected void onOpen(Socket client) {        
        try {
            
            
            if(Elk.WS_EVENTS.get(c.getCanonicalName()) == null){
                ArrayList<WebSocketEvent> tmp = new ArrayList<>();
                tmp.add(singleton);
                Elk.WS_EVENTS.put(c.getCanonicalName(), tmp);
            }else{
                Elk.WS_EVENTS.get(c.getCanonicalName()).add(singleton);
            }
            onOpenMethod.invoke(x,this.singleton,args);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            logger.log(Level.SEVERE,null,ex);
        }
    }

    @Override
    protected void onMessage(Socket client, byte[] data) {
        try {
            onMessageMethod.invoke(x,this.singleton,data,args);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            logger.log(Level.SEVERE,null,ex);
        }

    }
    
}
