/**
 * ElkServer is a Java library that makes it easier
 * to program and manage a Java servlet by providing different tools
 * such as:
 * 1) An MVC (Model-View-Controller) alike design pattern to manage 
 *    client requests without using any URL rewriting rules.
 * 2) A WebSocket Manager, allowing the server to accept and manage 
 *    incoming WebSocket connections.
 * 3) Direct access to every socket bound to every client application.
 * 4) Direct access to the headers of the incomming and outgoing Http messages.
 * Copyright (C) 2016  Tanase Razvan Catalin
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package elkserver.WebSocket;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import elkserver.Http.HttpHeader;
import elkserver.ELK;


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
        String[] uri = ELK.decodeUrl(location).split("/");
        if(uri.length>1){
            //System.out.println("Class defined");
            String[] classpath = uri[1].split("\\.");
            String classname =ELK.WS_CONTROLLER_PACKAGE_NAME;
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
                        c = Class.forName(ELK.WS_CONTROLLER_PACKAGE_NAME+".ControllerNotFound");
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
                    c = Class.forName(ELK.WS_CONTROLLER_PACKAGE_NAME+".ControllerNotFound");
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
                c = Class.forName(ELK.WS_CONTROLLER_PACKAGE_NAME+".ControllerNotFound");
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
            ELK.WS_EVENTS.get(c.getCanonicalName()).remove(singleton);
            onCloseMethod.invoke(x,this.singleton,args);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(WebSocketEvent.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected void onOpen(Socket client) {        
        try {
            
            if(ELK.WS_EVENTS.get(c.getCanonicalName()) == null){
                ArrayList<WebSocketEvent> tmp = new ArrayList<>();
                tmp.add(singleton);
                ELK.WS_EVENTS.put(c.getCanonicalName(), tmp);
            }else{
                ELK.WS_EVENTS.get(c.getCanonicalName()).add(singleton);
            }
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
