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
package elkserver.Http;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import elkserver.ELK;
import java.io.DataOutputStream;
import java.util.Map;

/**
 *
 * @author Razvan
 */
public class HttpEvent extends HttpEventManager{
    private final HttpEvent singleton;
    private final Map<String,String> post;
    public HttpEvent(DataOutputStream output, HttpHeader clientHeader, Socket client,Map<String,String> post) {
        super(output,clientHeader,client);
        singleton = this;
        this.post = post;
    }
    
    @Override
    void onControllerRequest(String location) {
        ArrayList<String> args = new ArrayList<>();
        Class<?> c;
        Object x;
        Method m;
        Method onCloseMethod;
        
        
        String[] uri = ELK.decodeUrl(location).split("/");
        if(uri.length>1){
            //System.out.println("Class defined");
            String[] classpath = uri[1].split("\\.");
            String classname =ELK.HTTP_CONTROLLER_PACKAGE_NAME;
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
                Class.forName(classname);
                if(!tmp.equals("@")){
                    setContentType("text/html");
                    sendFileContents(ELK.INDEX_FILE);
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
                        m = x.getClass().getDeclaredMethod("main",this.getClass(),args.getClass(),post.getClass());
                    }
                    onCloseMethod = x.getClass().getDeclaredMethod("onClose");
                    try {
                        m.invoke(x,singleton,args,post);
                        onCloseMethod.invoke(x);
                        client.close();
                    } catch (IllegalAccessException | 
                            IllegalArgumentException | 
                            InvocationTargetException | 
                            IOException ex) {
                        Logger.getLogger(HttpEvent.class.getName()).log(Level.SEVERE, null, ex);
                        try {
                            onCloseMethod.invoke(x);
                            client.close();
                        } catch (IOException | InvocationTargetException ex2) {
                            Logger.getLogger(HttpEvent.class.getName()).log(Level.SEVERE, null, ex2);
                        }
                    }
                }
            } catch (ClassNotFoundException ex) {
                //Logger.getLogger(HttpEvent.class.getName()).log(Level.SEVERE, null, ex);
                
                    
                try {
                    c = Class.forName(ELK.HTTP_CONTROLLER_PACKAGE_NAME+"."+ELK.HTTP_CONTROLLER_NOT_FOUND);
                    
                    x = c.newInstance();
                    m = x.getClass().getDeclaredMethod("main",this.getClass(),args.getClass(),post.getClass());
                    onCloseMethod = x.getClass().getDeclaredMethod("onClose");
                    m.invoke(x,singleton,args,post);
                    onCloseMethod.invoke(x);
                    client.close();
                } catch (ClassNotFoundException | IOException | IllegalAccessException | 
                        IllegalArgumentException | InvocationTargetException | NoSuchMethodException | 
                        SecurityException ex1) {
                    Logger.getLogger(HttpEvent.class.getName()).log(Level.SEVERE, null, ex1);
                    try {
                        client.close();
                    } catch (IOException ex2) {
                        Logger.getLogger(HttpEvent.class.getName()).log(Level.SEVERE, null, ex2);
                    }
                } catch (InstantiationException ex1) {
                    Logger.getLogger(HttpEvent.class.getName()).log(Level.SEVERE, null, ex1);
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
                sendFileContents(ELK.INDEX_FILE);
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
